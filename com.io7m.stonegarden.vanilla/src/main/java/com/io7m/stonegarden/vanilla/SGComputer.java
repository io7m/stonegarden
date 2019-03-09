/*
 * Copyright © 2019 Mark Raynsford <code@io7m.com> http://io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.io7m.stonegarden.vanilla;

import com.io7m.stonegarden.api.computer.SGComputerBootOrderItem;
import com.io7m.stonegarden.api.computer.SGComputerDescription;
import com.io7m.stonegarden.api.computer.SGComputerEventBootFailed;
import com.io7m.stonegarden.api.computer.SGComputerEventBooted;
import com.io7m.stonegarden.api.computer.SGComputerEventBooting;
import com.io7m.stonegarden.api.computer.SGComputerEventShutDown;
import com.io7m.stonegarden.api.computer.SGComputerEventShuttingDown;
import com.io7m.stonegarden.api.computer.SGComputerType;
import com.io7m.stonegarden.api.devices.SGDeviceKernelInterfaceType;
import com.io7m.stonegarden.api.kernels.SGKernelContextType;
import com.io7m.stonegarden.api.kernels.SGKernelExecutableDescriptionType;
import com.io7m.stonegarden.api.kernels.SGKernelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * A computer.
 */

final class SGComputer extends SGDevice implements SGComputerType
{
  private static final Logger LOG = LoggerFactory.getLogger(SGComputer.class);

  private final UUID id;
  private final SGComputerDescription description;
  private final AtomicBoolean running;
  private final SGSimulationInternalAPIType simulation;
  private final LinkedList<String> text_buffer;
  private final int text_buffer_limit;
  private SGKernelType kernel;

  SGComputer(
    final SGSimulationInternalAPIType in_simulation,
    final UUID in_id,
    final SGComputerDescription in_description)
  {
    super(in_simulation, in_description, in_id);

    this.simulation =
      Objects.requireNonNull(in_simulation, "simulation");
    this.id =
      Objects.requireNonNull(in_id, "id");
    this.description =
      Objects.requireNonNull(in_description, "description");

    this.running = new AtomicBoolean(false);
    this.text_buffer = new LinkedList<>();
    this.text_buffer_limit = 80;
  }

  private static Optional<SGKernelExecutableDescriptionType> findKernelWithMatchingName(
    final SGComputerBootOrderItem item)
  {
    return item.device()
      .kernels()
      .stream()
      .filter(exec -> executableMatches(item, exec))
      .findFirst();
  }

  private static boolean executableMatches(
    final SGComputerBootOrderItem item,
    final SGKernelExecutableDescriptionType exec)
  {
    final var description = exec.description();
    return Objects.equals(description.name(), item.name())
      && Objects.equals(description.version(), item.version());
  }

  @Override
  public void shutdown()
  {
    if (this.running.compareAndSet(true, false)) {
      this.simulation.publishEvent(SGComputerEventShuttingDown.of(this.id));

      if (this.kernel != null) {
        try {
          this.kernel.onShutDown();
        } catch (final Exception e) {
          this.writeConsole("kernel shutdown failed: %s", e.getMessage());
          LOG.debug("kernel shutdown failed: ", e);
        }
      }

      this.simulation.publishEvent(SGComputerEventShutDown.of(this.id));
    }
  }

  @Override
  public boolean isRunning()
  {
    return this.running.get();
  }

  @Override
  public SGComputerDescription description()
  {
    return this.description;
  }

  @Override
  public void boot(final List<SGComputerBootOrderItem> order)
  {
    Objects.requireNonNull(order, "order");

    if (this.running.compareAndSet(false, true)) {
      this.simulation.publishEvent(SGComputerEventBooting.of(this.id));

      for (final var item : order) {
        final var device = item.device();
        if (!this.simulation.deviceGraph().areDirectlyConnected(this, device)) {
          this.writeConsole("device %s is not connected", device.id());
          continue;
        }

        final var kernel_desc_found = findKernelWithMatchingName(item);
        if (kernel_desc_found.isEmpty()) {
          this.writeConsole(
            "no kernel found on %s with name %s:%s",
            device.id(),
            item.name(),
            item.version().toHumanString());
          continue;
        }

        final var kernel_desc = kernel_desc_found.get();
        if (!this.kernelIsCompatible(kernel_desc)) {
          this.writeConsole("kernel is not compatible with this architecture");
          continue;
        }

        final var context = new KernelContext(this);
        final var executable = kernel_desc.executable();

        try {
          this.kernel = executable.execute(context, item.parameters());
        } catch (final Exception e) {
          this.simulation.publishEvent(SGComputerEventBootFailed.of(this.id, e.getMessage()));
          this.running.set(false);
        }

        this.simulation.publishEvent(SGComputerEventBooted.of(this.id));
        this.running.set(true);

        try {
          this.kernel.onStart();
        } catch (final Exception e) {
          LOG.error("[{}]: kernel start failed: ", this.id.toString(), e);
        }
        return;
      }

      this.simulation.publishEvent(SGComputerEventBootFailed.of(this.id, "No kernel available"));
      this.running.set(false);
    }
  }

  private boolean kernelIsCompatible(
    final SGKernelExecutableDescriptionType kernel_exec)
  {
    return Objects.equals(
      kernel_exec.description().compatibility().architecture(),
      this.description.architecture());
  }

  private void writeConsole(
    final String format,
    final Object... arguments)
  {
    Objects.requireNonNull(format, "format");

    final var message = String.format(format, arguments);
    message.lines().forEach(line -> {
      LOG.trace("[{}]: console: {}", this.id(), line);
      if ((!this.text_buffer.isEmpty()) && this.text_buffer.size() >= this.text_buffer_limit) {
        this.text_buffer.removeFirst();
      }
      this.text_buffer.add(line);
    });
  }

  @Override
  protected void onClose()
  {

  }

  private static final class KernelContext implements SGKernelContextType
  {
    private final SGComputer computer;

    KernelContext(
      final SGComputer in_computer)
    {
      this.computer = Objects.requireNonNull(in_computer, "computer");
    }

    @Override
    public List<SGDeviceKernelInterfaceType> connectedDevices()
    {
      return this.computer.simulation.deviceGraph()
        .devicesConnectedTo(this.computer)
        .map(device -> (SGDeviceKernelInterfaceType) device)
        .collect(Collectors.toList());
    }

    @Override
    public void writeConsole(
      final String format,
      final Object... arguments)
    {
      this.computer.writeConsole(format, arguments);
    }

    @Override
    public void shutdown()
    {
      LOG.debug("[{}]: kernel triggered shutdown", this.computer.id.toString());
      this.computer.shutdown();
    }
  }
}
