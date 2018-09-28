package tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.util.FileUtils;
import org.topbraid.jenax.util.JenaUtil;

public class AskQueryTest {

	public static void main(String[] args) throws IOException {
		Model dataModel = JenaUtil.createMemoryModel();
		InputStream in = new FileInputStream(new File("./resources/hans-complete-0.1.ttl"));
		dataModel.read(in,null, FileUtils.langTurtle);
		
		String instanceURI = "http://data.open.ac.uk/kmi#Playroom_aaasssAAAA";
		
		String query1 = String.format(
				"  prefix hsf: <http://data.open.ac.uk/kmi/hans#>"
				+ "INSERT DATA { "
				+ "		<%s>   a hsf:Area ; "
				+ " .}", instanceURI);
		
		System.out.println(query1);
		UpdateAction.parseExecute(query1, dataModel);
		
		//String q1 = "  prefix hsf: <http://data.open.ac.uk/kmi/hans#> prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ASK { 		<http://data.open.ac.uk/kmi/hans#Point_e9464bd7-ce0c-4d23-ba47-cebacd131621>   rdf:type/rdfs:subClassOf* hsf:Entity .}";
		
		String query = String.format(
				"  prefix hsf: <http://data.open.ac.uk/kmi/hans#> "
				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "ASK { "
				+ "		<%s>   rdf:type/rdfs:subClassOf* hsf:Entity "
				+ ".}", instanceURI);
		System.out.println(query);
		
		QueryExecution qexec = QueryExecutionFactory.create(query, dataModel);
		System.out.println(qexec.execAsk());
		
		FileOutputStream fos = new FileOutputStream("./resources/test.ttl");
		RDFDataMgr.write(fos, dataModel, RDFFormat.TURTLE);
		fos.flush();
		fos.close();
		
	}

}
