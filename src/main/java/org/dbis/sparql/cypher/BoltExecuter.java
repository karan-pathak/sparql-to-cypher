package org.dbis.sparql.cypher;

import java.io.*;
import java.util.*;

import org.neo4j.cypherdsl.core.Statement;
import org.neo4j.cypherdsl.core.renderer.Renderer;
import org.neo4j.dbms.api.DatabaseManagementService;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.TransactionWork;

import static org.neo4j.driver.Values.parameters;

public class BoltExecuter implements AutoCloseable{

    private static Driver driver;

    public BoltExecuter( String uri, String user, String password )
    {
        driver = GraphDatabase.driver( uri, AuthTokens.basic( user, password ) );
    }

    @Override
    public void close() throws Exception
    {
        driver.close();
    }

    private static void executeAndDisplayResults( List<Statement> cypherStatements){
        long startTime = System.currentTimeMillis();
        Renderer cypherRenderer = Renderer.getDefaultRenderer();
        try ( Session session = driver.session() )
        {
            String result = session.writeTransaction( new TransactionWork<String>()
            {
                @Override
                public String execute( Transaction tx )
                {
                    Result result = tx.run( cypherRenderer.render(cypherStatements.get(0)) );
                    return String.valueOf(result.stream().count());
//                    return result.single().get( 0 ).asString();
                }
            } );
            System.out.println( "Number of records returned: " + result );
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Time taken to execute Cypher with bolt connector inside : " + (endTime - startTime) + " miliseconds");
    }

    public static void execute(List<Statement> statements){
        try{
            executeAndDisplayResults(statements);
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }

}
