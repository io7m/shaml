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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.io7m.shaml.server.ShConfiguration;
import io.helidon.webserver.http.Handler;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.UUID;

import static com.io7m.shaml.server.internal.ShDisableCache.disableCache;
import static com.io7m.shaml.server.internal.ShJSON.JSON;

/**
 * The SAML success/failure endpoint.
 */

public final class ShEndpointSAMLAuthenticateRun
  extends ShHandlerLogged
  implements Handler
{
  private static final Logger LOG =
    LoggerFactory.getLogger(ShEndpointSAMLAuthenticateRun.class);

  public ShEndpointSAMLAuthenticateRun(
    final ShConfiguration inConfiguration,
    final ShSessions inSessions)
  {
    super(inConfiguration, inSessions);
  }

  @Override
  public void handleLogged(
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
      this.sendFailure(serverResponse);
      return;
    }

    try {
      final var result = serverRequest.query().get("user_name").toUpperCase(Locale.ROOT);
      switch (result) {
        case "GROUCH" -> {
          LOG.info("grouch logged in");
          final var loans = new ArrayList<ShLoan>();
          loans.add(ShLoan.BOOK_SUCCEEDS);
          loans.add(ShLoan.BOOK_FAILS);
          loans.add(ShLoan.BOOK_SHOWS_LOGIN);
          session.put("Username", "grouch");
          session.put("Loans", loans);
          this.sendSuccess(serverResponse, session);
        }
        case "ERNIE" -> {
          LOG.info("ernie logged in");
          final var loans = new ArrayList<ShLoan>();
          session.put("Username", "ernie");
          session.put("Loans", loans);
          this.sendSuccess(serverResponse, session);
        }
        default -> {
          this.sendFailure(serverResponse);
          return;
        }
      }
    } catch (final NoSuchElementException e) {
      this.sendFailure(serverResponse);
    }
  }

  private void sendSuccess(
    final ServerResponse serverResponse,
    final ShSession session)
  {
    final var redirectURI =
      session.get("RedirectURI", URI.class);

    final var target = new StringBuilder();
    target.append(redirectURI);
    if (target.toString().contains("?")) {
      target.append("&");
    } else {
      target.append("?");
    }
    target.append("access_token=");
    target.append(session.id());
    target.append("&patron_info=");
    target.append(session.id());

    disableCache(serverResponse);
    serverResponse.status(302);
    serverResponse.header("Location", target.toString());
    serverResponse.send();
  }

  private void sendFailure(
    final ServerResponse serverResponse)
    throws JsonProcessingException
  {
    final var response = JSON.createObjectNode();
    response.put(
      "type",
      "http://librarysimplified.org/terms/problem/saml/authentication-error");
    response.put("title", "SAML authentication error.");
    response.put("status", 401);
    response.put("detail", "SAML authentication error.");

    disableCache(serverResponse);
    serverResponse.status(401);
    serverResponse.send(JSON.writeValueAsBytes(response));
  }
}
