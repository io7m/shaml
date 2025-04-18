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

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.io7m.shaml.server.ShConfiguration;
import io.helidon.webserver.http.Handler;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

import java.util.Objects;

import static com.io7m.shaml.server.internal.ShJSON.JSON;

/**
 * The authentication document endpoint.
 */

public final class ShAuthDocument implements Handler
{
  private final ShConfiguration configuration;

  public ShAuthDocument(
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
    final var authObject =
      this.createAuthObject();
    final var links =
      this.createLinks();
    final var features =
      this.createFeatures();
    final var announcements =
      this.createAnnouncements();
    final var publicKey =
      this.createPublicKey();

    final var baseURI =
      this.configuration.externalBaseURI();

    final var authDocument = ShJSON.JSON.createObjectNode();
    authDocument.put(
      "id",
      baseURI.resolve("authentication_document").toString()
    );
    authDocument.put("title", this.configuration.title());
    authDocument.set("authentication", authObject);
    authDocument.set("links", links);
    authDocument.set("features", features);
    authDocument.put("service_description", this.configuration.serviceDescription());
    authDocument.set("announcements", announcements);
    authDocument.set("public_key", publicKey);

    serverResponse.status(200);
    serverResponse.header("Content-Type", "application/vnd.opds.authentication.v1.0+json");
    serverResponse.send(ShJSON.JSON.writeValueAsBytes(authDocument));
  }

  private ObjectNode createPublicKey()
  {
    final var node = JSON.createObjectNode();
    node.put("type", "RSA");
    node.put("value", this.configuration.rsaPublicKey());
    return node;
  }

  private ArrayNode createAnnouncements()
  {
    return JSON.createArrayNode();
  }

  private ObjectNode createFeatures()
  {
    final var root = JSON.createObjectNode();
    final var enabled = JSON.createArrayNode();
    enabled.add("https://librarysimplified.org/rel/policy/reservations");
    root.set("enabled", enabled);
    root.set("disabled", JSON.createArrayNode());
    return root;
  }

  private ArrayNode createLinks()
  {
    final var baseURI =
      this.configuration.externalBaseURI();

    final var linkArray =
      JSON.createArrayNode();

    {
      final var o = JSON.createObjectNode();
      o.put("rel", "start");
      o.put("href", baseURI.resolve("/catalog").toString());
      o.put("type", "application/atom+xml;profile=opds-catalog;kind=acquisition");
      linkArray.add(o);
    }

    {
      final var o = JSON.createObjectNode();
      o.put("rel", "http://opds-spec.org/shelf");
      o.put("href", baseURI.resolve("/loans").toString());
      o.put("type", "application/atom+xml;profile=opds-catalog;kind=acquisition");
      linkArray.add(o);
    }

    {
      final var o = JSON.createObjectNode();
      o.put("rel", "http://librarysimplified.org/terms/rel/user-profile");
      o.put("href", baseURI.resolve("/patron").toString());
      o.put("type", "vnd.librarysimplified/user-profile+json");
      linkArray.add(o);
    }

    return linkArray;
  }

  private ObjectNode createAuthObject()
  {
    final var baseURI =
      this.configuration.externalBaseURI();

    final var authObject = JSON.createObjectNode();
    authObject.put("type", "http://librarysimplified.org/authtype/SAML-2.0");
    authObject.put("description", "SAML 2.0 Web SSO");

    final var links = JSON.createArrayNode();
    authObject.set("links", links);

    final var linkObject = JSON.createObjectNode();
    linkObject.put("rel", "authenticate");
    linkObject.put("href", baseURI.resolve("saml_authenticate").toString());
    links.add(linkObject);
    return authObject;
  }
}
