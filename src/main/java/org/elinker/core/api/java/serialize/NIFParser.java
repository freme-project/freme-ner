package org.elinker.core.api.java.serialize;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import eu.freme.common.conversion.rdf.JenaRDFConversionService;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

import static eu.freme.common.conversion.rdf.RDFConstants.*;


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
        model.read(new ByteArrayInputStream(nifString.getBytes()),null, JenaRDFConversionService.JENA_TURTLE);
        StmtIterator iter = model.listStatements(null, RDF.type, model.getResource(nifPrefix+NIF_PHRASE_TYPE));
        while(iter.hasNext()) {
            Statement stm = iter.nextStatement();
            Resource entityRes = stm.getSubject().asResource();
            String mention = entityRes.getProperty(model.getProperty(nifPrefix+ANCHOR_OF)).getObject().asLiteral().getString();
            int beginIndex = entityRes.getProperty(model.getProperty(nifPrefix+BEGIN_INDEX)).getObject().asLiteral().getInt();
            int endIndex = entityRes.getProperty(model.getProperty(nifPrefix+END_INDEX)).getObject().asLiteral().getInt();
            EntityMention em = new EntityMention();
            em.setMention(mention);
            em.setBeginIndex(beginIndex);
            em.setEndIndex(endIndex);
            list.add(em);
        }

        iter = model.listStatements(null, RDF.type, model.getResource(nifPrefix+NIF_CONTEXT_TYPE));
        Statement stm = iter.nextStatement();
        Resource contextRes = stm.getSubject().asResource();
        String text = contextRes.getProperty(model.getProperty(nifPrefix+IS_STRING)).getObject().asLiteral().getString();

        return new Document(list, text);
    }
}
