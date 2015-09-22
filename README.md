# FREME NER

A Named Entity Recognition and Entitity Linking tool.

NOTE: The documentation below is old. Currently, the code in this repository is sparely documented. We will be adding documentation, building and deployment instructions and polishing the code gradually.

Installation
------------

The tool can be installed using Maven, so make sure you have installed it first: http://maven.apache.org/download.cgi and java 1.8

    mvn clean install

#### Prerequisites

* Running Apache Solr instance. Read [how-to](http://lucene.apache.org/solr/quickstart.html).
* ...

Usage
-----

You can submit text for processing by executing the following command.

    curl -v -d "The Charles Bridge is a famous historic bridge that crosses the Vltava river in Prague, Czech Republic." "http://localhost:8080/api/documents?language=en&dataset=dbpedia"

Acknowledgments
---------------

The development of this software is supported by the [FREME H2020](http://www.freme-project.eu/) project.

License
-------

FREME NER is released under the terms of the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).
