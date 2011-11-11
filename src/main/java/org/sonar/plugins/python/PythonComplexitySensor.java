/*
 * Sonar Python Plugin
 * Copyright (C) 2011 Waleri Enns
 * dev@sonar.codehaus.org
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */

package org.sonar.plugins.python;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.PersistenceMode;
import org.sonar.api.measures.RangeDistributionBuilder;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;

public final class PythonComplexitySensor implements Sensor {

  private static final Logger LOGGER = LoggerFactory.getLogger(PythonComplexitySensor.class);
  private static final Number[] FUNCTIONS_DISTRIB_BOTTOM_LIMITS = { 1, 2, 4, 6, 8, 10, 12, 20, 30 };
  private static final Number[] FILES_DISTRIB_BOTTOM_LIMITS = { 0, 5, 10, 20, 30, 60, 90 };

  public boolean shouldExecuteOnProject(Project project) {
    return Python.INSTANCE.equals(project.getLanguage());
  }

  public void analyse(Project project, SensorContext sensorContext) {
    for (File file : project.getFileSystem().getSourceFiles(Python.INSTANCE)) {
      try {
        analyzeFile(file, project.getFileSystem(), sensorContext);
      } catch (Exception e) {
        LOGGER.error("Cannot analyze the file '{}', details: '{}'", file.getAbsolutePath(), e);
      }
    }
  }

  protected void analyzeFile(File file, ProjectFileSystem projectFileSystem, SensorContext sensorContext) throws IOException {

    org.sonar.api.resources.File pyfile = PythonFile.fromIOFile(file, projectFileSystem.getSourceDirs());

    PythonComplexityAnalyzer analyzer = new PythonComplexityAnalyzer();

    // contains global (file scope) complexity
    // as head and function complexity counts as tail
    List<ComplexityStat> stats = analyzer.analyzeComplexity(file.getPath());
    ComplexityStat globalScopeStat = stats.get(0);
    List<ComplexityStat> functionStats = stats.subList(1, stats.size());

    // file complexity
    int fileComplexity = 0;
    int cumFuncComplexity = 0;
    for (ComplexityStat stat : functionStats) {
      cumFuncComplexity += stat.count;
    }
    fileComplexity = cumFuncComplexity + globalScopeStat.count;
    sensorContext.saveMeasure(pyfile, CoreMetrics.COMPLEXITY, (double) fileComplexity);

    // file complexity distribution
    RangeDistributionBuilder fileDistribution = new RangeDistributionBuilder(CoreMetrics.FILE_COMPLEXITY_DISTRIBUTION,
        FILES_DISTRIB_BOTTOM_LIMITS);
    fileDistribution.add((double) fileComplexity);
    sensorContext.saveMeasure(pyfile, fileDistribution.build().setPersistenceMode(PersistenceMode.MEMORY));

    if ( !functionStats.isEmpty()) {
      // function complexity
      sensorContext.saveMeasure(pyfile, CoreMetrics.FUNCTION_COMPLEXITY, (double) cumFuncComplexity / functionStats.size());

      // function complexity distribution
      RangeDistributionBuilder functionDistribution = new RangeDistributionBuilder(CoreMetrics.FUNCTION_COMPLEXITY_DISTRIBUTION,
          FUNCTIONS_DISTRIB_BOTTOM_LIMITS);
      for (ComplexityStat stat : functionStats) {
        functionDistribution.add((double) stat.count);
      }
      sensorContext.saveMeasure(pyfile, functionDistribution.build().setPersistenceMode(PersistenceMode.MEMORY));
    }
  }
}