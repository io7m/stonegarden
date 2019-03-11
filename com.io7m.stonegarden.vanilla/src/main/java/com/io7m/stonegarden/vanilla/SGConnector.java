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

import com.io7m.stonegarden.api.connectors.SGConnectorDescription;
import com.io7m.stonegarden.api.connectors.SGConnectorSocketType;
import com.io7m.stonegarden.api.connectors.SGConnectorType;
import com.io7m.stonegarden.api.devices.SGDeviceType;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

final class SGConnector extends SGIdentifiable implements SGConnectorType
{
  private final SGConnectorDescription description;
  private final SGDeviceType owner;
  private final SGSimulationInternalAPIType simulation;

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
  public CompletableFuture<Void> connectTo(final SGConnectorSocketType socket)
  {
    Objects.requireNonNull(socket, "socket");
    return this.simulation.runLater(() -> this.simulation.deviceGraph().connect(this, socket));
  }

  @Override
  public CompletableFuture<Void> disconnect()
  {
    return this.simulation.runLater(() -> this.simulation.deviceGraph().disconnect(this));
  }

  @Override
  public Optional<SGConnectorSocketType> connectedTo()
  {
    return this.simulation.deviceGraph().connectedSocket(this);
  }

}
