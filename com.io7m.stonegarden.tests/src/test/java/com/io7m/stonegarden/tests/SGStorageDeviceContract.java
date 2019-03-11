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
import com.io7m.stonegarden.api.connectors.SGConnectedAlreadyException;
import com.io7m.stonegarden.api.connectors.SGConnectorDescription;
import com.io7m.stonegarden.api.connectors.SGConnectorEventConnected;
import com.io7m.stonegarden.api.connectors.SGConnectorEventDisconnected;
import com.io7m.stonegarden.api.connectors.SGConnectorIncompatibleException;
import com.io7m.stonegarden.api.connectors.SGConnectorProtocol;
import com.io7m.stonegarden.api.connectors.SGConnectorProtocolName;
import com.io7m.stonegarden.api.connectors.SGConnectorSocketDescription;
import com.io7m.stonegarden.api.devices.SGDeviceEventCreated;
import com.io7m.stonegarden.api.devices.SGDeviceEventType;
import com.io7m.stonegarden.api.devices.SGStorageDeviceDescription;
import com.io7m.stonegarden.api.simulation.SGSimulationEventTick;
import com.io7m.stonegarden.api.simulation.SGSimulationType;
import io.reactivex.disposables.Disposable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;

public abstract class SGStorageDeviceContract
{
  private static final SGArchitecture ARCH_0 =
    SGArchitecture.builder()
      .setName("PK3")
      .build();

  private static final SGConnectorProtocol HARDWARE_PORT_PROTOCOL_0 =
    SGConnectorProtocol.of(SGConnectorProtocolName.of("GPC-0"));
  private static final SGConnectorProtocol HARDWARE_PORT_PROTOCOL_1 =
    SGConnectorProtocol.of(SGConnectorProtocolName.of("GPC-1"));

  private ConcurrentLinkedQueue<SGEventType> events;
  private SGSimulationType simulation;
  private Disposable subscription;
  private Logger logger;

  @SuppressWarnings("unchecked")
  private static <T extends Exception> T exceptionOfFuture(
    final Class<T> clazz,
    final CompletableFuture<?> f)
  {
    try {
      f.get();
      Assertions.fail("Future has not completed exceptionally");
      return null;
    } catch (final Exception e) {
      final Throwable ex;

      if (e instanceof ExecutionException) {
        ex = e.getCause();
      } else {
        ex = e;
      }

      if (Objects.equals(ex.getClass(), clazz)) {
        return (T) ex;
      }

      Assertions.fail(
        new StringBuilder()
          .append("Wrong exception.")
          .append(System.lineSeparator())
          .append("  Expected: ")
          .append(clazz)
          .append(System.lineSeparator())
          .append("  Received: ")
          .append(e.getClass())
          .append(System.lineSeparator())
          .toString());
      return null;
    }
  }

  protected abstract SGSimulationType createSimulation();

  protected abstract Logger logger();

  @BeforeEach
  public final void testSetup()
  {
    this.logger = this.logger();
    this.events = new ConcurrentLinkedQueue<>();
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
  public final void testCreateStorageDevice()
  {
    final var device =
      this.simulation.createStorageDevice(
        SGStorageDeviceDescription.builder()
          .addConnectors(SGConnectorDescription.of(HARDWARE_PORT_PROTOCOL_0))
          .build());

    Assertions.assertEquals(1, this.events.size(), "Correct event count");
    EventAssertions.isType(SGDeviceEventCreated.class, this.events.poll());
  }

  @Test
  public final void testCreateStorageDeviceConnectIdentity0()
  {
    final var computer =
      this.simulation.createComputer(
        SGComputerDescription.builder()
          .setArchitecture(ARCH_0)
          .addSockets(SGConnectorSocketDescription.of(HARDWARE_PORT_PROTOCOL_0))
          .build());

    final var socket = computer.sockets().get(0);
    Assertions.assertEquals(computer, socket.owner());

    final var device =
      this.simulation.createStorageDevice(
        SGStorageDeviceDescription.builder()
          .addConnectors(SGConnectorDescription.of(HARDWARE_PORT_PROTOCOL_0))
          .build());

    final var connector = device.connectors().get(0);
    Assertions.assertEquals(device, connector.owner());

    connector.connectTo(socket);
    this.simulation.tick(1.0 / 60.0);

    Assertions.assertEquals(Optional.of(socket), connector.connectedTo());
    Assertions.assertEquals(Optional.of(connector), socket.connectedTo());

    connector.disconnect();
    this.simulation.tick(1.0 / 60.0);

    Assertions.assertEquals(Optional.empty(), connector.connectedTo());
    Assertions.assertEquals(Optional.empty(), socket.connectedTo());

    socket.connectTo(connector);
    this.simulation.tick(1.0 / 60.0);

    Assertions.assertEquals(Optional.of(socket), connector.connectedTo());
    Assertions.assertEquals(Optional.of(connector), socket.connectedTo());

    socket.disconnect();
    this.simulation.tick(1.0 / 60.0);

    Assertions.assertEquals(Optional.empty(), connector.connectedTo());
    Assertions.assertEquals(Optional.empty(), socket.connectedTo());

    socket.disconnect();
    connector.disconnect();
    this.simulation.tick(1.0 / 60.0);

    Assertions.assertEquals(11, this.events.size());
    this.events.removeIf(event -> event instanceof SGSimulationEventTick);

    EventAssertions.isTypeAndMatches(
      SGDeviceEventCreated.class,
      this.events.poll(),
      event -> Assertions.assertEquals(computer.id(), event.id()));

    EventAssertions.isTypeAndMatches(
      SGDeviceEventCreated.class,
      this.events.poll(),
      event -> Assertions.assertEquals(device.id(), event.id()));

    EventAssertions.isTypeAndMatches(
      SGConnectorEventConnected.class,
      this.events.poll(),
      event -> {
        Assertions.assertEquals(connector.id(), event.connector());
        Assertions.assertEquals(socket.id(), event.socket());
      });

    EventAssertions.isTypeAndMatches(
      SGConnectorEventDisconnected.class,
      this.events.poll(),
      event -> {
        Assertions.assertEquals(connector.id(), event.connector());
        Assertions.assertEquals(socket.id(), event.socket());
      });

    EventAssertions.isTypeAndMatches(
      SGConnectorEventConnected.class,
      this.events.poll(),
      event -> {
        Assertions.assertEquals(connector.id(), event.connector());
        Assertions.assertEquals(socket.id(), event.socket());
      });

    EventAssertions.isTypeAndMatches(
      SGConnectorEventDisconnected.class,
      this.events.poll(),
      event -> {
        Assertions.assertEquals(connector.id(), event.connector());
        Assertions.assertEquals(socket.id(), event.socket());
      });
  }

  @Test
  public final void testCreateStorageDeviceAttachIncompatible0()
  {
    final var computer =
      this.simulation.createComputer(
        SGComputerDescription.builder()
          .setArchitecture(ARCH_0)
          .addSockets(SGConnectorSocketDescription.of(HARDWARE_PORT_PROTOCOL_0))
          .build());

    final var socket =
      computer.sockets().get(0);

    final var device =
      this.simulation.createStorageDevice(
        SGStorageDeviceDescription.builder()
          .addConnectors(SGConnectorDescription.of(HARDWARE_PORT_PROTOCOL_1))
          .build());

    final var connector0 = device.connectors().get(0);
    final var connecting0 = socket.connectTo(connector0);

    this.simulation.tick(1.0 / 60.0);
    Assertions.assertTrue(connecting0.isDone());

    final var ex = exceptionOfFuture(SGConnectorIncompatibleException.class, connecting0);
    this.logger.debug("exception: ", ex);
  }

  @Test
  public final void testCreateStorageDeviceAttachIncompatible1()
  {
    final var computer =
      this.simulation.createComputer(
        SGComputerDescription.builder()
          .setArchitecture(ARCH_0)
          .addSockets(SGConnectorSocketDescription.of(HARDWARE_PORT_PROTOCOL_0))
          .build());

    final var socket =
      computer.sockets().get(0);

    final var device =
      this.simulation.createStorageDevice(
        SGStorageDeviceDescription.builder()
          .addConnectors(SGConnectorDescription.of(HARDWARE_PORT_PROTOCOL_1))
          .build());

    final var connector0 = device.connectors().get(0);

    final var connecting0 = connector0.connectTo(socket);

    this.simulation.tick(1.0 / 60.0);
    Assertions.assertTrue(connecting0.isDone());

    final var ex = exceptionOfFuture(SGConnectorIncompatibleException.class, connecting0);
    this.logger.debug("exception: ", ex);
  }

  @Test
  public final void testCreateStorageDeviceAttachAlreadyAttached0()
  {
    final var computer =
      this.simulation.createComputer(
        SGComputerDescription.builder()
          .setArchitecture(ARCH_0)
          .addSockets(SGConnectorSocketDescription.of(HARDWARE_PORT_PROTOCOL_0))
          .build());

    final var socket =
      computer.sockets().get(0);

    final var device =
      this.simulation.createStorageDevice(
        SGStorageDeviceDescription.builder()
          .addConnectors(SGConnectorDescription.of(HARDWARE_PORT_PROTOCOL_0))
          .addConnectors(SGConnectorDescription.of(HARDWARE_PORT_PROTOCOL_0))
          .build());

    final var connector0 = device.connectors().get(0);
    final var connector1 = device.connectors().get(1);

    final var connecting0 = connector0.connectTo(socket);
    final var connecting1 = connector1.connectTo(socket);

    this.simulation.tick(1.0 / 60.0);
    Assertions.assertTrue(connecting0.isDone());
    Assertions.assertTrue(connecting1.isCompletedExceptionally());

    final var ex = exceptionOfFuture(SGConnectedAlreadyException.class, connecting1);
    this.logger.debug("exception: ", ex);
  }

  @Test
  public final void testCreateStorageDeviceAttachAlreadyAttached1()
  {
    final var computer =
      this.simulation.createComputer(
        SGComputerDescription.builder()
          .setArchitecture(ARCH_0)
          .addSockets(SGConnectorSocketDescription.of(HARDWARE_PORT_PROTOCOL_0))
          .build());

    final var socket =
      computer.sockets().get(0);

    final var device =
      this.simulation.createStorageDevice(
        SGStorageDeviceDescription.builder()
          .addConnectors(SGConnectorDescription.of(HARDWARE_PORT_PROTOCOL_0))
          .addConnectors(SGConnectorDescription.of(HARDWARE_PORT_PROTOCOL_0))
          .build());

    final var connector0 = device.connectors().get(0);
    final var connector1 = device.connectors().get(1);

    socket.connectTo(connector0);
    final var connecting = socket.connectTo(connector1);
    this.simulation.tick(1.0 / 60.0);
    final var ex = exceptionOfFuture(SGConnectedAlreadyException.class, connecting);
    this.logger.debug("exception: ", ex);
  }

  @Test
  public final void testCreateStorageDeviceAttachAlreadyAttached2()
  {
    final var computer =
      this.simulation.createComputer(
        SGComputerDescription.builder()
          .setArchitecture(ARCH_0)
          .addSockets(SGConnectorSocketDescription.of(HARDWARE_PORT_PROTOCOL_0))
          .addSockets(SGConnectorSocketDescription.of(HARDWARE_PORT_PROTOCOL_0))
          .build());

    final var socket0 = computer.sockets().get(0);
    final var socket1 = computer.sockets().get(1);

    final var device =
      this.simulation.createStorageDevice(
        SGStorageDeviceDescription.builder()
          .addConnectors(SGConnectorDescription.of(HARDWARE_PORT_PROTOCOL_0))
          .build());

    final var connector0 = device.connectors().get(0);
    connector0.connectTo(socket0);
    final var connecting = connector0.connectTo(socket1);
    this.simulation.tick(1.0 / 60.0);
    final var ex = exceptionOfFuture(SGConnectedAlreadyException.class, connecting);
    this.logger.debug("exception: ", ex);
  }

  @Test
  public final void testCreateStorageDeviceAttachAlreadyAttachedOK0()
  {
    final var computer =
      this.simulation.createComputer(
        SGComputerDescription.builder()
          .setArchitecture(ARCH_0)
          .addSockets(SGConnectorSocketDescription.of(HARDWARE_PORT_PROTOCOL_0))
          .build());

    final var socket = computer.sockets().get(0);

    final var device =
      this.simulation.createStorageDevice(
        SGStorageDeviceDescription.builder()
          .addConnectors(SGConnectorDescription.of(HARDWARE_PORT_PROTOCOL_0))
          .build());

    final var connector = device.connectors().get(0);
    connector.connectTo(socket);

    final var connecting = connector.connectTo(socket);
    this.simulation.tick(1.0 / 60.0);
    final var ex = exceptionOfFuture(SGConnectedAlreadyException.class, connecting);
    this.logger.debug("exception: ", ex);
  }

  @Test
  public final void testCreateStorageDeviceAttachAlreadyAttachedOK1()
  {
    final var computer =
      this.simulation.createComputer(
        SGComputerDescription.builder()
          .setArchitecture(ARCH_0)
          .addSockets(SGConnectorSocketDescription.of(HARDWARE_PORT_PROTOCOL_0))
          .build());

    final var socket = computer.sockets().get(0);

    final var device =
      this.simulation.createStorageDevice(
        SGStorageDeviceDescription.builder()
          .addConnectors(SGConnectorDescription.of(HARDWARE_PORT_PROTOCOL_0))
          .build());

    final var connector = device.connectors().get(0);
    socket.connectTo(connector);

    final var connecting = socket.connectTo(connector);
    this.simulation.tick(1.0 / 60.0);
    final var ex = exceptionOfFuture(SGConnectedAlreadyException.class, connecting);
    this.logger.debug("exception: ", ex);
  }

  private void eventPublished(
    final SGEventType event)
  {
    this.logger().debug("event: {}", event);
    this.events.add(event);
  }
}
