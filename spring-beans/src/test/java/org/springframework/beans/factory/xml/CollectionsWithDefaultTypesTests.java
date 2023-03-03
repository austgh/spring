/*
 * Copyright 2002-2013 the original author or authors.
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

package org.springframework.beans.factory.xml;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.tests.sample.beans.TestBean;

import static org.junit.Assert.*;

/**
 * @author Rob Harrop
 * @author Juergen Hoeller
 */
public class CollectionsWithDefaultTypesTests {

	private final DefaultListableBeanFactory beanFactory;

	public CollectionsWithDefaultTypesTests() {
		this.beanFactory = new DefaultListableBeanFactory();
		new XmlBeanDefinitionReader(this.beanFactory).loadBeanDefinitions(
				new ClassPathResource("collectionsWithDefaultTypes.xml", getClass()));
	}

	@Test
	public void testListHasDefaultType() throws Exception {
		TestBean bean = (TestBean) this.beanFactory.getBean("testBean");
		for (Object o : bean.getSomeList()) {
			assertEquals("Value type is incorrect", Integer.class, o.getClass());
		}
	}

	@Test
	public void testSetHasDefaultType() throws Exception {
		TestBean bean = (TestBean) this.beanFactory.getBean("testBean");
		for (Object o : bean.getSomeSet()) {
			assertEquals("Value type is incorrect", Integer.class, o.getClass());
		}
	}

	@Test
	public void testMapHasDefaultKeyAndValueType() throws Exception {
		TestBean bean = (TestBean) this.beanFactory.getBean("testBean");
		assertMap(bean.getSomeMap());
	}

	@Test
	public void testMapWithNestedElementsHasDefaultKeyAndValueType() throws Exception {
		TestBean bean = (TestBean) this.beanFactory.getBean("testBean2");
		assertMap(bean.getSomeMap());
	}

	private void assertMap(Map<?,?> map) {
		for (Map.Entry entry : map.entrySet()) {
			assertEquals("Key type is incorrect", Integer.class, entry.getKey().getClass());
			assertEquals("Value type is incorrect", Boolean.class, entry.getValue().getClass());
		}
	}

	@Test
	public void testBuildCollectionFromMixtureOfReferencesAndValues() throws Exception {
		MixedCollectionBean jumble = (MixedCollectionBean) this.beanFactory.getBean("jumble");
		assertEquals("Expected 3 elements, not " + jumble.getJumble().size(), 3, jumble.getJumble().size());
		List l = (List) jumble.getJumble();
		assertEquals("literal", l.get(0));
		Integer[] array1 = (Integer[]) l.get(1);
		assertEquals(array1[0], new Integer(2));
		assertEquals(array1[1], new Integer(4));
		int[] array2 = (int[]) l.get(2);
		assertEquals(3, array2[0]);
		assertEquals(5, array2[1]);
	}

}
