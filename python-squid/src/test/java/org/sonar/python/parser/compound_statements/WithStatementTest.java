/*
 * SonarQube Python Plugin
 * Copyright (C) 2011 SonarSource and Waleri Enns
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
package org.sonar.python.parser.compound_statements;

import com.google.common.base.Charsets;
import com.sonar.sslr.impl.Parser;
import org.junit.Before;
import org.junit.Test;
import org.sonar.python.PythonConfiguration;
import org.sonar.python.api.PythonGrammar;
import org.sonar.python.parser.PythonParser;
import org.sonar.python.parser.PythonTestUtils;

import static com.sonar.sslr.test.parser.ParserMatchers.parse;
import static org.junit.Assert.assertThat;

public class WithStatementTest {

  Parser<PythonGrammar> p = PythonParser.create(new PythonConfiguration(Charsets.UTF_8));
  PythonGrammar g = p.getGrammar();

  @Before
  public void init() {
    p.setRootRule(g.with_stmt);
  }

  @Test
  public void ok() {
    g.suite.mock();
    g.with_item.mock();

    assertThat(p, parse("with with_item , with_item : suite"));
    assertThat(p, parse("with with_item : suite"));
  }

  @Test
  public void realLife() {
    assertThat(p, parse(PythonTestUtils.appendNewLine("with A() as a : pass")));
    assertThat(p, parse(PythonTestUtils.appendNewLine("with A() as a, B() as b : pass")));
  }

}
