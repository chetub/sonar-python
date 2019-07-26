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

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPolyVariantReference;
import com.jetbrains.python.PyElementTypes;
import com.jetbrains.python.psi.PyCallExpression;
import com.jetbrains.python.psi.PyExpression;
import com.jetbrains.python.psi.PyReferenceExpression;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.TypeEvalContext;
import com.sonar.sslr.api.AstNode;
import java.util.Set;
import org.sonar.python.PythonCheck;

public abstract class AbstractCallExpressionCheck extends PythonCheck {

  protected abstract Set<String> functionsToCheck();

  protected abstract String message();

  protected boolean isException(AstNode callExpression) {
    return false;
  }

  @Override
  public void initialize(Context context) {
    context.registerSyntaxNodeConsumer(PyElementTypes.INTEGER_LITERAL_EXPRESSION, ctx -> {
      PyExpression node = (PyExpression) ctx.syntaxNode();
      TypeEvalContext typeEvalContext = TypeEvalContext.codeAnalysis(node.getContainingFile().getProject(), node.getContainingFile());
      PyType type = typeEvalContext.getType(node);
//      System.out.println(type);
    });

    context.registerSyntaxNodeConsumer(PyElementTypes.CALL_EXPRESSION, ctx -> {
      PyCallExpression node = (PyCallExpression) ctx.syntaxNode();
//      System.out.println("******");
//      System.out.println(node.getText());
      PyExpression callee = node.getCallee();
      if (callee instanceof PyReferenceExpression) {
        PyReferenceExpression referenceExpression = (PyReferenceExpression) callee;

        PsiPolyVariantReference reference = referenceExpression.getReference();
        if (reference == null) {
//          System.out.println("null: " + callee.getText());
        }
        TypeEvalContext typeEvalContext = TypeEvalContext.codeAnalysis(node.getContainingFile().getProject(), node.getContainingFile());
//        System.out.println("call return type: " + type);
        PsiElement resolve = reference.resolve();
//        System.out.println(resolve);
//        System.out.println(resolve.getText());
//        Collection<? extends SymbolResolveResult> symbolResolveResults = reference.resolveReference();
//        for (SymbolResolveResult symbolResolveResult : symbolResolveResults) {
//          System.out.println(symbolResolveResult.getTarget());
//        }
      }
//      Symbol symbol = getContext().symbolTable().getSymbol(node);
//      if (!isException(node) && symbol != null && functionsToCheck().contains(symbol.qualifiedName())) {
//        addIssue(node, message());
//      }
    });
  }
}
