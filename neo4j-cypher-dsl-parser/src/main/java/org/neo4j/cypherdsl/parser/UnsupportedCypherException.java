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

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import org.apiguardian.api.API;

/**
 * Thrown when the parser detects a clause which is not yet supported.
 *
 * @author Michael J. Simons
 * @since 2021.3.0
 */
@API(status = EXPERIMENTAL, since = "2021.3.0")
public final class UnsupportedCypherException extends UnsupportedOperationException {

	private final String input;

	UnsupportedCypherException(String input) {
		super(String.format("You used one Cypher construct not yet supported by the Cypher-DSL:%n%n\t%s%n%n" +
							"Feel free to open an issue so that we might add support for it at https://github.com/neo4j-contrib/cypher-dsl/issues/new",
			input));
		this.input = input;
	}

	/**
	 * @return The original Cypher input handled to the parser.
	 */
	public String getInput() {
		return input;
	}
}
