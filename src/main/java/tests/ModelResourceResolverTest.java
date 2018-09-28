package tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileUtils;
import org.topbraid.jenax.util.JenaUtil;

public class ModelResourceResolverTest {

	public static void main(String[] args) throws FileNotFoundException {
		Model dataModel = JenaUtil.createMemoryModel();
//		InputStream in = new FileInputStream(new File("./resources/hans-complete-no-instances-0.1.ttl"));
		InputStream in = new FileInputStream(new File("./resources/hans-complete-0.1.ttl"));
		dataModel.read(in,null, FileUtils.langTurtle);
		
		Resource r = dataModel.getResource("<http://data.open.ac.uk/kmi/hans#fireActionNoticeRule>");
		System.out.println(r.listProperties().toList().toString());
		System.out.println("Pii " + r.getNameSpace());
	}

}
