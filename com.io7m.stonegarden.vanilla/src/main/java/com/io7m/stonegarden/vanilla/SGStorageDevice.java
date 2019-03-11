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

import com.io7m.stonegarden.api.devices.SGStorageDeviceDescription;
import com.io7m.stonegarden.api.devices.SGStorageDeviceKernelInterfaceType;
import com.io7m.stonegarden.api.devices.SGStorageDeviceOutOfSpaceException;
import com.io7m.stonegarden.api.kernels.SGKernelExecutableDescription;
import com.io7m.stonegarden.api.kernels.SGKernelExecutableDescriptionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

final class SGStorageDevice extends SGDevice implements SGStorageDeviceKernelInterfaceType
{
  private static final Logger LOG = LoggerFactory.getLogger(SGStorageDevice.class);

  private final SGStorageDeviceDescription description;
  private final ArrayList<SGKernelExecutableDescriptionType> kernels;
  private final List<SGKernelExecutableDescriptionType> kernels_read;
  private BigInteger space_used;

  SGStorageDevice(
    final SGSimulationInternalAPIType in_simulation,
    final UUID in_uuid,
    final SGStorageDeviceDescription in_description)
  {
    super(in_simulation, in_description, in_uuid);
    this.description = Objects.requireNonNull(in_description, "description");
    this.kernels = new ArrayList<>(in_description.kernels());
    this.kernels_read = Collections.unmodifiableList(this.kernels);
    this.space_used = BigInteger.valueOf(0L);
  }

  @Override
  public SGStorageDeviceDescription description()
  {
    return this.description;
  }

  @Override
  public BigInteger spaceUsedOctets()
  {
    return this.space_used;
  }

  @Override
  public List<SGKernelExecutableDescriptionType> kernels()
  {
    return this.kernels_read;
  }

  @Override
  protected void onClose()
  {

  }

  @Override
  public CompletableFuture<Void> addKernel(
    final SGKernelExecutableDescription kernel)
  {
    Objects.requireNonNull(kernel, "kernel");

    return this.simulation().runLater(() -> {
      final var required = kernel.description().sizeOctets();
      if (!this.spaceAvailableFor(required)) {
        throw new SGStorageDeviceOutOfSpaceException(this, required);
      }

      LOG.debug(
        "[{}]: add kernel {} {} ({} octets)",
        this.id(),
        kernel.description().name(),
        kernel.description().version().toHumanString(),
        required);
      this.kernels.add(Objects.requireNonNull(kernel, "kernel"));
      this.space_used = this.space_used.add(required);
    });
  }
}
