package org.dbis.sparql.cypher;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVisitorBase;
import org.apache.jena.sparql.algebra.OpWalker;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpLeftJoin;
import org.apache.jena.sparql.algebra.op.OpUnion;
import org.apache.jena.sparql.expr.Expr;

import org.neo4j.cypherdsl.core.Statement;

public class SparqlToCypherCompiler extends OpVisitorBase{
    List<Statement> statementList = new ArrayList<Statement>();

    List<Statement> convertToCypherStatements(final Query query) {

        long startTime = System.currentTimeMillis();
        long endTime;
        final Op op = Algebra.compile(query); // SPARQL query compiles here to
        // OP
        // System.out.println("OP Tree: " + op.toString());

        OpWalker.walk(op, this); // OP is being walked here
        return statementList;
    }

    private static List<Statement> convert(final Query query){
        return new SparqlToCypherCompiler().convertToCypherStatements(query);
    }

    public static List<Statement> convert(final String query){
        return convert(QueryFactory.create(Prefixes.prepend(query)));
    }

    @Override
    public void visit(final OpBGP opBGP) {
        {

            System.out.println("Inside opBGP ---------------------------------------------->");
            final List<Triple> triples = opBGP.getPattern().getList();
            final Statement[] matchStatements = new Statement[triples.size()];
            int i = 0;
            for (final Triple triple : triples) {

                matchStatements[i++] = StatementBuilder.transform(triple);
                statementList.add(matchStatements[i - 1]);
            }

        }

    }

}
