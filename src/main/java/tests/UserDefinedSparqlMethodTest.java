package tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.function.FunctionRegistry;
import org.apache.jena.sparql.resultset.RDFOutput;
import org.apache.jena.util.FileUtils;
import org.json.JSONException;
import org.topbraid.jenax.util.JenaUtil;

import sparql.functions.Overlaps;

public class UserDefinedSparqlMethodTest {

	public static void main(String[] args) throws FileNotFoundException, JSONException, UnsupportedEncodingException {
		Model kbDataModel = JenaUtil.createMemoryModel();
		InputStream kbIn = new FileInputStream(new File("./resources/geometry-kb.ttl"));
		kbDataModel.read(kbIn, null, FileUtils.langTurtle);
		FunctionRegistry.get().put("http://data.open.ac.uk/kmi/hans/overlap", Overlaps.class);
		
		Query query = QueryFactory.create(""
				+ " prefix hsf: <http://data.open.ac.uk/kmi/hans/>"
				+ " SELECT ?res WHERE {"
				+ " ?area a hsf:Area ."
				+ " ?object a hsf:Object ."
				+ " ?object hsf:hasCenter ?c . "
				+ " ?area hsf:hasShape ?s . "
				+ " BIND(hsf:overlap(?s, ?c) as ?res) ."
				+ "}");
					
		QueryExecution qexec = QueryExecutionFactory.create(query, kbDataModel);
		ResultSet results = qexec.execSelect();	
		
		RDFOutput a = new RDFOutput();
		Model aResSet = a.asModel(results);
		RDFDataMgr.write(System.out, aResSet, RDFFormat.JSONLD);	
	}

}
