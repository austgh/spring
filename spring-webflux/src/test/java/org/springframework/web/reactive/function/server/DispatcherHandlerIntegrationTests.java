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

package org.springframework.web.reactive.function.server;

import java.util.List;
import java.util.Objects;

import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.AbstractHttpHandlerIntegrationTests;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.DispatcherHandler;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;

import static org.junit.Assert.*;
import static org.springframework.web.reactive.function.BodyInserters.fromPublisher;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 * Tests the use of {@link HandlerFunction} and {@link RouterFunction} in a
 * {@link DispatcherHandler}, combined with {@link Controller}s.
 *
 * @author Arjen Poutsma
 */
public class DispatcherHandlerIntegrationTests extends AbstractHttpHandlerIntegrationTests {

	private final RestTemplate restTemplate = new RestTemplate();

	private AnnotationConfigApplicationContext wac;


	@Override
	protected HttpHandler createHttpHandler() {
		this.wac = new AnnotationConfigApplicationContext();
		this.wac.register(TestConfiguration.class);
		this.wac.refresh();

		DispatcherHandler webHandler = new DispatcherHandler();
		webHandler.setApplicationContext(this.wac);

		return WebHttpHandlerBuilder.webHandler(webHandler).build();
	}


	@Test
	public void mono() throws Exception {
		ResponseEntity<Person> result =
				this.restTemplate.getForEntity("http://localhost:" + this.port + "/mono", Person.class);

		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertEquals("John", result.getBody().getName());
	}

	@Test
	public void flux() throws Exception {
		ParameterizedTypeReference<List<Person>> reference = new ParameterizedTypeReference<List<Person>>() {};
		ResponseEntity<List<Person>> result =
				this.restTemplate
						.exchange("http://localhost:" + this.port + "/flux", HttpMethod.GET, null, reference);

		assertEquals(HttpStatus.OK, result.getStatusCode());
		List<Person> body = result.getBody();
		assertEquals(2, body.size());
		assertEquals("John", body.get(0).getName());
		assertEquals("Jane", body.get(1).getName());
	}

	@Test
	public void controller() throws Exception {
		ResponseEntity<Person> result =
				this.restTemplate.getForEntity("http://localhost:" + this.port + "/controller", Person.class);

		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertEquals("John", result.getBody().getName());
	}


	@EnableWebFlux
	@Configuration
	static class TestConfiguration {

		@Bean
		public PersonHandler personHandler() {
			return new PersonHandler();
		}

		@Bean
		public PersonController personController() {
			return new PersonController();
		}

		@Bean
		public RouterFunction<EntityResponse<Person>> monoRouterFunction(PersonHandler personHandler) {
			return route(RequestPredicates.GET("/mono"), personHandler::mono);
		}

		@Bean
		public RouterFunction<ServerResponse> fluxRouterFunction(PersonHandler personHandler) {
			return route(RequestPredicates.GET("/flux"), personHandler::flux);
		}

	}


	private static class PersonHandler {

		public Mono<EntityResponse<Person>> mono(ServerRequest request) {
			Person person = new Person("John");
			return EntityResponse.fromObject(person).build();
		}

		public Mono<ServerResponse> flux(ServerRequest request) {
			Person person1 = new Person("John");
			Person person2 = new Person("Jane");
			return ServerResponse.ok().body(
					fromPublisher(Flux.just(person1, person2), Person.class));
		}

	}

	@Controller
	private static class PersonController {

		@RequestMapping("/controller")
		@ResponseBody
		public Mono<Person> controller() {
			return Mono.just(new Person("John"));
		}
	}

	private static class Person {

		private String name;

		@SuppressWarnings("unused")
		public Person() {
		}

		public Person(String name) {
			this.name = name;
		}

		public String getName() {
			return this.name;
		}

		@SuppressWarnings("unused")
		public void setName(String name) {
			this.name = name;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			Person person = (Person) o;
			return !(!Objects.equals(this.name, person.name));
		}

		@Override
		public int hashCode() {
			return this.name != null ? this.name.hashCode() : 0;
		}

		@Override
		public String toString() {
			return "Person{" + "name='" + this.name + '\'' + '}';
		}
	}

}
