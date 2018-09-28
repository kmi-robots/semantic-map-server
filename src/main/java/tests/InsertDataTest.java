package tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.util.FileUtils;
import org.topbraid.jenax.util.JenaUtil;

public class InsertDataTest {

	public static void main(String[] args) throws FileNotFoundException {
		
		Model dataModel = JenaUtil.createMemoryModel();
		InputStream in = new FileInputStream(new File("./resources/hans-shacl-no-violation.ttl"));
		dataModel.read(in,null, FileUtils.langTurtle);
		
		String queryFormat = String.format(" prefix hsf: <http://data.open.ac.uk/kmi/hans/>"
				+ "INSERT DATA { "
				+ "		%s   a hsf:PointShape ; "
				+ "		hsf:hasXCoordinate %f ; "
				+ "		hsf:hasYCoordinate %f .}", "hsf:Point33", 3.13, 3.333);

		UpdateAction.parseExecute(queryFormat, dataModel);
		RDFDataMgr.write(System.out, dataModel, RDFFormat.TURTLE);
		
	}

}
