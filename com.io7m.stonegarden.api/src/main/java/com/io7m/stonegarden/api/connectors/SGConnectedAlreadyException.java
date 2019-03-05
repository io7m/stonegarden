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

/**
 * A connector or socket is already connected and cannot be connected again.
 */

public final class SGConnectedAlreadyException extends SGException
{
  /**
   * Construct an exception.
   *
   * @param socket              The socket
   * @param existing_connector  The connector already connected to the socket
   * @param attempted_connector The connector that tried to connect to the socket
   */

  public SGConnectedAlreadyException(
    final SGConnectorSocketType socket,
    final SGConnectorType existing_connector,
    final SGConnectorType attempted_connector)
  {
    super(makeMessage(socket, existing_connector, attempted_connector));
  }

  /**
   * Construct an exception.
   *
   * @param connector        The connector
   * @param existing_socket  The socket already connected to the connector
   * @param attempted_socket The socket that tried to connect to the connector
   */

  public SGConnectedAlreadyException(
    final SGConnectorType connector,
    final SGConnectorSocketType existing_socket,
    final SGConnectorSocketType attempted_socket)
  {
    super(makeMessage(connector, existing_socket, attempted_socket));
  }

  private static String makeMessage(
    final SGConnectorType connector,
    final SGConnectorSocketType existing_socket,
    final SGConnectorSocketType attempted_socket)
  {
    final var separator = System.lineSeparator();
    return new StringBuilder(128)
      .append("Connector is already connected to a different socket.")
      .append(separator)
      .append("  Connector: ")
      .append(connector.kind())
      .append(" ")
      .append(connector.id())
      .append(separator)
      .append("  Existing socket: ")
      .append(existing_socket.kind())
      .append(" ")
      .append(existing_socket.id())
      .append(separator)
      .append("  Attempted socket: ")
      .append(attempted_socket.kind())
      .append(" ")
      .append(attempted_socket.id())
      .append(separator)
      .toString();
  }

  private static String makeMessage(
    final SGConnectorSocketType socket,
    final SGConnectorType existing_connector,
    final SGConnectorType attempted_connector)
  {
    final var separator = System.lineSeparator();
    return new StringBuilder(128)
      .append("Socket is already connected to a different connector.")
      .append(separator)
      .append("  Socket: ")
      .append(socket.kind())
      .append(" ")
      .append(socket.id())
      .append(separator)
      .append("  Existing connector: ")
      .append(existing_connector.kind())
      .append(" ")
      .append(existing_connector.id())
      .append(separator)
      .append("  Attempted connector: ")
      .append(attempted_connector.kind())
      .append(" ")
      .append(attempted_connector.id())
      .append(separator)
      .toString();
  }
}
