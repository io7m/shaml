/*
 * Copyright © 2025 Mark Raynsford <code@io7m.com> https://www.io7m.com
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


package com.io7m.shaml.server;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.File;
import java.io.IOException;

/**
 * Main CLI.
 */

public final class ShMain
{
  private static final Logger LOG =
    LoggerFactory.getLogger(ShMain.class);

  private ShMain()
  {

  }

  /**
   * Main CLI.
   *
   * @param args Command-line arguments
   *
   * @throws Exception On errors
   */

  public static void main(
    final String[] args)
    throws Exception
  {
    if (args.length != 1) {
      LOG.info("Usage: configuration.json");
      return;
    }

    /*
     * Install the SLF4J <-> JUL logging bridge so that Helidon's log messages
     * are sent through it.
     */

    SLF4JBridgeHandler.removeHandlersForRootLogger();
    SLF4JBridgeHandler.install();

    final var configuration =
      loadConfiguration(args[0]);

    LOG.info("Starting server...");
    try (final var server = ShServer.create(configuration)) {
      server.start();
      LOG.info("Server started.");

      while (true) {
        Thread.sleep(1_000L);
      }
    }
  }

  private static ShConfiguration loadConfiguration(
    final String file)
    throws IOException
  {
    final var mapper =
      JsonMapper.builder()
        .enable(JsonParser.Feature.ALLOW_COMMENTS)
        .build();

    return mapper.readValue(new File(file), ShConfiguration.class);
  }
}
