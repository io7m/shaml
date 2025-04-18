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

import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.IOException;

public final class ShTemplates
{
  private static final Configuration TEMPLATES =
    new Configuration(Configuration.VERSION_2_3_34);

  static {
    TEMPLATES.setClassForTemplateLoading(
      ShTemplates.class,
      "/com/io7m/shaml/server/"
    );
  }

  private ShTemplates()
  {

  }

  /**
   * Retrieve a template.
   *
   * @param name The template name
   *
   * @return The template
   *
   * @throws IOException On errors
   */

  public static Template get(
    final String name)
    throws IOException
  {
    return TEMPLATES.getTemplate(name);
  }
}
