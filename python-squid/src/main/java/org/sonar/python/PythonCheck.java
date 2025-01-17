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

import com.intellij.psi.PsiElement;
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Token;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

public abstract class PythonCheck extends PythonVisitor implements SubscriptionCheck {

  protected final PreciseIssue addIssue(AstNode node, @Nullable String message) {
    PreciseIssue newIssue = new PreciseIssue(this, IssueLocation.preciseLocation(node, message));
    getContext().addIssue(newIssue);
    return newIssue;
  }

  protected final PreciseIssue addIssue(IssueLocation primaryLocation) {
    PreciseIssue newIssue = new PreciseIssue(this, primaryLocation);
    getContext().addIssue(newIssue);
    return newIssue;
  }

  protected final PreciseIssue addLineIssue(String message, int lineNumber) {
    PreciseIssue newIssue = new PreciseIssue(this, IssueLocation.atLineLevel(message, lineNumber));
    getContext().addIssue(newIssue);
    return newIssue;
  }

  protected final PreciseIssue addFileIssue(String message) {
    PreciseIssue newIssue = new PreciseIssue(this, IssueLocation.atFileLevel(message));
    getContext().addIssue(newIssue);
    return newIssue;
  }

  protected final PreciseIssue addIssue(Token token, String message) {
    return addIssue(new AstNode(token), message);
  }

  public static class PreciseIssue {

    private final PythonCheck check;
    private final IssueLocation primaryLocation;
    private Integer cost;
    private final List<IssueLocation> secondaryLocations;

    PreciseIssue(PythonCheck check, IssueLocation primaryLocation) {
      this.check = check;
      this.primaryLocation = primaryLocation;
      this.secondaryLocations = new ArrayList<>();
    }

    @Nullable
    public Integer cost() {
      return cost;
    }

    public PreciseIssue withCost(int cost) {
      this.cost = cost;
      return this;
    }

    public IssueLocation primaryLocation() {
      return primaryLocation;
    }

    public PreciseIssue secondary(AstNode node, @Nullable String message) {
      secondaryLocations.add(IssueLocation.preciseLocation(node, message));
      return this;
    }

    public PreciseIssue secondary(IssueLocation issueLocation) {
      secondaryLocations.add(issueLocation);
      return this;
    }

    public PreciseIssue secondary(PsiElement element, @Nullable String message) {
      secondaryLocations.add(IssueLocation.preciseLocation(element, message));
      return this;
    }

    public List<IssueLocation> secondaryLocations() {
      return secondaryLocations;
    }

    public PythonCheck check() {
      return check;
    }
  }

  public static <T> Set<T> immutableSet(T... el) {
    return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(el)));
  }

  @Override
  public void initialize(Context context) {
  }
}
