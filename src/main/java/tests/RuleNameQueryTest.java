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
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.resultset.RDFOutput;
import org.apache.jena.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.jenax.util.JenaUtil;

public class RuleNameQueryTest {
static Logger logger = LoggerFactory.getLogger(testJenaSHACL.class);
	
	public static void main(String[] args) throws FileNotFoundException {
		
		Model dataModel = JenaUtil.createMemoryModel();
//		InputStream in = new FileInputStream(new File("./resources/hans-complete-no-instances-0.1.ttl"));
		InputStream in = new FileInputStream(new File("./resources/hans-complete-0.1.ttl"));
		dataModel.read(in,null, FileUtils.langTurtle);
		
//		Query query = QueryFactory.create("prefix hsf: <http://data.open.ac.uk/kmi/hans/>"
//				+ " prefix : <http://data.open.ac.uk/hans/shacl#>"
//				+ " prefix sh: <http://www.w3.org/ns/shacl#>"
//				+ " SELECT ?shape ?coord WHERE {"
//				+ " 	?item hsf:hasShape ?shape ."
//				+ " 	BIND( IF ( EXISTS{ ?shape a hsf:PointShape. ?shape hsf:hasXCoordinate ?x . ?shape hsf:hasYCoordinate ?y. }, STR(?x)  , \"unkwnown\" ) as  ?coord) ."
//				+ "}");
		
		String rule = "hsf:heaterFreeAreaRule";
		

		
		String queryFormat = String.format(""
				+ " prefix hsf: <http://data.open.ac.uk/kmi/hans#>"
				+ " prefix sh: <http://www.w3.org/ns/shacl#>"
				+ " prefix geo: <http://www.opengis.net/ont/geosparql#>"
				+ " prefix schema: <http://schema.org/>"
				+ " SELECT ?item ?coord WHERE {"
				+ "		%s a sh:NodeShape . "
				+ "		%s sh:targetClass ?class ."
				+ "		?item a ?class . "
				+ " 	OPTIONAL { ?item geo:hasGeometry ?geometry.  "
				+ "				   ?geometry geo:asWKT ?wkt . "
				+ "                BIND(?wkt as ?coord) } ."
				+ "     OPTIONAL { ?item geo:asWKT ?wkt .  "
				+ "				   ?geometry geo:asWKT ?wkt . "
				+ "                BIND(?wkt as ?coord) } ."
				+ " }", rule, rule);
		  try (QueryExecution qexec = QueryExecutionFactory.create(queryFormat, dataModel)) {

			  ResultSet results = qexec.execSelect();
			  
			  System.out.println(results.hasNext());
			  for ( ; results.hasNext() ; ) {
				  
				  QuerySolution soln = results.nextSolution() ;
//				  System.out.println(soln.get("item").toString()  + " " + soln.get("coord").toString() );
				  System.out.println(soln.get("coord").toString());
			  }
			  
			
			  QueryExecution qexec2 = QueryExecutionFactory.create(queryFormat, dataModel);
			  ResultSet results2 = qexec2.execSelect();
				RDFOutput a = new RDFOutput();
				Model aResSet = a.asModel(results2);
				RDFDataMgr.write(System.out, aResSet, RDFFormat.JSONLD);
		  }
			
		// Perform the validation of everything, using the data model
		// also as the shapes model - you may have them separated
//		Resource report = ValidationUtil.validateModel(dataModel, dataModel, true);
//				
//		// Print violations
//		System.out.println(ModelPrinter.get().print(report.getModel()));
			
	}
}
