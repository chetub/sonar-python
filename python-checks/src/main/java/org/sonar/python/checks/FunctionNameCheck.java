/*
 * SonarQube Python Plugin
 * Copyright (C) 2011-2019 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
package org.sonar.python.checks;

import com.jetbrains.python.psi.PyFunction;
import org.sonar.check.Rule;

@Rule(key = "S1542")
public class FunctionNameCheck extends AbstractFunctionNameCheck {

  @Override
  public String typeName() {
    return "function";
  }

  @Override
  public boolean shouldCheckFunctionDeclaration(PyFunction function) {
    return !CheckUtils.isMethodDefinition(function);
  }

}
