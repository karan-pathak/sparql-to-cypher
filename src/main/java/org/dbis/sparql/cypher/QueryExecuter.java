package org.dbis.sparql.cypher;

import java.io.*;
import java.util.*;

import org.neo4j.cypherdsl.core.Statement;
import org.neo4j.configuration.connectors.BoltConnector;
import org.neo4j.configuration.helpers.SocketAddress;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.cypherdsl.core.renderer.Renderer;

public class QueryExecuter {
    static String nodeResult;

    private static DatabaseManagementService createConnection() throws IOException{
        System.out.println( "Starting Neo4j database ..." );
        InputStream input = new FileInputStream("src/main/resources/config.properties");
        Properties props = new Properties();
        props.load(input);
        File DB_PATH = new File( props.getProperty("DB_URL") );
        // tag::startDb[]
        DatabaseManagementService managementService = new DatabaseManagementServiceBuilder(DB_PATH)
                .setConfig( BoltConnector.enabled, true )
                .setConfig( BoltConnector.listen_address, new SocketAddress( "localhost", 7687 ) )
                .build();
        // end::startDb[]

        return managementService;
    }

    private static void executeAndDisplayResults(DatabaseManagementService managementService, List<Statement> cypherStatements){
        GraphDatabaseService graphDb = managementService.database( "neo4j" );
        Renderer cypherRenderer = Renderer.getDefaultRenderer();

        // TODO: Remove dummy queries
        try ( Transaction tx = graphDb.beginTx();
               Result result = tx.execute( cypherRenderer.render(cypherStatements.get(0)) ) ){
            String resultString;
            String columnsString;
            String renderedCS;
            // Dummy call to get node by id. TODO: Remove later
            // tx.getNodeById(177).getAllProperties();

            // tag::items[]
            Iterator<Node> n_column = result.columnAs( "n" );
            n_column.forEachRemaining( node -> nodeResult = node + ": " );
            // end::items[]

            // tag::columns[]
            List<String> columns = result.columns();
            // end::columns[]
            columnsString = columns.toString();
            for(Statement cs: cypherStatements){
                resultString = tx.execute(cypherRenderer.render(cs)).resultAsString();
                System.out.println(resultString);
                System.out.println("The resulting cypher code is : "+cypherRenderer.render(cs));
            }
        }

        managementService.shutdown();
    }

    public static void execute(List<Statement> statements){
        try{
            DatabaseManagementService managementService = createConnection();
            executeAndDisplayResults(managementService, statements);
        }
        catch(IOException ex){
            ex.printStackTrace();
        }
    }
}
