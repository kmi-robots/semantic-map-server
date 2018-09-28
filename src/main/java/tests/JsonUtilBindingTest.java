package tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.util.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.WKTWriter;
import org.topbraid.jenax.util.JenaUtil;

import semanticmap.KBInterface;
import utils.JsonUtils;

public class JsonUtilBindingTest {

	public static void main(String[] args) throws FileNotFoundException, JSONException, UnsupportedEncodingException {
		Model dataModel = JenaUtil.createMemoryModel();
		InputStream in = new FileInputStream(new File("./resources/hans-shacl-gianluca-ros-test.ttl"));
		dataModel.read(in,null, FileUtils.langTurtle);
		
		KBInterface kbInt = new KBInterface(dataModel, new GeometryFactory(), new WKTWriter());
		JSONObject o = kbInt.testQuery();
		System.out.println(o);
		System.out.println(JsonUtils.bindJsonLDResultSet(o));
	}

}
