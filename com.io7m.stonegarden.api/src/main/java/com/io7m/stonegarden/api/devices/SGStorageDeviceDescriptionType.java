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

import com.io7m.immutables.styles.ImmutablesStyleType;
import com.io7m.stonegarden.api.connectors.SGConnectorDescription;
import com.io7m.stonegarden.api.connectors.SGConnectorSocketDescription;
import com.io7m.stonegarden.api.kernels.SGKernelExecutableDescription;
import org.immutables.value.Value;

import java.math.BigInteger;
import java.util.List;

/**
 * Storage device description.
 */

@ImmutablesStyleType
@Value.Immutable
public interface SGStorageDeviceDescriptionType extends SGDeviceDescriptionType
{
  @Override
  @Value.Parameter
  List<SGConnectorSocketDescription> sockets();

  @Override
  @Value.Parameter
  List<SGConnectorDescription> connectors();

  /**
   * @return The size of the storage device in octets
   */

  @Value.Parameter
  @Value.Default
  default BigInteger spaceCapacityOctets()
  {
    return BigInteger.ZERO;
  }

  /**
   * @return The kernels that will initially be present on the device
   */

  @Value.Parameter
  List<SGKernelExecutableDescription> kernels();
}
