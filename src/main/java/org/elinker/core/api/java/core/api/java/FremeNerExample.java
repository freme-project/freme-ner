package org.elinker.core.api.java.core.api.java;

/**
 * Created by nilesh on 16/12/15.
 */
public class FremeNerExample {
    public void main(String[] args) {
        Config config = new Config(new String[] {"en"}, "/home/nilesh/models/", "http://rv2622.1blu.de:8983/solr", "jdbc:sqlite:fremener.db");
        FremeNer fner = new FremeNer(config);
        System.out.println(fner.spot("Berlin is a city in Germany.", "en", "TTL", ""));
    }
}
