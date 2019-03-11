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

import com.io7m.stonegarden.api.devices.SGDeviceType;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * A hardware socket on a device.
 */

public interface SGConnectorSocketType extends SGConnectableType
{
  @Override
  default String kind()
  {
    return "socket";
  }

  /**
   * @return The device to which this socket belongs
   */

  SGDeviceType owner();

  /**
   * @return The description of the socket
   */

  SGConnectorSocketDescription description();

  /**
   * Connect this socket to the given connector.
   *
   * @param connector The connector
   *
   * @return The operation in progress
   */

  CompletableFuture<Void> connectTo(SGConnectorType connector);

  /**
   * Disconnect this socket from any connected connector. Does nothing if the socket is not
   * connected.
   *
   * @return The operation in progress
   */

  CompletableFuture<Void> disconnect();

  /**
   * @return The connector to which this socket is connected, if any
   */

  Optional<SGConnectorType> connectedTo();

  /**
   * @return The protocol used
   */

  default SGConnectorProtocol protocol()
  {
    return this.description().protocol();
  }
}
