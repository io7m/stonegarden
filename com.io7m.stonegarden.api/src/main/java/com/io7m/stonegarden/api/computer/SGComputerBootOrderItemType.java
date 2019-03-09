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


package com.io7m.stonegarden.api.computer;

import com.io7m.immutables.styles.ImmutablesStyleType;
import com.io7m.stonegarden.api.SGVersion;
import com.io7m.stonegarden.api.devices.SGStorageDeviceType;
import org.immutables.value.Value;

import java.util.Properties;

/**
 * An item in a computer's boot order.
 */

@ImmutablesStyleType
@Value.Immutable
public interface SGComputerBootOrderItemType
{
  /**
   * @return The kernel name
   */

  @Value.Parameter
  String name();

  /**
   * @return The kernel version
   */

  @Value.Parameter
  SGVersion version();

  /**
   * @return Parameters passed to the kernel
   */

  @Value.Parameter
  Properties parameters();

  /**
   * @return The device from which a kernel should be loaded
   */

  @Value.Parameter
  SGStorageDeviceType device();
}
