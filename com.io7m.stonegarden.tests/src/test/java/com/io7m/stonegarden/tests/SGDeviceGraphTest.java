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
import com.io7m.stonegarden.api.connectors.SGConnectedAlreadyException;
import com.io7m.stonegarden.api.connectors.SGConnectorIncompatibleException;
import com.io7m.stonegarden.api.connectors.SGConnectorProtocol;
import com.io7m.stonegarden.api.connectors.SGConnectorProtocolName;
import com.io7m.stonegarden.api.connectors.SGConnectorSocketType;
import com.io7m.stonegarden.api.connectors.SGConnectorType;
import com.io7m.stonegarden.api.devices.SGDeviceType;
import com.io7m.stonegarden.vanilla.SGDeviceGraph;
import io.reactivex.subjects.PublishSubject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public final class SGDeviceGraphTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(SGDeviceGraphTest.class);

  private static final SGConnectorProtocol PROTOCOL_0 =
    SGConnectorProtocol.of(SGConnectorProtocolName.of("GCX-0"));

  private static final SGConnectorProtocol PROTOCOL_1 =
    SGConnectorProtocol.of(SGConnectorProtocolName.of("GCX-1"));

  private PublishSubject<SGEventType> events;
  private SGDeviceGraph graph;

  @BeforeEach
  public void testSetup()
  {
    this.events = PublishSubject.create();
    this.graph = new SGDeviceGraph(this.events::onNext, this.events, new HashMap<>());
  }

  @Test
  public void testConnectDisconnect()
    throws Exception
  {
    final var id0 = UUID.randomUUID();
    final var id1 = UUID.randomUUID();

    final var device0 = Mockito.mock(SGDeviceType.class);
    final var device1 = Mockito.mock(SGDeviceType.class);

    final var connector = Mockito.mock(SGConnectorType.class);
    Mockito.when(connector.id()).thenReturn(id0);
    Mockito.when(connector.protocol()).thenReturn(PROTOCOL_0);
    Mockito.when(connector.owner()).thenReturn(device0);

    final var socket = Mockito.mock(SGConnectorSocketType.class);
    Mockito.when(socket.id()).thenReturn(id1);
    Mockito.when(socket.protocol()).thenReturn(PROTOCOL_0);
    Mockito.when(socket.owner()).thenReturn(device1);

    Assertions.assertEquals(Optional.empty(), this.graph.connectedConnector(socket));
    Assertions.assertEquals(Optional.empty(), this.graph.connectedSocket(connector));

    this.graph.connect(connector, socket);
    Assertions.assertEquals(Optional.of(connector), this.graph.connectedConnector(socket));
    Assertions.assertEquals(Optional.of(socket), this.graph.connectedSocket(connector));

    this.graph.disconnect(connector);
    Assertions.assertEquals(Optional.empty(), this.graph.connectedConnector(socket));
    Assertions.assertEquals(Optional.empty(), this.graph.connectedSocket(connector));

    this.graph.connect(connector, socket);
    Assertions.assertEquals(Optional.of(connector), this.graph.connectedConnector(socket));
    Assertions.assertEquals(Optional.of(socket), this.graph.connectedSocket(connector));

    this.graph.disconnect(socket);
    Assertions.assertEquals(Optional.empty(), this.graph.connectedConnector(socket));
    Assertions.assertEquals(Optional.empty(), this.graph.connectedSocket(connector));
  }

  @Test
  public void testConnectedAlready0()
    throws Exception
  {
    final var id0 = UUID.randomUUID();
    final var id1 = UUID.randomUUID();

    final var device0 = Mockito.mock(SGDeviceType.class);
    final var device1 = Mockito.mock(SGDeviceType.class);

    final var connector = Mockito.mock(SGConnectorType.class);
    Mockito.when(connector.id()).thenReturn(id0);
    Mockito.when(connector.kind()).thenReturn("connector");
    Mockito.when(connector.protocol()).thenReturn(PROTOCOL_0);
    Mockito.when(connector.owner()).thenReturn(device0);

    final var socket = Mockito.mock(SGConnectorSocketType.class);
    Mockito.when(socket.id()).thenReturn(id1);
    Mockito.when(socket.protocol()).thenReturn(PROTOCOL_0);
    Mockito.when(socket.kind()).thenReturn("socket");
    Mockito.when(socket.owner()).thenReturn(device1);

    this.graph.connect(connector, socket);

    final var ex = Assertions.assertThrows(
      SGConnectedAlreadyException.class,
      () -> this.graph.connect(connector, socket));

    LOG.debug("exception: ", ex);
  }

  @Test
  public void testConnectedAlready1()
    throws Exception
  {
    final var id0 = UUID.randomUUID();
    final var id1 = UUID.randomUUID();
    final var id2 = UUID.randomUUID();

    final var device0 = Mockito.mock(SGDeviceType.class);
    final var device1 = Mockito.mock(SGDeviceType.class);

    final var connector0 = Mockito.mock(SGConnectorType.class);
    Mockito.when(connector0.id()).thenReturn(id0);
    Mockito.when(connector0.kind()).thenReturn("connector");
    Mockito.when(connector0.protocol()).thenReturn(PROTOCOL_0);
    Mockito.when(connector0.owner()).thenReturn(device0);

    final var connector1 = Mockito.mock(SGConnectorType.class);
    Mockito.when(connector1.id()).thenReturn(id1);
    Mockito.when(connector1.kind()).thenReturn("connector");
    Mockito.when(connector1.protocol()).thenReturn(PROTOCOL_0);
    Mockito.when(connector1.owner()).thenReturn(device0);

    final var socket = Mockito.mock(SGConnectorSocketType.class);
    Mockito.when(socket.id()).thenReturn(id2);
    Mockito.when(socket.protocol()).thenReturn(PROTOCOL_0);
    Mockito.when(socket.kind()).thenReturn("socket");
    Mockito.when(socket.owner()).thenReturn(device1);

    this.graph.connect(connector0, socket);

    final var ex = Assertions.assertThrows(
      SGConnectedAlreadyException.class,
      () -> this.graph.connect(connector1, socket));

    LOG.debug("exception: ", ex);
  }

  @Test
  public void testConnectIncompatible0()
  {
    final var id0 = UUID.randomUUID();
    final var id1 = UUID.randomUUID();

    final var connector = Mockito.mock(SGConnectorType.class);
    Mockito.when(connector.id()).thenReturn(id0);
    Mockito.when(connector.kind()).thenReturn("connector");
    Mockito.when(connector.protocol()).thenReturn(PROTOCOL_0);

    final var socket = Mockito.mock(SGConnectorSocketType.class);
    Mockito.when(socket.id()).thenReturn(id1);
    Mockito.when(socket.protocol()).thenReturn(PROTOCOL_1);
    Mockito.when(socket.kind()).thenReturn("socket");

    final var ex = Assertions.assertThrows(
      SGConnectorIncompatibleException.class,
      () -> this.graph.connect(connector, socket));

    LOG.debug("exception: ", ex);
  }
}
