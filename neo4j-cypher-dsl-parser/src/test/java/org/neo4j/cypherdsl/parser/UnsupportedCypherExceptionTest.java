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
package org.neo4j.cypherdsl.parser;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * @author Michael J. Simons
 */
class UnsupportedCypherExceptionTest {

	@Test
	void causeShouldBePresent() {
		var cause = new UnsupportedOperationException("booms");
		var unsupportedCypherException = new UnsupportedCypherException("This is invalid cypher", cause);
		assertThat(unsupportedCypherException.getCause()).isEqualTo(cause);
	}

	@Test
	void inputShouldBeRetrievable() {
		var cause = new UnsupportedOperationException("booms");
		var input = "This is invalid cypher";
		var unsupportedCypherException = new UnsupportedCypherException(input, cause);
		assertThat(unsupportedCypherException.getInput()).isEqualTo(input);
	}

	@Test
	void messageShouldBeFormatted() {
		var cause = new UnsupportedOperationException("booms");
		var input = "This is invalid cypher";
		var unsupportedCypherException = new UnsupportedCypherException(input, cause);

		assertThat(unsupportedCypherException.getMessage()).isEqualTo("""
			You used one Cypher construct not yet supported by the Cypher-DSL:

			\tThis is invalid cypher

			Feel free to open an issue so that we might add support for it at https://github.com/neo4j-contrib/cypher-dsl/issues/new"""
		);
	}
}
