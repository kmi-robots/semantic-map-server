package tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.function.FunctionRegistry;
import org.apache.jena.util.FileUtils;
import org.locationtech.jts.io.WKTReader;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.validation.ValidationUtil;

import sparql.functions.Contains;
import sparql.functions.Covers;
import sparql.functions.Crosses;
import sparql.functions.Disjoint;
import sparql.functions.Equals;
import sparql.functions.Intersects;
import sparql.functions.IsWithinDistance;
import sparql.functions.Overlaps;
import sparql.functions.Touches;
import sparql.functions.Within;

public class ValidateKB {

	public static void main(String[] args) throws FileNotFoundException {
		Model dataModel = JenaUtil.createMemoryModel();
		InputStream in = new FileInputStream(new File("./resources/hans-complete-0.1.ttl"));
		dataModel.read(in,null, FileUtils.langTurtle);
		
		WKTReader wktReader = new WKTReader();
		FunctionRegistry.get().put("http://data.open.ac.uk/kmi/hans#contains", Contains.class);
		Contains.model = dataModel;
		Contains.wktReader = wktReader;
		FunctionRegistry.get().put("http://data.open.ac.uk/kmi/hans#covers", Covers.class);
		Covers.model = dataModel;
		Covers.wktReader = wktReader;		
		FunctionRegistry.get().put("http://data.open.ac.uk/kmi/hans#crosses", Crosses.class);		
		Crosses.model = dataModel;
		Crosses.wktReader = wktReader;
		FunctionRegistry.get().put("http://data.open.ac.uk/kmi/hans#disjoint", Disjoint.class);		
		Disjoint.model = dataModel;
		Disjoint.wktReader = wktReader;
		FunctionRegistry.get().put("http://data.open.ac.uk/kmi/hans#equals", Equals.class);		
		Equals.model = dataModel;
		Equals.wktReader = wktReader;
		FunctionRegistry.get().put("http://data.open.ac.uk/kmi/hans#intersects", Intersects.class);		
		Intersects.model = dataModel;
		Intersects.wktReader = wktReader;
		FunctionRegistry.get().put("http://data.open.ac.uk/kmi/hans#overlap", Overlaps.class);		
		Overlaps.model = dataModel;
		Overlaps.wktReader = wktReader;
		FunctionRegistry.get().put("http://data.open.ac.uk/kmi/hans#touches", Touches.class);		
		Touches.model = dataModel;
		Touches.wktReader = wktReader;
		FunctionRegistry.get().put("http://data.open.ac.uk/kmi/hans#within", Within.class);		
		Within.model = dataModel;
		Within.wktReader = wktReader;
		FunctionRegistry.get().put("http://data.open.ac.uk/kmi/hans#isWithinDistance", IsWithinDistance.class);		
		IsWithinDistance.model = dataModel;
		IsWithinDistance.wktReader = wktReader;
		
		Query query = QueryFactory.create("prefix hsf: <http://data.open.ac.uk/kmi/hans#>"
		+ " prefix schema: <http://schema.org/>"		
		+ "SELECT  ?t ?hname ?name "
		+ "WHERE {"
		+ " ?heater  a hsf:Heater ."
        + " ?heater  schema:name ?hname ."
        + " ?t a hsf:HeaterFreeArea ."
        + " ?t  schema:name ?name ."
        + " FILTER(hsf:isWithinDistance(\"eee\", ?heater, \"cccsss\"))"
        + "}");
		
  try  {
	  QueryExecution qexec = QueryExecutionFactory.create(query, dataModel);
	  ResultSet results = qexec.execSelect();
	  for ( ; results.hasNext() ; ) {
		  
		  QuerySolution soln = results.nextSolution() ;
//		  System.out.println(soln.get("t").toString() + " " + soln.get("hname").toString() + " " + soln.get("name").toString());
		  System.out.println(soln.get("name").toString() + " " + soln.get("hname").toString());

	  }
  } catch ( Exception e) {
	e.printStackTrace();
}
		
		
		Resource report = ValidationUtil.validateModel(dataModel, dataModel, true);
		RDFDataMgr.write(System.out, report.getModel(), RDFFormat.JSONLD);	

	}

}
