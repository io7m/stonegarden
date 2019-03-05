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

import com.io7m.stonegarden.api.SGArchitecture;
import com.io7m.stonegarden.api.SGEventType;
import com.io7m.stonegarden.api.computer.SGComputerDescription;
import com.io7m.stonegarden.api.computer.SGComputerEventBootFailed;
import com.io7m.stonegarden.api.computer.SGComputerEventBooting;
import com.io7m.stonegarden.api.connectors.SGConnectorDescription;
import com.io7m.stonegarden.api.connectors.SGConnectorEventConnected;
import com.io7m.stonegarden.api.connectors.SGConnectorProtocol;
import com.io7m.stonegarden.api.connectors.SGConnectorProtocolName;
import com.io7m.stonegarden.api.connectors.SGConnectorSocketDescription;
import com.io7m.stonegarden.api.devices.SGDeviceEventCreated;
import com.io7m.stonegarden.api.devices.SGStorageDeviceDescription;
import com.io7m.stonegarden.api.simulation.SGSimulationType;
import io.reactivex.disposables.Disposable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.util.ArrayList;

public abstract class SGComputerContract
{
  private static final SGArchitecture ARCH_0 =
    SGArchitecture.builder()
      .setName("PK3")
      .build();

  private static final SGConnectorProtocol HARDWARE_PORT_PROTOCOL_0 =
    SGConnectorProtocol.of(SGConnectorProtocolName.of("GPB-0"));

  private ArrayList<SGEventType> events;
  private SGSimulationType simulation;
  private Disposable subscription;
  private Logger logger;

  protected abstract SGSimulationType createSimulation();

  protected abstract Logger logger();

  @BeforeEach
  public final void testSetup()
  {
    this.logger = this.logger();
    this.events = new ArrayList<>();
    this.simulation = this.createSimulation();
    this.subscription = this.simulation.events().subscribe(this::eventPublished);
  }

  @AfterEach
  public final void testTearDown()
    throws Exception
  {
    this.simulation.close();
    Assertions.assertTrue(this.subscription.isDisposed(), "Events closed");
  }

  @Test
  public final void testCreateComputer()
    throws Exception
  {
    final Disposable observable;
    try (var sim = this.createSimulation()) {
      observable = sim.events().subscribe(this::eventPublished);

      final var computer =
        sim.createComputer(
          SGComputerDescription.builder()
            .setArchitecture(ARCH_0)
            .build());

      Assertions.assertEquals(0, computer.connectors().size(), "Correct connector count");
      Assertions.assertEquals(0, computer.sockets().size(), "Correct socket count");

      Assertions.assertEquals(1, this.events.size(), "Correct event count");
      EventAssertions.isType(SGDeviceEventCreated.class, this.events, 0);
    }

    Assertions.assertTrue(observable.isDisposed(), "Events closed");
  }

  @Test
  public final void testCreateComputerBootInstallKernel()
    throws Exception
  {
    final var device =
      this.simulation.createStorageDevice(
        SGStorageDeviceDescription.builder()
          .addConnectors(SGConnectorDescription.of(HARDWARE_PORT_PROTOCOL_0))
          .build());

    final var computer =
      this.simulation.createComputer(
        SGComputerDescription.builder()
          .setArchitecture(ARCH_0)
          .addSockets(SGConnectorSocketDescription.of(HARDWARE_PORT_PROTOCOL_0))
          .build());

    final var socket = computer.sockets().get(0);
    final var connector = device.connectors().get(0);

    connector.connectTo(socket);

    computer.boot();
    computer.shutdown();

    Assertions.assertEquals(5, this.events.size(), "Correct event count");
    EventAssertions.isTypeAndMatches(
      SGDeviceEventCreated.class,
      this.events,
      0,
      e -> Assertions.assertEquals(device.id(), e.id()));
    EventAssertions.isTypeAndMatches(
      SGDeviceEventCreated.class,
      this.events,
      1,
      e -> Assertions.assertEquals(computer.id(), e.id()));
    EventAssertions.isTypeAndMatches(
      SGConnectorEventConnected.class,
      this.events,
      2,
      e -> Assertions.assertEquals(connector.id(), e.connector()));
    EventAssertions.isTypeAndMatches(
      SGComputerEventBooting.class,
      this.events,
      3,
      e -> Assertions.assertEquals(computer.id(), e.id()));
    EventAssertions.isTypeAndMatches(
      SGComputerEventBootFailed.class,
      this.events,
      4,
      e -> {
        Assertions.assertEquals(computer.id(), e.id());
        Assertions.assertEquals("No kernel installed", e.message());
      });
  }

  @Test
  public final void testCreateComputerBootNoKernel()
  {
    final var computer =
      this.simulation.createComputer(
        SGComputerDescription.builder()
          .setArchitecture(ARCH_0)
          .build());

    computer.boot();
    computer.shutdown();

    Assertions.assertEquals(3, this.events.size(), "Correct event count");
    EventAssertions.isTypeAndMatches(
      SGDeviceEventCreated.class,
      this.events,
      0,
      e -> Assertions.assertEquals(computer.id(), e.id()));
    EventAssertions.isTypeAndMatches(
      SGComputerEventBooting.class,
      this.events,
      1,
      e -> Assertions.assertEquals(computer.id(), e.id()));
    EventAssertions.isTypeAndMatches(
      SGComputerEventBootFailed.class,
      this.events,
      2,
      e -> {
        Assertions.assertEquals(computer.id(), e.id());
        Assertions.assertEquals("No kernel installed", e.message());
      });
  }

  private void eventPublished(
    final SGEventType event)
  {
    this.logger().debug("event: {}", event);
    this.events.add(event);
  }
}
