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

package org.springframework.cache.ehcache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.constructs.blocking.BlockingCache;
import net.sf.ehcache.constructs.blocking.SelfPopulatingCache;
import net.sf.ehcache.constructs.blocking.UpdatingCacheEntryFactory;
import net.sf.ehcache.constructs.blocking.UpdatingSelfPopulatingCache;
import org.junit.Test;

import org.springframework.core.io.ClassPathResource;

import static org.junit.Assert.*;

/**
 * @author Juergen Hoeller
 * @author Dmitriy Kopylenko
 * @since 27.09.2004
 */
public class EhCacheSupportTests {

	@Test
	public void testBlankCacheManager() {
		EhCacheManagerFactoryBean cacheManagerFb = new EhCacheManagerFactoryBean();
		cacheManagerFb.setCacheManagerName("myCacheManager");
		assertEquals(CacheManager.class, cacheManagerFb.getObjectType());
		assertTrue("Singleton property", cacheManagerFb.isSingleton());
		cacheManagerFb.afterPropertiesSet();
		try {
			CacheManager cm = cacheManagerFb.getObject();
			assertEquals("Loaded CacheManager with no caches", 0, cm.getCacheNames().length);
			Cache myCache1 = cm.getCache("myCache1");
			assertNull("No myCache1 defined", myCache1);
		}
		finally {
			cacheManagerFb.destroy();
		}
	}

	@Test
	public void testCacheManagerConflict() {
		EhCacheManagerFactoryBean cacheManagerFb = new EhCacheManagerFactoryBean();
		cacheManagerFb.setCacheManagerName("myCacheManager");
		assertEquals(CacheManager.class, cacheManagerFb.getObjectType());
		assertTrue("Singleton property", cacheManagerFb.isSingleton());
		cacheManagerFb.afterPropertiesSet();
		try {
			CacheManager cm = cacheManagerFb.getObject();
			assertEquals("Loaded CacheManager with no caches", 0, cm.getCacheNames().length);
			Cache myCache1 = cm.getCache("myCache1");
			assertNull("No myCache1 defined", myCache1);

			EhCacheManagerFactoryBean cacheManagerFb2 = new EhCacheManagerFactoryBean();
			cacheManagerFb2.setCacheManagerName("myCacheManager");
			cacheManagerFb2.afterPropertiesSet();
			fail("Should have thrown CacheException because of naming conflict");
		}
		catch (CacheException ex) {
			// expected
		}
		finally {
			cacheManagerFb.destroy();
		}
	}

	@Test
	public void testAcceptExistingCacheManager() {
		EhCacheManagerFactoryBean cacheManagerFb = new EhCacheManagerFactoryBean();
		cacheManagerFb.setCacheManagerName("myCacheManager");
		assertEquals(CacheManager.class, cacheManagerFb.getObjectType());
		assertTrue("Singleton property", cacheManagerFb.isSingleton());
		cacheManagerFb.afterPropertiesSet();
		try {
			CacheManager cm = cacheManagerFb.getObject();
			assertEquals("Loaded CacheManager with no caches", 0, cm.getCacheNames().length);
			Cache myCache1 = cm.getCache("myCache1");
			assertNull("No myCache1 defined", myCache1);

			EhCacheManagerFactoryBean cacheManagerFb2 = new EhCacheManagerFactoryBean();
			cacheManagerFb2.setCacheManagerName("myCacheManager");
			cacheManagerFb2.setAcceptExisting(true);
			cacheManagerFb2.afterPropertiesSet();
			CacheManager cm2 = cacheManagerFb2.getObject();
			assertSame(cm, cm2);
			cacheManagerFb2.destroy();
		}
		finally {
			cacheManagerFb.destroy();
		}
	}

	public void testCacheManagerFromConfigFile() {
		EhCacheManagerFactoryBean cacheManagerFb = new EhCacheManagerFactoryBean();
		cacheManagerFb.setConfigLocation(new ClassPathResource("testEhcache.xml", getClass()));
		cacheManagerFb.setCacheManagerName("myCacheManager");
		cacheManagerFb.afterPropertiesSet();
		try {
			CacheManager cm = cacheManagerFb.getObject();
			assertEquals("Correct number of caches loaded", 1, cm.getCacheNames().length);
			Cache myCache1 = cm.getCache("myCache1");
			assertFalse("myCache1 is not eternal", myCache1.getCacheConfiguration().isEternal());
			assertEquals("myCache1.maxElements == 300", 300, myCache1.getCacheConfiguration().getMaxEntriesLocalHeap());
		}
		finally {
			cacheManagerFb.destroy();
		}
	}

	@Test
	public void testEhCacheFactoryBeanWithDefaultCacheManager() {
		doTestEhCacheFactoryBean(false);
	}

	@Test
	public void testEhCacheFactoryBeanWithExplicitCacheManager() {
		doTestEhCacheFactoryBean(true);
	}

	private void doTestEhCacheFactoryBean(boolean useCacheManagerFb) {
		Cache cache;
		EhCacheManagerFactoryBean cacheManagerFb = null;
		boolean cacheManagerFbInitialized = false;
		try {
			EhCacheFactoryBean cacheFb = new EhCacheFactoryBean();
			Class<? extends Ehcache> objectType = cacheFb.getObjectType();
			assertTrue(Ehcache.class.isAssignableFrom(objectType));
			assertTrue("Singleton property", cacheFb.isSingleton());
			if (useCacheManagerFb) {
				cacheManagerFb = new EhCacheManagerFactoryBean();
				cacheManagerFb.setConfigLocation(new ClassPathResource("testEhcache.xml", getClass()));
				cacheManagerFb.setCacheManagerName("cache");
				cacheManagerFb.afterPropertiesSet();
				cacheManagerFbInitialized = true;
				cacheFb.setCacheManager(cacheManagerFb.getObject());
			}

			cacheFb.setCacheName("myCache1");
			cacheFb.afterPropertiesSet();
			cache = (Cache) cacheFb.getObject();
			Class<? extends Ehcache> objectType2 = cacheFb.getObjectType();
			assertSame(objectType, objectType2);
			CacheConfiguration config = cache.getCacheConfiguration();
			assertEquals("myCache1", cache.getName());
			if (useCacheManagerFb){
				assertEquals("myCache1.maxElements", 300, config.getMaxEntriesLocalHeap());
			}
			else {
				assertEquals("myCache1.maxElements", 10000, config.getMaxEntriesLocalHeap());
			}

			// Cache region is not defined. Should create one with default properties.
			cacheFb = new EhCacheFactoryBean();
			if (useCacheManagerFb) {
				cacheFb.setCacheManager(cacheManagerFb.getObject());
			}
			cacheFb.setCacheName("undefinedCache");
			cacheFb.afterPropertiesSet();
			cache = (Cache) cacheFb.getObject();
			config = cache.getCacheConfiguration();
			assertEquals("undefinedCache", cache.getName());
			assertEquals("default maxElements is correct", 10000, config.getMaxEntriesLocalHeap());
			assertFalse("default eternal is correct", config.isEternal());
			assertEquals("default timeToLive is correct", 120, config.getTimeToLiveSeconds());
			assertEquals("default timeToIdle is correct", 120, config.getTimeToIdleSeconds());
			assertEquals("default diskExpiryThreadIntervalSeconds is correct", 120,
					config.getDiskExpiryThreadIntervalSeconds());

			// overriding the default properties
			cacheFb = new EhCacheFactoryBean();
			if (useCacheManagerFb) {
				cacheFb.setCacheManager(cacheManagerFb.getObject());
			}
			cacheFb.setBeanName("undefinedCache2");
			cacheFb.setMaxEntriesLocalHeap(5);
			cacheFb.setTimeToLive(8);
			cacheFb.setTimeToIdle(7);
			cacheFb.setDiskExpiryThreadIntervalSeconds(10);
			cacheFb.afterPropertiesSet();
			cache = (Cache) cacheFb.getObject();
			config = cache.getCacheConfiguration();

			assertEquals("undefinedCache2", cache.getName());
			assertEquals("overridden maxElements is correct", 5, config.getMaxEntriesLocalHeap());
			assertEquals("default timeToLive is correct", 8, config.getTimeToLiveSeconds());
			assertEquals("default timeToIdle is correct", 7, config.getTimeToIdleSeconds());
			assertEquals("overridden diskExpiryThreadIntervalSeconds is correct", 10,
					config.getDiskExpiryThreadIntervalSeconds());
		}
		finally {
			if (cacheManagerFbInitialized) {
				cacheManagerFb.destroy();
			}
			else {
				CacheManager.getInstance().shutdown();
			}
		}
	}

	@Test
	public void testEhCacheFactoryBeanWithBlockingCache() {
		EhCacheManagerFactoryBean cacheManagerFb = new EhCacheManagerFactoryBean();
		cacheManagerFb.afterPropertiesSet();
		try {
			CacheManager cm = cacheManagerFb.getObject();
			EhCacheFactoryBean cacheFb = new EhCacheFactoryBean();
			cacheFb.setCacheManager(cm);
			cacheFb.setCacheName("myCache1");
			cacheFb.setBlocking(true);
			assertEquals(cacheFb.getObjectType(), BlockingCache.class);
			cacheFb.afterPropertiesSet();
			Ehcache myCache1 = cm.getEhcache("myCache1");
			assertTrue(myCache1 instanceof BlockingCache);
		}
		finally {
			cacheManagerFb.destroy();
		}
	}

	@Test
	public void testEhCacheFactoryBeanWithSelfPopulatingCache() {
		EhCacheManagerFactoryBean cacheManagerFb = new EhCacheManagerFactoryBean();
		cacheManagerFb.afterPropertiesSet();
		try {
			CacheManager cm = cacheManagerFb.getObject();
			EhCacheFactoryBean cacheFb = new EhCacheFactoryBean();
			cacheFb.setCacheManager(cm);
			cacheFb.setCacheName("myCache1");
			cacheFb.setCacheEntryFactory(key -> key);
			assertEquals(cacheFb.getObjectType(), SelfPopulatingCache.class);
			cacheFb.afterPropertiesSet();
			Ehcache myCache1 = cm.getEhcache("myCache1");
			assertTrue(myCache1 instanceof SelfPopulatingCache);
			assertEquals("myKey1", myCache1.get("myKey1").getObjectValue());
		}
		finally {
			cacheManagerFb.destroy();
		}
	}

	@Test
	public void testEhCacheFactoryBeanWithUpdatingSelfPopulatingCache() {
		EhCacheManagerFactoryBean cacheManagerFb = new EhCacheManagerFactoryBean();
		cacheManagerFb.afterPropertiesSet();
		try {
			CacheManager cm = cacheManagerFb.getObject();
			EhCacheFactoryBean cacheFb = new EhCacheFactoryBean();
			cacheFb.setCacheManager(cm);
			cacheFb.setCacheName("myCache1");
			cacheFb.setCacheEntryFactory(new UpdatingCacheEntryFactory() {
				@Override
				public Object createEntry(Object key) {
					return key;
				}
				@Override
				public void updateEntryValue(Object key, Object value) {
				}
			});
			assertEquals(cacheFb.getObjectType(), UpdatingSelfPopulatingCache.class);
			cacheFb.afterPropertiesSet();
			Ehcache myCache1 = cm.getEhcache("myCache1");
			assertTrue(myCache1 instanceof UpdatingSelfPopulatingCache);
			assertEquals("myKey1", myCache1.get("myKey1").getObjectValue());
		}
		finally {
			cacheManagerFb.destroy();
		}
	}

}
