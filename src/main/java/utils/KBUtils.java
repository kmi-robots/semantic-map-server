package utils;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import semanticmap.Namespaces;

public class KBUtils {
	
	// given a resource, it is added to the model model
	public static Model generateSingleResourceModel(Model model, Resource rule) {
		
		for(StmtIterator i = rule.listProperties(); i.hasNext(); ) {
			Statement s = i.nextStatement();
			
			if(s.getObject().isAnon()) {
				model.add(s);
				model = generateSingleResourceModel(model, s.getObject().asResource());	
			}
			else {
				model.add(s);
			}
		}
	
		return model;
	}
	
	// try to read the property geo:asWKT either from the object of the property
	// geo:hasGeometry of the current Resource r
	// or directly from the current Resource r
	// TODO add extension for ros:Geometry
	public static Geometry getGeometryFromResource(Resource r, Model model, WKTReader wktReader) {
		
		Property wktProperty = model.getProperty(Namespaces.geosparqlPrefix + "asWKT");
		Property hasGeometryProperty = model.getProperty(Namespaces.geosparqlPrefix + "hasGeometry");
		Resource resourceWithWKT = r;
		
		if(r.hasProperty(hasGeometryProperty)) {
			Statement s = r.getProperty(hasGeometryProperty);
			resourceWithWKT = s.getResource();
		}
		
		if(resourceWithWKT.hasProperty(wktProperty)) {
			try {
				Statement s = resourceWithWKT.getProperty(wktProperty);
				return wktReader.read(s.getLiteral().toString());
			}
			catch (ParseException e) {
				e.printStackTrace();
				return null;
			}
		}
		else {
			return null;
		}
	}
}
