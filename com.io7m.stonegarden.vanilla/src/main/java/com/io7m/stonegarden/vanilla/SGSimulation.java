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

import com.io7m.stonegarden.api.SGEventType;
import com.io7m.stonegarden.api.SGIdentifiableType;
import com.io7m.stonegarden.api.computer.SGComputerDescription;
import com.io7m.stonegarden.api.computer.SGComputerType;
import com.io7m.stonegarden.api.devices.SGDeviceEventCreated;
import com.io7m.stonegarden.api.devices.SGDeviceType;
import com.io7m.stonegarden.api.devices.SGStorageDeviceDescription;
import com.io7m.stonegarden.api.devices.SGStorageDeviceType;
import com.io7m.stonegarden.api.simulation.SGSimulationEventTick;
import com.io7m.stonegarden.api.simulation.SGSimulationType;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

final class SGSimulation implements SGSimulationType, SGSimulationInternalAPIType
{
  private final PublishSubject<SGEventType> events;
  private final HashMap<UUID, SGIdentifiableType> objects;
  private final HashSet<UUID> uuids;
  private final AtomicBoolean closed;
  private final Observable<SGEventType> events_distinct;
  private final SGDeviceGraph device_graph;
  private BigInteger frame;

  SGSimulation(
    final PublishSubject<SGEventType> in_events)
  {
    this.events = Objects.requireNonNull(in_events, "events");
    this.events_distinct = this.events.distinctUntilChanged();

    this.objects = new HashMap<>(128);
    this.uuids = new HashSet<>(128);
    this.closed = new AtomicBoolean(false);
    this.device_graph = new SGDeviceGraph(this.events::onNext, this.events_distinct, this.objects);
    this.frame = BigInteger.ZERO;
  }

  @Override
  public void close()
  {
    if (this.closed.compareAndSet(false, true)) {
      this.events.onComplete();
    }
  }

  private void checkNotClosed()
  {
    if (this.closed.get()) {
      throw new IllegalStateException("Simulation has been closed");
    }
  }

  @Override
  public void tick(final double seconds)
  {
    this.checkNotClosed();

    this.events.onNext(SGSimulationEventTick.of(this.frame, seconds));
    this.frame = this.frame.add(BigInteger.ONE);
  }

  @Override
  public Observable<SGEventType> events()
  {
    return this.events_distinct;
  }

  @Override
  public SGComputerType createComputer(
    final SGComputerDescription description)
  {
    Objects.requireNonNull(description, "description");

    this.checkNotClosed();
    return this.createDevice(uuid -> new SGComputer(this, uuid, description));
  }

  private <T extends SGDeviceType> T createDevice(
    final Function<UUID, T> constructor)
  {
    final var uuid = this.freshUUID();
    final var device = constructor.apply(uuid);
    this.objects.put(uuid, device);
    this.device_graph.addDevice(device);
    this.events.onNext(SGDeviceEventCreated.of(uuid));
    return device;
  }

  @Override
  public SGStorageDeviceType createStorageDevice(
    final SGStorageDeviceDescription description)
  {
    Objects.requireNonNull(description, "description");

    this.checkNotClosed();
    return this.createDevice(uuid -> new SGStorageDevice(this, uuid, description));
  }

  @Override
  public UUID freshUUID()
  {
    while (true) {
      final var uuid = UUID.randomUUID();
      if (this.uuids.contains(uuid)) {
        continue;
      }
      this.uuids.add(uuid);
      return uuid;
    }
  }

  @Override
  public SGDeviceGraph deviceGraph()
  {
    return this.device_graph;
  }

  @Override
  public void publishEvent(final SGEventType event)
  {
    this.events.onNext(Objects.requireNonNull(event, "event"));
  }
}
