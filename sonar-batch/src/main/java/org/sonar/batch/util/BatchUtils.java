/*
 * SonarQube, open source software quality management tool.
 * Copyright (C) 2008-2014 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * SonarQube is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * SonarQube is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.batch.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.lang.StringUtils;

public class BatchUtils {

  private BatchUtils() {
  }

  /**
   * Clean provided string to remove chars that are not valid as file name.
   * @param projectKey e.g. my:file
   */
  public static String cleanKeyForFilename(String projectKey) {
    String cleanKey = StringUtils.deleteWhitespace(projectKey);
    return StringUtils.replace(cleanKey, ":", "_");
  }

  public static String encodeForUrl(String url) {
    try {
      return URLEncoder.encode(url, "UTF-8");

    } catch (UnsupportedEncodingException e) {
      throw new IllegalStateException("Encoding not supported", e);
    }
  }

  public static String describe(Object o) {
    try {
      if (o.getClass().getMethod("toString").getDeclaringClass() != Object.class) {
        return o.toString();
      }
    } catch (Exception e) {
      // fallback
    }

    return o.getClass().getName();
  }
}
