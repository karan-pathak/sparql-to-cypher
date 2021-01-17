package org.dbis.sparql.cypher;

import java.util.Arrays;
import java.util.List;


public class Prefixes {

    public final static String BASE_URI = "http://northwind.com/model/";

    final static List<String> PREFIXES = Arrays.asList("edge", "property", "value");

    final static String PREFIX_DEFINITIONS;

    static {
        final StringBuilder builder = new StringBuilder();
        for (final String prefix : PREFIXES) {
            builder.append("PREFIX ").append(prefix.substring(0, 1)).append(": <").append(getURI(prefix)).
                    append(">").append(System.lineSeparator());
        }
        PREFIX_DEFINITIONS = builder.toString();
    }

    public static String getURI(final String prefix) {
        return BASE_URI + prefix + "#";
    }

    public static String getURIValue(final String uri) {
        return uri.substring(uri.indexOf("#") + 1);
    }

    public static String getPrefix(final String uri) {
        final String tmp = uri.substring(0, uri.indexOf("#"));
        return tmp.substring(tmp.lastIndexOf("/") + 1);
    }

    public static String prepend(final String script) {
        return PREFIX_DEFINITIONS + script;
    }

    public static StringBuilder prepend(final StringBuilder scriptBuilder) {
        return scriptBuilder.insert(0, PREFIX_DEFINITIONS);
    }
}

