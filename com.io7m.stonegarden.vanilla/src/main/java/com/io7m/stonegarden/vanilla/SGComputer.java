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

import com.io7m.stonegarden.api.computer.SGComputerDescription;
import com.io7m.stonegarden.api.computer.SGComputerEventBootFailed;
import com.io7m.stonegarden.api.computer.SGComputerEventBooted;
import com.io7m.stonegarden.api.computer.SGComputerEventBooting;
import com.io7m.stonegarden.api.computer.SGComputerEventShutDown;
import com.io7m.stonegarden.api.computer.SGComputerType;
import com.io7m.stonegarden.api.connectors.SGConnectorEventDisconnected;
import com.io7m.stonegarden.api.connectors.SGConnectorEventType;
import com.io7m.stonegarden.api.devices.SGDeviceEventDestroying;
import com.io7m.stonegarden.api.devices.SGDeviceEventType;
import com.io7m.stonegarden.api.devices.SGDeviceNotConnectedException;
import com.io7m.stonegarden.api.devices.SGStorageDeviceType;
import com.io7m.stonegarden.api.kernels.SGKernelType;
import io.reactivex.disposables.Disposable;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A computer.
 */

final class SGComputer extends SGDevice implements SGComputerType
{
  private final UUID id;
  private final SGComputerDescription description;
  private final AtomicBoolean running;
  private final SGSimulationInternalAPIType simulation;
  private final Optional<SGKernelType> installed_kernel;
  private final Disposable device_sub;
  private final Disposable connector_sub;
  private SGStorageDeviceType boot_device;

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
    this.installed_kernel = Optional.empty();

    this.device_sub =
      in_simulation.events()
        .ofType(SGDeviceEventType.class)
        .subscribe(this::onDeviceEvent);

    this.connector_sub =
      in_simulation.events()
      .ofType(SGConnectorEventType.class)
      .subscribe(this::onConnectorEvent);
  }

  private void onConnectorEvent(
    final SGConnectorEventType event)
  {
    if (event instanceof SGConnectorEventDisconnected) {
      this.onConnectorDisconnected((SGConnectorEventDisconnected) event);
    }
  }

  private void onConnectorDisconnected(
    final SGConnectorEventDisconnected event)
  {
    if (this.boot_device != null) {
      if (!this.simulation.deviceGraph().areDirectlyConnected(this, this.boot_device)) {
        this.boot_device = null;
      }
    }
  }

  private void onDeviceEvent(
    final SGDeviceEventType event)
  {
    if (event instanceof SGDeviceEventDestroying) {
      this.onDeviceEventDestroying((SGDeviceEventDestroying) event);
    }
  }

  private void onDeviceEventDestroying(
    final SGDeviceEventDestroying event)
  {
    if (this.boot_device != null && Objects.equals(this.boot_device.id(), event.id())) {
      this.boot_device = null;
    }
  }

  @Override
  public void boot()
  {
    if (this.running.compareAndSet(false, true)) {
      this.simulation.publishEvent(SGComputerEventBooting.of(this.id));
      if (this.installed_kernel.isPresent()) {
        this.simulation.publishEvent(SGComputerEventBooted.of(this.id));
      } else {
        this.running.set(false);
        this.simulation.publishEvent(SGComputerEventBootFailed.of(this.id, "No kernel installed"));
      }
    }
  }

  @Override
  public void shutdown()
  {
    if (this.running.compareAndSet(true, false)) {
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
  public void setBootDevice(
    final SGStorageDeviceType device)
    throws SGDeviceNotConnectedException
  {
    Objects.requireNonNull(device, "device");

    if (this.simulation.deviceGraph().areDirectlyConnected(this, device)) {
      this.boot_device = device;
    } else {
      throw new SGDeviceNotConnectedException(this, device);
    }
  }

  @Override
  public Optional<SGStorageDeviceType> bootDevice()
  {
    return Optional.ofNullable(this.boot_device);
  }

  @Override
  protected void onClose()
  {
    this.device_sub.dispose();
    this.connector_sub.dispose();
  }
}
