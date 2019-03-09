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

import com.io7m.stonegarden.api.devices.SGDeviceType;

import java.util.List;

/**
 * A computer instance.
 */

public interface SGComputerType extends SGDeviceType
{
  @Override
  SGComputerDescription description();

  @Override
  default String kind()
  {
    return "computer";
  }

  /**
   * Boot the computer. Does nothing if the computer is already running.
   *
   * @param order The boot order
   */

  void boot(List<SGComputerBootOrderItem> order);

  /**
   * Shut down the computer. Does nothing if the computer is not running.
   */

  void shutdown();

  /**
   * @return {@code true} if {@link #boot(List)} has been called, booting succeeded, and {@link
   * #shutdown()} has not been called since then
   */

  boolean isRunning();
}
