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

import java.time.OffsetDateTime;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ShSession
{
  private final ConcurrentHashMap<String, Object> data;
  private final UUID id;
  private volatile OffsetDateTime accessed;

  public ShSession(
    final UUID inId)
  {
    this.id =
      Objects.requireNonNull(inId, "id");
    this.data =
      new ConcurrentHashMap<>();
    this.accessed =
      OffsetDateTime.now();
  }

  public OffsetDateTime accessed()
  {
    return this.accessed;
  }

  public void touch()
  {
    this.accessed = OffsetDateTime.now();
  }

  public void put(
    final String name,
    final Object object)
  {
    this.data.put(
      Objects.requireNonNull(name, "name"),
      Objects.requireNonNull(object, "object")
    );
  }

  public <T> T get(
    final String name,
    final Class<T> type)
  {
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(type, "type");

    final var item = this.data.get(name);
    if (item == null) {
      throw new NoSuchElementException(name);
    }
    if (!type.isAssignableFrom(item.getClass())) {
      throw new IllegalArgumentException(
        "Value is of type %s".formatted(item.getClass())
      );
    }
    return type.cast(item);
  }

  public UUID id()
  {
    return this.id;
  }
}
