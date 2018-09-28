package tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.validation.ValidationUtil;


public class testJenaSHACL {
	
	static Logger logger = LoggerFactory.getLogger(testJenaSHACL.class);
	
	public static void main(String[] args) throws FileNotFoundException {
		
		Model dataModel = JenaUtil.createMemoryModel();
		InputStream in = new FileInputStream(new File("./resources/hans-shacl-no-violation.ttl"));
//		InputStream in = new FileInputStream(new File("./resources/hans-shacl.ttl"));
		dataModel.read(in,null, FileUtils.langTurtle);
		  
		// Perform the validation of everything, using the data model
		// also as the shapes model - you may have them separated
		Resource report = ValidationUtil.validateModel(dataModel, dataModel, true);
//				
//		// Print violations
//		System.out.println(ModelPrinter.get().print(report.getModel()));
		RDFDataMgr.write(System.out, report.getModel(), RDFFormat.JSONLD);	
	}
}
