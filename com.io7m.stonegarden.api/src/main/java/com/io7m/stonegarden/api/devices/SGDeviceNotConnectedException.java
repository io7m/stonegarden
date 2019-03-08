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

import com.io7m.stonegarden.api.SGException;

/**
 * A device is not connected.
 */

public final class SGDeviceNotConnectedException extends SGException
{
  /**
   * Construct an exception.
   *
   * @param host      The device to which the other device is supposed to be connected
   * @param connected The device that is not connected
   */

  public SGDeviceNotConnectedException(
    final SGDeviceType host,
    final SGDeviceType connected)
  {
    super(makeMessage(host, connected));
  }

  private static String makeMessage(
    final SGDeviceType host,
    final SGDeviceType connected)
  {
    final var separator = System.lineSeparator();
    return new StringBuilder(128)
      .append("Device is not connected.")
      .append(separator)
      .append("  Host: ")
      .append(host.kind())
      .append(" ")
      .append(host.id())
      .append(separator)
      .append("  Device: ")
      .append(connected.kind())
      .append(" ")
      .append(connected.id())
      .append(separator)
      .toString();
  }
}
