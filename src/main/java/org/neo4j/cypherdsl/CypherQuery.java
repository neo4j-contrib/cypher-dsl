/**
 * Copyright (c) 2002-2012 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
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
package org.neo4j.cypherdsl;


import org.neo4j.cypherdsl.query.Expression;
import org.neo4j.cypherdsl.query.MatchExpression;
import org.neo4j.cypherdsl.query.OrderByExpression;
import org.neo4j.cypherdsl.query.Query;
import org.neo4j.cypherdsl.query.ReturnExpression;
import org.neo4j.cypherdsl.query.StartExpression;
import org.neo4j.cypherdsl.query.WhereExpression;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * DSL for creating Cypher queries. Once created you can serialize to a string,
 * or retrieve the internal Query model for further processing.
 *
 * It is possible to iteratively construct Cypher queries by calling toQuery()
 * and then use the Query as continuation point. When a new CypherQuery is created
 * by using the newQuery() method the Query is cloned, so that the original Query
 * is not modified. This can be used effectively to create a base query which can then
 * be used many times. Typical examples would be to create a query which is reused many times
 * for paging purposes, and also to provide a base query using START and MATCH which is then
 * expanded using WHERE and RETURN clauses.
 */
public class CypherQuery
{
    public static CypherQuery cypherVersion( String version )
    {
        CypherQuery query = new CypherQuery();
        return query.usingVersion(version);
    }

    public static Match start( StartExpression... startExpressions )
    {
        CypherQuery query = new CypherQuery();
        return query.starts( startExpressions );
    }

    public static CypherQuery newQuery( Query query )
    {
        return new CypherQuery( query );
    }

    protected Query query;

    public CypherQuery()
    {
        query = new Query();
    }

    private CypherQuery( Query query )
    {
        try
        {
            this.query = (Query) query.clone();
        }
        catch( CloneNotSupportedException e )
        {
            throw new IllegalStateException( "Query was not cloneable" );
        }
    }

    // Syntax version -----------------------------------------------
    protected CypherQuery usingVersion( String version )
    {
        query.cypherVersion = version;
        return this;
    }


    // Start --------------------------------------------------------
    protected Match starts( StartExpression... startExpression )
    {
        Collections.addAll(query.startExpressions, startExpression);

        return new Grammar();
    }

    protected Match starts( Iterable<StartExpression> startExpression )
    {
        for (StartExpression expression : startExpression)
        {
            query.startExpressions.add(expression);
        }

        return new Grammar();
    }

    protected StartExpression.StartNodes node( String name, long... id )
    {
        return StartExpression.node( name, id );
    }

    protected StartExpression.StartNodes node( String name, String parameter )
    {
        return StartExpression.node( name, parameter );
    }

    protected StartExpression.StartNodesLookup lookup( String name, String indexName, String key, String value )
    {
        return StartExpression.lookup( name, indexName, key, value );
    }

    protected StartExpression.StartNodesLookup lookup( String name, String indexName, Expression.Identifier key, Expression.Value value )
    {
        return StartExpression.lookup( name, indexName, key, value );
    }

    protected StartExpression.StartNodesQuery query( String name, String indexName, String query )
    {
        return StartExpression.query( name, indexName, query );
    }

    protected StartExpression.StartRelationships relationship( String name, long... id )
    {
        return StartExpression.relationship( name, id );
    }

    protected StartExpression.StartRelationshipsParameters relationship( String name, String parameter )
    {
        return StartExpression.relationship( name, parameter );
    }

    protected StartExpression.StartRelationshipsIndex relationshipLookup( String name, String indexName, String key, String value )
    {
        return StartExpression.relationshipLookup( name, indexName, key, value );
    }

    protected StartExpression.StartRelationshipsIndex relationshipLookup( String name, String indexName, Expression.Identifier key, Expression.Value value )
    {
        return StartExpression.relationshipLookup( name, indexName, key, value );
    }

    // Match --------------------------------------------------------
    protected MatchExpression.Path path()
    {
        return MatchExpression.path( );
    }

    protected MatchExpression.Path path( String name )
    {
        return MatchExpression.path( name );
    }

    protected MatchExpression.FunctionPath shortestPath( String name )
    {
        return MatchExpression.shortestPath( name );
    }

    // Where --------------------------------------------------------
    protected WhereExpression.CommonType prop(String name)
    {
        return WhereExpression.prop( name );
    }

    protected WhereExpression.StringType string(String name)
    {
        return WhereExpression.string( name );
    }

    protected WhereExpression.NumberType number(String name)
    {
        return WhereExpression.number( name );
    }

    protected WhereExpression.Not not(WhereExpression.PredicateExpression expr)
    {
        return WhereExpression.not( expr );
    }

    // Return -------------------------------------------------------
    protected ReturnExpression.ReturnNodes nodes( String... names )
    {
        return ReturnExpression.nodes( names );
    }

    protected ReturnExpression.ReturnRelationships relationships( String... names )
    {
        return ReturnExpression.relationships( names );
    }

    protected ReturnExpression.ReturnProperties properties( String... names )
    {
        return ReturnExpression.properties( names );
    }

    protected ReturnExpression.ReturnPaths paths( String... names )
    {
        return ReturnExpression.paths(names );
    }

    protected ReturnExpression.ReturnAggregate count()
    {
        return ReturnExpression.count(  );
    }

    protected ReturnExpression.ReturnAggregate count(String name)
    {
        return ReturnExpression.count( name );
    }

    protected ReturnExpression.ReturnAggregate sum(String name)
    {
        return ReturnExpression.sum( name );
    }

    protected ReturnExpression.ReturnAggregate avg(String name)
    {
        return ReturnExpression.avg( name );
    }

    protected ReturnExpression.ReturnAggregate max(String name)
    {
        return ReturnExpression.max( name );
    }

    protected ReturnExpression.ReturnAggregate min(String name)
    {
        return ReturnExpression.min( name );
    }

    protected ReturnExpression.ReturnAggregate collect(String name)
    {
        return ReturnExpression.collect( name );
    }

    protected ReturnExpression.ReturnNameFunction length( String name )
    {
        return ReturnExpression.length( name );
    }

    protected ReturnExpression.ReturnNameFunction type(String name)
    {
        return ReturnExpression.type( name );
    }

    protected ReturnExpression.ReturnNameFunction id(String name)
    {
        return ReturnExpression.id( name );
    }

    protected ReturnExpression.ReturnExpressionsFunction coalesce(ReturnExpression... expressions)
    {
        return ReturnExpression.coalesce( expressions );
    }

    protected ReturnExpression.ReturnNameFunction nodesOf(String name)
    {
        return ReturnExpression.nodesOf( name );
    }

    protected ReturnExpression.ReturnNameFunction relationshipsOf(String name)
    {
        return ReturnExpression.relationshipsOf( name );
    }

    // Order by -----------------------------------------------------
    protected OrderByExpression property( String name )
    {
        return OrderByExpression.property( name );
    }

    protected OrderByExpression property( String name, OrderByExpression.Order order )
    {
        return OrderByExpression.property( name, order );
    }

    @Override
    public String toString()
    {
        return query.toString();
    }

    // Grammar
    protected class Grammar
        implements Match, ReturnNext, OrderBy, Skip, Limit, Execute
    {
        // Match --------------------------------------------------------
        @Override
        public Match match( MatchExpression... expression )
        {
            Collections.addAll(query.matchExpressions, expression);
            return this;
        }

        @Override
        public Match match(Iterable<MatchExpression> expressions)
        {
            for (MatchExpression expression : expressions)
            {
                query.matchExpressions.add(expression);
            }
            return this;
        }

        // Where --------------------------------------------------------
        @Override
        public Return where( WhereExpression expression )
        {
            Query.checkNull( expression, "Expression" );
            query.whereExpression = expression;
            return this;
        }

        // Return -------------------------------------------------------
        @Override
        public ReturnNext returns( ReturnExpression... returnExpression )
        {
            Collections.addAll(query.returnExpressions, returnExpression);
            return this;
        }

        @Override
        public ReturnNext returns(Iterable<ReturnExpression> returnExpressions)
        {
            for (ReturnExpression returnExpression : returnExpressions)
            {
                query.returnExpressions.add(returnExpression);
            }
            return this;
        }

        // OrderBy ------------------------------------------------------
        @Override
        public OrderBy orderBy( OrderByExpression... orderByExpression )
        {
            Collections.addAll(query.orderByExpressions, orderByExpression);
            return this;
        }

        @Override
        public OrderBy orderBy(Iterable<OrderByExpression> orderByExpressions)
        {
            for (OrderByExpression orderByExpression : orderByExpressions)
            {
                query.orderByExpressions.add(orderByExpression);
            }
            return this;
        }

        // Skip ---------------------------------------------------------
        @Override
        public Limit skip( int skip )
        {
            if( skip < 0 )
            {
                throw new IllegalArgumentException( "Skip may not be below zero" );
            }

            query.skip = skip;
            return this;
        }

        // Limit --------------------------------------------------------
        @Override
        public Execute limit( int limit )
        {
            if( limit < 0 )
            {
                throw new IllegalArgumentException( "Limit may not be below zero" );
            }

            query.limit = limit;
            return this;
        }

        // Execute ------------------------------------------------------
        @Override
        public void asString( StringBuilder builder )
        {
            query.asString( builder );
        }

        @Override
        public Query toQuery()
        {
            return query;
        }

        @Override
        public ExecuteWithParameters parameter(String name, Object value)
        {
            ExecuteWithParams withParams = new ExecuteWithParams(query);
            return withParams.parameter(name, value);
        }

        @Override
        public String toString()
        {
            return CypherQuery.this.toString();
        }
    }

    protected class ExecuteWithParams
        implements ExecuteWithParameters
    {
        private Query query;
        private Map<String, Object> parameters = new HashMap<String, Object>();

        public ExecuteWithParams(Query query)
        {
            this.query = query;
        }

        @Override
        public Query toQuery()
        {
            return query;
        }

        public Map<String, Object> getParameters()
        {
            return parameters;
        }

        @Override
        public ExecuteWithParameters parameter(String name, Object value)
        {
            parameters.put(name, value);
            return this;
        }

        @Override
        public void asString(StringBuilder builder)
        {
            query.asString(builder);
        }

        @Override
        public String toString()
        {
            return query.toString();
        }
    }
}
