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

import com.io7m.stonegarden.api.SGArchitecture;
import com.io7m.stonegarden.api.SGException;
import com.io7m.stonegarden.api.SGVersion;
import com.io7m.stonegarden.api.SGVersionRange;
import com.io7m.stonegarden.api.connectors.SGConnectorProtocol;
import com.io7m.stonegarden.api.connectors.SGConnectorProtocolName;
import com.io7m.stonegarden.api.connectors.SGConnectorSocketType;
import com.io7m.stonegarden.api.connectors.SGConnectorType;
import com.io7m.stonegarden.api.devices.SGStorageDeviceDescription;
import com.io7m.stonegarden.api.devices.SGStorageDeviceType;
import com.io7m.stonegarden.api.filesystem.SGFilesystemFormat;
import com.io7m.stonegarden.api.filesystem.SGFilesystemFormatName;
import com.io7m.stonegarden.api.kernels.SGKernelCompatibility;
import com.io7m.stonegarden.api.kernels.SGKernelDescription;
import com.io7m.stonegarden.api.kernels.SGKernelExecutableDescriptionType;
import com.io7m.stonegarden.api.kernels.SGKernelExecutableType;
import com.io7m.stonegarden.api.programs.SGProgramCompatibility;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class SGBruteForceEqualityTest
{
  private static final Class<?> CLASSES[] = {
    com.io7m.stonegarden.api.computer.SGComputerBootOrderItem.class,
    com.io7m.stonegarden.api.computer.SGComputerDescription.class,
    com.io7m.stonegarden.api.computer.SGComputerEventBooted.class,
    com.io7m.stonegarden.api.computer.SGComputerEventBootFailed.class,
    com.io7m.stonegarden.api.computer.SGComputerEventBooting.class,
    com.io7m.stonegarden.api.computer.SGComputerEventShutDown.class,
    com.io7m.stonegarden.api.computer.SGComputerEventShuttingDown.class,
    com.io7m.stonegarden.api.connectors.SGConnectorDescription.class,
    com.io7m.stonegarden.api.connectors.SGConnectorEventConnected.class,
    com.io7m.stonegarden.api.connectors.SGConnectorEventDisconnected.class,
    com.io7m.stonegarden.api.connectors.SGConnectorProtocol.class,
    com.io7m.stonegarden.api.connectors.SGConnectorProtocolName.class,
    com.io7m.stonegarden.api.connectors.SGConnectorSocketDescription.class,
    com.io7m.stonegarden.api.devices.SGDeviceEventCreated.class,
    com.io7m.stonegarden.api.devices.SGDeviceEventDestroyed.class,
    com.io7m.stonegarden.api.devices.SGDeviceEventDestroying.class,
    com.io7m.stonegarden.api.devices.SGStorageDeviceDescription.class,
    com.io7m.stonegarden.api.filesystem.SGFilesystemDescription.class,
    com.io7m.stonegarden.api.filesystem.SGFilesystemFormat.class,
    com.io7m.stonegarden.api.filesystem.SGFilesystemFormatName.class,
    com.io7m.stonegarden.api.kernels.SGKernelCompatibility.class,
    com.io7m.stonegarden.api.kernels.SGKernelDescription.class,
    com.io7m.stonegarden.api.kernels.SGKernelExecutableDescription.class,
    com.io7m.stonegarden.api.programs.SGProgramCompatibility.class,
    com.io7m.stonegarden.api.programs.SGProgramDescription.class,
    com.io7m.stonegarden.api.simulation.SGSimulationEventTick.class,
    com.io7m.stonegarden.api.SGArchitecture.class,
    com.io7m.stonegarden.api.SGVersion.class,
    com.io7m.stonegarden.api.SGVersionRange.class,
  };

  private static void checkClassEquality(
    final Class<?> clazz)
  {
    final var fields =
      Stream.of(clazz.getDeclaredFields())
        .filter(SGBruteForceEqualityTest::fieldIsOK)
        .map(Field::getName)
        .collect(Collectors.toList());

    final var field_names = new String[fields.size()];
    fields.toArray(field_names);

    EqualsVerifier.forClass(clazz)
      .withNonnullFields(field_names)
      .verify();
  }

  private static void checkCopyOf(
    final Class<?> clazz)
    throws Exception
  {
    final var interface_type = clazz.getInterfaces()[0];
    final var mock = Mockito.mock(interface_type, new SensibleAnswers());
    final var copy_method = clazz.getMethod("copyOf", interface_type);
    final var copy = copy_method.invoke(clazz, mock);
    Assertions.assertTrue(interface_type.isAssignableFrom(copy.getClass()));
  }

  private static boolean fieldIsOK(
    final Field field)
  {
    if (Objects.equals(field.getName(), "$jacocoData")) {
      return false;
    }

    return !field.getType().isPrimitive();
  }

  @TestFactory
  public Stream<DynamicTest> testEquals()
  {
    final var classes = Stream.of(CLASSES);

    return classes
      .map(clazz -> DynamicTest.dynamicTest(
        "testEquals" + clazz.getSimpleName(),
        () -> checkClassEquality(clazz)));
  }

  @TestFactory
  public Stream<DynamicTest> testCopyOf()
  {
    final var classes = Stream.of(CLASSES);

    return classes
      .map(clazz -> DynamicTest.dynamicTest(
        "testCopyOf" + clazz.getSimpleName(),
        () -> checkCopyOf(clazz)));
  }

  private static final class SensibleAnswers implements Answer<Object>
  {
    @Override
    public Object answer(final InvocationOnMock invocation)
      throws Throwable
    {
      final var return_type = invocation.getMethod().getReturnType();
      if (return_type.equals(String.class)) {
        return "xyz";
      }
      if (return_type.equals(BigInteger.class)) {
        return BigInteger.valueOf(23L);
      }
      if (return_type.equals(URI.class)) {
        return URI.create("stonegarden:com.io7m.xyz");
      }
      if (return_type.equals(SGVersion.class)) {
        return SGVersion.of(1, 2, 3);
      }
      if (return_type.equals(UUID.class)) {
        return UUID.randomUUID();
      }
      if (return_type.equals(Properties.class)) {
        return new Properties();
      }
      if (return_type.equals(SGKernelCompatibility.class)) {
        return SGKernelCompatibility.builder()
          .setArchitecture(SGArchitecture.builder()
                             .setName("PK3")
                             .build())
          .build();
      }
      if (return_type.equals(SGProgramCompatibility.class)) {
        return SGProgramCompatibility.of(
          "xnux",
          SGVersionRange.of(
            SGVersion.of(1, 0, 0),
            false,
            SGVersion.of(2, 0, 0),
            true),
          SGArchitecture.builder()
            .setName("PK3")
            .build());
      }
      if (return_type.equals(SGStorageDeviceType.class)) {
        return new SGStorageDeviceType()
        {
          @Override
          public UUID id()
          {
            return null;
          }

          @Override
          public void close()
            throws SGException
          {

          }

          @Override
          public SGStorageDeviceDescription description()
          {
            return null;
          }

          @Override
          public List<SGConnectorSocketType> sockets()
          {
            return null;
          }

          @Override
          public List<SGConnectorType> connectors()
          {
            return null;
          }

          @Override
          public BigInteger spaceUsedOctets()
          {
            return null;
          }

          @Override
          public List<SGKernelExecutableDescriptionType> kernels()
          {
            return null;
          }
        };
      }
      if (return_type.equals(SGKernelExecutableType.class)) {
        return (SGKernelExecutableType) (simulation, context, parameters) -> null;
      }
      if (return_type.equals(SGKernelDescription.class)) {
        return SGKernelDescription.of(
          "xyz",
          SGVersion.of(1, 0, 0),
          SGKernelCompatibility.of(SGArchitecture.of("PK3")),
          BigInteger.TEN);
      }
      if (return_type.equals(SGFilesystemFormat.class)) {
        return SGFilesystemFormat.builder()
          .setName(SGFilesystemFormatName.of("BLOCKFS"))
          .build();
      }
      if (return_type.equals(SGConnectorProtocolName.class)) {
        return SGConnectorProtocolName.of("GPB-0");
      }
      if (return_type.equals(SGConnectorProtocol.class)) {
        return SGConnectorProtocol.of(SGConnectorProtocolName.of("GPB-0"));
      }
      if (return_type.equals(SGFilesystemFormatName.class)) {
        return SGFilesystemFormatName.of("BLOCKFS");
      }
      if (return_type.equals(SGVersionRange.class)) {
        return SGVersionRange.of(
          SGVersion.of(1, 0, 0),
          false,
          SGVersion.of(2, 0, 0),
          true);
      }
      if (return_type.equals(SGArchitecture.class)) {
        return SGArchitecture.builder()
          .setName("PK3")
          .build();
      }
      if (return_type.equals(Supplier.class)) {
        return (Supplier<Object>) () -> null;
      }

      return Mockito.RETURNS_DEFAULTS.answer(invocation);
    }
  }
}
