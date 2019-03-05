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
import com.io7m.stonegarden.api.kernels.SGKernelType;

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
}
