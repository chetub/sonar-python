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

import com.intellij.lang.ASTNode;
import com.jetbrains.python.PyElementTypes;
import com.jetbrains.python.psi.PyFunction;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.python.PythonCheck;
import org.sonar.python.metrics.ComplexityVisitor;

@Rule(key = "FunctionComplexity")
public class FunctionComplexityCheck extends PythonCheck {
  private static final int DEFAULT_MAXIMUM_FUNCTION_COMPLEXITY_THRESHOLD = 15;
  private static final String MESSAGE = "Function has a complexity of %s which is greater than %s authorized.";

  @RuleProperty(
    key = "maximumFunctionComplexityThreshold",
    defaultValue = "" + DEFAULT_MAXIMUM_FUNCTION_COMPLEXITY_THRESHOLD)
  int maximumFunctionComplexityThreshold = DEFAULT_MAXIMUM_FUNCTION_COMPLEXITY_THRESHOLD;

  @Override
  public void initialize(Context context) {
    context.registerSyntaxNodeConsumer(PyElementTypes.FUNCTION_DECLARATION, ctx -> {
      PyFunction node = (PyFunction) ctx.syntaxNode();
      int complexity = ComplexityVisitor.complexity(node);
      if (complexity > maximumFunctionComplexityThreshold) {
        String message = String.format(MESSAGE, complexity, maximumFunctionComplexityThreshold);
        ASTNode nameNode = node.getNameNode();
        if (nameNode != null) {
          ctx.addIssue(nameNode.getPsi(), message).withCost(complexity - maximumFunctionComplexityThreshold);
        }
      }
    });
  }

}
