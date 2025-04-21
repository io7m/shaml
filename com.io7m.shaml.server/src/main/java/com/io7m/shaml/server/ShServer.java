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

import com.io7m.shaml.server.internal.ShEndpointAuthDocument;
import com.io7m.shaml.server.internal.ShEndpointCSS;
import com.io7m.shaml.server.internal.ShEndpointCatalog;
import com.io7m.shaml.server.internal.ShEndpointDownload;
import com.io7m.shaml.server.internal.ShEndpointDownloadFails;
import com.io7m.shaml.server.internal.ShEndpointImage;
import com.io7m.shaml.server.internal.ShEndpointLoans;
import com.io7m.shaml.server.internal.ShEndpointRoot;
import com.io7m.shaml.server.internal.ShEndpointSAMLAuthenticate;
import com.io7m.shaml.server.internal.ShEndpointSAMLAuthenticateRun;
import com.io7m.shaml.server.internal.ShSessions;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.WebServerConfig;
import io.helidon.webserver.http.HttpRouting;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;
import java.util.Objects;

import static java.net.StandardSocketOptions.SO_REUSEADDR;
import static java.net.StandardSocketOptions.SO_REUSEPORT;

public final class ShServer
  implements AutoCloseable
{
  private final WebServer webServer;

  private ShServer(
    final WebServer inServer)
  {
    this.webServer =
      Objects.requireNonNull(inServer, "webServer");
  }

  public static ShServer create(
    final ShConfiguration configuration)
    throws IOException
  {
    final var sessions =
      ShSessions.create(configuration);

    final var routingBuilder =
      HttpRouting.builder();

    routingBuilder.get(
      "/",
      new ShEndpointRoot(configuration, sessions)
    );
    routingBuilder.get(
      "/css",
      new ShEndpointCSS(configuration, sessions)
    );
    routingBuilder.get(
      "/image/*",
      new ShEndpointImage(configuration, sessions)
    );

    routingBuilder.get(
      "/download",
      new ShEndpointDownload(configuration, sessions)
    );
    routingBuilder.get(
      "/download/*",
      new ShEndpointDownload(configuration, sessions)
    );

    routingBuilder.get(
      "/download-fails",
      new ShEndpointDownloadFails(configuration, sessions)
    );
    routingBuilder.get(
      "/download-fails/*",
      new ShEndpointDownloadFails(configuration, sessions)
    );

    routingBuilder.get(
      "/authentication",
      new ShEndpointAuthDocument(configuration, sessions)
    );
    routingBuilder.get(
      "/loans",
      new ShEndpointLoans(configuration, sessions)
    );
    routingBuilder.get(
      "/catalog",
      new ShEndpointCatalog(configuration, sessions)
    );

    routingBuilder.get(
      "/saml_authenticate",
      new ShEndpointSAMLAuthenticate(configuration, sessions)
    );
    routingBuilder.get(
      "/saml_authenticate_run",
      new ShEndpointSAMLAuthenticateRun(configuration, sessions)
    );
    routingBuilder.post(
      "/saml_authenticate_run",
      new ShEndpointSAMLAuthenticateRun(configuration, sessions)
    );

    final var webServerBuilder =
      WebServerConfig.builder();

    final var webServer =
      webServerBuilder
        .port(configuration.port())
        .address(InetAddress.getByName(configuration.host()))
        .listenerSocketOptions(Map.ofEntries(
          Map.entry(SO_REUSEADDR, Boolean.TRUE),
          Map.entry(SO_REUSEPORT, Boolean.TRUE)
        ))
        .maxPayloadSize(32768)
        .routing(routingBuilder)
        .build();

    return new ShServer(webServer);
  }

  @Override
  public void close()
  {
    this.webServer.stop();
  }

  public void start()
  {
    this.webServer.start();
  }
}
