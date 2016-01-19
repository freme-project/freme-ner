package org.elinker.core.api.java.core.api.java;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by nilesh on 16/12/15.
 */
public class FremeNerExample {
    public static void main(String[] args) {

        ApplicationContext springContext = new ClassPathXmlApplicationContext("applicationContext.xml");
        Config config = (Config)springContext.getBean("fremener");
        System.out.println(config.modelsLocation);
        FremeNer fner = new FremeNer(config);
        System.out.println(fner.spot("Welcome to Berlin.", "en", "TTL", "http://freme-project.eu"));
        System.out.println(fner.spot("Welcome to Berlin", "en", "TTL", "http://freme-project.eu"));

        System.out.println(fner.spotLinkClassify("Berlin is a city in Germany.", "en", "dbpedia", "TTL", "http://freme-project.eu", 1));

        Set<String> types = new HashSet<>();

        types.add("http://dbpedia.org/ontology/Scientist");
        types.add("http://dbpedia.org/ontology/Philosopher");
        System.out.println(fner.spotLinkClassify("Albert Einstein was a famous physicist.", "en", "dbpedia", "TTL", "", 1, types));
        System.out.println(fner.spotLinkClassify("Arthur Schopenhauer was a renowned philosopher.", "en", "dbpedia", "TTL", "", 1, types));

        String domain = "TaaS-2007 Sports";
        System.out.println(fner.spotLinkClassify("Alaska is a place. Albert Einstein is from Austria. Andre Agassi plays tennis.", "en", "dbpedia", "TTL", "", 1, domain));


    }
}
