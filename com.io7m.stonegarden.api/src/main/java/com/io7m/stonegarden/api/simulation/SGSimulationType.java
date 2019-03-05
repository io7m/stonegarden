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

package com.io7m.stonegarden.api.simulation;

import com.io7m.stonegarden.api.SGEventType;
import com.io7m.stonegarden.api.computer.SGComputerDescription;
import com.io7m.stonegarden.api.computer.SGComputerType;
import com.io7m.stonegarden.api.devices.SGStorageDeviceDescription;
import com.io7m.stonegarden.api.devices.SGStorageDeviceType;
import io.reactivex.Observable;

/**
 * A simulation.
 */

public interface SGSimulationType extends AutoCloseable
{
  @Override
  void close()
    throws Exception;

  /**
   * @return An observable that produces events
   */

  Observable<SGEventType> events();

  /**
   * Create a new computer.
   *
   * @param description The description
   *
   * @return A new computer
   */

  SGComputerType createComputer(
    SGComputerDescription description);

  /**
   * Create a new storage device.
   *
   * @param description The description
   *
   * @return A new storage device
   */

  SGStorageDeviceType createStorageDevice(
    SGStorageDeviceDescription description);
}
