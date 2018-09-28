package tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.util.FileUtils;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.validation.ValidationUtil;

public class ValidateSingleRuleTest {

	public static void main(String[] args) throws FileNotFoundException {
		Model kbDataModel = JenaUtil.createMemoryModel();
		InputStream kbIn = new FileInputStream(new File("./resources/hans-shacl-only-kb-no-rule.ttl"));
		kbDataModel.read(kbIn, null, FileUtils.langTurtle);
		
		Model ruleDataModel = JenaUtil.createMemoryModel();
		InputStream ruleIn = new FileInputStream(new File("./resources/hans-shacl-only-rules-no-kb.ttl"));
		ruleDataModel.read(ruleIn, null, FileUtils.langTurtle);
		
		// CHECK 1: two separate files for two separate models
		//Resource report = ValidationUtil.validateModel(kbDataModel, ruleDataModel, true);
		//RDFDataMgr.write(System.out, report.getModel(), RDFFormat.JSONLD);	
		
		System.out.println("Check 2");
		Resource ruleResource = ruleDataModel.getResource("http://data.open.ac.uk/kmi/hans/heaterFreeAreaRule");
		Model m = JenaUtil.createDefaultModel();
		m = generateSingleRuleModel(m, ruleResource);
		
		Resource report2 = ValidationUtil.validateModel(kbDataModel, m, true);
		RDFDataMgr.write(System.out, report2.getModel(), RDFFormat.JSONLD);	
	}
	
	public static Model generateSingleRuleModel(Model model, Resource rule) {
		
		for(StmtIterator i = rule.listProperties(); i.hasNext(); ) {
			Statement s = i.nextStatement();
			
			if(s.getObject().isAnon()) {
				//System.out.println( s.getObject().asResource());
				model.add(s);
				model = generateSingleRuleModel(model, s.getObject().asResource());	
			}
			else {
				model.add(s);
			}
		}
			
		return model;
	}
		

}
