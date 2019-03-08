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

package com.io7m.stonegarden.api.connectors;

import com.io7m.stonegarden.api.SGException;
import com.io7m.stonegarden.api.devices.SGDeviceType;

import java.util.Optional;

/**
 * A hardware connector on a device.
 */

public interface SGConnectorType extends SGConnectableType
{
  @Override
  default String kind()
  {
    return "connector";
  }

  /**
   * @return The device to which this connector belongs
   */

  SGDeviceType owner();

  /**
   * @return The description of the connector
   */

  SGConnectorDescription description();

  /**
   * Connect this connector to the given socket.
   *
   * @param socket The socket
   *
   * @throws SGException On errors
   */

  void connectTo(SGConnectorSocketType socket)
    throws SGException;

  /**
   * Disconnect this connector from any connected socket. Does nothing if the connector is not
   * connected.
   */

  void disconnect();

  /**
   * @return The socket to which this connector is connected, if any
   */

  Optional<SGConnectorSocketType> connectedTo();

  /**
   * @return The protocol used
   */

  default SGConnectorProtocol protocol()
  {
    return this.description().protocol();
  }
}
