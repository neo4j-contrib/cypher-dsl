package org.neo4j.cypherdsl.codegen.sdn6.models.same_rel_mixed_different_directions;

import java.util.List;
import javax.annotation.Generated;
import org.neo4j.cypherdsl.core.MapExpression;
import org.neo4j.cypherdsl.core.NodeBase;
import org.neo4j.cypherdsl.core.NodeLabel;
import org.neo4j.cypherdsl.core.Properties;
import org.neo4j.cypherdsl.core.Property;
import org.neo4j.cypherdsl.core.SymbolicName;

@Generated(
		value = "org.neo4j.cypherdsl.codegen.core.NodeImplBuilder",
		date = "2019-09-21T21:21:00+01:00",
		comments = "This class is generated by the Neo4j Cypher-DSL. All changes to it will be lost after regeneration."
)
public final class Movie_ extends NodeBase<Movie_> {
	public static final Movie_ MOVIE = new Movie_();

	public final Property TITLE = this.property("title");

	public final Wrote_<Person_, Movie_> WRITTEN_BY = new Wrote_<Person_, Movie_>(Person_.PERSON, this);

	public Movie_() {
		super("Movie");
	}

	private Movie_(SymbolicName symbolicName, List<NodeLabel> labels, Properties properties) {
		super(symbolicName, labels, properties);
	}

	@Override
	public Movie_ named(SymbolicName newSymbolicName) {
		return new Movie_(newSymbolicName, getLabels(), getProperties());
	}

	@Override
	public Movie_ withProperties(MapExpression newProperties) {
		return new Movie_(getSymbolicName().orElse(null), getLabels(), Properties.create(newProperties));
	}
}
