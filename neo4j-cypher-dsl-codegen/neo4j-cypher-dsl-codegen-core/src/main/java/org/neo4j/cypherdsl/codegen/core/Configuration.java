/*
 * Copyright (c) 2019-2024 "Neo4j,"
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
package org.neo4j.cypherdsl.codegen.core;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.nio.file.Path;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;
import java.util.function.UnaryOperator;

import org.apiguardian.api.API;

/**
 * Main configuration objects for all aspects of code generation.
 *
 * @author Michael J. Simons
 * @soundtrack Foo Fighters - Echoes, Silence, Patience &amp; Grace
 * @since 2021.1.0
 */
@API(status = EXPERIMENTAL, since = "2021.1.0")
public final class Configuration {

	private static final Configuration DEFAULT_CONFIG = newConfig().build();

	public static final String PROPERTY_PREFIX = "org.neo4j.cypherdsl.codegen.prefix";
	public static final String PROPERTY_SUFFIX = "org.neo4j.cypherdsl.codegen.suffix";
	public static final String PROPERTY_INDENT_STYLE = "org.neo4j.cypherdsl.codegen.indent_style";
	public static final String PROPERTY_INDENT_SIZE = "org.neo4j.cypherdsl.codegen.indent_size";
	public static final String PROPERTY_TIMESTAMP = "org.neo4j.cypherdsl.codegen.timestamp";
	public static final String PROPERTY_ADD_AT_GENERATED = "org.neo4j.cypherdsl.codegen.add_at_generated";

	/**
	 * Enum for the available indent styles.
	 */
	public enum IndentStyle {
		/** Use tabs for indentation. */
		TAB,
		/** Use a configurable number of spaces for indentation. */
		SPACE
	}

	/**
	 * The target Java base line.
	 */
	public enum JavaVersion {

		RELEASE_8,
		RELEASE_11
	}

	/**
	 * Defines decoration for generated type names, applies to both nodes and relationships.
	 */
	private final UnaryOperator<String> typeNameDecorator;

	/**
	 * Defines how classes representing nodes should be named.
	 */
	private final ClassNameGenerator nodeNameGenerator;

	/**
	 * Defines how classes representing relationships should be named.
	 */
	private final ClassNameGenerator relationshipNameGenerator;

	private final FieldNameGenerator fieldNameGenerator;

	/**
	 * On which Java version should the generated classes be compilable? Defaults to Java Release 8.
	 */
	private final JavaVersion target;

	/**
	 * The fully qualified name of the Java package into which the classes should be generated.
	 */
	private final String defaultPackage;

	/**
	 * The path into which the Java classes should be generated. A package structure matching {@link #defaultPackage} will
	 * be created.
	 */
	private final Optional<Path> path;

	/**
	 * The indention string used in the generated source files.
	 */
	private final String indent;

	/**
	 * Optional clock to use while generation things
	 */
	private final Optional<Clock> clock;

	/**
	 * Flag if the {@code @Generated}-annotation should be added. On JDK9+ on the module path it would require jdk.compiler.
	 * If you don't want it, disable it with this flag.
	 */
	private final boolean addAtGenerated;

	public static Configuration defaultConfig() {
		return DEFAULT_CONFIG;
	}

	public static Builder newConfig() {
		return Builder.newConfig();
	}

	public static Builder newConfig(final Path path) {
		return Builder.newConfig(path);
	}

	/**
	 * Use this builder to create new {@link org.neo4j.cypherdsl.core.renderer.Configuration} instances.
	 */
	@SuppressWarnings("HiddenField")
	public static final class Builder {

		private ClassNameGenerator nodeNameGenerator = new NodeNameGenerator();
		private ClassNameGenerator relationshipNameGenerator = new RelationshipNameGenerator();
		private JavaVersion target = JavaVersion.RELEASE_8;
		private String defaultPackage = "";
		private Path path;
		private String prefix;
		private String suffix = "_";
		private IndentStyle indentStyle = IndentStyle.TAB;
		private int indentSize = 2;
		private String timestamp;
		private boolean addAtGenerated = false;

		private Builder() {
		}

		static Builder newConfig() {
			return new Builder();
		}

		static Builder newConfig(final Path path) {
			return new Builder().withPath(path);
		}

		public Builder withNodeNameGenerator(ClassNameGenerator nodeNameGenerator) {

			if (nodeNameGenerator == null) {
				throw new IllegalArgumentException("A class name generator for nodes is required.");
			}
			this.nodeNameGenerator = nodeNameGenerator;
			return this;
		}

		public Builder withRelationshipNameGenerator(ClassNameGenerator relationshipNameGenerator) {

			if (relationshipNameGenerator == null) {
				throw new IllegalArgumentException("A class name generator for relationships is required.");
			}
			this.relationshipNameGenerator = relationshipNameGenerator;
			return this;
		}

		public Builder withTarget(JavaVersion target) {

			if (target == null) {
				throw new IllegalArgumentException("A java version is required.");
			}
			this.target = target;
			return this;
		}

		public Builder withDefaultPackage(String defaultPackage) {

			if (defaultPackage == null) {
				throw new IllegalArgumentException("A default package is required.");
			}
			this.defaultPackage = defaultPackage;
			return this;
		}

		/**
		 * @param timestamp Timestamp to write into the generated classes. Uses the current time when no timestamp is given.
		 *                  Expected format is {@link DateTimeFormatter#ISO_OFFSET_DATE_TIME}.
		 * @return This builder
		 */
		public Builder withTimestamp(String timestamp) {

			this.timestamp = timestamp;
			return this;
		}

		/**
		 * A path is not always necessary, for example in an annotation processor.
		 * @param path A path into which the files should be written
		 * @return This builder
		 */
		public Builder withPath(Path path) {

			this.path = path;
			return this;
		}

		/**
		 * Configure a prefix for the generated classes. Will only be used when no other naming generator is configured.
		 *
		 * @param prefix Prepended to the names of generated classes.
		 * @return This builder
		 */
		public Builder withPrefix(String prefix) {

			this.prefix = prefix;
			return this;
		}

		/**
		 * Configure a suffix for the generated classes. Will only be used when no other naming generator is configured.
		 *
		 * @param suffix Appended to the names of generated classes.
		 * @return This builder
		 */
		public Builder withSuffix(String suffix) {

			this.suffix = suffix;
			return this;
		}

		public Builder withAddAtGenerated(boolean addAtGenerated) {

			this.addAtGenerated = addAtGenerated;
			return this;
		}

		public Builder withIndentStyle(IndentStyle indentStyle) {

			if (indentStyle == null) {
				throw new IllegalArgumentException("Indent style is required.");
			}
			this.indentStyle = indentStyle;
			return this;
		}

		public Builder withIndentSize(int indentSize) {
			this.indentSize = indentSize;
			return this;
		}

		public Configuration build() {

			UnaryOperator<String> typeNameDecorator;
			if (prefix == null && suffix == null) {
				typeNameDecorator = UnaryOperator.identity();
			} else {
				typeNameDecorator = s -> (prefix != null ? prefix.trim() : "") + s + (suffix != null ? suffix.trim() : "");
			}

			String indent;
			if (indentStyle == IndentStyle.TAB) {
				indent = "\t";
			} else {
				StringBuilder indentBuilder = new StringBuilder();
				for (int i = 0; i < indentSize; ++i) {
					indentBuilder.append(" ");
				}
				indent = indentBuilder.toString();
			}

			Clock clock = null;
			if (this.timestamp != null && !this.timestamp.isEmpty()) {
				ZonedDateTime z = ZonedDateTime.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(timestamp));
				clock = Clock.fixed(z.toInstant(), z.getZone());
			}
			return new Configuration(typeNameDecorator, nodeNameGenerator, relationshipNameGenerator,
				FieldNameGenerator.Default.INSTANCE, target, defaultPackage, path, indent, clock, addAtGenerated);
		}
	}

	private Configuration(UnaryOperator<String> typeNameDecorator,
		ClassNameGenerator nodeNameGenerator,
		ClassNameGenerator relationshipNameGenerator,
		FieldNameGenerator fieldNameGenerator,
		JavaVersion target, String defaultPackage, Path path, String indent, Clock clock, boolean addAtGenerated) {
		this.typeNameDecorator = typeNameDecorator;
		this.nodeNameGenerator = nodeNameGenerator;
		this.relationshipNameGenerator = relationshipNameGenerator;
		this.fieldNameGenerator = fieldNameGenerator;
		this.target = target;
		this.defaultPackage = defaultPackage;
		this.path = Optional.ofNullable(path);
		this.indent = indent;
		this.clock = Optional.ofNullable(clock);
		this.addAtGenerated = addAtGenerated;
	}

	public ClassNameGenerator getNodeNameGenerator() {
		return nodeNameGenerator;
	}

	public ClassNameGenerator getRelationshipNameGenerator() {
		return relationshipNameGenerator;
	}

	public FieldNameGenerator getConstantFieldNameGenerator() {
		return fieldNameGenerator;
	}

	public JavaVersion getTarget() {
		return target;
	}

	public String getDefaultPackage() {
		return defaultPackage;
	}

	public Optional<Path> getPath() {
		return path;
	}

	public UnaryOperator<String> getTypeNameDecorator() {
		return typeNameDecorator;
	}

	public String getIndent() {
		return indent;
	}

	public Optional<Clock> getClock() {
		return clock;
	}

	public boolean isAddAtGenerated() {
		return addAtGenerated;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Configuration that = (Configuration) o;
		return typeNameDecorator.equals(that.typeNameDecorator) && nodeNameGenerator.equals(that.nodeNameGenerator)
			&& relationshipNameGenerator.equals(that.relationshipNameGenerator) && fieldNameGenerator.equals(that.fieldNameGenerator)
			&& target == that.target && defaultPackage.equals(that.defaultPackage)
			&& path.equals(that.path)
			&& indent.equals(that.indent)
			&& clock.equals(that.clock)
			&& addAtGenerated == that.addAtGenerated;
	}

	@Override
	public int hashCode() {
		return Objects.hash(typeNameDecorator, nodeNameGenerator, relationshipNameGenerator, fieldNameGenerator,
			target, defaultPackage, path, indent, clock, addAtGenerated);
	}
}
