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
package org.neo4j.cypherdsl.core;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.util.Collection;
import java.util.Map;

import org.apiguardian.api.API;
import org.neo4j.cypherdsl.core.support.Visitable;

/**
 * Shall be the common interfaces for queries that we support.
 * <p>
 * For reference see: <a href="https://s3.amazonaws.com/artifacts.opencypher.org/railroad/Cypher.html">Cypher</a>.
 * We have skipped the intermediate "Query" structure so a statement in the context of this generator is either a
 * {@link RegularQuery} or a {@code StandaloneCall}.
 *
 * @author Michael J. Simons
 * @since 1.0
 */
@API(status = EXPERIMENTAL, since = "1.0")
public interface Statement extends Visitable {

	/**
	 * @return A new statement builder.
	 */
	static StatementBuilder builder() {

		return new DefaultStatementBuilder();
	}

	/**
	 * @param namespaceAndProcedure The fully qualified name of a stored procedure. Each part can be given as a separate
	 *                              String, but a fully qualified String is ok as well.
	 * @return An entry point into a statement that starts with a call to stored procedure.
	 */
	static ProcedureCall.OngoingStandaloneCallWithoutArguments call(String... namespaceAndProcedure) {

		return new ProcedureCall.StandaloneCallBuilder(ProcedureName.from(namespaceAndProcedure));
	}

	/**
	 * After a statement has been build, all parameters that have been added to the statement can be retrieved through
	 * this method. The result will only contain parameters with a defined value. If you are interested in all parameter
	 * names, use {@link #getParameterNames()}.
	 * <p>
	 * The map can be used for example as an argument with various methods on the Neo4j Java Driver that allow the
	 * execution of parameterized queries.
	 *
	 * @return A map of all parameters with a bound value.
	 * @since 2021.0.0
	 */
	Map<String, Object> getParameters();

	/**
	 * After the statement has been build, this method returns a list of all parameter names used, regardless whether
	 * a value was bound to the parameter o not.
	 * @return A set of parameter names being used.
	 * @since 2021.0.0
	 */
	Collection<String> getParameterNames();

	/**
	 * Represents {@code RegularQuery}.
	 *
	 * @since 1.0
	 */
	interface RegularQuery extends Statement {
	}

	/**
	 * Represents a {@code SingleQuery}.
	 *
	 * @since 1.0
	 */
	interface SingleQuery extends RegularQuery {
	}
}
