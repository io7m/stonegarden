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

import com.io7m.stonegarden.api.SGException;
import com.io7m.stonegarden.api.connectors.SGConnectedAlreadyException;
import com.io7m.stonegarden.api.connectors.SGConnectorDescription;
import com.io7m.stonegarden.api.connectors.SGConnectorEventConnected;
import com.io7m.stonegarden.api.connectors.SGConnectorEventDisconnected;
import com.io7m.stonegarden.api.connectors.SGConnectorIncompatibleException;
import com.io7m.stonegarden.api.connectors.SGConnectorSocketType;
import com.io7m.stonegarden.api.connectors.SGConnectorType;
import com.io7m.stonegarden.api.devices.SGDeviceType;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

final class SGConnector extends SGIdentifiable implements SGConnectorType
{
  private final SGConnectorDescription description;
  private final AtomicBoolean connection_changing;
  private final SGDeviceType owner;
  private final SGSimulationInternalAPIType simulation;
  private SGConnectorSocketType connected;

  SGConnector(
    final SGSimulationInternalAPIType in_simulation,
    final SGDeviceType in_device,
    final UUID in_id,
    final SGConnectorDescription in_description)
  {
    super(in_id);

    this.simulation =
      Objects.requireNonNull(in_simulation, "simulation");
    this.owner =
      Objects.requireNonNull(in_device, "device");
    this.description =
      Objects.requireNonNull(in_description, "description");

    this.connection_changing = new AtomicBoolean(false);
    this.connected = null;
  }

  @Override
  public SGDeviceType owner()
  {
    return this.owner;
  }

  @Override
  public SGConnectorDescription description()
  {
    return this.description;
  }

  @Override
  public void connectTo(final SGConnectorSocketType socket)
    throws SGException
  {
    Objects.requireNonNull(socket, "socket");

    if (this.connection_changing.compareAndSet(false, true)) {
      try {
        if (this.connected != null) {
          if (!Objects.equals(this.connected, socket)) {
            throw new SGConnectedAlreadyException(this, this.connected, socket);
          }
          return;
        }

        if (!Objects.equals(socket.description().protocol(), this.description.protocol())) {
          throw new SGConnectorIncompatibleException(socket, this);
        }

        socket.acceptConnector(this);
        this.connected = socket;

        this.simulation.publishEvent(
          SGConnectorEventConnected.builder()
            .setConnector(this.id())
            .setSocket(socket.id())
            .build());

      } finally {
        this.connection_changing.set(false);
      }
    }
  }

  @Override
  public void disconnect()
  {
    if (this.connection_changing.compareAndSet(false, true)) {
      try {
        final var peer = this.connected;
        if (peer != null) {
          peer.disconnect();

          this.connected = null;

          this.simulation.publishEvent(
            SGConnectorEventDisconnected.builder()
              .setConnector(this.id())
              .setSocket(peer.id())
              .build());
        }
      } finally {
        this.connection_changing.set(false);
      }
    }
  }

  @Override
  public Optional<SGConnectorSocketType> connectedTo()
  {
    return Optional.ofNullable(this.connected);
  }
}
