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

import com.io7m.stonegarden.api.SGEventType;
import com.io7m.stonegarden.api.simulation.SGSimulationType;
import io.reactivex.disposables.Disposable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.util.ArrayList;

public abstract class SGSimulationContract
{
  private ArrayList<SGEventType> events;

  protected abstract SGSimulationType createSimulation();

  protected abstract Logger logger();

  @BeforeEach
  public final void testSetup()
  {
    this.events = new ArrayList<>();
  }

  @Test
  public final void testEmpty()
    throws Exception
  {
    final Disposable observable;
    try (var sim = this.createSimulation()) {
      observable = sim.events().subscribe(this::eventPublished);
    }
    Assertions.assertTrue(observable.isDisposed(), "Events closed");
  }


  private void eventPublished(
    final SGEventType event)
  {
    this.logger().debug("event: {}", event);
    this.events.add(event);
  }
}
