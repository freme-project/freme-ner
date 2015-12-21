package org.elinker.core.api.java.core.api.java;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by nilesh on 16/12/15.
 */
public class FremeNerExample {
    public static void main(String[] args) {
        Config config = new Config(new String[] {"en"}, "/Users/nilesh/models/", "http://rv2622.1blu.de:8983/solr", "jdbc:sqlite:fremener.db",
                    "/Users/nilesh/instance_types_transitive_en.ttl", "/Users/nilesh/domains.csv");
        FremeNer fner = new FremeNer(config);
//        System.out.println(fner.spot("Berlin is a city in Germany.", "en", "TTL", "http://freme-project.eu"));
//
//        System.out.println(fner.spotLinkClassify("Berlin is a city in Germany.", "en", "dbpedia", "TTL", "http://freme-project.eu", 1));

        Set<String> types = new HashSet<>();

        types.add("http://dbpedia.org/ontology/Scientist");
        types.add("http://dbpedia.org/ontology/Philosopher");
        System.out.println(fner.spotLinkClassify("Albert Einstein was a famous physicist.", "en", "dbpedia", "TTL", "", 1, types));
        System.out.println(fner.spotLinkClassify("Arthur Schopenhauer was a renowned philosopher.", "en", "dbpedia", "TTL", "", 1, types));

        String domain = "TaaS-2007 Sports";
        System.out.println(fner.spotLinkClassify("Alaska is a place. Andre Agassi plays tennis.", "en", "dbpedia", "TTL", "", 1, domain));


    }
}
