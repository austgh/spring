/*
 * Copyright 2002-2017 the original author or authors.
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

package org.springframework.util;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Juergen Hoeller
 */
public class LinkedCaseInsensitiveMapTests {

	private final LinkedCaseInsensitiveMap<String> map = new LinkedCaseInsensitiveMap<>();


	@Test
	public void putAndGet() {
		map.put("key", "value1");
		map.put("key", "value2");
		map.put("key", "value3");
		assertEquals(1, map.size());
		assertEquals("value3", map.get("key"));
		assertEquals("value3", map.get("KEY"));
		assertEquals("value3", map.get("Key"));
		assertTrue(map.containsKey("key"));
		assertTrue(map.containsKey("KEY"));
		assertTrue(map.containsKey("Key"));
		assertTrue(map.containsKey("key"));
		assertTrue(map.containsKey("KEY"));
		assertTrue(map.containsKey("Key"));
	}

	@Test
	public void putWithOverlappingKeys() {
		map.put("key", "value1");
		map.put("KEY", "value2");
		map.put("Key", "value3");
		assertEquals(1, map.size());
		assertEquals("value3", map.get("key"));
		assertEquals("value3", map.get("KEY"));
		assertEquals("value3", map.get("Key"));
		assertTrue(map.containsKey("key"));
		assertTrue(map.containsKey("KEY"));
		assertTrue(map.containsKey("Key"));
		assertTrue(map.containsKey("key"));
		assertTrue(map.containsKey("KEY"));
		assertTrue(map.containsKey("Key"));
	}

	@Test
	public void getOrDefault() {
		map.put("key", "value1");
		map.put("KEY", "value2");
		map.put("Key", "value3");
		assertEquals("value3", map.getOrDefault("key", "N"));
		assertEquals("value3", map.getOrDefault("KEY", "N"));
		assertEquals("value3", map.getOrDefault("Key", "N"));
		assertEquals("N", map.getOrDefault("keeeey", "N"));
		assertEquals("N", map.getOrDefault(new Object(), "N"));
	}

	@Test
	public void getOrDefaultWithNullValue() {
		map.put("key", null);
		map.put("KEY", null);
		map.put("Key", null);
		assertNull(map.getOrDefault("key", "N"));
		assertNull(map.getOrDefault("KEY", "N"));
		assertNull(map.getOrDefault("Key", "N"));
		assertEquals("N", map.getOrDefault("keeeey", "N"));
		assertEquals("N", map.getOrDefault(new Object(), "N"));
	}

	@Test
	public void computeIfAbsentWithExistingValue() {
		map.put("key", "value1");
		map.put("KEY", "value2");
		map.put("Key", "value3");
		assertEquals("value3", map.computeIfAbsent("key", key -> "value1"));
		assertEquals("value3", map.computeIfAbsent("KEY", key -> "value2"));
		assertEquals("value3", map.computeIfAbsent("Key", key -> "value3"));
	}

	@Test
	public void computeIfAbsentWithComputedValue() {
		assertEquals("value1", map.computeIfAbsent("key", key -> "value1"));
		assertEquals("value1", map.computeIfAbsent("KEY", key -> "value2"));
		assertEquals("value1", map.computeIfAbsent("Key", key -> "value3"));
	}

	@Test
	public void mapClone() {
		map.put("key", "value1");
		LinkedCaseInsensitiveMap<String> copy = map.clone();

		assertEquals(map.getLocale(), copy.getLocale());
		assertEquals("value1", map.get("key"));
		assertEquals("value1", map.get("KEY"));
		assertEquals("value1", map.get("Key"));
		assertEquals("value1", copy.get("key"));
		assertEquals("value1", copy.get("KEY"));
		assertEquals("value1", copy.get("Key"));

		copy.put("Key", "value2");
		assertEquals(1, map.size());
		assertEquals(1, copy.size());
		assertEquals("value1", map.get("key"));
		assertEquals("value1", map.get("KEY"));
		assertEquals("value1", map.get("Key"));
		assertEquals("value2", copy.get("key"));
		assertEquals("value2", copy.get("KEY"));
		assertEquals("value2", copy.get("Key"));
	}

}
