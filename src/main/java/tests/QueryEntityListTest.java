package tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.util.FileUtils;
import org.topbraid.jenax.util.JenaUtil;

public class QueryEntityListTest {

	public static void main(String[] args) throws FileNotFoundException {
		Model dataModel = JenaUtil.createMemoryModel();
		InputStream in = new FileInputStream(new File("./resources/hans-complete-0.1.ttl"));
		dataModel.read(in,null, FileUtils.langTurtle);
		
		String query = String.format(
				"   prefix sh: <http://www.w3.org/ns/shacl#> "
				+ " prefix hsf: <http://data.open.ac.uk/kmi/hans#> "
				+ " prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ " prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ " prefix schema: <http://schema.org/> "
				+ " prefix geo: <http://www.opengis.net/ont/geosparql#> "
				+ " SELECT ?entity ?name ?wkt WHERE {"
				+ " 	?entity rdf:type/rdfs:subClassOf* <%s> ."
				+ " 	?entity schema:name ?name . "
				+ "		?entity geo:hasGeometry ?geometry ."
				+ "		?geometry geo:asWKT ?wkt ."
				+ " }", "http://data.open.ac.uk/kmi/hans#HeaterFreeArea");
				
					
		QueryExecution qexec = QueryExecutionFactory.create(query, dataModel);
		ResultSet results = qexec.execSelect();	
		
		for ( ; results.hasNext() ; ) {
  
			QuerySolution soln = results.nextSolution() ;
			System.out.println(soln.get("entity").toString() + " " + soln.get("name").toString() + " " + soln.get("wkt").toString());
		}	  
		  
	}

}
