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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class ShSessions
{
  private static final Logger LOG =
    LoggerFactory.getLogger(ShSessions.class);

  private final ConcurrentHashMap<UUID, ShSession> sessions;
  private final ScheduledExecutorService executor;

  private ShSessions()
  {
    this.sessions =
      new ConcurrentHashMap<>();
    this.executor =
      Executors.newSingleThreadScheduledExecutor(r -> {
        final var thread = Thread.ofVirtual().unstarted(r);
        thread.setName(
          "com.io7m.shaml.server.sessions[%s]".formatted(thread.threadId())
        );
        return thread;
      });
  }

  public static ShSessions create(
    final ShConfiguration configuration)
  {
    final var r = new ShSessions();
    r.executor.scheduleAtFixedRate(
      () -> r.expire(configuration.sessionExpiration()),
      1L,
      1L,
      TimeUnit.MINUTES
    );
    return r;
  }

  public ShSession createSession()
  {
    final var session = new ShSession(UUID.randomUUID());
    this.sessions.put(session.id(), session);
    LOG.debug("Created new session {}", session.id());
    return session;
  }

  private void expire(
    final Duration expiration)
  {
    final var timeNow = OffsetDateTime.now();
    final var sessionSet = Set.copyOf(this.sessions.values());
    for (final var session : sessionSet) {
      final var accessed = session.accessed();
      final var age = Duration.between(accessed, timeNow);
      if (age.compareTo(expiration) > 0) {
        LOG.debug("Session {} expired", session.id());
        this.sessions.remove(session.id());
      }
    }
    LOG.debug("{} currently active sessions", this.sessions.size());
  }

  public ShSession findSession(
    final UUID id)
  {
    final var result = this.sessions.get(id);
    if (result == null) {
      throw new NoSuchElementException("No such session.");
    }
    result.touch();
    return result;
  }
}
