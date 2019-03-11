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

import com.io7m.stonegarden.api.SGArchitecture;
import com.io7m.stonegarden.api.SGVersion;
import com.io7m.stonegarden.api.devices.SGStorageDeviceKernelInterfaceType;
import com.io7m.stonegarden.api.kernels.SGKernelCompatibility;
import com.io7m.stonegarden.api.kernels.SGKernelContextType;
import com.io7m.stonegarden.api.kernels.SGKernelDescription;
import com.io7m.stonegarden.api.kernels.SGKernelExecutableDescription;
import com.io7m.stonegarden.api.kernels.SGKernelType;
import com.io7m.stonegarden.api.simulation.SGSimulationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Objects;
import java.util.Properties;

/**
 * A generic installer kernel.
 */

public final class SGKernelInstaller
{
  private static final Logger LOG = LoggerFactory.getLogger(SGKernelInstaller.class);

  private SGKernelInstaller()
  {

  }

  /**
   * Create a kernel.
   *
   * @param architecture The architecture on which the kernel will run
   * @param install      The kernel that will be installed
   *
   * @return A kernel
   */

  public static SGKernelExecutableDescription create(
    final SGArchitecture architecture,
    final SGKernelExecutableDescription install)
  {
    final var description =
      SGKernelDescription.builder()
        .setCompatibility(SGKernelCompatibility.of(architecture))
        .setName("INSTALLER")
        .setVersion(SGVersion.of(0, 1, 0))
        .setSizeOctets(BigInteger.valueOf(5855104L))
        .build();

    return SGKernelExecutableDescription.builder()
      .setDescription(description)
      .setExecutable((simulation, context, parameters) -> new Kernel(simulation, install, context, parameters))
      .build();
  }

  private static final class Kernel implements SGKernelType
  {
    private final SGSimulationType simulation;
    private final SGKernelExecutableDescription install;
    private final SGKernelContextType context;
    private final Properties parameters;

    Kernel(
      final SGSimulationType in_simulation,
      final SGKernelExecutableDescription in_install,
      final SGKernelContextType in_context,
      final Properties in_parameters)
    {
      this.simulation =
        Objects.requireNonNull(in_simulation, "simulation");
      this.install =
        Objects.requireNonNull(in_install, "install");
      this.context =
        Objects.requireNonNull(in_context, "context");
      this.parameters =
        Objects.requireNonNull(in_parameters, "parameters");
    }

    @Override
    public void onStart()
    {
      try {
        this.context.writeConsole(
          "starting installer for %s %s",
          this.install.description().name(),
          this.install.description().version().toHumanString());

        final var target = this.parameters.getProperty("target_device");
        if (target == null) {
          this.context.writeConsole("no target device specified");
          return;
        }

        final var device_opt =
          this.context.connectedDevices()
            .stream()
            .filter(device -> Objects.equals(device.id().toString(), target))
            .findFirst();

        if (device_opt.isEmpty()) {
          this.context.writeConsole("target device does not exist or is not connected");
          return;
        }

        final var device = device_opt.get();
        if (!(device instanceof SGStorageDeviceKernelInterfaceType)) {
          this.context.writeConsole("target device is not a storage device");
          return;
        }

        final var storage_device = (SGStorageDeviceKernelInterfaceType) device;
        if (!storage_device.spaceAvailableFor(this.install.description().sizeOctets())) {
          this.context.writeConsole("not enough space available to install kernel");
          return;
        }

        storage_device.addKernel(this.install);
      } catch (final Exception e) {
        this.context.writeConsole("an error occurred during kernel installation");
        throw e;
      } finally {
        this.context.writeConsole("shutting down");
        this.context.shutdown();
      }
    }

    @Override
    public void onShutDown()
    {
      this.context.writeConsole("shutting down");
    }
  }
}
