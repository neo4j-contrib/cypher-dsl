/*
 * Copyright (c) "Neo4j"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.cypherdsl.examples.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.neo4j.cypherdsl.core.AliasedExpression;
import org.neo4j.cypherdsl.core.Clauses;
import org.neo4j.cypherdsl.core.Cypher;
import org.neo4j.cypherdsl.core.Expression;
import org.neo4j.cypherdsl.core.Return;
import org.neo4j.cypherdsl.core.SymbolicName;
// tag::main-entry-point[]
import org.neo4j.cypherdsl.parser.CypherParser;
// end::main-entry-point[]
import org.neo4j.cypherdsl.parser.ExpressionCreatedEventType;
import org.neo4j.cypherdsl.parser.Options;
import org.neo4j.cypherdsl.parser.ReturnDefinition;

/**
 * @author Michael J. Simons
 */
class CypherDSLParserExamplesTest {

	@Test
	void createASubqueryCallWithUserProvidedCypher() {

		// tag::example-using-input[]
		var userProvidedCypher
			= "MATCH (this)-[:LINK]-(o:Other) RETURN o as result"; // <.>
		var userStatement = CypherParser.parse(userProvidedCypher); // <.>

		var node = Cypher.node("Node").named("node");
		var result = Cypher.name("result");
		var cypher = Cypher // <.>
			.match(node)
			.call(// <.>
				userStatement,
				node.as("this")
			)
			.returning(result.project("foo", "bar"))
			.build()
			.getCypher();

		assertThat(cypher).isEqualTo(
			"MATCH (node:`Node`) "
			+ "CALL {"
			+ "WITH node "
			+ "WITH node AS this " // <.>
			+ "MATCH (this)-[:`LINK`]-(o:`Other`) RETURN o AS result" // <.>
			+ "} "
			+ "RETURN result{.foo, .bar}");
		// end::example-using-input[]
	}

	@Test
	void ensureAReturnAlias() {

		// tag::example-required-alias[]
		var userProvidedCypher = "MATCH (this)-[:LINK]-(o:Other) RETURN o";

		Function<Expression, AliasedExpression> ensureAlias = r -> {
			if (!(r instanceof AliasedExpression)) {
				return r.as("result");
			}
			return (AliasedExpression) r;
		}; // <.>

		var options = Options.newOptions() // <.>
			.withCallback( // <.>
				ExpressionCreatedEventType.ON_RETURN_ITEM,
				AliasedExpression.class,
				ensureAlias
			)
			.build();

		var userStatement = CypherParser.parse(userProvidedCypher, options); // <.>
		// end::example-required-alias[]

		var node = Cypher.node("Node").named("node");
		var result = Cypher.name("result");
		var cypher = Cypher
			.match(node)
			.call(userStatement, node.as("this")) // <.>
			.returning(result.project("foo", "bar"))
			.build()
			.getCypher();

		assertThat(cypher).isEqualTo(
			"MATCH (node:`Node`) CALL {WITH node WITH node AS this MATCH (this)-[:`LINK`]-(o:`Other`) RETURN o AS result} RETURN result{.foo, .bar}");
	}

	@Test
	void preventPropertyDeletion() {

		// tag::example-preventing-things[]
		var userProvidedCypher = "MATCH (this)-[:LINK]-(o:Other) REMOVE this.something RETURN o";

		UnaryOperator<Expression> preventPropertyDeletion = r -> {
			throw new RuntimeException("Not allowed to remove properties!"); // <.>
		};

		var options = Options.newOptions()
			.withCallback( // <.>
				ExpressionCreatedEventType.ON_REMOVE_PROPERTY,
				Expression.class,
				preventPropertyDeletion
			)
			.build();

		assertThatExceptionOfType(RuntimeException.class)
			.isThrownBy(() -> CypherParser.parse(userProvidedCypher, options)); // <.>
		// end::example-preventing-things[]
	}

	@Test
	void modifyReturnClause() {

		// tag::example-shape-the-return-clause[]
		var userProvidedCypher = "MATCH (this)-[:LINK]-(o:Other) RETURN distinct this, o LIMIT 23";

		Function<ReturnDefinition, Return> returnClauseFactory = d -> { // <.>
			var finalExpressionsReturned = d.getExpressions().stream()
				.filter(e -> e instanceof SymbolicName && "o".equals(((SymbolicName) e).getValue()))
				.map(e -> e.as("result"))
				.collect(Collectors.<Expression>toList());

			return Clauses.returning(
				false,
				finalExpressionsReturned,
				List.of(Cypher.name("o").property("x").descending()),
				d.getOptionalSkip(), d.getOptionalLimit()
			);
		};

		var options = Options.newOptions()
			.withReturnClauseFactory(returnClauseFactory) // <.>
			.build();

		var userStatement = CypherParser.parse(userProvidedCypher, options);
		var cypher = userStatement.getCypher();

		assertThat(cypher) // <.>
			.isEqualTo("MATCH (this)-[:`LINK`]-(o:`Other`) RETURN o AS result ORDER BY o.x DESC LIMIT 23");
		// end::example-shape-the-return-clause[]
	}
}
