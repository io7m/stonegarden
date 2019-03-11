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

import org.junit.jupiter.api.Assertions;

import java.util.List;
import java.util.function.Consumer;

public final class EventAssertions
{
  private EventAssertions()
  {

  }

  /**
   * Assert that the element at {@code index} in {@code events} is of type {@code clazz} and obeys
   * the property given in {@code property}.
   */

  public static <T extends TB, TB> void isTypeAndMatches(
    final Class<T> clazz,
    final List<TB> events,
    final int index,
    final Consumer<T> property)
  {
    Assertions.assertTrue(
      events.size() >= index + 1,
      new StringBuilder(64)
        .append("List of events must be at least ")
        .append(index + 1)
        .append(" elements long (is: ")
        .append(events.size())
        .append(")")
        .toString());

    final var x = events.get(index);
    isTypeAndMatches(clazz, x, property);
  }

  public static <T extends TB, TB> void isTypeAndMatches(
    final Class<T> clazz,
    final TB x,
    final Consumer<T> property)
  {
    final var y = isType(clazz, x);
    property.accept(y);
  }

  public static <T extends TB, TB> T isType(
    final Class<T> clazz,
    final TB x)
  {
    Assertions.assertTrue(x.getClass().equals(clazz), "Class is " + clazz);
    return (T) x;
  }

  /**
   * Assert that the element at {@code index} in {@code events} is of type {@code clazz}.
   */

  public static <T extends TB, TB> void isType(
    final Class<T> clazz,
    final List<TB> events,
    final int index)
  {
    isTypeAndMatches(clazz, events, index, e -> {
    });
  }
}
