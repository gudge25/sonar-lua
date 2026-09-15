/*
 * SonarQube Lua Plugin
 * Copyright (C) 2016
 * mailto:fati.ahmadi66@gmail.com
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
package org.sonar.plugins.lua;

import org.sonar.api.Plugin;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;
import org.sonar.plugins.lua.core.Lua;

public class LuaPlugin implements Plugin {

  public static final String FILE_SUFFIXES_KEY = "sonar.lua.file.suffixes";

  @Override
  public void define(Context context) {
    context.addExtensions(
      Lua.class,

      LuaSquidSensor.class,

      LuaRulesDefinition.class,
      LuaProfile.class,

      PropertyDefinition.builder(FILE_SUFFIXES_KEY)
        .defaultValue(Lua.DEFAULT_FILE_SUFFIXES)
        .name("File suffixes")
        .description("Comma-separated list of suffixes for files to analyze. To not filter, leave the list empty.")
        .onQualifiers(Qualifiers.MODULE, Qualifiers.PROJECT)
        .multiValues(true)
        .build()
    );
  }
}
