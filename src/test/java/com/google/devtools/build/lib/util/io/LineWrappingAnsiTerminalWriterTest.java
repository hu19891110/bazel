// Copyright 2016 The Bazel Authors. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.devtools.build.lib.util.io;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;

/**
 * Tests {@link LineWrappingAnsiTerminalWriter}.
 */
@RunWith(JUnit4.class)
public class LineWrappingAnsiTerminalWriterTest {
  static final String NL = LoggingTerminalWriter.NEWLINE;
  static final String OK = LoggingTerminalWriter.OK;
  static final String FAIL = LoggingTerminalWriter.FAIL;
  static final String NORMAL = LoggingTerminalWriter.NORMAL;

  @Test
  public void testSimpleLineWrapping() throws IOException {
    LoggingTerminalWriter terminal = new LoggingTerminalWriter();
    (new LineWrappingAnsiTerminalWriter(terminal, 5, '+')).append("abcdefghij");
    assertEquals("abcd+" + NL + "efgh+" + NL + "ij", terminal.getTranscript());
  }

  @Test
  public void testAlwaysWrap() throws IOException {
    LoggingTerminalWriter terminal = new LoggingTerminalWriter();
    (new LineWrappingAnsiTerminalWriter(terminal, 5, '+')).append("12345").newline();
    assertEquals("1234+" + NL + "5" + NL, terminal.getTranscript());
  }

  @Test
  public void testWrapLate() throws IOException {
    LoggingTerminalWriter terminal = new LoggingTerminalWriter();
    (new LineWrappingAnsiTerminalWriter(terminal, 5, '+')).append("1234");
    // Lines are only wrapped, once a character is written that cannot fit in the current line, and
    // not already once the last usable character of a line is used. Hence, in this example, we do
    // not want to see the continuation character.
    assertEquals("1234", terminal.getTranscript());
  }

  @Test
  public void testNewlineTranslated() throws IOException {
    LoggingTerminalWriter terminal = new LoggingTerminalWriter();
    (new LineWrappingAnsiTerminalWriter(terminal, 80, '+')).append("foo\nbar\n");
    assertEquals("foo" + NL + "bar" + NL, terminal.getTranscript());
  }

  @Test
  public void testNewlineResetsCount() throws IOException {
    LoggingTerminalWriter terminal = new LoggingTerminalWriter();
    (new LineWrappingAnsiTerminalWriter(terminal, 5, '+'))
        .append("123")
        .newline()
        .append("abc")
        .newline()
        .append("ABC\nABC")
        .newline();
    assertEquals("123" + NL + "abc" + NL + "ABC" + NL + "ABC" + NL, terminal.getTranscript());
  }

  @Test
  public void testEventsPassedThrough() throws IOException {
    LoggingTerminalWriter terminal = new LoggingTerminalWriter();
    (new LineWrappingAnsiTerminalWriter(terminal, 80, '+'))
        .okStatus()
        .append("ok")
        .failStatus()
        .append("fail")
        .normal()
        .append("normal");
    assertEquals(OK + "ok" + FAIL + "fail" + NORMAL + "normal", terminal.getTranscript());
  }
}
