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

import freemarker.template.SimpleScalar;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateModel;

import java.net.URI;
import java.util.List;
import java.util.UUID;

public record ShLoan(
  UUID bookId,
  String title,
  String description,
  List<Acquisition> acquisitionSequence)
{
  public record Acquisition(
    URI target,
    String type)
  {
    public TemplateModel toTemplateData()
    {
      final var data = new ShTemplateData();
      data.put("Target", new SimpleScalar(this.target.toString()));
      data.put("Type", new SimpleScalar(this.type));
      return data;
    }
  }

  /**
   * The ID of a book that always succeeds downloading.
   */

  public static final UUID BOOK_SUCCEEDS_ID =
    UUID.randomUUID();

  /**
   * A book that always succeeds downloading.
   */

  public static final ShLoan BOOK_SUCCEEDS =
    new ShLoan(
      BOOK_SUCCEEDS_ID,
      "Always Succeeds",
      "A book with a download that always succeeds.",
      List.of(
        new Acquisition(
          URI.create("/borrow/" + BOOK_SUCCEEDS_ID),
          "application/atom+xml;type=entry;profile=opds-catalog"
        ),
        new Acquisition(
          URI.create("/download/" + BOOK_SUCCEEDS_ID),
          "application/epub+zip"
        )
      )
    );

  /**
   * The ID of a book that always fails downloading.
   */

  public static final UUID BOOK_FAILS_ID =
    UUID.randomUUID();

  /**
   * A book that always fails downloading.
   */

  public static final ShLoan BOOK_FAILS =
    new ShLoan(
      BOOK_FAILS_ID,
      "Always Fails",
      "A book with a download that always fails.",
      List.of(
        new Acquisition(
          URI.create("/borrow-fails/" + BOOK_FAILS_ID),
          "application/atom+xml;type=entry;profile=opds-catalog"
        ),
        new Acquisition(
          URI.create("/download-fails/" + BOOK_FAILS_ID),
          "application/epub+zip"
        )
      )
    );

  /**
   * The ID of a book that always shows a login form in the middle of downloading.
   */

  public static final UUID BOOK_SHOWS_LOGIN_ID =
    UUID.randomUUID();

  /**
   * A book that always shows a login form in the middle of downloading.
   */

  public static final ShLoan BOOK_SHOWS_LOGIN =
    new ShLoan(
      BOOK_SHOWS_LOGIN_ID,
      "Shows Login",
      "A book with a download that shows a login form.",
      List.of(
        new Acquisition(
          URI.create("/borrow-shows-form/" + BOOK_SHOWS_LOGIN_ID),
          "application/atom+xml;type=entry;profile=opds-catalog"
        ),
        new Acquisition(
          URI.create("/borrow-shows-form/" + BOOK_SHOWS_LOGIN_ID),
          "application/epub+zip"
        )
      )
    );

  public TemplateModel toTemplateData()
  {
    final var configData = new ShTemplateData();
    configData.put("BookID", new SimpleScalar(this.bookId.toString()));
    configData.put("Title", new SimpleScalar(this.title));
    configData.put("Description", new SimpleScalar(this.description));
    
    final var acquisitions = new SimpleSequence();
    for (final var acquisition : this.acquisitionSequence) {
      acquisitions.add(acquisition.toTemplateData());
    }

    configData.put("Acquisitions", acquisitions);
    configData.put(
      "AcquisitionLast",
      this.acquisitionSequence.getLast()
        .toTemplateData()
    );
    return configData;
  }
}
