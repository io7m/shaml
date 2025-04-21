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
import freemarker.template.SimpleSequence;
import io.helidon.webserver.http.Handler;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

import java.io.OutputStreamWriter;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static com.io7m.shaml.server.internal.ShDisableCache.disableCache;

public final class ShEndpointLoans
  extends ShLoggingHandler
  implements Handler
{
  public ShEndpointLoans(
    final ShConfiguration inConfiguration,
    final ShSessions inSessions)
  {
    super(inConfiguration, inSessions);
  }

  @Override
  public void handleActual(
    final ServerRequest serverRequest,
    final ServerResponse serverResponse)
    throws Exception
  {
    final ShSession session;

    try {
      final var cookie =
        serverRequest.headers().cookies().get("SHAML_SESSION_ID");
      final var cookieValue =
        UUID.fromString(cookie);
      session =
        this.sessions.findSession(cookieValue);
    } catch (final Exception e) {
      disableCache(serverResponse);
      serverResponse.status(401);
      serverResponse.send("Unauthorized");
      return;
    }

    final var template =
      ShTemplates.get("loans.ftx");

    final var data = new ShTemplateData();
    data.put("Configuration", this.configuration.toTemplateData());
    data.put("Updated", new SimpleScalar(OffsetDateTime.now().toString()));

    final List<ShLoan> sessionLoans =
      session.get("Loans", List.class);
    final var loans =
      new SimpleSequence();

    for (final var loan : sessionLoans) {
      loans.add(loan.toTemplateData());
    }

    data.put("Loans", loans);

    serverResponse.status(200);
    serverResponse.header("Content-Type", "application/atom+xml;profile=opds-catalog;kind=acquisition");
    disableCache(serverResponse);

    try (final var output = new OutputStreamWriter(serverResponse.outputStream())) {
      template.process(data, output);
    }
  }
}
