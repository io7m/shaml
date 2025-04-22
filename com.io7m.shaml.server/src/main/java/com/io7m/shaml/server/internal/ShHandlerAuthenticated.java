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
import io.helidon.http.HeaderNames;
import io.helidon.webserver.http.Handler;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

import java.util.List;
import java.util.UUID;

import static com.io7m.shaml.server.internal.ShDisableCache.disableCache;

public abstract class ShHandlerAuthenticated
  extends ShHandlerLogged
  implements Handler
{
  protected ShHandlerAuthenticated(
    final ShConfiguration inConfiguration,
    final ShSessions inSessions)
  {
    super(inConfiguration, inSessions);
  }

  protected abstract void handleAuthenticated(
    final ServerRequest req,
    final ShSession session,
    final ServerResponse res)
    throws Exception;

  @Override
  protected final void handleLogged(
    final ServerRequest req,
    final ServerResponse res)
    throws Exception
  {
    ShSession session = null;

    try {
      final var cookie = req.headers().cookies().get("SHAML_SESSION_ID");
      final var sessionID = UUID.fromString(cookie);
      session = this.sessions.findSession(sessionID);
    } catch (final Exception e) {
      // Ignored
    }

    try {
      final var bearer = req.headers().get(HeaderNames.AUTHORIZATION);
      final var segments = List.of(bearer.values().split("\\s+"));
      final var sessionID = UUID.fromString(segments.get(1));
      session = this.sessions.findSession(sessionID);
    } catch (final Exception e) {
      // Ignored
    }

    if (session == null) {
      disableCache(res);
      res.status(401);
      res.send("Unauthorized");
      return;
    }

    this.handleAuthenticated(req, session, res);
  }
}
