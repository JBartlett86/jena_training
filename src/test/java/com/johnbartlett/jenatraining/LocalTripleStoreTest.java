package com.johnbartlett.jenatraining;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.apache.jena.vocabulary.VCARD;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Testing reading and writing from a locally stored triple store
 *
 * Created by john.bartlett on 03/06/2016.
 */
public class LocalTripleStoreTest {

    private static final String URI = "http://www.jenatraining.com/2016/properties/1.0#";

    @Test
    public void testLocalTripleStore() throws IOException {

        Dataset ds = TDBFactory.createDataset();
        ds.begin(ReadWrite.WRITE) ;

        Model m = ds.getDefaultModel();

        // properties
        Property writtenBy = m.createProperty(URI, "Was_written_by");
        Property numberOfPages = m.createProperty(URI, "Number_of_pages");
        Property publishedInYear = m.createProperty(URI, "Published_in_year");

        Resource r1 = m.createResource("https://en.wikipedia.org/wiki/Patricia_Cornwell");
        r1.addProperty(VCARD.FN, "Patricia Cornwell");
        r1.addProperty(VCARD.N, m.createResource().addProperty(VCARD.Given, "Patricia").addProperty(VCARD.Family, "Cornwell"));

        m.createResource("https://en.wikipedia.org/wiki/Postmortem_%28novel%29").addProperty(writtenBy, r1).addProperty(numberOfPages, "150").addProperty(publishedInYear, "1990");
        m.createResource("https://en.wikipedia.org/wiki/Body_of_Evidence_%28novel%29").addProperty(writtenBy, r1).addProperty(numberOfPages, "300").addProperty(publishedInYear, "2000");

        // Turn model into String
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        RDFDataMgr.write(bas, m, RDFFormat.NTRIPLES);
        bas.close();

        // Execute Update
        UpdateRequest u = UpdateFactory.create();
        u.add("insert { " + bas.toString() + "} where {}");
        UpdateProcessor updateProcessor = UpdateExecutionFactory.create(u, ds);
        updateProcessor.execute();

        ds.commit();
        ds.end();

        ds.begin(ReadWrite.READ);

        String qs1 = "SELECT * {?s ?p ?o} LIMIT 10" ;
        try(QueryExecution qExec = QueryExecutionFactory.create(qs1, ds)) {
            ResultSet rs = qExec.execSelect() ;
            ResultSetFormatter.out(rs) ;
        }
    }

}
