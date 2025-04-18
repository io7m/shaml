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


package com.io7m.shaml.server.internal;

import com.io7m.shaml.server.ShConfiguration;
import io.helidon.webserver.http.Handler;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

import java.io.IOException;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class ShImage implements Handler
{
  private final ShConfiguration configuration;

  public ShImage(
    final ShConfiguration inConfiguration)
  {
    this.configuration =
      Objects.requireNonNull(inConfiguration, "configuration");
  }

  @Override
  public void handle(
    final ServerRequest serverRequest,
    final ServerResponse serverResponse)
    throws Exception
  {
    final var path =
      serverRequest.path();
    final var last =
      path.segments().getLast();

    switch (last.value()) {
      case "orange.png" -> {
        this.send(serverResponse, "orange.png");
      }
      case "orangeThumb.png" -> {
        this.send(serverResponse, "orangeThumb.png");
      }
      default -> {
        serverResponse.status(404);
        serverResponse.send("No such image.\n".getBytes(UTF_8));
      }
    }
  }

  private void send(
    final ServerResponse serverResponse,
    final String image)
    throws IOException
  {
    final var path =
      "/com/io7m/shaml/server/%s".formatted(image);

    serverResponse.status(200);
    serverResponse.header("Content-Type", "image/png");

    try (final var stream = ShImage.class.getResourceAsStream(path)) {
      try (final var output = serverResponse.outputStream()) {
        stream.transferTo(output);
      }
    }
  }
}
