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
package org.sonar.batch.repository;

import org.sonarqube.ws.QualityProfiles.WsSearchResponse.QualityProfile;
import org.sonar.batch.analysis.DefaultAnalysisMode;
import org.sonar.api.batch.bootstrap.ProjectKey;

import java.util.List;

import org.sonar.batch.analysis.AnalysisProperties;
import org.sonar.batch.rule.ModuleQProfiles;
import org.picocontainer.injectors.ProviderAdapter;

public class QualityProfileProvider extends ProviderAdapter {
  private ModuleQProfiles profiles = null;

  public ModuleQProfiles provide(ProjectKey projectKey, QualityProfileLoader loader, ProjectRepositories projectRepositories, AnalysisProperties props, DefaultAnalysisMode mode) {
    if (this.profiles == null) {
      List<QualityProfile> profileList;

      if (mode.isNotAssociated() || !projectRepositories.exists()) {
        profileList = loader.loadDefault(null);
      } else {
        profileList = loader.load(projectKey.get(), getSonarProfile(props, mode), null);
      }

      profiles = new ModuleQProfiles(profileList);
    }

    return profiles;
  }

  private static String getSonarProfile(AnalysisProperties props, DefaultAnalysisMode mode) {
    String profile = null;
    if (!mode.isIssues()) {
      profile = props.property(ModuleQProfiles.SONAR_PROFILE_PROP);
    }
    return profile;
  }

}
