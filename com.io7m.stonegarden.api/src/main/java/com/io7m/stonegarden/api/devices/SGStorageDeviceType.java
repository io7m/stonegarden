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

package com.io7m.stonegarden.api.devices;

import com.io7m.stonegarden.api.kernels.SGKernelExecutableDescriptionType;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

/**
 * A storage device instance.
 */

public interface SGStorageDeviceType extends SGDeviceType
{
  @Override
  SGStorageDeviceDescription description();

  @Override
  default String kind()
  {
    return "storage-device";
  }

  /**
   * @return The amount of space used on the device
   */

  BigInteger spaceUsedOctets();

  /**
   * @return The amount of space available on the device
   */

  default BigInteger spaceAvailableOctets()
  {
    return this.description().spaceCapacityOctets().subtract(this.spaceUsedOctets());
  }

  /**
   * @param required The number of octets required
   *
   * @return {@code true} if {@code required} or more octets of space are available on the device
   */

  default boolean spaceAvailableFor(
    final BigInteger required)
  {
    Objects.requireNonNull(required, "required");
    return this.spaceAvailableOctets().compareTo(required) >= 0;
  }

  /**
   * @return The list of bootable kernels available on the storage device
   */

  List<SGKernelExecutableDescriptionType> kernels();
}
