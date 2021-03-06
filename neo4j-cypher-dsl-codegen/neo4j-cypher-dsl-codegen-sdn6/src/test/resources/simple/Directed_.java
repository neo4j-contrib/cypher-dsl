package org.neo4j.cypherdsl.codegen.sdn6.models.simple;

import javax.annotation.Generated;
import org.neo4j.cypherdsl.core.MapExpression;
import org.neo4j.cypherdsl.core.Node;
import org.neo4j.cypherdsl.core.Properties;
import org.neo4j.cypherdsl.core.RelationshipBase;
import org.neo4j.cypherdsl.core.SymbolicName;

@Generated(
		value = "org.neo4j.cypherdsl.codegen.core.RelationshipImplBuilder",
		date = "2019-09-21T21:21:00+01:00",
		comments = "This class is generated by the Neo4j Cypher-DSL. All changes to it will be lost after regeneration."
)
public final class Directed_ extends RelationshipBase<Person_, Movie_, Directed_> {
	public static final String $TYPE = "DIRECTED";

	public Directed_(Person_ start, Movie_ end) {
		super(start, $TYPE, end);
	}

	private Directed_(SymbolicName symbolicName, Node start, Properties properties, Node end) {
		super(symbolicName, start, $TYPE, properties, end);
	}

	@Override
	public Directed_ named(SymbolicName newSymbolicName) {
		return new Directed_(newSymbolicName, getLeft(), getDetails().getProperties(), getRight());
	}

	@Override
	public Directed_ withProperties(MapExpression newProperties) {
		return new Directed_(getSymbolicName().orElse(null), getLeft(), Properties.create(newProperties), getRight());
	}
}