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

package org.springframework.jndi;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Logger;
import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.sql.DataSource;

import org.junit.Test;

import org.springframework.tests.mock.jndi.SimpleNamingContext;
import org.springframework.tests.mock.jndi.SimpleNamingContextBuilder;

import static org.junit.Assert.*;

/**
 * @author Juergen Hoeller
 * @author Chris Beams
 */
public class SimpleNamingContextTests {

	@Test
	public void testNamingContextBuilder() throws NamingException {
		SimpleNamingContextBuilder builder = new SimpleNamingContextBuilder();
		InitialContextFactory factory = builder.createInitialContextFactory(null);

		DataSource ds = new StubDataSource();
		builder.bind("java:comp/env/jdbc/myds", ds);
		Object obj = new Object();
		builder.bind("myobject", obj);

		Context context1 = factory.getInitialContext(null);
		assertSame("Correct DataSource registered", context1.lookup("java:comp/env/jdbc/myds"), ds);
		assertSame("Correct Object registered", context1.lookup("myobject"), obj);

		Hashtable<String, String> env2 = new Hashtable<>();
		env2.put("key1", "value1");
		Context context2 = factory.getInitialContext(env2);
		assertSame("Correct DataSource registered", context2.lookup("java:comp/env/jdbc/myds"), ds);
		assertSame("Correct Object registered", context2.lookup("myobject"), obj);
		assertNotSame("Correct environment", context2.getEnvironment(), env2);
		assertEquals("Correct key1", "value1", context2.getEnvironment().get("key1"));

		Integer i = new Integer(0);
		context1.rebind("myinteger", i);
		String s = "";
		context2.bind("mystring", s);

		Context context3 = (Context) context2.lookup("");
		context3.rename("java:comp/env/jdbc/myds", "jdbc/myds");
		context3.unbind("myobject");

		assertNotSame("Correct environment", context3.getEnvironment(), context2.getEnvironment());
		context3.addToEnvironment("key2", "value2");
		assertEquals("key2 added", "value2", context3.getEnvironment().get("key2"));
		context3.removeFromEnvironment("key1");
		assertNull("key1 removed", context3.getEnvironment().get("key1"));

		assertSame("Correct DataSource registered", context1.lookup("jdbc/myds"), ds);
		try {
			context1.lookup("myobject");
			fail("Should have thrown NameNotFoundException");
		}
		catch (NameNotFoundException ex) {
			// expected
		}
		assertSame("Correct Integer registered", context1.lookup("myinteger"), i);
		assertSame("Correct String registered", context1.lookup("mystring"), s);

		assertSame("Correct DataSource registered", context2.lookup("jdbc/myds"), ds);
		try {
			context2.lookup("myobject");
			fail("Should have thrown NameNotFoundException");
		}
		catch (NameNotFoundException ex) {
			// expected
		}
		assertSame("Correct Integer registered", context2.lookup("myinteger"), i);
		assertSame("Correct String registered", context2.lookup("mystring"), s);

		assertSame("Correct DataSource registered", context3.lookup("jdbc/myds"), ds);
		try {
			context3.lookup("myobject");
			fail("Should have thrown NameNotFoundException");
		}
		catch (NameNotFoundException ex) {
			// expected
		}
		assertSame("Correct Integer registered", context3.lookup("myinteger"), i);
		assertSame("Correct String registered", context3.lookup("mystring"), s);

		Map<String, Binding> bindingMap = new HashMap<>();
		NamingEnumeration<?> bindingEnum = context3.listBindings("");
		while (bindingEnum.hasMoreElements()) {
			Binding binding = (Binding) bindingEnum.nextElement();
			bindingMap.put(binding.getName(), binding);
		}
		assertTrue("Correct jdbc subcontext", bindingMap.get("jdbc").getObject() instanceof Context);
		assertEquals("Correct jdbc subcontext", SimpleNamingContext.class.getName(),
				bindingMap.get("jdbc").getClassName());

		Context jdbcContext = (Context) context3.lookup("jdbc");
		jdbcContext.bind("mydsX", ds);
		Map<String, Binding> subBindingMap = new HashMap<>();
		NamingEnumeration<?> subBindingEnum = jdbcContext.listBindings("");
		while (subBindingEnum.hasMoreElements()) {
			Binding binding = (Binding) subBindingEnum.nextElement();
			subBindingMap.put(binding.getName(), binding);
		}

		assertEquals("Correct DataSource registered", ds, subBindingMap.get("myds").getObject());
		assertEquals("Correct DataSource registered", StubDataSource.class.getName(),
				subBindingMap.get("myds").getClassName());
		assertEquals("Correct DataSource registered", ds, subBindingMap.get("mydsX").getObject());
		assertEquals("Correct DataSource registered", StubDataSource.class.getName(),
				subBindingMap.get("mydsX").getClassName());
		assertEquals("Correct Integer registered", i, bindingMap.get("myinteger").getObject());
		assertEquals("Correct Integer registered", Integer.class.getName(), bindingMap.get("myinteger").getClassName());
		assertEquals("Correct String registered", s, bindingMap.get("mystring").getObject());
		assertEquals("Correct String registered", String.class.getName(), bindingMap.get("mystring").getClassName());

		context1.createSubcontext("jdbc").bind("sub/subds", ds);

		Map<String, String> pairMap = new HashMap<>();
		NamingEnumeration<?> pairEnum = context2.list("jdbc");
		while (pairEnum.hasMore()) {
			NameClassPair pair = (NameClassPair) pairEnum.next();
			pairMap.put(pair.getName(), pair.getClassName());
		}
		assertEquals("Correct sub subcontext", SimpleNamingContext.class.getName(), pairMap.get("sub"));

		Context subContext = (Context) context2.lookup("jdbc/sub");
		Map<String, String> subPairMap = new HashMap<>();
		NamingEnumeration<?> subPairEnum = subContext.list("");
		while (subPairEnum.hasMoreElements()) {
			NameClassPair pair = (NameClassPair) subPairEnum.next();
			subPairMap.put(pair.getName(), pair.getClassName());
		}

		assertEquals("Correct DataSource registered", StubDataSource.class.getName(), subPairMap.get("subds"));
		assertEquals("Correct DataSource registered", StubDataSource.class.getName(), pairMap.get("myds"));
		assertEquals("Correct DataSource registered", StubDataSource.class.getName(), pairMap.get("mydsX"));

		pairMap.clear();
		pairEnum = context1.list("jdbc/");
		while (pairEnum.hasMore()) {
			NameClassPair pair = (NameClassPair) pairEnum.next();
			pairMap.put(pair.getName(), pair.getClassName());
		}
		assertEquals("Correct DataSource registered", StubDataSource.class.getName(), pairMap.get("myds"));
		assertEquals("Correct DataSource registered", StubDataSource.class.getName(), pairMap.get("mydsX"));
	}

	/**
	 * Demonstrates how emptyActivatedContextBuilder() method can be
	 * used repeatedly, and how it affects creating a new InitialContext()
	 */
	@Test
	public void testCreateInitialContext() throws Exception {
		SimpleNamingContextBuilder builder = SimpleNamingContextBuilder.emptyActivatedContextBuilder();
		String name = "foo";
		Object o = new Object();
		builder.bind(name, o);
		// Check it affects JNDI
		Context ctx = new InitialContext();
		assertSame(ctx.lookup(name), o);
		// Check it returns mutable contexts
		ctx.unbind(name);
		try {
			ctx = new InitialContext();
			ctx.lookup(name);
			fail("Should have thrown NamingException");
		}
		catch (NamingException ex) {
			// expected
		}

		// Check the same call will work again, but the context is empty
		builder = SimpleNamingContextBuilder.emptyActivatedContextBuilder();
		try {
			ctx = new InitialContext();
			ctx.lookup(name);
			fail("Should have thrown NamingException");
		}
		catch (NamingException ex) {
			// expected
		}
		Object o2 = new Object();
		builder.bind(name, o2);
		assertEquals(ctx.lookup(name), o2);
	}


	static class StubDataSource implements DataSource {

		@Override
		public Connection getConnection() throws SQLException {
			return null;
		}

		@Override
		public Connection getConnection(String username, String password) throws SQLException {
			return null;
		}

		@Override
		public PrintWriter getLogWriter() throws SQLException {
			return null;
		}

		@Override
		public int getLoginTimeout() throws SQLException {
			return 0;
		}

		@Override
		public void setLogWriter(PrintWriter arg0) throws SQLException {

		}

		@Override
		public void setLoginTimeout(int arg0) throws SQLException {

		}

		@Override
		public boolean isWrapperFor(Class<?> arg0) throws SQLException {
			return false;
		}

		@Override
		public <T> T unwrap(Class<T> arg0) throws SQLException {
			return null;
		}

		@Override
		public Logger getParentLogger() {
			return null;
		}
	}

}
