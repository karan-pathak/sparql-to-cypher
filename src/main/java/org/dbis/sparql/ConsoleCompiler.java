package org.dbis.sparql;

import java.io.*;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import org.neo4j.cypherdsl.core.Statement;

import org.dbis.sparql.cypher.SparqlToCypherCompiler;
import org.dbis.sparql.cypher.QueryExecuter;
import org.dbis.sparql.cypher.BoltExecuter;

class ConsoleCompiler {

    public static void main(final String[] args) throws Exception {
        //args = "/examples/modern1.sparql";
        final Options options = new Options();
        options.addOption("f", "file", true, "a file that contains a SPARQL query");

        final CommandLineParser parser = new DefaultParser();
        final CommandLine commandLine;

        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            return;
        }

        final InputStream inputStream = commandLine.hasOption("file")
                ? new FileInputStream(commandLine.getOptionValue("file"))
                : System.in;
        final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        final StringBuilder queryBuilder = new StringBuilder();

        String line;
        while (null != (line = reader.readLine())) {
            queryBuilder.append(System.lineSeparator()).append(line);
        }

        final String queryString = queryBuilder.toString();


        long startTime = System.currentTimeMillis();
        List<Statement> cypherStatements = SparqlToCypherCompiler.convert(queryString);
        long endTime = System.currentTimeMillis();
        System.out.println("Time taken to convert SPARQL to Cypher : " + (endTime - startTime) + " miliseconds");
        System.out.println("Executing statements ...");
        // Query executer below uses neo4j embedded and is slower than a bolt connection.
//        QueryExecuter.execute(cypherStatements);
        startTime = System.currentTimeMillis();
        // Execution using a bolt connection
        try ( BoltExecuter be = new BoltExecuter( "bolt://localhost:7687", "neo4j", "waterloo" ) ) {
            be.execute(cypherStatements);
        }
        endTime = System.currentTimeMillis();
        System.out.println("Time taken to execute Cypher with bolt connector outside : " + (endTime - startTime) + " miliseconds");

    }
}
