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

import com.io7m.stonegarden.api.devices.SGDeviceNotConnectedException;
import com.io7m.stonegarden.api.devices.SGDeviceType;
import com.io7m.stonegarden.api.devices.SGStorageDeviceType;

import java.util.Optional;

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
   * Set the boot device for the computer. Must be connected to the computer.
   *
   * @param device The device
   *
   * @throws SGDeviceNotConnectedException If the boot device is not connected to the computer
   */

  void setBootDevice(SGStorageDeviceType device)
    throws SGDeviceNotConnectedException;

  /**
   * @return The computer's boot device, if one exists
   */

  Optional<SGStorageDeviceType> bootDevice();

  /**
   * Boot the computer. Does nothing if the computer is already running.
   */

  void boot();

  /**
   * Shut down the computer. Does nothing if the computer is not running.
   */

  void shutdown();

  /**
   * @return {@code true} if {@link #boot()} has been called, booting succeeded, and {@link
   * #shutdown()} has not been called since then
   */

  boolean isRunning();
}
