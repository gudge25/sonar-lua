/*
 * SonarQube Lua Plugin
 * Copyright (C) 2016
 * mailto:contact AT sonarsource DOT com
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

import java.lang.reflect.Field;
import java.util.List;
import org.sonar.api.rule.RuleStatus;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinition.NewRule;
import org.sonar.api.server.rule.RulesDefinition.NewRepository;
import org.sonar.check.Priority;
import org.sonar.check.RuleProperty;


import org.sonar.lua.checks.CheckList;
import org.sonar.plugins.lua.core.Lua;
import org.sonar.squidbridge.annotations.ActivatedByDefault;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;

public final class LuaRulesDefinition implements RulesDefinition {

  private static final String REPOSITORY_NAME = "SonarQube";

  @Override
  public void define(Context context) {
    NewRepository repository = context
      .createRepository(CheckList.REPOSITORY_KEY, Lua.KEY)
      .setName(REPOSITORY_NAME);

    for (Class<?> checkClass : CheckList.getChecks()) {
      registerRule(repository, checkClass);
    }

    repository.done();
  }

  private static void registerRule(NewRepository repository, Class<?> checkClass) {
    org.sonar.check.Rule rule = checkClass.getAnnotation(org.sonar.check.Rule.class);
    if (rule == null) {
      return;
    }

    String description = rule.description();
    if (description == null || description.isEmpty()) {
      description = rule.name();
    }

    NewRule newRule = repository.createRule(rule.key())
      .setName(rule.name())
      .setHtmlDescription(description)
      .setStatus(RuleStatus.READY)
      .setSeverity(mapPriority(rule.priority()))
      .setTags(rule.tags());

    if (checkClass.isAnnotationPresent(ActivatedByDefault.class)) {
      newRule.setActivatedByDefault(true);
    }

    for (Field field : checkClass.getDeclaredFields()) {
      RuleProperty property = field.getAnnotation(RuleProperty.class);
      if (property != null) {
        newRule.createParam(property.key().isEmpty() ? field.getName() : property.key())
          .setDescription(property.description())
          .setDefaultValue(property.defaultValue());
      }
    }
  }

  private static String mapPriority(Priority priority) {
    switch (priority) {
      case BLOCKER:
        return "BLOCKER";
      case CRITICAL:
        return "CRITICAL";
      case MAJOR:
        return "MAJOR";
      case MINOR:
        return "MINOR";
      case INFO:
        return "INFO";
      default:
        return "MAJOR";
    }
  }
}
