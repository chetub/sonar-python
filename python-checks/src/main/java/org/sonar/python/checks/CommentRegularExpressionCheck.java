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

import com.jetbrains.python.PyTokenTypes;
import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.python.PythonCheck;

@Rule(key = "CommentRegularExpression")
public class CommentRegularExpressionCheck extends PythonCheck {
  private static final String DEFAULT_REGULAR_EXPRESSION = "";
  private static final String DEFAULT_MESSAGE = "The regular expression matches this comment";

  private Pattern pattern = null;

  @RuleProperty(
    key = "regularExpression",
    defaultValue = "" + DEFAULT_REGULAR_EXPRESSION)
  public String regularExpression = DEFAULT_REGULAR_EXPRESSION;

  @RuleProperty(
    key = "message",
    defaultValue = "" + DEFAULT_MESSAGE)
  public String message = DEFAULT_MESSAGE;

  private boolean isPatternInitialized = false;

  private Pattern pattern() {
    if (!isPatternInitialized) {
      if (regularExpression != null && !regularExpression.isEmpty()) {
        try {
          pattern = Pattern.compile(regularExpression, Pattern.DOTALL);
        } catch (RuntimeException e) {
          throw new IllegalStateException("Unable to compile regular expression: " + regularExpression, e);
        }
      }
      isPatternInitialized = true;
    }
    return pattern;
  }

  @Override
  public void initialize(Context context) {
    context.registerSyntaxNodeConsumer(PyTokenTypes.END_OF_LINE_COMMENT, ctx -> {
      if (pattern().matcher(ctx.syntaxNode().getText()).matches()) {
        ctx.addIssue(ctx.syntaxNode(), message);
      }
    });
  }

}
