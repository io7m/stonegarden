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

import com.io7m.stonegarden.api.connectors.SGConnectorSocketType;
import com.io7m.stonegarden.api.connectors.SGConnectorType;
import com.io7m.stonegarden.api.devices.SGDeviceDescriptionType;
import com.io7m.stonegarden.api.devices.SGDeviceEventDestroyed;
import com.io7m.stonegarden.api.devices.SGDeviceEventDestroying;
import com.io7m.stonegarden.api.devices.SGDeviceKernelInterfaceType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

abstract class SGDevice extends SGIdentifiable implements SGDeviceKernelInterfaceType
{
  private final SGSimulationInternalAPIType simulation;
  private final SGDeviceDescriptionType description;
  private final ArrayList<SGConnectorSocketType> sockets;
  private final ArrayList<SGConnectorType> connectors;
  private final List<SGConnectorSocketType> sockets_read;
  private final List<SGConnectorType> connectors_read;
  private final TreeMap<UUID, SGConnectorType> connectors_by_id;
  private final TreeMap<UUID, SGConnectorSocketType> sockets_by_id;
  private final AtomicBoolean closed;

  SGDevice(
    final SGSimulationInternalAPIType in_simulation,
    final SGDeviceDescriptionType in_description,
    final UUID in_uuid)
  {
    super(in_uuid);

    this.closed = new AtomicBoolean(false);

    this.simulation =
      Objects.requireNonNull(in_simulation, "simulation");
    this.description =
      Objects.requireNonNull(in_description, "description");

    this.sockets_by_id = new TreeMap<>();
    this.sockets = new ArrayList<>(this.description.sockets().size());
    for (final var socket_description : this.description.sockets()) {
      final var socket = this.simulation.createConnectorSocket(this, socket_description);
      this.sockets.add(socket);
      this.sockets_by_id.put(socket.id(), socket);
    }

    this.connectors_by_id = new TreeMap<>();
    this.connectors = new ArrayList<>(this.description.connectors().size());
    for (final var connector_description : this.description.connectors()) {
      final var connector = this.simulation.createConnector(this, connector_description);
      this.connectors.add(connector);
      this.connectors_by_id.put(connector.id(), connector);
    }

    this.sockets_read = Collections.unmodifiableList(this.sockets);
    this.connectors_read = Collections.unmodifiableList(this.connectors);
  }

  @Override
  public final void close()
  {
    this.simulation.runLater(() -> {
      if (this.closed.compareAndSet(false, true)) {
        final var id = this.id();
        this.simulation.publishEvent(SGDeviceEventDestroying.of(id));
        this.onClose();
        this.simulation.publishEvent(SGDeviceEventDestroyed.of(id));
      }
    });
  }

  protected abstract void onClose();

  @Override
  public final List<SGConnectorSocketType> sockets()
  {
    return this.sockets_read;
  }

  @Override
  public final List<SGConnectorType> connectors()
  {
    return this.connectors_read;
  }

  protected final SGSimulationInternalAPIType simulation()
  {
    return this.simulation;
  }
}
