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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.commons.util.ReflectionUtils;
import org.mockito.Answers;

/**
 * @author Michael J. Simons
 */
class FunctionsTest {

	private static final String FUNCTION_NAME_FIELD = "functionName";

	@Nested
	class coalesce {

		@Test
		void preconditionsShouldBeAsserted() {

			String message = "The expression for coalesce() is required.";
			assertThatIllegalArgumentException().isThrownBy(() -> Functions.coalesce((Expression[]) null))
				.withMessage(message);

			assertThatIllegalArgumentException().isThrownBy(() -> Functions.coalesce(new Expression[0]))
				.withMessage(message);

			assertThatIllegalArgumentException().isThrownBy(() -> Functions.coalesce(new Expression[] { null }))
				.withMessage(message);
		}

		@Test
		void shouldCreateCorrectInvocation() {

			FunctionInvocation invocation = Functions.coalesce(mock(Expression.class));
			assertThat(invocation).hasFieldOrPropertyWithValue(FUNCTION_NAME_FIELD, "coalesce");
		}
	}

	private static Stream<Arguments> functionsToTest() {

		return ReflectionUtils
			.findMethods(
				Functions.class,
				method -> method.getParameterCount() == 1 && (
					method.getParameterTypes()[0].isAssignableFrom(Expression.class) ||
					method.getParameterTypes()[0].isAssignableFrom(Node.class) ||
					method.getParameterTypes()[0].isAssignableFrom(Relationship.class) ||
					method.getParameterTypes()[0].isAssignableFrom(MapExpression.class)
				) && (!java.util.Set.of("properties", "expressionOrNullLit").contains(method.getName()) || !method.getParameterTypes()[0].isAssignableFrom(MapExpression.class)),
				ReflectionUtils.HierarchyTraversalMode.TOP_DOWN
			)
			.stream().map(method -> Arguments.of(Named.of(method.getName(), method)));
	}

	@ParameterizedTest(name = "{index}: {0}")
	@MethodSource("functionsToTest")
	void preconditionsShouldBeAsserted(Method method) {
		assertThatIllegalArgumentException()
			.isThrownBy(() -> TestUtils.invokeMethod(method, null, (Expression) null))
			.withMessageMatching("The (expression|node|relationship|temporalAmount|temporalValue|variable|pattern|components) (?:for .* )?(?:is|are) required.");
	}

	@ParameterizedTest
	@MethodSource("functionsToTest")
	void functionInvocationsShouldBeCreated(Method method) {

		var expectedValue = method.getName().replace("Distinct", "");
		if (expectedValue.startsWith("graph")) {
			expectedValue = "graph." + expectedValue.substring(5, 6).toLowerCase(Locale.ROOT) + expectedValue.substring(6);
		}

		Class<?> parameterType = method.getParameterTypes()[0];
		Object mock = mock(parameterType, Answers.RETURNS_DEEP_STUBS);
		if (mock instanceof Relationship relationship) {
			when(relationship.getSymbolicName()).thenReturn(Optional.of(SymbolicName.of("x")));
		}
		FunctionInvocation invocation = (FunctionInvocation) TestUtils
			.invokeMethod(method, null, mock);
		assertThat(invocation).hasFieldOrPropertyWithValue(FUNCTION_NAME_FIELD, expectedValue);
	}
}
