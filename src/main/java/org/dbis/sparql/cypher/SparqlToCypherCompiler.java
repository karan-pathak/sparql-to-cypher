package org.dbis.sparql.cypher;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import org.apache.jena.graph.Node;
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
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;

import org.neo4j.cypherdsl.core.Cypher;
import org.neo4j.cypherdsl.core.Literal;
import org.neo4j.cypherdsl.core.Statement;
import org.neo4j.cypherdsl.core.StatementBuilder;

public class SparqlToCypherCompiler extends OpVisitorBase{
    List<Statement> statementList = new ArrayList<Statement>();
    List<String> varsToReturn = new ArrayList<>();
//    List<String> aggregatedVars = new ArrayList<>();
    HashSet<String> aggregatedVars = new HashSet<String>();
    // Referenced java doc: https://neo4j-contrib.github.io/cypher-dsl/current/project-info/apidocs/org/neo4j/cypherdsl/core/package-summary.html
    StatementBuilder.OrderableOngoingReadingAndWith statement_rw;
    StatementBuilder.OrderableOngoingReadingAndWithWithoutWhere statement_rww;

    List<Statement> convertToCypherStatements(final Query query) {

        long startTime = System.currentTimeMillis();
        long endTime;
        final Op op = Algebra.compile(query); // SPARQL query compiles here to
        for(Var projVar: query.getProjectVars()){
            varsToReturn.add(projVar.getName());
        }
        OpWalker.walk(op, this); // OP is being walked here. This calls the visit method
        return statementList;
    }

    private static List<Statement> convert(final Query query){
        return new SparqlToCypherCompiler().convertToCypherStatements(query);
    }

    public static List<Statement> convert(final String query){
        return convert(QueryFactory.create(Prefixes.prepend(query)));
        // Can be used for variable predicate in future. Commented below.
        // return convert(QueryFactory.create(query));
    }

    @Override
    public void visit(final OpBGP opBGP) {
        System.out.println("################ Inside opBGP ####################");
        final List<Triple> triples = opBGP.getPattern().getList();
        int count = 1;
        for (final Triple triple : triples) {
            final String subj = triple.getSubject().getName(); // Change this. It can also be variable
            final Node node_obj = triple.getObject();
            String obj = node_obj.isConcrete()
                    ? node_obj.getLiteralValue().toString()
                    : node_obj.getName();
            final Node predicate = triple.getPredicate();
            final String uri = predicate.getURI();
            final String uriValue = Prefixes.getURIValue(uri);
            final String prefix = Prefixes.getPrefix(uri);
            var subject_node = Cypher.anyNode(subj);

            switch(prefix){
                case "edge":
                    var object_node = Cypher.anyNode(obj);
                    if(!aggregatedVars.contains(subj)){
                        aggregatedVars.add(subj);
                    }
                    if(!aggregatedVars.contains(obj)){
                        aggregatedVars.add(obj);
                    }
//                    aggregatedVars.addAll(Arrays.asList(subj,obj));
                    String[] aggArgsE = new String[aggregatedVars.size()];
                    aggArgsE = aggregatedVars.toArray(aggArgsE);
                    if(statement_rw != null && statement_rww == null){
                        statement_rw = statement_rw
                                .match(subject_node.relationshipTo(object_node, uriValue))
                                .with(aggArgsE);
                    }
                    else if(statement_rww != null){
                        statement_rww = statement_rww
                                .match(subject_node.relationshipTo(object_node, uriValue))
                                .with(aggArgsE);
                    }
                    else{
                        statement_rw = Cypher
                                .match(subject_node.relationshipTo(object_node, uriValue))
                                .with(aggArgsE);
                    }
                    break;
                case "value":
                    subject_node = Cypher.node(uriValue);
                    if(statement_rw != null && statement_rww == null){
                        statement_rw = statement_rw
                                .match(subject_node)
                                .with(subj);
                    }
                    else if(statement_rww != null){
                        statement_rww = statement_rww
                                .match(subject_node)
                                .with(subj);
                    }
                    else{
                        statement_rw = Cypher
                                .match(subject_node)
                                .with(subj);
                    }
                    break;
                case "property":
                    if(node_obj.isConcrete()){
                        subject_node = subject_node.withProperties(uriValue, Cypher.literalOf(obj));
                    }
                    if(!aggregatedVars.contains(subj)){
                        aggregatedVars.add(subj);
                    }
                    String[] aggArgsP = new String[aggregatedVars.size()];
                    aggArgsP = aggregatedVars.toArray(aggArgsP);
                    // Define order based on statement ranking
                    if(statement_rw != null && statement_rww == null){
                        statement_rww = statement_rw
                                .match(subject_node)
                                .where(subject_node.property(uriValue).isNotNull())
                                .with(aggArgsP);
                    }
                    else if(statement_rww != null){
                        statement_rww = statement_rw
                                .match(subject_node)
                                .where(subject_node.property(uriValue).isNotNull())
                                .with(aggArgsP);
                    }
                    else{
                        statement_rww = Cypher
                                .match(subject_node)
                                .where(subject_node.property(uriValue).isNotNull())
                                .with(aggArgsP);
                    }
                    break;
                default:
                    System.out.println("######### Error in statement transform. Default case used. ##############");

            }
            if(count == triples.size()) {
                String[] varArgs = new String[varsToReturn.size()];
                varArgs = varsToReturn.toArray(varArgs);
                var final_statement = statement_rw.returning(varArgs).build();
                statementList.add(final_statement);
            }
            count++;
        }
    }

}
