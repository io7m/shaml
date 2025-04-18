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
import freemarker.template.SimpleScalar;
import io.helidon.webserver.http.Handler;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

import java.io.OutputStreamWriter;
import java.time.OffsetDateTime;
import java.util.Objects;

import static com.io7m.shaml.server.internal.ShDisableCache.disableCache;

public final class ShCatalog implements Handler
{
  private final ShConfiguration configuration;
  private final ShSessions sessions;

  public ShCatalog(
    final ShConfiguration inConfiguration,
    final ShSessions inSessions)
  {
    this.configuration =
      Objects.requireNonNull(inConfiguration, "configuration");
    this.sessions =
      Objects.requireNonNull(inSessions, "sessions");
  }

  @Override
  public void handle(
    final ServerRequest serverRequest,
    final ServerResponse serverResponse)
    throws Exception
  {
    final var template =
      ShTemplates.get("catalog.ftx");

    final var data = new ShTemplateData();
    data.put("Configuration", this.configuration.toTemplateData());
    data.put("Updated", new SimpleScalar(OffsetDateTime.now().toString()));

    serverResponse.status(200);
    serverResponse.header("Content-Type", "application/atom+xml;profile=opds-catalog;kind=acquisition");
    disableCache(serverResponse);

    try (final var output = new OutputStreamWriter(serverResponse.outputStream())) {
      template.process(data, output);
    }
  }
}
