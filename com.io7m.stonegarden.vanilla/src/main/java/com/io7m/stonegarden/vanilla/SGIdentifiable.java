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

import com.io7m.stonegarden.api.SGIdentifiableType;

import java.util.Objects;
import java.util.UUID;

abstract class SGIdentifiable implements SGIdentifiableType
{
  private final UUID id;

  protected SGIdentifiable(
    final UUID in_id)
  {
    this.id = Objects.requireNonNull(in_id, "id");
  }

  @Override
  public final boolean equals(final Object o)
  {
    if (this == o) {
      return true;
    }
    if (o == null || !Objects.equals(this.getClass(), o.getClass())) {
      return false;
    }
    final var other = (SGIdentifiable) o;
    return this.id.equals(other.id);
  }

  @Override
  public final int hashCode()
  {
    return Objects.hash(this.id);
  }

  @Override
  public final UUID id()
  {
    return this.id;
  }
}
