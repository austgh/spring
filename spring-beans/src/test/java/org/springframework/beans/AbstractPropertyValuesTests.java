/*
 * Copyright 2002-2018 the original author or authors.
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

package org.springframework.beans;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author Rod Johnson
 * @author Chris Beams
 */
public abstract class AbstractPropertyValuesTests {

	/**
	 * Must contain: forname=Tony surname=Blair age=50
	 */
	protected void doTestTony(PropertyValues pvs) {
        assertEquals("Contains 3", 3, pvs.getPropertyValues().length);
		assertTrue("Contains forname", pvs.contains("forname"));
		assertTrue("Contains surname", pvs.contains("surname"));
		assertTrue("Contains age", pvs.contains("age"));
        assertFalse("Doesn't contain tory", pvs.contains("tory"));

		PropertyValue[] ps = pvs.getPropertyValues();
		Map<String, String> m = new HashMap<>();
		m.put("forname", "Tony");
		m.put("surname", "Blair");
		m.put("age", "50");
		for (int i = 0; i < ps.length; i++) {
			Object val = m.get(ps[i].getName());
            assertNotNull("Can't have unexpected value", val);
			assertTrue("Val i string", val instanceof String);
            assertEquals("val matches expected", val, ps[i].getValue());
			m.remove(ps[i].getName());
		}
        assertEquals("Map size is 0", 0, m.size());
	}

}
