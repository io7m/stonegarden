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

import com.io7m.stonegarden.api.computer.SGComputerType;
import com.io7m.stonegarden.api.kernels.SGKernelDescription;
import com.io7m.stonegarden.api.kernels.SGKernelType;

import java.util.Objects;
import java.util.UUID;

final class SGKernel implements SGKernelType
{
  private final UUID id;
  private final SGComputerType computer;
  private final SGKernelDescription description;

  SGKernel(
    final SGSimulationInternalAPIType in_simulation,
    final UUID in_id,
    final SGComputerType in_computer,
    final SGKernelDescription in_description)
  {
    Objects.requireNonNull(in_simulation, "simulation");

    this.id =
      Objects.requireNonNull(in_id, "id");
    this.computer =
      Objects.requireNonNull(in_computer, "computer");
    this.description =
      Objects.requireNonNull(in_description, "description");
  }

  @Override
  public UUID id()
  {
    return this.id;
  }
}
