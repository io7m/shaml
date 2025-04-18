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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.io7m.shaml.server.internal.ShTemplateData;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateModel;

import java.net.URI;
import java.time.Duration;
import java.util.Objects;

/**
 * The main server configuration.
 */

@JsonDeserialize
@JsonSerialize
public record ShConfiguration(
  @JsonProperty(value = "ExternalBaseURI", required = true)
  @JsonPropertyDescription("The externally-visible base URI for this server.")
  URI externalBaseURI,

  @JsonProperty(value = "Title", required = true)
  @JsonPropertyDescription("The library title.")
  String title,

  @JsonProperty(value = "ServiceDescription", required = true)
  @JsonPropertyDescription("The library service description.")
  String serviceDescription,

  @JsonProperty(value = "SessionExpiration", defaultValue = "PT1M")
  @JsonPropertyDescription("The session expiration time.")
  Duration sessionExpiration,

  @JsonProperty(value = "RSAPublicKey", required = true)
  @JsonPropertyDescription("The library public key.")
  String rsaPublicKey,

  @JsonProperty(value = "ListenHost", required = true)
  @JsonPropertyDescription("The host address to which to bind the HTTP server.")
  String host,

  @JsonProperty(value = "ListenPort", required = true)
  @JsonPropertyDescription("The host port to which to bind the HTTP server.")
  int port)
  implements TemplateModel
{
  public ShConfiguration
  {
    Objects.requireNonNull(host, "host");
  }

  public TemplateModel toTemplateData()
  {
    final var configData = new ShTemplateData();
    configData.put(
      "ExternalBaseURI",
      new SimpleScalar(this.externalBaseURI().toString())
    );
    configData.put("Title", new SimpleScalar(this.title()));
    configData.put("ServiceDescription", new SimpleScalar(this.serviceDescription()));
    return configData;
  }
}
