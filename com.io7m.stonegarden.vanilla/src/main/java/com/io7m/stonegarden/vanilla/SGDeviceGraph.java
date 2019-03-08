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

import com.io7m.jaffirm.core.Invariants;
import com.io7m.jaffirm.core.Preconditions;
import com.io7m.stonegarden.api.SGEventType;
import com.io7m.stonegarden.api.SGIdentifiableType;
import com.io7m.stonegarden.api.connectors.SGConnectableType;
import com.io7m.stonegarden.api.connectors.SGConnectedAlreadyException;
import com.io7m.stonegarden.api.connectors.SGConnectorEventConnected;
import com.io7m.stonegarden.api.connectors.SGConnectorEventDisconnected;
import com.io7m.stonegarden.api.connectors.SGConnectorIncompatibleException;
import com.io7m.stonegarden.api.connectors.SGConnectorSocketType;
import com.io7m.stonegarden.api.connectors.SGConnectorType;
import com.io7m.stonegarden.api.devices.SGDeviceEventDestroying;
import com.io7m.stonegarden.api.devices.SGDeviceEventType;
import com.io7m.stonegarden.api.devices.SGDeviceType;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import org.jgrapht.graph.SimpleGraph;

import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public final class SGDeviceGraph
{
  private final SimpleGraph<SGConnectableType, ConnectorSocketEdge> connector_graph;
  private final SimpleGraph<SGDeviceType, DeviceEdge> device_graph;
  private final Disposable event_sub;
  private final Consumer<SGEventType> event_sink;
  private final HashMap<UUID, SGIdentifiableType> objects;

  public SGDeviceGraph(
    final Consumer<SGEventType> event_sink,
    final Observable<SGEventType> events,
    final HashMap<UUID, SGIdentifiableType> in_devices)
  {
    Objects.requireNonNull(events, "events");

    this.objects =
      Objects.requireNonNull(in_devices, "devices");
    this.event_sink =
      Objects.requireNonNull(event_sink, "event_sink");

    this.connector_graph =
      new SimpleGraph<>(
        () -> {
          throw new IllegalStateException();
        },
        () -> {
          throw new IllegalStateException();
        },
        false);

    this.device_graph =
      new SimpleGraph<>(
        () -> {
          throw new IllegalStateException();
        },
        () -> {
          throw new IllegalStateException();
        },
        false);

    this.event_sub =
      events.ofType(SGDeviceEventType.class)
        .subscribe(this::onDeviceEvent);
  }

  private static Optional<ConnectorSocketEdge> edgeOf(
    final Set<ConnectorSocketEdge> edges)
  {
    if (!edges.isEmpty()) {
      Invariants.checkInvariantI(
        edges.size(),
        edges.size() == 1,
        e -> "Connectable must have exactly one edge");
      final var edge = edges.iterator().next();
      return Optional.of(edge);
    }
    return Optional.empty();
  }

  private void onDeviceEvent(
    final SGDeviceEventType event)
  {
    if (event instanceof SGDeviceEventDestroying) {
      this.onDeviceDestroying((SGDeviceEventDestroying) event);
    }
  }

  private void onDeviceDestroying(
    final SGDeviceEventDestroying event)
  {
    final var object =
      Objects.requireNonNull(this.objects.get(event.id()), "device");

    if (object instanceof SGDeviceType) {
      final var device = (SGDeviceType) object;
      for (final var connector : device.connectors()) {
        this.disconnect(connector);
        this.connector_graph.removeVertex(connector);
      }
      for (final var socket : device.sockets()) {
        this.disconnect(socket);
        this.connector_graph.removeVertex(socket);
      }
      this.device_graph.removeVertex(device);
    }
  }

  public boolean areDirectlyConnected(
    final SGDeviceType device0,
    final SGDeviceType device1)
  {
    Objects.requireNonNull(device0, "device0");
    Objects.requireNonNull(device1, "device1");

    final var edges = this.device_graph.edgesOf(device0);
    for (final var edge : edges) {
      if ((Objects.equals(edge.device0, device0) && Objects.equals(edge.device1, device1))
        || (Objects.equals(edge.device0, device1) && Objects.equals(edge.device1, device0))) {
        return true;
      }
    }
    return false;
  }

  public void disconnect(
    final SGConnectorType connector)
  {
    Objects.requireNonNull(connector, "connector");

    final var edges = Set.copyOf(this.connector_graph.outgoingEdgesOf(connector));
    for (final var edge : edges) {
      Invariants.checkInvariant(
        edge.connector,
        Objects.equals(edge.connector, connector),
        x -> "Must have correct connector");

      this.connector_graph.removeEdge(edge);

      this.device_graph.removeEdge(
        new DeviceEdge(edge.connector.owner(), edge.connector, edge.socket.owner(), edge.socket));

      this.event_sink.accept(
        SGConnectorEventDisconnected.builder()
          .setConnector(edge.connector.id())
          .setSocket(edge.socket.id())
          .build());
    }
  }

  public void disconnect(
    final SGConnectorSocketType socket)
  {
    Objects.requireNonNull(socket, "socket");

    final var edges = Set.copyOf(this.connector_graph.outgoingEdgesOf(socket));
    for (final var edge : edges) {
      Invariants.checkInvariant(
        edge.connector,
        Objects.equals(edge.socket, socket),
        x -> "Must have correct socket");

      this.connector_graph.removeEdge(edge);

      this.device_graph.removeEdge(
        new DeviceEdge(edge.connector.owner(), edge.connector, edge.socket.owner(), edge.socket));

      this.event_sink.accept(
        SGConnectorEventDisconnected.builder()
          .setConnector(edge.connector.id())
          .setSocket(edge.socket.id())
          .build());
    }
  }

  public Optional<SGConnectorSocketType> connectedSocket(
    final SGConnectorType connector)
  {
    Objects.requireNonNull(connector, "connector");
    return this.edgesOf(connector).flatMap(SGDeviceGraph::edgeOf).map(ConnectorSocketEdge::socket);
  }

  public Optional<SGConnectorType> connectedConnector(
    final SGConnectorSocketType socket)
  {
    Objects.requireNonNull(socket, "socket");
    return this.edgesOf(socket).flatMap(SGDeviceGraph::edgeOf).map(ConnectorSocketEdge::connector);
  }

  private Optional<Set<ConnectorSocketEdge>> edgesOf(
    final SGConnectableType connectable)
  {
    try {
      return Optional.of(this.connector_graph.edgesOf(connectable));
    } catch (final IllegalArgumentException e) {
      return Optional.empty();
    }
  }

  public void connect(
    final SGConnectorType connector,
    final SGConnectorSocketType socket)
    throws SGConnectedAlreadyException, SGConnectorIncompatibleException
  {
    Objects.requireNonNull(connector, "connector");
    Objects.requireNonNull(socket, "socket");

    final var socket_existing = this.connectedSocket(connector);
    if (socket_existing.isPresent()) {
      throw new SGConnectedAlreadyException(connector, socket_existing.get(), socket);
    }

    final var connector_existing = this.connectedConnector(socket);
    if (connector_existing.isPresent()) {
      throw new SGConnectedAlreadyException(socket, connector_existing.get(), connector);
    }

    if (!Objects.equals(connector.protocol(), socket.protocol())) {
      throw new SGConnectorIncompatibleException(socket, connector);
    }

    this.connector_graph.addVertex(connector);
    this.connector_graph.addVertex(socket);
    this.connector_graph.addEdge(connector, socket, new ConnectorSocketEdge(connector, socket));

    this.device_graph.addVertex(connector.owner());
    this.device_graph.addVertex(socket.owner());
    this.device_graph.addEdge(
      connector.owner(),
      socket.owner(),
      new DeviceEdge(connector.owner(), connector, socket.owner(), socket));

    this.event_sink.accept(SGConnectorEventConnected.of(connector.id(), socket.id()));
  }

  public <T extends SGDeviceType> void addDevice(final T device)
  {
    Objects.requireNonNull(device, "device");
    this.device_graph.addVertex(device);
  }

  private static final class ConnectorSocketEdge
  {
    private final SGConnectorType connector;
    private final SGConnectorSocketType socket;

    public ConnectorSocketEdge(
      final SGConnectorType in_connector,
      final SGConnectorSocketType in_socket)
    {
      this.connector =
        Objects.requireNonNull(in_connector, "connector");
      this.socket =
        Objects.requireNonNull(in_socket, "socket");
    }

    SGConnectorType connector()
    {
      return this.connector;
    }

    SGConnectorSocketType socket()
    {
      return this.socket;
    }

    @Override
    public boolean equals(final Object o)
    {
      if (this == o) {
        return true;
      }
      if (o == null || !Objects.equals(this.getClass(), o.getClass())) {
        return false;
      }
      final var that = (ConnectorSocketEdge) o;
      return this.connector.equals(that.connector) && this.socket.equals(that.socket);
    }

    @Override
    public int hashCode()
    {
      return Objects.hash(this.connector, this.socket);
    }
  }

  private static final class DeviceEdge
  {
    private final SGDeviceType device0;
    private final SGConnectorType connector;
    private final SGDeviceType device1;
    private final SGConnectorSocketType socket;

    public DeviceEdge(
      final SGDeviceType in_device0,
      final SGConnectorType in_connector,
      final SGDeviceType in_device1,
      final SGConnectorSocketType in_socket)
    {
      this.device0 =
        Objects.requireNonNull(in_device0, "device0");
      this.connector =
        Objects.requireNonNull(in_connector, "connector");
      this.device1 =
        Objects.requireNonNull(in_device1, "device1");
      this.socket =
        Objects.requireNonNull(in_socket, "socket");

      Preconditions.checkPrecondition(
        in_connector.owner(),
        Objects.equals(in_connector.owner(), in_device0),
        x -> "Connector must be owned by device " + in_device0);
      Preconditions.checkPrecondition(
        in_socket.owner(),
        Objects.equals(in_socket.owner(), in_device1),
        x -> "Socket must be owned by device " + in_device1);
    }

    @Override
    public boolean equals(final Object o)
    {
      if (this == o) {
        return true;
      }
      if (o == null || !Objects.equals(this.getClass(), o.getClass())) {
        return false;
      }
      final var that = (DeviceEdge) o;
      return this.device0.equals(that.device0)
        && this.connector.equals(that.connector)
        && this.device1.equals(that.device1)
        && this.socket.equals(that.socket);
    }

    @Override
    public int hashCode()
    {
      return Objects.hash(this.device0, this.connector, this.device1, this.socket);
    }
  }
}
