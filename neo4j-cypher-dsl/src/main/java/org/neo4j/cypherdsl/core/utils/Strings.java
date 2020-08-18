/*
 * Copyright (c) 2019-2020 "Neo4j,"
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
package org.neo4j.cypherdsl.core.utils;

import java.util.concurrent.ThreadLocalRandom;

import org.apiguardian.api.API;

/**
 * The usual, static class with helper methods centered around missing functionality in {@link String}.
 * Not supported for external use in anyway.
 *
 * @author Michael J. Simons
 * @soundtrack Genesis - We Can't Dance
 * @since 2020.1.0
 */
@API(status = API.Status.INTERNAL, since = "2020.1.0")
public final class Strings {

	public static boolean hasText(String str) {
		return (str != null && !str.isEmpty() && containsText(str));
	}

	public static String randomIdentifier(int length) {

		int leftLimit = 65; // letter 'A'
		int rightLimit = 122; // letter 'z'
		ThreadLocalRandom random = ThreadLocalRandom.current();

		return random.ints(leftLimit, rightLimit + 1)
			.filter(Character::isLetter)
			.limit(length)
			.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
			.toString();
	}

	/**
	 * This is a literal copy of {@link javax.lang.model.SourceVersion#isIdentifier(CharSequence)} included here to
	 * be not dependent on the compiler module.
	 *
	 * @param name A possible Java identifier
	 * @return True, if {@code name} represents an identifier.
	 */
	public static boolean isIdentifier(CharSequence name) {
		String id = name.toString();

		if (id.length() == 0) {
			return false;
		}
		int cp = id.codePointAt(0);
		if (!Character.isJavaIdentifierStart(cp)) {
			return false;
		}
		for (int i = Character.charCount(cp);
			 i < id.length();
			 i += Character.charCount(cp)) {
			cp = id.codePointAt(i);
			if (!Character.isJavaIdentifierPart(cp)) {
				return false;
			}
		}
		return true;
	}

	private static boolean containsText(CharSequence str) {
		int strLen = str.length();
		for (int i = 0; i < strLen; i++) {
			if (!Character.isWhitespace(str.charAt(i))) {
				return true;
			}
		}
		return false;
	}

	private Strings() {
	}
}
