## SPARQL to CYPHER [Work In Progress]
#### A library to convert sparql queries to cypher queries

### Requirements
* Java SE 11
Note: Neo4j version 4.1.0 and desktop version 1.3.4 were used at the time of writing.

### Quick Start
* Rename `src/main/resources/config.properties.example` to `src/main/resources/config.properties`
* Add the correct DB_URL in the config.properties file. A reference DB_URL for mac is provided. For windows refer this post(https://stackoverflow.com/questions/28890426/reading-windows-paths-in-properties-file)
* To find the DB_URL from neo4j desktop:
    * Select three dots on top-right corner of database and select `Manage`
    * Click on `Open Folder` and select the folder path
* Run the `ConsoleCompiler.java` file with arguments `-f <path-to-sparql-query-file>`. For instance `-f examples/sparql_test.sparql`
  