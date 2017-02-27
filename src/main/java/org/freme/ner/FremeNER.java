package org.freme.ner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.solr.SolrAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"org.elinker.core.rest", "eu.freme.common", "org.elinker.core.spotter", "org.elinker.core.api.java", "org.elinker.core.link"})
@EnableAutoConfiguration(exclude = {SolrAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration.class})
public class FremeNER {
    public static void main(String[] args) {
        SpringApplication.run(FremeNER.class, args);
    }
}
