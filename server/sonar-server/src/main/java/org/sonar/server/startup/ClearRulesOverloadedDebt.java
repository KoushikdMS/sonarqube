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

package org.sonar.server.startup;

import org.picocontainer.Startable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.core.persistence.DbSession;
import org.sonar.core.rule.RuleDto;
import org.sonar.core.template.LoadedTemplateDto;
import org.sonar.server.db.DbClient;

import static org.sonar.core.template.LoadedTemplateDto.ONE_SHOT_TASK_TYPE;

/**
 * Clear the overloaded technical debt of rules when SQALE plugin is not installed.
 *
 * Should be removed after next LTS
 *
 * @since 5.2
 */
public class ClearRulesOverloadedDebt implements Startable {

  private static final Logger LOG = LoggerFactory.getLogger(ClearRulesOverloadedDebt.class);

  private static final String TEMPLATE_KEY = "ClearRulesOverloadedDebt";

  private static final String SQALE_LICENSE_PROPERTY = "sonar.sqale.licenseHash.secured";

  private final DbClient dbClient;

  public ClearRulesOverloadedDebt(DbClient dbClient) {
    this.dbClient = dbClient;
  }

  @Override
  public void start() {
    DbSession session = dbClient.openSession(false);
    try {
      if (hasAlreadyBeenExecuted(session)) {
        return;
      }
      if (!isSqalePluginInstalled(session)) {
        clearDebt(session);
      }
      registerTasks(session);
      session.commit();
    } finally {
      session.close();
    }
  }

  private void clearDebt(DbSession session) {
    int countClearedRules = 0;
    for (RuleDto rule : dbClient.ruleDao().findAll(session)) {
      if (isDebtOverridden(rule)) {
        rule.setSubCharacteristicId(null);
        rule.setRemediationFunction(null);
        rule.setRemediationCoefficient(null);
        rule.setRemediationOffset(null);
        dbClient.ruleDao().update(session, rule);
        countClearedRules++;
      }
    }
    if (countClearedRules > 0) {
      LOG.warn("The SQALE model has been cleaned to remove useless data left over by previous migrations. The technical debt of {} rules was reset to their default values.",
        countClearedRules);
      LOG.warn("=> As a consequence, the overall technical debt of your projects might slightly evolve during the next analysis.");
    }
  }

  private static boolean isDebtOverridden(RuleDto ruleDto) {
    return ruleDto.getSubCharacteristicId() != null || ruleDto.getRemediationFunction() != null;
  }

  private boolean isSqalePluginInstalled(DbSession session) {
    return dbClient.propertiesDao().selectGlobalProperty(session, SQALE_LICENSE_PROPERTY) != null;
  }

  private boolean hasAlreadyBeenExecuted(DbSession session) {
    return dbClient.loadedTemplateDao().countByTypeAndKey(ONE_SHOT_TASK_TYPE, TEMPLATE_KEY, session) > 0;
  }

  private void registerTasks(DbSession session) {
    dbClient.loadedTemplateDao().insert(new LoadedTemplateDto(TEMPLATE_KEY, ONE_SHOT_TASK_TYPE), session);
  }

  @Override
  public void stop() {
    // Nothing to do
  }
}
