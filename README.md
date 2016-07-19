# FREME NER

A Named Entity Recognition and Entitity Linking tool, see [FREME NER documentation](http://api.freme-project.eu/doc/current/knowledge-base/freme-for-api-users/freme-ner.html).

NOTE: The documentation below is old. Currently, the code in this repository is sparely documented. We will be adding documentation, building and deployment instructions and polishing the code gradually.

Installation
------------

The tool can be installed using Maven, so make sure you have installed it first: http://maven.apache.org/download.cgi and java 1.8

    mvn clean install

#### Prerequisites

* Running Apache Solr instance. Read [how-to](http://lucene.apache.org/solr/quickstart.html).


Usage
-----

You can submit text for processing by executing the following command.

    curl -v -d "The Charles Bridge is a famous historic bridge that crosses the Vltava river in Prague, Czech Republic." "http://localhost:8080/api/documents?language=en&dataset=dbpedia"

Acknowledgments
---------------

The development of this software is supported by the [FREME H2020](http://www.freme-project.eu/) project.

## License

Copyright 2015  Agro-Know, Deutsches Forschungszentrum für Künstliche Intelligenz, iMinds, 
Institut für Angewandte Informatik e. V. an der Universität Leipzig, 
Istituto Superiore Mario Boella, Tilde, Vistatec, WRIPL

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

This project uses 3rd party tools. You can find the list of 3rd party tools including their authors and licenses [here](LICENSE-3RD-PARTY).

