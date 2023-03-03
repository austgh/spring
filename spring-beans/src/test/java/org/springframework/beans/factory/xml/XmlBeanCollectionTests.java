/*
 * Copyright 2002-2016 the original author or authors.
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.junit.Before;
import org.junit.Test;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.ListFactoryBean;
import org.springframework.beans.factory.config.MapFactoryBean;
import org.springframework.beans.factory.config.SetFactoryBean;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.tests.sample.beans.HasMap;
import org.springframework.tests.sample.beans.TestBean;

import static org.junit.Assert.*;

/**
 * Tests for collections in XML bean definitions.
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 19.12.2004
 */
public class XmlBeanCollectionTests {

	private final DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();


	@Before
	public void loadBeans() {
		new XmlBeanDefinitionReader(this.beanFactory).loadBeanDefinitions(
				new ClassPathResource("collections.xml", getClass()));
	}


	@Test
	public void testCollectionFactoryDefaults() throws Exception {
		ListFactoryBean listFactory = new ListFactoryBean();
		listFactory.setSourceList(new LinkedList());
		listFactory.afterPropertiesSet();
		assertTrue(listFactory.getObject() instanceof ArrayList);

		SetFactoryBean setFactory = new SetFactoryBean();
		setFactory.setSourceSet(new TreeSet());
		setFactory.afterPropertiesSet();
		assertTrue(setFactory.getObject() instanceof LinkedHashSet);

		MapFactoryBean mapFactory = new MapFactoryBean();
		mapFactory.setSourceMap(new TreeMap());
		mapFactory.afterPropertiesSet();
		assertTrue(mapFactory.getObject() instanceof LinkedHashMap);
	}

	@Test
	public void testRefSubelement() throws Exception {
		//assertTrue("5 beans in reftypes, not " + this.beanFactory.getBeanDefinitionCount(), this.beanFactory.getBeanDefinitionCount() == 5);
		TestBean jen = (TestBean) this.beanFactory.getBean("jenny");
		TestBean dave = (TestBean) this.beanFactory.getBean("david");
		assertSame(jen.getSpouse(), dave);
	}

	@Test
	public void testPropertyWithLiteralValueSubelement() throws Exception {
		TestBean verbose = (TestBean) this.beanFactory.getBean("verbose");
		assertEquals("verbose", verbose.getName());
	}

	@Test
	public void testPropertyWithIdRefLocalAttrSubelement() throws Exception {
		TestBean verbose = (TestBean) this.beanFactory.getBean("verbose2");
		assertEquals("verbose", verbose.getName());
	}

	@Test
	public void testPropertyWithIdRefBeanAttrSubelement() throws Exception {
		TestBean verbose = (TestBean) this.beanFactory.getBean("verbose3");
		assertEquals("verbose", verbose.getName());
	}

	@Test
	public void testRefSubelementsBuildCollection() throws Exception {
		TestBean jen = (TestBean) this.beanFactory.getBean("jenny");
		TestBean dave = (TestBean) this.beanFactory.getBean("david");
		TestBean rod = (TestBean) this.beanFactory.getBean("rod");

		// Must be a list to support ordering
		// Our bean doesn't modify the collection:
		// of course it could be a different copy in a real object.
		Object[] friends = rod.getFriends().toArray();
		assertEquals(2, friends.length);

		assertSame("First friend must be jen, not " + friends[0], friends[0], jen);
		assertSame(friends[1], dave);
		// Should be ordered
	}

	@Test
	public void testRefSubelementsBuildCollectionWithPrototypes() throws Exception {
		TestBean jen = (TestBean) this.beanFactory.getBean("pJenny");
		TestBean dave = (TestBean) this.beanFactory.getBean("pDavid");
		TestBean rod = (TestBean) this.beanFactory.getBean("pRod");

		Object[] friends = rod.getFriends().toArray();
		assertEquals(2, friends.length);
		assertEquals("First friend must be jen, not " + friends[0], friends[0].toString(), jen.toString());
		assertNotSame("Jen not same instance", friends[0], jen);
		assertEquals(friends[1].toString(), dave.toString());
		assertNotSame("Dave not same instance", friends[1], dave);
		assertEquals("Jen", dave.getSpouse().getName());

		TestBean rod2 = (TestBean) this.beanFactory.getBean("pRod");
		Object[] friends2 = rod2.getFriends().toArray();
		assertEquals(2, friends2.length);
		assertEquals("First friend must be jen, not " + friends2[0], friends2[0].toString(), jen.toString());
		assertNotSame("Jen not same instance", friends2[0], friends[0]);
		assertEquals(friends2[1].toString(), dave.toString());
		assertNotSame("Dave not same instance", friends2[1], friends[1]);
	}

	@Test
	public void testRefSubelementsBuildCollectionFromSingleElement() throws Exception {
		TestBean loner = (TestBean) this.beanFactory.getBean("loner");
		TestBean dave = (TestBean) this.beanFactory.getBean("david");
		assertEquals(1, loner.getFriends().size());
		assertTrue(loner.getFriends().contains(dave));
	}

	@Test
	public void testBuildCollectionFromMixtureOfReferencesAndValues() throws Exception {
		MixedCollectionBean jumble = (MixedCollectionBean) this.beanFactory.getBean("jumble");
		assertEquals("Expected 5 elements, not " + jumble.getJumble().size(), 5, jumble.getJumble().size());
		List l = (List) jumble.getJumble();
		assertEquals(l.get(0), this.beanFactory.getBean("david"));
		assertEquals("literal", l.get(1));
		assertEquals(l.get(2), this.beanFactory.getBean("jenny"));
		assertEquals("rod", l.get(3));
		Object[] array = (Object[]) l.get(4);
		assertEquals(array[0], this.beanFactory.getBean("david"));
		assertEquals("literal2", array[1]);
	}

	@Test
	public void testInvalidBeanNameReference() throws Exception {
		try {
			this.beanFactory.getBean("jumble2");
			fail("Should have thrown BeanCreationException");
		}
		catch (BeanCreationException ex) {
			assertTrue(ex.getCause() instanceof BeanDefinitionStoreException);
			assertTrue(ex.getCause().getMessage().contains("rod2"));
		}
	}

	@Test
	public void testEmptyMap() throws Exception {
		HasMap hasMap = (HasMap) this.beanFactory.getBean("emptyMap");
		assertEquals(0, hasMap.getMap().size());
	}

	@Test
	public void testMapWithLiteralsOnly() throws Exception {
		HasMap hasMap = (HasMap) this.beanFactory.getBean("literalMap");
		assertEquals(3, hasMap.getMap().size());
		assertEquals("bar", hasMap.getMap().get("foo"));
		assertEquals("fum", hasMap.getMap().get("fi"));
		assertNull(hasMap.getMap().get("fa"));
	}

	@Test
	public void testMapWithLiteralsAndReferences() throws Exception {
		HasMap hasMap = (HasMap) this.beanFactory.getBean("mixedMap");
		assertEquals(5, hasMap.getMap().size());
		assertEquals(hasMap.getMap().get("foo"), new Integer(10));
		TestBean jenny = (TestBean) this.beanFactory.getBean("jenny");
		assertSame(hasMap.getMap().get("jenny"), jenny);
		assertEquals("david", hasMap.getMap().get(new Integer(5)));
		assertTrue(hasMap.getMap().get("bar") instanceof Long);
		assertEquals(hasMap.getMap().get("bar"), new Long(100));
		assertTrue(hasMap.getMap().get("baz") instanceof Integer);
		assertEquals(hasMap.getMap().get("baz"), new Integer(200));
	}

	@Test
	public void testMapWithLiteralsAndPrototypeReferences() throws Exception {
		TestBean jenny = (TestBean) this.beanFactory.getBean("pJenny");
		HasMap hasMap = (HasMap) this.beanFactory.getBean("pMixedMap");
		assertEquals(2, hasMap.getMap().size());
		assertEquals("bar", hasMap.getMap().get("foo"));
		assertEquals(hasMap.getMap().get("jenny").toString(), jenny.toString());
		assertNotSame("Not same instance", hasMap.getMap().get("jenny"), jenny);

		HasMap hasMap2 = (HasMap) this.beanFactory.getBean("pMixedMap");
		assertEquals(2, hasMap2.getMap().size());
		assertEquals("bar", hasMap2.getMap().get("foo"));
		assertEquals(hasMap2.getMap().get("jenny").toString(), jenny.toString());
		assertNotSame("Not same instance", hasMap2.getMap().get("jenny"), hasMap.getMap().get("jenny"));
	}

	@Test
	public void testMapWithLiteralsReferencesAndList() throws Exception {
		HasMap hasMap = (HasMap) this.beanFactory.getBean("mixedMapWithList");
		assertEquals(4, hasMap.getMap().size());
		assertEquals("bar", hasMap.getMap().get(null));
		TestBean jenny = (TestBean) this.beanFactory.getBean("jenny");
		assertEquals(hasMap.getMap().get("jenny"), jenny);

		// Check list
		List l = (List) hasMap.getMap().get("list");
		assertNotNull(l);
		assertEquals(4, l.size());
		assertEquals("zero", l.get(0));
		assertNull(l.get(3));

		// Check nested map in list
		Map m = (Map) l.get(1);
		assertNotNull(m);
		assertEquals(2, m.size());
		assertEquals("bar", m.get("fo"));
		assertEquals("Map element 'jenny' should be equal to jenny bean, not " + m.get("jen"), m.get("jen"), jenny);

		// Check nested list in list
		l = (List) l.get(2);
		assertNotNull(l);
		assertEquals(2, l.size());
		assertEquals(l.get(0), jenny);
		assertEquals("ba", l.get(1));

		// Check nested map
		m = (Map) hasMap.getMap().get("map");
		assertNotNull(m);
		assertEquals(2, m.size());
		assertEquals("bar", m.get("foo"));
		assertEquals("Map element 'jenny' should be equal to jenny bean, not " + m.get("jenny"), m.get("jenny"), jenny);
	}

	@Test
	public void testEmptySet() throws Exception {
		HasMap hasMap = (HasMap) this.beanFactory.getBean("emptySet");
		assertEquals(0, hasMap.getSet().size());
	}

	@Test
	public void testPopulatedSet() throws Exception {
		HasMap hasMap = (HasMap) this.beanFactory.getBean("set");
		assertEquals(3, hasMap.getSet().size());
		assertTrue(hasMap.getSet().contains("bar"));
		TestBean jenny = (TestBean) this.beanFactory.getBean("jenny");
		assertTrue(hasMap.getSet().contains(jenny));
		assertTrue(hasMap.getSet().contains(null));
		Iterator it = hasMap.getSet().iterator();
		assertEquals("bar", it.next());
		assertEquals(jenny, it.next());
		assertNull(it.next());
	}

	@Test
	public void testPopulatedConcurrentSet() throws Exception {
		HasMap hasMap = (HasMap) this.beanFactory.getBean("concurrentSet");
		assertEquals(3, hasMap.getConcurrentSet().size());
		assertTrue(hasMap.getConcurrentSet().contains("bar"));
		TestBean jenny = (TestBean) this.beanFactory.getBean("jenny");
		assertTrue(hasMap.getConcurrentSet().contains(jenny));
		assertTrue(hasMap.getConcurrentSet().contains(null));
	}

	@Test
	public void testPopulatedIdentityMap() throws Exception {
		HasMap hasMap = (HasMap) this.beanFactory.getBean("identityMap");
		assertEquals(2, hasMap.getIdentityMap().size());
		HashSet set = new HashSet(hasMap.getIdentityMap().keySet());
		assertTrue(set.contains("foo"));
		assertTrue(set.contains("jenny"));
	}

	@Test
	public void testEmptyProps() throws Exception {
		HasMap hasMap = (HasMap) this.beanFactory.getBean("emptyProps");
		assertEquals(0, hasMap.getProps().size());
		assertEquals(hasMap.getProps().getClass(), Properties.class);
	}

	@Test
	public void testPopulatedProps() throws Exception {
		HasMap hasMap = (HasMap) this.beanFactory.getBean("props");
		assertEquals(2, hasMap.getProps().size());
		assertEquals("bar", hasMap.getProps().get("foo"));
		assertEquals("TWO", hasMap.getProps().get("2"));
	}

	@Test
	public void testObjectArray() throws Exception {
		HasMap hasMap = (HasMap) this.beanFactory.getBean("objectArray");
		assertEquals(2, hasMap.getObjectArray().length);
		assertEquals("one", hasMap.getObjectArray()[0]);
		assertEquals(hasMap.getObjectArray()[1], this.beanFactory.getBean("jenny"));
	}

	@Test
	public void testIntegerArray() throws Exception {
		HasMap hasMap = (HasMap) this.beanFactory.getBean("integerArray");
		assertEquals(3, hasMap.getIntegerArray().length);
		assertEquals(0, hasMap.getIntegerArray()[0].intValue());
		assertEquals(1, hasMap.getIntegerArray()[1].intValue());
		assertEquals(2, hasMap.getIntegerArray()[2].intValue());
	}

	@Test
	public void testClassArray() throws Exception {
		HasMap hasMap = (HasMap) this.beanFactory.getBean("classArray");
		assertEquals(2, hasMap.getClassArray().length);
		assertEquals(hasMap.getClassArray()[0], String.class);
		assertEquals(hasMap.getClassArray()[1], Exception.class);
	}

	@Test
	public void testClassList() throws Exception {
		HasMap hasMap = (HasMap) this.beanFactory.getBean("classList");
		assertEquals(2, hasMap.getClassList().size());
		assertEquals(hasMap.getClassList().get(0), String.class);
		assertEquals(hasMap.getClassList().get(1), Exception.class);
	}

	@Test
	public void testProps() throws Exception {
		HasMap hasMap = (HasMap) this.beanFactory.getBean("props");
		assertEquals(2, hasMap.getProps().size());
		assertEquals("bar", hasMap.getProps().getProperty("foo"));
		assertEquals("TWO", hasMap.getProps().getProperty("2"));

		HasMap hasMap2 = (HasMap) this.beanFactory.getBean("propsViaMap");
		assertEquals(2, hasMap2.getProps().size());
		assertEquals("bar", hasMap2.getProps().getProperty("foo"));
		assertEquals("TWO", hasMap2.getProps().getProperty("2"));
	}

	@Test
	public void testListFactory() throws Exception {
		List list = (List) this.beanFactory.getBean("listFactory");
		assertTrue(list instanceof LinkedList);
		assertEquals(2, list.size());
		assertEquals("bar", list.get(0));
		assertEquals("jenny", list.get(1));
	}

	@Test
	public void testPrototypeListFactory() throws Exception {
		List list = (List) this.beanFactory.getBean("pListFactory");
		assertTrue(list instanceof LinkedList);
		assertEquals(2, list.size());
		assertEquals("bar", list.get(0));
		assertEquals("jenny", list.get(1));
	}

	@Test
	public void testSetFactory() throws Exception {
		Set set = (Set) this.beanFactory.getBean("setFactory");
		assertTrue(set instanceof TreeSet);
		assertEquals(2, set.size());
		assertTrue(set.contains("bar"));
		assertTrue(set.contains("jenny"));
	}

	@Test
	public void testPrototypeSetFactory() throws Exception {
		Set set = (Set) this.beanFactory.getBean("pSetFactory");
		assertTrue(set instanceof TreeSet);
		assertEquals(2, set.size());
		assertTrue(set.contains("bar"));
		assertTrue(set.contains("jenny"));
	}

	@Test
	public void testMapFactory() throws Exception {
		Map map = (Map) this.beanFactory.getBean("mapFactory");
		assertTrue(map instanceof TreeMap);
		assertEquals(2, map.size());
		assertEquals("bar", map.get("foo"));
		assertEquals("jenny", map.get("jen"));
	}

	@Test
	public void testPrototypeMapFactory() throws Exception {
		Map map = (Map) this.beanFactory.getBean("pMapFactory");
		assertTrue(map instanceof TreeMap);
		assertEquals(2, map.size());
		assertEquals("bar", map.get("foo"));
		assertEquals("jenny", map.get("jen"));
	}

	@Test
	public void testChoiceBetweenSetAndMap() {
		MapAndSet sam = (MapAndSet) this.beanFactory.getBean("setAndMap");
		assertTrue("Didn't choose constructor with Map argument", sam.getObject() instanceof Map);
		Map map = (Map) sam.getObject();
		assertEquals(3, map.size());
		assertEquals("val1", map.get("key1"));
		assertEquals("val2", map.get("key2"));
		assertEquals("val3", map.get("key3"));
	}

	@Test
	public void testEnumSetFactory() throws Exception {
		Set set = (Set) this.beanFactory.getBean("enumSetFactory");
		assertEquals(2, set.size());
		assertTrue(set.contains("ONE"));
		assertTrue(set.contains("TWO"));
	}


	public static class MapAndSet {

		private final Object obj;

		public MapAndSet(Map map) {
			this.obj = map;
		}

		public MapAndSet(Set set) {
			this.obj = set;
		}

		public Object getObject() {
			return obj;
		}
	}

}
