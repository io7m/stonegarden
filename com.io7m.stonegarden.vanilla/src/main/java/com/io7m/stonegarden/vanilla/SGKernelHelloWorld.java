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

import com.io7m.stonegarden.api.SGArchitecture;
import com.io7m.stonegarden.api.SGVersion;
import com.io7m.stonegarden.api.kernels.SGKernelCompatibility;
import com.io7m.stonegarden.api.kernels.SGKernelContextType;
import com.io7m.stonegarden.api.kernels.SGKernelDescription;
import com.io7m.stonegarden.api.kernels.SGKernelExecutableDescription;
import com.io7m.stonegarden.api.kernels.SGKernelType;

import java.math.BigInteger;
import java.util.Objects;
import java.util.Properties;

/**
 * A HELLO WORLD kernel.
 */

public final class SGKernelHelloWorld
{
  private SGKernelHelloWorld()
  {

  }

  /**
   * Create a kernel.
   *
   * @param architecture The architecture on which the kernel will run
   * @param size         The size of the kernel
   *
   * @return A kernel
   */

  public static SGKernelExecutableDescription get(
    final SGArchitecture architecture,
    final BigInteger size)
  {
    final var description =
      SGKernelDescription.builder()
        .setCompatibility(SGKernelCompatibility.of(architecture))
        .setName("HELLO")
        .setVersion(SGVersion.of(0, 1, 0))
        .setSizeOctets(size)
        .build();

    return SGKernelExecutableDescription.builder()
      .setDescription(description)
      .setExecutable(Kernel::new)
      .build();
  }

  private static final class Kernel implements SGKernelType
  {
    private final SGKernelContextType context;
    private final Properties parameters;

    Kernel(
      final SGKernelContextType in_context,
      final Properties in_parameters)
    {
      this.context =
        Objects.requireNonNull(in_context, "context");
      this.parameters =
        Objects.requireNonNull(in_parameters, "parameters");
    }

    @Override
    public void onStart()
    {
      this.context.writeConsole("booting HELLO WORLD!");
    }

    @Override
    public void onShutDown()
    {
      this.context.writeConsole("shutting down HELLO WORLD!");
    }
  }
}
