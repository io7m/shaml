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

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

public abstract class ShLoggingHandler implements Handler
{
  protected final ShConfiguration configuration;
  protected final ShSessions sessions;

  protected ShLoggingHandler(
    final ShConfiguration inConfiguration,
    final ShSessions inSessions)
  {
    this.configuration =
      Objects.requireNonNull(inConfiguration, "configuration");
    this.sessions =
      Objects.requireNonNull(inSessions, "sessions");
  }

  protected abstract void handleActual(
    final ServerRequest req,
    final ServerResponse res)
    throws Exception;

  @Override
  public final void handle(
    final ServerRequest req,
    final ServerResponse res)
    throws Exception
  {
    this.handleActual(req, res);

    String userName;

    try {
      final var cookie =
        req.headers().cookies().get("SHAML_SESSION_ID");
      final var cookieValue =
        UUID.fromString(cookie);
      final var session =
        this.sessions.findSession(cookieValue);

      userName = session.get("Username", String.class);
    } catch (final Exception e) {
      userName = "-";
    }

    System.out.printf(
      "%s - %s %s \"%s\" %s %d - -%n",
      req.remotePeer().host(),
      userName,
      OffsetDateTime.now(),
      req.path(),
      res.status().code(),
      res.bytesWritten()
    );
  }
}
