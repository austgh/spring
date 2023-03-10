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

package org.springframework.http.converter.protobuf;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import org.junit.Before;
import org.junit.Test;

import org.springframework.http.MediaType;
import org.springframework.http.MockHttpInputMessage;
import org.springframework.http.MockHttpOutputMessage;
import org.springframework.protobuf.Msg;
import org.springframework.protobuf.SecondMsg;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Test suite for {@link ProtobufHttpMessageConverter}.
 *
 * @author Alex Antonov
 * @author Juergen Hoeller
 * @author Andreas Ahlenstorf
 */
public class ProtobufHttpMessageConverterTests {

	private ProtobufHttpMessageConverter converter;

	private ExtensionRegistryInitializer registryInitializer;

	private Msg testMsg;


	@Before
	public void setup() {
		this.registryInitializer = mock(ExtensionRegistryInitializer.class);
		this.converter = new ProtobufHttpMessageConverter(this.registryInitializer);
		this.testMsg = Msg.newBuilder().setFoo("Foo").setBlah(SecondMsg.newBuilder().setBlah(123).build()).build();
	}


	@Test
	public void extensionRegistryInitialized() {
	    verify(this.registryInitializer, times(1)).initializeExtensionRegistry(any());
	}

	@Test
	public void extensionRegistryNull() {
		new ProtobufHttpMessageConverter(null);
	}

	@Test
	public void canRead() {
		assertTrue(this.converter.canRead(Msg.class, null));
		assertTrue(this.converter.canRead(Msg.class, ProtobufHttpMessageConverter.PROTOBUF));
		assertTrue(this.converter.canRead(Msg.class, MediaType.APPLICATION_JSON));
		assertTrue(this.converter.canRead(Msg.class, MediaType.APPLICATION_XML));
		assertTrue(this.converter.canRead(Msg.class, MediaType.TEXT_PLAIN));

		// only supported as an output format
		assertFalse(this.converter.canRead(Msg.class, MediaType.TEXT_HTML));
	}

	@Test
	public void canWrite() {
		assertTrue(this.converter.canWrite(Msg.class, null));
		assertTrue(this.converter.canWrite(Msg.class, ProtobufHttpMessageConverter.PROTOBUF));
		assertTrue(this.converter.canWrite(Msg.class, MediaType.APPLICATION_JSON));
		assertTrue(this.converter.canWrite(Msg.class, MediaType.APPLICATION_XML));
		assertTrue(this.converter.canWrite(Msg.class, MediaType.TEXT_PLAIN));
		assertTrue(this.converter.canWrite(Msg.class, MediaType.TEXT_HTML));
	}

	@Test
	public void read() throws IOException {
		byte[] body = this.testMsg.toByteArray();
		MockHttpInputMessage inputMessage = new MockHttpInputMessage(body);
		inputMessage.getHeaders().setContentType(ProtobufHttpMessageConverter.PROTOBUF);
		Message result = this.converter.read(Msg.class, inputMessage);
		assertEquals(this.testMsg, result);
	}

	@Test
	public void readNoContentType() throws IOException {
		byte[] body = this.testMsg.toByteArray();
		MockHttpInputMessage inputMessage = new MockHttpInputMessage(body);
		Message result = this.converter.read(Msg.class, inputMessage);
		assertEquals(this.testMsg, result);
	}

	@Test
	public void writeProtobuf() throws IOException {
		MockHttpOutputMessage outputMessage = new MockHttpOutputMessage();
		MediaType contentType = ProtobufHttpMessageConverter.PROTOBUF;
		this.converter.write(this.testMsg, contentType, outputMessage);
		assertEquals(contentType, outputMessage.getHeaders().getContentType());
		assertTrue(outputMessage.getBodyAsBytes().length > 0);
		Message result = Msg.parseFrom(outputMessage.getBodyAsBytes());
		assertEquals(this.testMsg, result);

		String messageHeader =
				outputMessage.getHeaders().getFirst(ProtobufHttpMessageConverter.X_PROTOBUF_MESSAGE_HEADER);
		assertEquals("Msg", messageHeader);
		String schemaHeader =
				outputMessage.getHeaders().getFirst(ProtobufHttpMessageConverter.X_PROTOBUF_SCHEMA_HEADER);
		assertEquals("sample.proto", schemaHeader);
	}

	@Test
	public void writeJsonWithGoogleProtobuf() throws IOException {
		this.converter = new ProtobufHttpMessageConverter(
				new ProtobufHttpMessageConverter.ProtobufJavaUtilSupport(null, null),
				this.registryInitializer);
		MockHttpOutputMessage outputMessage = new MockHttpOutputMessage();
		MediaType contentType = MediaType.APPLICATION_JSON_UTF8;
		this.converter.write(this.testMsg, contentType, outputMessage);

		assertEquals(contentType, outputMessage.getHeaders().getContentType());

		final String body = outputMessage.getBodyAsString(StandardCharsets.UTF_8);
		assertFalse("body is empty", body.isEmpty());

		Msg.Builder builder = Msg.newBuilder();
		JsonFormat.parser().merge(body, builder);
		assertEquals(this.testMsg, builder.build());

		assertNull(outputMessage.getHeaders().getFirst(
				ProtobufHttpMessageConverter.X_PROTOBUF_MESSAGE_HEADER));
		assertNull(outputMessage.getHeaders().getFirst(
				ProtobufHttpMessageConverter.X_PROTOBUF_SCHEMA_HEADER));
	}

	@Test
	public void writeJsonWithJavaFormat() throws IOException {
		this.converter = new ProtobufHttpMessageConverter(
				new ProtobufHttpMessageConverter.ProtobufJavaFormatSupport(),
				this.registryInitializer);
		MockHttpOutputMessage outputMessage = new MockHttpOutputMessage();
		MediaType contentType = MediaType.APPLICATION_JSON_UTF8;
		this.converter.write(this.testMsg, contentType, outputMessage);

		assertEquals(contentType, outputMessage.getHeaders().getContentType());

		final String body = outputMessage.getBodyAsString(StandardCharsets.UTF_8);
		assertFalse("body is empty", body.isEmpty());

		Msg.Builder builder = Msg.newBuilder();
		JsonFormat.parser().merge(body, builder);
		assertEquals(this.testMsg, builder.build());

		assertNull(outputMessage.getHeaders().getFirst(
				ProtobufHttpMessageConverter.X_PROTOBUF_MESSAGE_HEADER));
		assertNull(outputMessage.getHeaders().getFirst(
				ProtobufHttpMessageConverter.X_PROTOBUF_SCHEMA_HEADER));
	}

	@Test
	public void defaultContentType() throws Exception {
		assertEquals(ProtobufHttpMessageConverter.PROTOBUF, this.converter.getDefaultContentType(this.testMsg));
	}

	@Test
	public void getContentLength() throws Exception {
		MockHttpOutputMessage outputMessage = new MockHttpOutputMessage();
		MediaType contentType = ProtobufHttpMessageConverter.PROTOBUF;
		this.converter.write(this.testMsg, contentType, outputMessage);
		assertEquals(-1, outputMessage.getHeaders().getContentLength());
	}

}
