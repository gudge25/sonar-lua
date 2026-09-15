/*
 * SonarQube Lua Plugin
 * Copyright (C) 2016 
 * mailto:fati.ahmadi66 AT sonarsource DOT com
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
package org.sonar.lua.checks;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.io.Files;
import com.sonar.sslr.api.AstNode;

import org.sonar.lua.CharsetAwareVisitor;
import org.sonar.lua.checks.utils.Tags;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.squidbridge.annotations.ActivatedByDefault;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.parser.LexerlessGrammar;

@Rule(
    key = "LineLength",
    name = "Lines should not be too long",
    priority = Priority.MINOR,
    tags = Tags.CONVENTION
)
@ActivatedByDefault
public class LineLengthCheck extends SquidCheck<LexerlessGrammar> implements CharsetAwareVisitor {

  private static final int DEFAULT_MAXIMUM_LINE_LENGTH = 80;

  private Charset charset;

  @RuleProperty(
    key = "maximumLineLength",
    description = "The maximum authorized line length.",
    defaultValue = "" + DEFAULT_MAXIMUM_LINE_LENGTH)
  public int maximumLineLength = DEFAULT_MAXIMUM_LINE_LENGTH;

  public int getMaximumLineLength() {
    return maximumLineLength;
  }

  @Override
  public void setCharset(Charset charset) {
    this.charset = charset;
  }

  @Override
  public void visitFile(@Nullable AstNode astNode) {
    List<String> lines;
    try {
      lines = Files.readLines(getContext().getFile(), charset);
    } catch (IOException e) {
      throw new IllegalStateException("Unable to read file " + getContext().getFile() + " for LineLength check", e);
    }

    for (int i = 0; i < lines.size(); i++) {
      String line = lines.get(i);
      if (line.length() > maximumLineLength) {
        getContext().createLineViolation(this, "Split this {0} characters long line (which is greater than {1} authorized).", i + 1, line.length(), maximumLineLength);
      }
    }
  }

}
