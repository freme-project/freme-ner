package org.elinker.core.api.java.core.api.java;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by nilesh on 16/12/15.
 */
public class FremeNerExample {
    public void main(String[] args) {
        Config config = new Config(new String[] {"en"}, "/home/nilesh/models/", "http://rv2622.1blu.de:8983/solr", "jdbc:sqlite:fremener.db",
                "/home/nilesh/instance_types_transitive_en.ttl", "/home/nilesh/domains.csv");
        FremeNer fner = new FremeNer(config);
        System.out.println(fner.spot("Berlin is a city in Germany.", "en", "TTL", ""));

        Set<String> types = new HashSet<>();
        types.add("http://dbpedia.org/ontology/Scientist");
        types.add("http://dbpedia.org/ontology/Writer");
        System.out.println(fner.spotLinkClassify("Albert Einstein was a famous physicist.", "en", "dbpedia", "TTL", "", 1, types));

        String domain = "TaaS-2303 Literature";
        System.out.println(fner.spotLinkClassify("Guy de Maupassant was a French author.", "en", "dbpedia", "TTL", "", 1, domain));

    }
}
