package org.dbis.sparql.cypher;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

import org.neo4j.cypherdsl.core.Cypher;
import org.neo4j.cypherdsl.core.Conditions;
import org.neo4j.cypherdsl.core.Functions;
import org.neo4j.cypherdsl.core.Statement;
import org.neo4j.cypherdsl.core.SymbolicName;
import org.neo4j.cypherdsl.core.renderer.Renderer;

public class StatementBuilder {

    public static Statement transform(final Triple triple){
        final String subj = triple.getSubject().getName();
        var subject_node = Cypher.anyNode(subj);
        final Node node_obj = triple.getObject();
        String obj = node_obj.isConcrete()
                ? node_obj.getLiteralValue().toString()
                : node_obj.getName();
        var object_node = Cypher.anyNode(obj);
        final String predicate = triple.getPredicate().getURI();
        var statement = Cypher
                .match(subject_node.relationshipTo(object_node, predicate))
                .returning(subj, obj)
                .build();
//        Renderer cypherRenderer = Renderer.getDefaultRenderer();
//        cypherRenderer.render(statement);
        return statement;
    }

    // Alternate implementation from scratch below. TODO: Remove in later versions

//    // Enum for all cypher keywords
//    public enum kwords{
//        MATCH,
//        OPTIONAL,
//        RETURN,
//        WITH,
//        UNWIND,
//        AS,
//        WHERE,
//        SKIP,
//        LIMIT,
//        DISTINCT,
//        CREATE,
//        DELETE,
//        DETACH,
//        REMOVE,
//        SET,
//        MERGE
//    }
//
//    public static String transform2(final Triple triple){
//        // isConcrete is used for object to check whether the object contains variable or concrete object.
//        StringBuilder cypherQuery = new StringBuilder();
//        // Check conditions for appending the first keyword
//        cypherQuery.append(StatementBuilder.kwords.MATCH);
//        final String subj = triple.getSubject().getName();
//        final String predicate = triple.getPredicate().getName();
//        cypherQuery.append(createNode(subj));
//        cypherQuery.append(createRelationship(predicate, "to"));
//        // Match Property
//        final Node node_obj = triple.getObject();
//        String obj = node_obj.isConcrete()
//                ? node_obj.getLiteralValue().toString()
//                : node_obj.getName();
//        cypherQuery.append(createNode(obj));
//        // Add the return statement
//        cypherQuery.append(StatementBuilder.kwords.RETURN);
//        cypherQuery.append(" *");
//        return cypherQuery.toString();
//    }
//
//    public static String createNode(String object){
//        // Helper function to create a cypher query node
//        StringBuilder node = new StringBuilder("(");
//        node.append(object+")");
//        return node.toString();
//    }
//
//    public static String createRelationship(String object, String direction){
//        // helper function to create a cypher query relationship
//        StringBuilder rel = new StringBuilder();
//        switch (direction){
//            case "to":
//                rel.append("-[");
//                rel.append(object);
//                rel.append("]->");
//            case "from":
//                rel.append("<-[");
//                rel.append(object);
//                rel.append("]-");
//            case "bidirectional":
//                rel.append("-[");
//                rel.append(object);
//                rel.append("]-");
//        }
//        return rel.toString();
//    }


}
