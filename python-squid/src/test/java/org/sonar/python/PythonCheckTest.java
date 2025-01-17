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
package org.sonar.python;

import com.google.common.collect.ImmutableSet;
import com.intellij.lang.ASTNode;
import com.jetbrains.python.PyStubElementTypes;
import com.jetbrains.python.psi.PyFunction;
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstNodeType;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.sonar.python.PythonCheck.PreciseIssue;
import org.sonar.python.api.PythonGrammar;
import org.sonar.python.frontend.PythonParser;

import static org.assertj.core.api.Assertions.assertThat;

public class PythonCheckTest {

  private static final File FILE = new File("src/test/resources/file.py");
  public static final String MESSAGE = "message";

  private static List<PreciseIssue> scanFileForIssues(File file, PythonCheck check) {
    PythonVisitorContext context = TestPythonVisitorRunner.createContext(file);
    check.scanFile(context);
    SubscriptionVisitor.analyze(Collections.singletonList(check), context, PythonParser.parse(file));
    return context.getIssues();
  }

  @Test
  public void test() {
    TestPythonCheck check = new TestPythonCheck (){
      @Override
      public void visitNode(AstNode astNode) {
        AstNode funcName = astNode.getFirstChild(PythonGrammar.FUNCNAME);
        addIssue(funcName, funcName.getTokenValue());
      }

      @Override
      public void initialize(Context context) {
        context.registerSyntaxNodeConsumer(PyStubElementTypes.FUNCTION_DECLARATION, ctx -> {
          ASTNode nameNode = ((PyFunction) ctx.syntaxNode()).getNameNode();
          ctx.addIssue(nameNode.getPsi(), nameNode.getText());
        });
      }
    };

    List<PreciseIssue> issues = scanFileForIssues(FILE, check);

    assertThat(issues).hasSize(4);
    PreciseIssue firstIssue = issues.get(0);

    assertThat(firstIssue.cost()).isNull();
    assertThat(firstIssue.secondaryLocations()).isEmpty();

    IssueLocation primaryLocation = firstIssue.primaryLocation();
    assertThat(primaryLocation.message()).isEqualTo("hello");

    assertThat(primaryLocation.startLine()).isEqualTo(1);
    assertThat(primaryLocation.endLine()).isEqualTo(1);
    assertThat(primaryLocation.startLineOffset()).isEqualTo(4);
    assertThat(primaryLocation.endLineOffset()).isEqualTo(9);

    PreciseIssue issue = issues.get(1);
    assertThat(issue.primaryLocation().message()).isEqualTo("method");

    issue = issues.get(2);
    assertThat(issue.primaryLocation().message()).isEqualTo("hello");
    primaryLocation = issue.primaryLocation();
    assertThat(primaryLocation.message()).isEqualTo("hello");
    assertThat(primaryLocation.startLine()).isEqualTo(1);
    assertThat(primaryLocation.endLine()).isEqualTo(1);
    assertThat(primaryLocation.startLineOffset()).isEqualTo(4);
    assertThat(primaryLocation.endLineOffset()).isEqualTo(9);

    issue = issues.get(3);
    assertThat(issue.primaryLocation().message()).isEqualTo("method");
  }

  @Test
  public void test_cost() {
    TestPythonCheck check = new TestPythonCheck (){
      @Override
      public void visitNode(AstNode astNode) {
        addIssue(astNode.getFirstChild(PythonGrammar.FUNCNAME), MESSAGE).withCost(42);
      }
    };

    List<PreciseIssue> issues = scanFileForIssues(FILE, check);
    PreciseIssue firstIssue = issues.get(0);
    assertThat(firstIssue.cost()).isEqualTo(42);
  }

  @Test
  public void test_secondary_location() {
    TestPythonCheck check = new TestPythonCheck (){
      @Override
      public void visitNode(AstNode astNode) {
        PreciseIssue issue = addIssue(astNode.getFirstChild(PythonGrammar.FUNCNAME), MESSAGE)
          .secondary(astNode.getFirstChild(), "def keyword");

        AstNode returnStmt = astNode.getFirstDescendant(PythonGrammar.RETURN_STMT);
        if (returnStmt != null) {
          issue.secondary(returnStmt, "return statement");
        }
      }
    };

    List<PreciseIssue> issues = scanFileForIssues(FILE, check);

    List<IssueLocation> secondaryLocations = issues.get(0).secondaryLocations();
    assertThat(secondaryLocations).hasSize(2);

    IssueLocation firstSecondaryLocation = secondaryLocations.get(0);
    IssueLocation secondSecondaryLocation = secondaryLocations.get(1);

    assertThat(firstSecondaryLocation.message()).isEqualTo("def keyword");
    assertThat(firstSecondaryLocation.startLine()).isEqualTo(1);
    assertThat(firstSecondaryLocation.startLineOffset()).isEqualTo(0);
    assertThat(firstSecondaryLocation.endLine()).isEqualTo(1);
    assertThat(firstSecondaryLocation.endLineOffset()).isEqualTo(3);

    assertThat(secondSecondaryLocation.message()).isEqualTo("return statement");
    assertThat(secondSecondaryLocation.startLine()).isEqualTo(3);
    assertThat(secondSecondaryLocation.startLineOffset()).isEqualTo(4);
    assertThat(secondSecondaryLocation.endLine()).isEqualTo(4);
    assertThat(secondSecondaryLocation.endLineOffset()).isEqualTo(5);
  }

  @Test
  public void file_level_issue() {
    TestPythonCheck check = new TestPythonCheck() {
      @Override
      public void visitFile(AstNode astNode) {
        addFileIssue(MESSAGE);
      }
    };
    List<PreciseIssue> issues = scanFileForIssues(FILE, check);
    assertThat(issues).hasSize(1);
    PreciseIssue issue = issues.get(0);
    assertThat(issue.primaryLocation().message()).isEqualTo(MESSAGE);
    assertThat(issue.primaryLocation().startLine()).isEqualTo(0);
    assertThat(issue.primaryLocation().endLine()).isEqualTo(0);
  }

  @Test
  public void line_issue() {
    TestPythonCheck check = new TestPythonCheck() {
      @Override
      public void visitFile(AstNode astNode) {
        addLineIssue(MESSAGE, 3);
      }
    };
    List<PreciseIssue> issues = scanFileForIssues(FILE, check);
    assertThat(issues).hasSize(1);
    PreciseIssue issue = issues.get(0);
    assertThat(issue.primaryLocation().message()).isEqualTo(MESSAGE);
    assertThat(issue.primaryLocation().startLine()).isEqualTo(3);
    assertThat(issue.primaryLocation().endLine()).isEqualTo(3);
  }

  private static class TestPythonCheck extends PythonCheck {

    @Override
    public Set<AstNodeType> subscribedKinds() {
      return ImmutableSet.of(PythonGrammar.FUNCDEF);
    }

  }
}
