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
import io.helidon.http.SetCookie;
import io.helidon.webserver.http.Handler;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.NoSuchElementException;

import static com.io7m.shaml.server.internal.ShDisableCache.disableCache;
import static com.io7m.shaml.server.internal.ShJSON.JSON;

/**
 * The initial SAML authentication endpoint.
 */

public final class ShEndpointSAMLAuthenticate
  extends ShHandlerLogged
  implements Handler
{
  public ShEndpointSAMLAuthenticate(
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
    final URI redirectURI;

    try {
      redirectURI = URI.create(serverRequest.query().get("redirect_uri"));
    } catch (final NoSuchElementException e) {
      this.sendMissingParameterError(serverResponse, "redirect_uri");
      return;
    }

    final var session = this.sessions.createSession();
    session.put("RedirectURI", redirectURI);

    final var cookie =
      SetCookie.create("SHAML_SESSION_ID", session.id().toString());

    serverResponse.status(200);
    serverResponse.headers().addCookie(cookie);
    disableCache(serverResponse);

    final var template =
      ShTemplates.get("loginForm.ftx");
    final var data =
      new ShTemplateData();

    try (final var output = new OutputStreamWriter(serverResponse.outputStream())) {
      template.process(data, output);
    }
  }

  private void sendMissingParameterError(
    final ServerResponse serverResponse,
    final String name)
    throws JsonProcessingException
  {
    final var response = JSON.createObjectNode();
    response.put(
      "type",
      "http://librarysimplified.org/terms/problem/saml/invalid-saml-request");
    response.put("title", "Invalid SAML request.");
    response.put("status", 401);
    response.put(
      "detail",
      "Required parameter '%s' is missing.".formatted(name));

    disableCache(serverResponse);
    serverResponse.status(401);
    serverResponse.send(JSON.writeValueAsBytes(response));
  }
}
