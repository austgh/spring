/*
 * Copyright 2002-2012 the original author or authors.
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

package org.springframework.util.comparator;

import java.util.Comparator;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for {@link NullSafeComparator}.
 *
 * @author Keith Donald
 * @author Chris Beams
 * @author Phillip Webb
 */
public class NullSafeComparatorTests {

	@SuppressWarnings("unchecked")
	@Test
	public void shouldCompareWithNullsLow() {
		Comparator<String> c = NullSafeComparator.NULLS_LOW;
		assertTrue(c.compare(null, "boo") < 0);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldCompareWithNullsHigh() {
		Comparator<String> c = NullSafeComparator.NULLS_HIGH;
		assertTrue(c.compare(null, "boo") > 0);
        assertEquals(0, c.compare(null, null));
	}

}
