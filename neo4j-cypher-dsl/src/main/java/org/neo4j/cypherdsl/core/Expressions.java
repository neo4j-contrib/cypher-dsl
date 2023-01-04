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

import static org.apiguardian.api.API.Status.STABLE;

import java.util.Arrays;
import java.util.List;

import org.apiguardian.api.API;
import org.jetbrains.annotations.NotNull;

/**
 * Utility methods for dealing with expressions.
 *
 * @author Michael J. Simons
 * @since 1.0
 */
@API(status = STABLE, since = "1.0")
public final class Expressions {

	@NotNull
	public static CountExpression count(PatternElement patternElement) {
		return new CountExpression(new Pattern(List.of(patternElement)), null);
	}

	public static CountExpression count(Statement.UnionQuery union) {
		return new CountExpression(union, null);
	}

	/**
	 * @param expression Possibly named with a non-empty symbolic name.
	 * @param <T>        The type being returned
	 * @return The name of the expression if the expression is named or the expression itself.
	 */
	static <T extends Expression> Expression nameOrExpression(T expression) {

		if (expression instanceof Named) {
			return ((Named) expression).getSymbolicName().map(Expression.class::cast).orElse(expression);
		} else {
			return expression;
		}
	}

	static Expression[] createSymbolicNames(String[] variables) {
		return Arrays.stream(variables).map(SymbolicName::of).toArray(Expression[]::new);
	}

	static Expression[] createSymbolicNames(Named[] variables) {
		return Arrays.stream(variables).map(Named::getRequiredSymbolicName)
			.toArray(Expression[]::new);
	}

	static Expression[] createSymbolicNames(IdentifiableElement[] variables) {
		return Arrays.stream(variables).map(IdentifiableElement::asExpression).toArray(Expression[]::new);
	}

	static String format(Expression expression) {

		if (expression instanceof Named) {
			return ((Named) expression).getRequiredSymbolicName().getValue();
		} else if (expression instanceof AliasedExpression) {
			return ((AliasedExpression) expression).getAlias();
		} else if (expression instanceof SymbolicName) {
			return ((SymbolicName) expression).getValue();
		} else if (expression instanceof Property) {
			StringBuilder ref = new StringBuilder();
			expression.accept(segment -> {
				if (segment instanceof SymbolicName) {
					if (ref.length() > 0) {
						ref.append(".");
					}
					ref.append(((SymbolicName) segment).getValue());

				}
			});
			return ref.toString();
		}

		throw new IllegalArgumentException("Cannot format expression " + expression.toString());
	}

	private Expressions() {
	}
}
