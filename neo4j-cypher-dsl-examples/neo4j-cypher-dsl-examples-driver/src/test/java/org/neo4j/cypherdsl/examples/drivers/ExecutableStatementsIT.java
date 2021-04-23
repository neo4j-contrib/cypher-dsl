/*
 * Copyright (c) 2019-2021 "Neo4j,"
 * Neo4j Sweden AB [https://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.cypherdsl.examples.drivers;

import static org.assertj.core.api.Assertions.assertThat;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.cypherdsl.core.Cypher;
import org.neo4j.cypherdsl.core.ResultStatement;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.reactive.RxSession;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * @author Michael J. Simons
 * @soundtrack Motörhead - Live At Brixton Academy
 */
@Testcontainers(disabledWithoutDocker = true)
class ExecutableStatementsIT {

	@Container
	private static final Neo4jContainer<?> neo4j = new Neo4jContainer<>("neo4j:4.2")
		.withReuse(true);

	private static Driver driver;

	@BeforeAll
	static void loadMovies() throws IOException, URISyntaxException {

		// tag::create-driver[]
		driver = GraphDatabase.driver(neo4j.getBoltUrl(), AuthTokens.basic("neo4j", neo4j.getAdminPassword()));
		// end::create-driver[]
		try (var session = driver.session()) {
			var movies = Files.readString(Path.of(ExecutableStatementsIT.class.getResource("/movies.cypher").toURI()));
			session.run(movies);
		}
	}

	@AfterAll
	static void closeDriver() {
		driver.close();
	}

	@Test
	void statementWithoutResult() {

		// tag::statement-without-result[]
		var m = Cypher.node("Test").named("m");
		var statement = Cypher.create(m)
			.set(m.property("name").to(Cypher.anonParameter("statementWithoutResult")))
			.build();
		// end::statement-without-result[]

		// tag::statement-without-result-unmanaged-tx[]
		try (var session = driver.session();
			var tx = session.beginTransaction() // <.>
		) {
			var summary = statement.executeWith(tx); // <.>
			tx.commit(); // <.>

			assertThat(summary.counters().nodesCreated()).isEqualTo(1);
		}
		// end::statement-without-result-unmanaged-tx[]
	}

	@Test
	void statementWithoutResultWithTxFunction() {

		var m = Cypher.node("Test").named("m");
		var statement = Cypher.create(m)
			.set(m.property("name").to(Cypher.anonParameter("statementWithoutResultWithTxFunction")))
			.build();

		// tag::statement-without-result-tx-function[]
		try (var session = driver.session()) { // <.>
			var summary = session.writeTransaction(statement::executeWith); // <.>
			assertThat(summary.counters().nodesCreated()).isEqualTo(1); // <.>
		}
		// end::statement-without-result-tx-function[]
	}

	@Test
	void statementsWithResultListed() {

		// tag::statement-with-result[]
		var m = Cypher.node("Movie").named("m");
		var statement = Cypher.match(m).returning(m.property("title").as("title")).build();
		// end::statement-with-result[]

		// tag::statement-with-result-unmanaged-tx[]
		try (var session = driver.session();
			var tx = session.beginTransaction()) {

			var moviesTitles = statement.fetchWith(
				tx, // <.>
				r -> r.get("title").asString() // <.>
			);
			tx.commit();

			assertMovieTitleList(moviesTitles);
		}
		// end::statement-with-result-unmanaged-tx[]
	}

	@Test
	void statementsWithResultListedWithTxFunction() {

		var m = Cypher.node("Movie").named("m");
		var statement = Cypher.match(m).returning(m.property("title").as("title")).build();

		// tag::statement-with-result-tx-function[]
		try (var session = driver.session()) { // <.>

			var moviesTitles = session.readTransaction(statement::fetchWith) // <.>
				.stream() // <.>
				.map(r -> r.get("title").asString())
				.collect(Collectors.toList());

			assertMovieTitleList(moviesTitles);
		}
		// end::statement-with-result-tx-function[]
	}

	@Test
	void statementsWithResultStreamed() {

		var m = Cypher.node("Movie").named("m");
		var statement = Cypher.match(m).returning(m.property("title").as("title")).build();

		// tag::statement-with-result-auto-commit-stream[]
		try (var session = driver.session()) {

			var summary = statement.streamWith(session, stream -> {
				var moviesTitles = stream
					.map(r -> r.get("title").asString())
					.collect(Collectors.toList());
				assertMovieTitleList(moviesTitles);
			});
			assertThat(summary.query().text()).isEqualTo(statement.getCypher());
		}
		// end::statement-with-result-auto-commit-stream[]
	}

	@Test
	void statementsWithResultStreamedInTxFunction() {

		var m = Cypher.node("Movie").named("m");
		var statement = Cypher.match(m).returning(m.property("title").as("title")).build();

		// tag::statement-with-result-tx-function-stream[]
		try (var session = driver.session()) { // <.>

			var summary = session.readTransaction(tx -> // <.>
				statement.streamWith(tx, s -> { // <.>
					var moviesTitles = s.map(r -> r.get("title").asString())
						.collect(Collectors.toList()); // <.>
					assertMovieTitleList(moviesTitles);
				})
			);
			assertThat(summary.query().text()).isEqualTo(statement.getCypher());
		}
		// end::statement-with-result-tx-function-stream[]
	}

	@Test
	void statementsWithResultStreamedProfiled() {

		var m = Cypher.node("Movie").named("m");
		var statement = (ResultStatement) Cypher.match(m).returning(m.property("title").as("title")).profile();

		try (var session = driver.session()) {

			var summary = statement.streamWith(session, s -> {
				// We don't consume the stream but only are interested in the profile
			});
			assertThat(summary.query().text()).isEqualTo(statement.getCypher());
			assertThat(summary.profile()).isNotNull();
		}
	}

	@Test
	void statementsWithoutResultReactive() {

		var m = Cypher.node("Test").named("m");
		var statement = Cypher.create(m)
			.set(m.property("name").to(Cypher.anonParameter("statementsWithoutResultReactive")))
			.build();

		// tag::statement-without-result-tx-function-reactive-stream[]
		Mono.usingWhen(
			Mono.fromSupplier(driver::rxSession), // <.>
			s -> Mono.fromDirect(s.writeTransaction(statement::executeWith)), // <.>
			RxSession::close // <.>
		).as(StepVerifier::create)
			.expectNextMatches(r -> r.counters().nodesCreated() == 1) // <.>
			.verifyComplete();
		// end::statement-without-result-tx-function-reactive-stream[]
	}

	@Test
	void statementsWithResultReactive() {

		var m = Cypher.node("Movie").named("m");
		var statement = Cypher.match(m).returning(m.property("title").as("title")).build();

		Flux.usingWhen(Mono.fromSupplier(driver::rxSession), statement::fetchWith, RxSession::close)
			.as(StepVerifier::create)
			.expectNextCount(38)
			.verifyComplete();
	}

	@Test
	void statementsWithResultReactiveTxFunction() {

		var m = Cypher.node("Movie").named("m");
		var statement = Cypher.match(m).returning(m.property("title").as("title")).build();

		// tag::statement-with-result-tx-function-reactive-stream[]
		Flux.usingWhen(
			Mono.fromSupplier(driver::rxSession),
			s -> s.readTransaction(statement::fetchWith),
			RxSession::close
		)
			.skip(2)
			.take(30)
			.as(StepVerifier::create)
			.expectNextCount(30)
			.verifyComplete();
		// end::statement-with-result-tx-function-reactive-stream[]
	}

	void assertMovieTitleList(List<String> movieTitles) {
		assertThat(movieTitles).hasSize(38);
		assertThat(movieTitles).contains("The Matrix");
	}
}
