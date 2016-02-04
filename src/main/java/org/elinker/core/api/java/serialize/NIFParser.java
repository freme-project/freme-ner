package org.elinker.core.api.java.serialize;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;


/**
 * Create a Document containing the raw context and list of EntityMentions from a NIF document.
 *
 * @author Milan Dojchinovski <milan.dojchinovski@fit.cvut.cz>, Nilesh Chakraborty <nilesh@nileshc.com>
 * http://dojchinovski.mk
 */
public class NIFParser {

    public Document getDocumentFromNIFString(String nifString) {
        ArrayList<EntityMention> list = new ArrayList<>();
        Model model = ModelFactory.createDefaultModel();
        model.read(new ByteArrayInputStream(nifString.getBytes()),null, "TTL");
        StmtIterator iter = model.listStatements(null, RDF.type, model.getResource("http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#Phrase"));
        while(iter.hasNext()) {
            Statement stm = iter.nextStatement();
            Resource entityRes = stm.getSubject().asResource();
            String mention = entityRes.getProperty(model.getProperty("http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#anchorOf")).getObject().asLiteral().getString();
            int beginIndex = entityRes.getProperty(model.getProperty("http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#beginIndex")).getObject().asLiteral().getInt();
            int endIndex = entityRes.getProperty(model.getProperty("http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#endIndex")).getObject().asLiteral().getInt();
            EntityMention em = new EntityMention();
            em.setMention(mention);
            em.setBeginIndex(beginIndex);
            em.setEndIndex(endIndex);
            list.add(em);
        }

        iter = model.listStatements(null, RDF.type, model.getResource("http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#Context"));
        Statement stm = iter.nextStatement();
        Resource contextRes = stm.getSubject().asResource();
        String text = contextRes.getProperty(model.getProperty("http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#isString")).getObject().asLiteral().getString();

        return new Document(list, text);
    }
}
