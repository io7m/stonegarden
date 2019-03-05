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
import com.io7m.stonegarden.api.devices.SGStorageDeviceType;

import java.util.Objects;
import java.util.UUID;

final class SGStorageDevice extends SGDevice implements SGStorageDeviceType
{
  private final SGStorageDeviceDescription description;

  SGStorageDevice(
    final SGSimulationInternalAPIType in_simulation,
    final UUID in_uuid,
    final SGStorageDeviceDescription in_description)
  {
    super(in_simulation, in_description, in_uuid);
    this.description = Objects.requireNonNull(in_description, "description");
  }

  @Override
  public SGStorageDeviceDescription description()
  {
    return this.description;
  }
}
