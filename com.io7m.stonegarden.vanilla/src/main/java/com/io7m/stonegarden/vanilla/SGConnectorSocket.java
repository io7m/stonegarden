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
import com.io7m.stonegarden.api.connectors.SGConnectorEventConnected;
import com.io7m.stonegarden.api.connectors.SGConnectorEventDisconnected;
import com.io7m.stonegarden.api.connectors.SGConnectorIncompatibleException;
import com.io7m.stonegarden.api.connectors.SGConnectorSocketDescription;
import com.io7m.stonegarden.api.connectors.SGConnectorSocketType;
import com.io7m.stonegarden.api.connectors.SGConnectorType;
import com.io7m.stonegarden.api.devices.SGDeviceType;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

final class SGConnectorSocket extends SGIdentifiable implements SGConnectorSocketType
{
  private final SGConnectorSocketDescription description;
  private final AtomicBoolean connection_changing;
  private final SGSimulationInternalAPIType simulation;
  private final SGDeviceType owner;
  private SGConnectorType connected;

  SGConnectorSocket(
    final SGSimulationInternalAPIType in_simulation,
    final SGDeviceType in_device,
    final UUID in_id,
    final SGConnectorSocketDescription in_description)
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
  public SGConnectorSocketDescription description()
  {
    return this.description;
  }

  @Override
  public void acceptConnector(final SGConnectorType connector)
    throws SGException
  {
    Objects.requireNonNull(connector, "connector");

    if (this.connection_changing.compareAndSet(false, true)) {
      try {
        if (this.connected != null) {
          if (!Objects.equals(this.connected, connector)) {
            throw new SGConnectedAlreadyException(this, this.connected, connector);
          }
          return;
        }

        if (!Objects.equals(connector.description().protocol(), this.description.protocol())) {
          throw new SGConnectorIncompatibleException(this, connector);
        }

        connector.connectTo(this);
        this.connected = connector;

        this.simulation.publishEvent(
          SGConnectorEventConnected.builder()
            .setConnector(connector.id())
            .setSocket(this.id())
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
              .setConnector(peer.id())
              .setSocket(this.id())
              .build());
        }
      } finally {
        this.connection_changing.set(false);
      }
    }
  }

  @Override
  public Optional<SGConnectorType> connectedTo()
  {
    return Optional.ofNullable(this.connected);
  }
}
