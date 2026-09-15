/*
 * SonarQube Lua Plugin
 * Copyright (C) 2016
 * mailto:fati.ahmadi66 AT gmail DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.lua;

import org.sonar.squidbridge.CommentAnalyser;

public class LuaCommentAnalyser extends CommentAnalyser {

  @Override
  public boolean isBlank(String line) {
    for (int i = 0; i < line.length(); i++) {
      if (Character.isLetterOrDigit(line.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  @Override
  public String getContents(String comment) {
    if (!comment.startsWith("--")) {
      throw new IllegalArgumentException("Not a Lua comment: " + comment);
    }

    String afterPrefix = comment.substring(2);
    int level = readLongBracketLevel(afterPrefix);
    if (level < 0) {
      return comment.substring(2);
    }

    String closing = "]" + repeatEquals(level) + "]";
    if (!comment.endsWith(closing)) {
      return comment.substring(2 + closing.length());
    }
    return comment.substring(2 + closing.length(), comment.length() - closing.length());
  }

  private static int readLongBracketLevel(String text) {
    if (text.isEmpty() || text.charAt(0) != '[') {
      return -1;
    }
    int level = 0;
    int i = 1;
    while (i < text.length() && text.charAt(i) == '=') {
      level++;
      i++;
    }
    if (i >= text.length() || text.charAt(i) != '[') {
      return -1;
    }
    return level;
  }

  private static String repeatEquals(int count) {
    StringBuilder builder = new StringBuilder(count);
    for (int i = 0; i < count; i++) {
      builder.append('=');
    }
    return builder.toString();
  }

}
