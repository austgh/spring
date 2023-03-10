/*
 * Copyright 2004-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.web.util;

import java.io.UnsupportedEncodingException;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test fixture for {@link JavaScriptUtils}.
 *
 * @author Rossen Stoyanchev
 */
public class JavaScriptUtilsTests {

	@Test
	public void escape() {
        String sb = '"' +
                "'" +
                "\\" +
                "/" +
                "\t" +
                "\n" +
                "\r" +
                "\f" +
                "\b" +
                "\013";
		assertEquals("\\\"\\'\\\\\\/\\t\\n\\n\\f\\b\\v", JavaScriptUtils.javaScriptEscape(sb));
	}

	// SPR-9983

	@Test
	public void escapePsLsLineTerminators() {
        String sb = "\u2028" +
                '\u2029';
		String result = JavaScriptUtils.javaScriptEscape(sb);

		assertEquals("\\u2028\\u2029", result);
	}

	// SPR-9983

	@Test
	public void escapeLessThanGreaterThanSigns() throws UnsupportedEncodingException {
		assertEquals("\\u003C\\u003E", JavaScriptUtils.javaScriptEscape("<>"));
	}

}
