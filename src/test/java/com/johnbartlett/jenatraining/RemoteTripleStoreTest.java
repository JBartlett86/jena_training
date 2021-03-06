package com.johnbartlett.jenatraining;

import org.apache.jena.query.DatasetAccessor;
import org.apache.jena.query.DatasetAccessorFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.apache.jena.vocabulary.DB;
import org.apache.jena.vocabulary.VCARD;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;

/**
 * Simple test of reading and writing to a remote triple store
 *
 * Created by johnbartlett on 02/06/2016.
 */
public class RemoteTripleStoreTest {


    private static final String URI = "http://www.jenatraining.com/2016/properties/1.0#";

    private static final String SERVER_URL = "http://localhost:3030";

    private static final String DB_NAME = "jenatraining";

    @Ignore
    @Test
    public void testWriteToRemoteTripleStore() throws IOException {

        DatasetAccessor dsa = DatasetAccessorFactory.createHTTP(String.format("%s/data", SERVER_URL));
        Model m = dsa.getModel();

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
        UpdateProcessor p = UpdateExecutionFactory.createRemote(u, String.format("%s/%s/update", SERVER_URL, DB_NAME));
        p.execute();

        m.close();
    }

    @Ignore
    @Test
    public void testReadFromTripleStore() {
        DatasetAccessor dsa = DatasetAccessorFactory.createHTTP(String.format("%s/%s/data", SERVER_URL, DB_NAME));
        Model m = dsa.getModel();
        RDFDataMgr.write(System.out, m, RDFFormat.NTRIPLES);
        m.close();
    }

}
