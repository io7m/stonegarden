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


package com.io7m.stonegarden.tests;

import com.io7m.stonegarden.api.SGVersion;
import com.io7m.stonegarden.api.SGVersionRange;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

public final class SGVersionTest
{
  private static final Logger LOG = LoggerFactory.getLogger(SGVersionTest.class);

  @Test
  public void testOrdering()
  {
    final var v1_0_0 = SGVersion.of(1, 0, 0);
    final var v1_1_0 = SGVersion.of(1, 1, 0);
    final var v1_1_1 = SGVersion.of(1, 1, 1);
    final var v2_0_0 = SGVersion.of(2, 0, 0);

    final var ordered = List.of(v1_0_0, v1_1_0, v1_1_1, v2_0_0);

    for (var index = 0; index < ordered.size(); ++index) {
      final var vx = ordered.get(index);
      for (var x = index; x >= 0; --x) {
        final var vy = ordered.get(x);

        LOG.debug("compare: {} {} → {}", vx, vy, Integer.valueOf(vx.compareTo(vy)));
        if (index == x) {
          Assertions.assertEquals(0, vx.compareTo(vy));
        } else {
          Assertions.assertTrue(vx.compareTo(vy) > 0);
          Assertions.assertTrue(vy.compareTo(vx) < 0);
        }
      }
    }
  }

  @Test
  public void testRangeInvalid()
  {
    final var v1_0_0 = SGVersion.of(1, 0, 0);
    final var v2_0_0 = SGVersion.of(2, 0, 0);

    Assertions.assertThrows(
      IllegalArgumentException.class,
      () -> SGVersionRange.of(v2_0_0, false, v1_0_0, false));
  }

  @Test
  public void testRanges()
  {
    final var v1_0_0 = SGVersion.of(1, 0, 0);
    final var v1_1_0 = SGVersion.of(1, 1, 0);
    final var v1_1_1 = SGVersion.of(1, 1, 1);
    final var v2_0_0 = SGVersion.of(2, 0, 0);
    final var ordered = List.of(v1_0_0, v1_1_0, v1_1_1, v2_0_0);

    {
      final var range = SGVersionRange.of(v1_0_0, false, v2_0_0, false);
      for (final var v : ordered) {
        LOG.debug("includes: {} {} → {}", range, v, Boolean.valueOf(range.includes(v)));
        Assertions.assertTrue(range.includes(v));
      }
    }

    {
      final var range = SGVersionRange.of(v1_0_0, false, v2_0_0, true);
      for (final var v : ordered) {
        LOG.debug("includes: {} {} → {}", range, v, Boolean.valueOf(range.includes(v)));
        Assertions.assertEquals(
          Boolean.valueOf(!Objects.equals(v, v2_0_0)),
          Boolean.valueOf(range.includes(v)));
      }
    }

    {
      final var range = SGVersionRange.of(v1_0_0, true, v2_0_0, true);
      for (final var v : ordered) {
        LOG.debug("includes: {} {} → {}", range, v, Boolean.valueOf(range.includes(v)));
        Assertions.assertEquals(
          Boolean.valueOf(!Objects.equals(v, v2_0_0) && !Objects.equals(v, v1_0_0)),
          Boolean.valueOf(range.includes(v)));
      }
    }

    {
      final var range = SGVersionRange.of(v1_0_0, true, v2_0_0, false);
      for (final var v : ordered) {
        LOG.debug("includes: {} {} → {}", range, v, Boolean.valueOf(range.includes(v)));
        Assertions.assertEquals(
          Boolean.valueOf(!Objects.equals(v, v1_0_0)),
          Boolean.valueOf(range.includes(v)));
      }
    }
  }
}
