/*
 * Copyright (c) 2019-2023 "Neo4j,"
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

import java.util.Collection;

import org.neo4j.cypherdsl.core.ast.Visitable;
import org.neo4j.cypherdsl.core.internal.ReflectiveVisitor;
import org.neo4j.cypherdsl.core.internal.ScopingStrategy;

/**
 * @author Michael J. Simons
 * @soundtrack Knorkator - The Schlechtst Of Knorkator
 * @since 2021.3.2
 */
final class IdentifiableExpressionCollectingVisitor extends ReflectiveVisitor {

	private final ScopingStrategy scopingStrategy = new ScopingStrategy();

	@Override
	protected boolean preEnter(Visitable visitable) {
		scopingStrategy.doEnter(visitable);
		return true;
	}

	@Override
	protected void postLeave(Visitable visitable) {
		scopingStrategy.doLeave(visitable);
	}

	Collection<Expression> getResult() {
		return this.scopingStrategy.getIdentifiables();
	}
}
