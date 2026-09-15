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
package org.sonar.plugins.lua;

import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.rule.RuleKey;
import org.sonar.lua.LuaAstScanner;
import org.sonar.lua.LuaConfiguration;
import org.sonar.lua.api.LuaMetric;
import org.sonar.lua.lexer.LuaLexer;
import org.sonar.lua.checks.CheckList;
import org.sonar.plugins.lua.core.Lua;
import org.sonar.squidbridge.AstScanner;
import org.sonar.squidbridge.SquidAstVisitor;
import org.sonar.squidbridge.api.CheckMessage;
import org.sonar.squidbridge.api.SourceCode;
import org.sonar.squidbridge.api.SourceFile;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.squidbridge.indexer.QueryByType;
import org.sonar.sslr.parser.LexerlessGrammar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LuaSquidSensor implements Sensor {

  private static final Logger LOG = LoggerFactory.getLogger(LuaSquidSensor.class);

  private final List<SquidCheck<LexerlessGrammar>> checks;
  private final Map<Class<? extends SquidCheck<LexerlessGrammar>>, RuleKey> ruleKeysByCheck;

  private AstScanner<LexerlessGrammar> scanner;

  public LuaSquidSensor() {
    this.checks = new ArrayList<>();
    this.ruleKeysByCheck = new HashMap<>();
    for (Class<?> checkClass : CheckList.getChecks()) {
      org.sonar.check.Rule rule = checkClass.getAnnotation(org.sonar.check.Rule.class);
      if (rule == null) {
        continue;
      }
      try {
        @SuppressWarnings("unchecked")
        SquidCheck<LexerlessGrammar> check = (SquidCheck<LexerlessGrammar>) checkClass.getDeclaredConstructor().newInstance();
        checks.add(check);
        @SuppressWarnings("unchecked")
        Class<SquidCheck<LexerlessGrammar>> checkClassKey = (Class<SquidCheck<LexerlessGrammar>>) check.getClass();
        ruleKeysByCheck.put(checkClassKey, RuleKey.of(CheckList.REPOSITORY_KEY, rule.key()));
      } catch (Exception e) {
        LOG.warn("Unable to instantiate check: " + checkClass.getName(), e);
      }
    }
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .name("Lua")
      .onlyOnFileType(InputFile.Type.MAIN)
      .onlyOnLanguage(Lua.KEY);
  }

  @Override
  public void execute(SensorContext context) {
    FileSystem fileSystem = context.fileSystem();
    FilePredicates predicates = fileSystem.predicates();
    List<SquidAstVisitor<LexerlessGrammar>> visitors = new ArrayList<>(checks);
    LuaConfiguration configuration = new LuaConfiguration(fileSystem.encoding());
    visitors.add(new LuaTokensVisitor(context, LuaLexer.create(configuration)));

    scanner = LuaAstScanner.create(configuration, visitors);

    Iterable<java.io.File> files = fileSystem.files(
      predicates.and(
        predicates.hasType(InputFile.Type.MAIN),
        predicates.hasLanguage(Lua.KEY),
        inputFile -> !inputFile.absolutePath().endsWith("mxml")
      ));
    scanner.scanFiles(ImmutableList.copyOf(files));

    Collection<SourceCode> squidSourceFiles = scanner.getIndex().search(new QueryByType(SourceFile.class));
    save(context, squidSourceFiles);
  }

  private void save(SensorContext context, Collection<SourceCode> squidSourceFiles) {
    FileSystem fileSystem = context.fileSystem();
    for (SourceCode squidSourceFile : squidSourceFiles) {
      SourceFile squidFile = (SourceFile) squidSourceFile;

      InputFile inputFile = fileSystem.inputFile(fileSystem.predicates().hasPath(squidFile.getKey()));

      saveMeasures(context, inputFile, squidFile);
      saveViolations(context, inputFile, squidFile);
    }
  }

  private static void saveMeasures(SensorContext context, InputFile inputFile, SourceFile squidFile) {
    context.<Integer>newMeasure()
      .on(inputFile)
      .forMetric(CoreMetrics.NCLOC)
      .withValue(squidFile.getInt(LuaMetric.LINES_OF_CODE))
      .save();
    context.<Integer>newMeasure()
    .on(inputFile)
    .forMetric(CoreMetrics.CLASSES)
    .withValue(squidFile.getInt(LuaMetric.TABLECONSTRUCTORS))
    .save();
    context.<Integer>newMeasure()
      .on(inputFile)
      .forMetric(CoreMetrics.COMMENT_LINES)
      .withValue(squidFile.getInt(LuaMetric.COMMENT_LINES))
      .save();

    context.<Integer>newMeasure()
      .on(inputFile)
      .forMetric(CoreMetrics.FUNCTIONS)
      .withValue(squidFile.getInt(LuaMetric.FUNCTIONS))
      .save();
    context.<Integer>newMeasure()
      .on(inputFile)
      .forMetric(CoreMetrics.STATEMENTS)
      .withValue(squidFile.getInt(LuaMetric.STATEMENTS))
      .save();
    context.<Integer>newMeasure()
      .on(inputFile)
      .forMetric(CoreMetrics.COMPLEXITY)
      .withValue(squidFile.getInt(LuaMetric.COMPLEXITY))
      .save();
  }

  private void saveViolations(SensorContext context, InputFile inputFile, SourceFile squidFile) {
    Collection<CheckMessage> messages = squidFile.getCheckMessages();
    if (messages != null) {

      for (CheckMessage message : messages) {
        @SuppressWarnings("unchecked")
        SquidCheck<LexerlessGrammar> check = (SquidCheck<LexerlessGrammar>) message.getCheck();
        RuleKey ruleKey = ruleKeysByCheck.get(check.getClass());
        if (ruleKey == null) {
          continue;
        }
        NewIssue newIssue = context.newIssue()
          .forRule(ruleKey)
          .gap(message.getCost());
        Integer line = message.getLine();
        NewIssueLocation location = newIssue.newLocation()
          .on(inputFile)
          .message(message.getText(Locale.ENGLISH));
        if (line != null) {
          location.at(inputFile.selectLine(line));
        }
        newIssue.at(location);
        newIssue.save();
      }
    }
  }

}
