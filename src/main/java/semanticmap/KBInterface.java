package semanticmap;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.util.SplitIRI;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.WKTWriter;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.validation.ValidationUtil;

import utils.JsonUtils;
import utils.KBUtils;

public class KBInterface {
	
	private Model dataModel;
	private GeometryFactory gf;
	private WKTWriter wktWriter;
	
	public KBInterface(Model dataModel, GeometryFactory gf, WKTWriter wktWriter) {
		this.dataModel = dataModel;
		this.gf = gf;
		this.wktWriter = wktWriter;
	}
	
	// this is just a query test method
	public JSONObject testQuery() throws JSONException, UnsupportedEncodingException {
		Query query = QueryFactory.create(""
				+ " prefix sh: <http://www.w3.org/ns/shacl#>"
				+ " prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ " SELECT ?rule  WHERE { "
				+ " ?rule a sh:NodeShape ."
				+ " ?rule rdfs:comment ?description . "
				+ "}");
					
		QueryExecution qexec = QueryExecutionFactory.create(query, dataModel);
		ResultSet results = qexec.execSelect();	
		return JsonUtils.resultSetToJsonLD(results);
	}
	
	public JSONObject queryRuleList() throws JSONException, UnsupportedEncodingException {
		
		Query query = QueryFactory.create(""
				+ " prefix sh: <http://www.w3.org/ns/shacl#>"
				+ " prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ " SELECT ?rule ?comment ?target ?description WHERE {"
				+ " ?rule a sh:RuleNodeShape ."
				+ " ?rule rdfs:comment ?comment ."
				+ " ?rule sh:targetClass ?target ."
				+ " ?rule sh:description ?description ."
				+ "}");
					
		QueryExecution qexec = QueryExecutionFactory.create(query, dataModel);
		ResultSet results = qexec.execSelect();	
		return JsonUtils.resultSetToJsonLD(results);
	}
	
	public JSONObject queryRuleWaypointList(String rule) throws JSONException, UnsupportedEncodingException {
		
//		String ruleForResource = rule;
//
//		if(ruleForResource.startsWith("<")) {
//			ruleForResource = ruleForResource.substring(1);
//		}
//		if(ruleForResource.endsWith(">")) {
//			ruleForResource = ruleForResource.substring(0, ruleForResource.length()-1);
//		}

		Resource resource = dataModel.getResource(rule);
		
		// if the resource is in the wrong format (not a URI)
		// the resource will have a listProperties of size 0
		if(!resource.listProperties().hasNext()) {
			throw new DatatypeFormatException("The resource you're looking for does not exist, or the URI is misspelled");
		}
		
		String queryFormat = String.format(""
				+ " prefix hsf: <http://data.open.ac.uk/kmi/hans#>"
				+ " prefix sh: <http://www.w3.org/ns/shacl#>"
				+ " prefix geo: <http://www.opengis.net/ont/geosparql#>"
				+ " prefix schema: <http://schema.org/>"
				+ " SELECT ?item ?coord WHERE {"
				+ "		<%s> a sh:RuleNodeShape . "
				+ "		<%s> sh:targetClass ?class ."
				+ "		?item a ?class . "
				+ " 	OPTIONAL { ?item geo:hasGeometry ?geometry.  "
				+ "				   ?geometry geo:asWKT ?wkt . "
				+ "                BIND(?wkt as ?coord) } ."
				+ "     OPTIONAL { ?item geo:asWKT ?wkt .  "
				+ "				   ?geometry geo:asWKT ?wkt . "
				+ "                BIND(?wkt as ?coord) } ."
				+ " }", rule, rule);
	
		Query query = QueryFactory.create(queryFormat);
							
		QueryExecution qexec = QueryExecutionFactory.create(query, dataModel);
		ResultSet results = qexec.execSelect();	
		return JsonUtils.resultSetToJsonLD(results);
	}

	public JSONObject validateKB() throws JSONException, UnsupportedEncodingException {
		Resource report = ValidationUtil.validateModel(dataModel, dataModel, true);
		return JsonUtils.validationSetToJsonLD(report);
	}
	
	public JSONObject validateSingleRule(String rule) throws JSONException, UnsupportedEncodingException {
		
		Model emptyModel = JenaUtil.createDefaultModel();
		
//		if(rule.startsWith("<")) {
//			rule = rule.substring(1);
//		}
//		if(rule.endsWith(">")) {
//			rule = rule.substring(0, rule.length()-1);
//		}
		
		Resource resource = dataModel.getResource(rule);
		
		// if the resource is in the wrong format (not a URI)
		// the resource will have a listProperties of size 0
		if(!resource.listProperties().hasNext()) {
			throw new DatatypeFormatException("The resource you're looking for does not exist, or the URI is misspelled");
		}
		
		KBUtils.generateSingleResourceModel(emptyModel, resource);
		Resource report = ValidationUtil.validateModel(dataModel, emptyModel, true);
		return JsonUtils.validationSetToJsonLD(report);	
	}
	
	// insert an object whose position is represented by a point
	// what should I return if the object is not added
	public JSONObject insertPointEntity(JSONObject pointEntity) throws InterruptedException {
		
		// TODO check on pointObject fields
		// TODO how to know whether the INSERT DATA was successful?
		
		String uuid = UUID.randomUUID().toString();
		JSONObject entityGeometry = pointEntity.getJSONObject("geometry");
		ArrayList<String> insertedInstances = new ArrayList<>();
		boolean allInserted = true;
		
		double entityX = entityGeometry.getJSONObject("pose").getJSONObject("position").getFloat("x");
		double entityY = entityGeometry.getJSONObject("pose").getJSONObject("position").getFloat("y");
		double entityZ = entityGeometry.getJSONObject("pose").getJSONObject("position").getFloat("z");
		
		double entityOrientationX = entityGeometry.getJSONObject("pose").getJSONObject("orientation").getFloat("x");
		double entityOrientationY = entityGeometry.getJSONObject("pose").getJSONObject("orientation").getFloat("y");
		double entityOrientationZ = entityGeometry.getJSONObject("pose").getJSONObject("orientation").getFloat("z");
		double entityOrientationW = entityGeometry.getJSONObject("pose").getJSONObject("orientation").getFloat("w");
		
		// inserting point first
		// this is used to generate the WKT format
		Point point = this.gf.createPoint(new Coordinate(entityX, entityY, entityZ));
		String wkt = this.wktWriter.write(point);
		
		String pointClassURI = Namespaces.geosparqlPrefix + "Point";
		String pointInstanceURI = Namespaces.hsfPrefix + "Point_" + uuid;
		insertGeometry(pointInstanceURI, pointClassURI, wkt);
		
		if(checkPresence(pointInstanceURI, pointClassURI))
			insertedInstances.add(pointInstanceURI);
		else allInserted = false;

		// inserting ros orientation
		String rosOrientationURI = Namespaces.rosPrefix + "Orientation_" + uuid;
		insertRosQuaternion(rosOrientationURI, entityOrientationX, entityOrientationY, entityOrientationZ, entityOrientationW);
		
		if(checkPresence(rosOrientationURI, Namespaces.rosPrefix + "Quaternion"))
			insertedInstances.add(rosOrientationURI);
		else allInserted = false;
			
		// inserting pose
		String poseURI = Namespaces.hsfPrefix  + "Pose_" + uuid;
		insertPose(poseURI, pointInstanceURI, rosOrientationURI);
		
		if(checkPresence(poseURI, Namespaces.rosPrefix + "Pose"))
			insertedInstances.add(poseURI);
		else allInserted = false;
		
		// inserting the point entity
		String entityInstanceURI = Namespaces.hsfPrefix + pointEntity.getString("class") + "_" + uuid;
		String entityClassURI = Namespaces.hsfPrefix + pointEntity.getString("class");
		long entityTimestamp = pointEntity.getLong("timestamp");
		
		String entityName = null;
		String entityId = null;
		try {
			entityName = pointEntity.getString("name");	
		}
		catch (JSONException e) {}
		
		try {
			entityId = pointEntity.getString("id");
		}
		catch (JSONException e) {}
	
		insertEntity(entityClassURI, entityInstanceURI, entityTimestamp, pointInstanceURI, entityId, entityName);
		addPoseToEntity(entityInstanceURI, poseURI);
		
		if(checkPresence(entityInstanceURI, entityClassURI))
			insertedInstances.add(entityInstanceURI);
		else allInserted = false;
		
		if(!allInserted) {
			removeAllInstancesFromKB(insertedInstances);
			return pointEntity;
		}
		else return null;
		
	}

	// TODO: we do not manage holes
	public JSONObject insertAreaEntity(JSONObject areaEntity) {
		String uuid = UUID.randomUUID().toString();
		
		JSONArray points = areaEntity.getJSONObject("geometry").getJSONArray("points");
		Coordinate[] coordinates = new Coordinate[points.length()];
		ArrayList<String> insertedInstances = new ArrayList<>();
		boolean allInserted = true;
		
		for (int i = 0; i < points.length(); ++i) {
			JSONObject jsonPoint = points.getJSONObject(i);
			double x = jsonPoint.getFloat("x");
			double y = jsonPoint.getFloat("y");
			double z = jsonPoint.getFloat("z");
			
			coordinates[i] = new Coordinate(x, y, z);
		}
		
		// inserting the geometry
		Polygon area = this.gf.createPolygon(coordinates);
		String wkt = this.wktWriter.write(area);
		
		String areaClassURI = Namespaces.geosparqlPrefix + "Polygon";
		String areaInstanceURI = Namespaces.hsfPrefix + "Polygon_" + uuid;
		insertGeometry(areaInstanceURI, areaClassURI, wkt);
		
		if(checkPresence(areaInstanceURI, areaClassURI))
			insertedInstances.add(areaInstanceURI);
		else allInserted = false;
		
		// inserting the point entity
		String entityInstanceURI = Namespaces.hsfPrefix + areaEntity.getString("class") + "_" + uuid;
		String entityClassURI = Namespaces.hsfPrefix + areaEntity.getString("class");
		long entityTimestamp = areaEntity.getLong("timestamp");
		
		String entityName = null;
		String entityId = null;
		try {
			entityName = areaEntity.getString("name");	
		}
		catch (JSONException e) {}
		
		try {
			entityId = areaEntity.getString("id");
		}
		catch (JSONException e) {}
	
		insertEntity(entityClassURI, entityInstanceURI, entityTimestamp, areaInstanceURI, entityId, entityName);
		
		if(checkPresence(entityInstanceURI, entityClassURI))
			insertedInstances.add(entityInstanceURI);
		else allInserted = false;

		try {
			JSONObject pose =  areaEntity.getJSONObject("geometry").getJSONObject("pose");
			
			double entityX = pose.getJSONObject("position").getFloat("x");
			double entityY = pose.getJSONObject("position").getFloat("y");
			double entityZ = pose.getJSONObject("position").getFloat("z");
			
			double entityOrientationX = pose.getJSONObject("orientation").getFloat("x");
			double entityOrientationY = pose.getJSONObject("orientation").getFloat("y");
			double entityOrientationZ = pose.getJSONObject("orientation").getFloat("z");
			double entityOrientationW = pose.getJSONObject("orientation").getFloat("w");
			
			// inserting position
			Point point = this.gf.createPoint(new Coordinate(entityX, entityY, entityZ));
			String positionWkt = this.wktWriter.write(point);
			
			String positionPointClassURI = Namespaces.geosparqlPrefix + "Point";
			String positionPointInstanceURI = Namespaces.hsfPrefix + "Point_" + uuid;
			insertGeometry(positionPointInstanceURI, positionPointClassURI, positionWkt);
			
			if(checkPresence(positionPointInstanceURI, positionPointClassURI))
				insertedInstances.add(positionPointInstanceURI);
			else allInserted = false;
			
			// inserting ros orientation
			String rosOrientationURI = Namespaces.rosPrefix + "Orientation_" + uuid;
			insertRosQuaternion(rosOrientationURI, entityOrientationX, entityOrientationY, entityOrientationZ, entityOrientationW);
			
			if(checkPresence(rosOrientationURI, Namespaces.rosPrefix+"Quaternion"))
				insertedInstances.add(rosOrientationURI);
			else allInserted = false;
			
			// inserting pose
			String poseInstanceURI = Namespaces.hsfPrefix  + "Pose_" + uuid;
			insertPose(poseInstanceURI, positionPointInstanceURI, rosOrientationURI);
			
			if(checkPresence(poseInstanceURI, Namespaces.rosPrefix+"Pose"))
				insertedInstances.add(poseInstanceURI);
			else allInserted = false;

			addPoseToEntity(entityInstanceURI, poseInstanceURI);
			
		}
		catch (JSONException e) {}
		
		if(!allInserted) {
			removeAllInstancesFromKB(insertedInstances);
			return areaEntity;
		}
		else return null;
		
	}
	
	public JSONObject insertLineEntity(JSONObject lineEntity) {
		
		String uuid = UUID.randomUUID().toString();
		
		JSONArray points = lineEntity.getJSONObject("geometry").getJSONArray("points");
		Coordinate[] coordinates = new Coordinate[points.length()];
		ArrayList<String> insertedInstances = new ArrayList<>();
		boolean allInserted = true;
		
		for (int i = 0; i < points.length(); ++i) {
			JSONObject jsonPoint = points.getJSONObject(i);
			double x = jsonPoint.getFloat("x");
			double y = jsonPoint.getFloat("y");
			double z = jsonPoint.getFloat("z");
			
			coordinates[i] = new Coordinate(x, y, z);
		}
		
		// inserting the geometry
		LineString line = this.gf.createLineString(coordinates);
		String wkt = this.wktWriter.write(line);
		
		String lineClassURI = Namespaces.geosparqlPrefix + "Polygon";
		String lineInstanceURI = Namespaces.hsfPrefix + "Polygon_" + uuid;
		insertGeometry(lineInstanceURI, lineClassURI, wkt);
		
		if(checkPresence(lineInstanceURI, lineClassURI))
			insertedInstances.add(lineInstanceURI);
		else allInserted = false;
		
		// inserting the point entity
		String entityInstanceURI = Namespaces.hsfPrefix + lineEntity.getString("class") + "_" + uuid;
		String entityClassURI = Namespaces.hsfPrefix + lineEntity.getString("class");
		long entityTimestamp = lineEntity.getLong("timestamp");
		
		String entityName = null;
		String entityId = null;
		try {
			entityName = lineEntity.getString("name");	
		}
		catch (JSONException e) {}
		
		try {
			entityId = lineEntity.getString("id");
		}
		catch (JSONException e) {}
	
		insertEntity(entityClassURI, entityInstanceURI, entityTimestamp, lineInstanceURI, entityId, entityName);
		
		if(checkPresence(entityInstanceURI, entityClassURI))
			insertedInstances.add(entityInstanceURI);
		else allInserted = false;
		
		try {
			JSONObject pose =  lineEntity.getJSONObject("geometry").getJSONObject("pose");
			
			double entityX = pose.getJSONObject("position").getFloat("x");
			double entityY = pose.getJSONObject("position").getFloat("y");
			double entityZ = pose.getJSONObject("position").getFloat("z");
			
			double entityOrientationX = pose.getJSONObject("orientation").getFloat("x");
			double entityOrientationY = pose.getJSONObject("orientation").getFloat("y");
			double entityOrientationZ = pose.getJSONObject("orientation").getFloat("z");
			double entityOrientationW = pose.getJSONObject("orientation").getFloat("w");
			
			// inserting position
			Point point = this.gf.createPoint(new Coordinate(entityX, entityY, entityZ));
			String positionWkt = this.wktWriter.write(point);
			
			String positionPointClassURI = Namespaces.geosparqlPrefix + "Point";
			String positionPointInstanceURI = Namespaces.hsfPrefix + "Point_" + uuid;
			insertGeometry(positionPointInstanceURI, positionPointClassURI, positionWkt);
			
			if(checkPresence(positionPointInstanceURI, positionPointClassURI))
				insertedInstances.add(positionPointInstanceURI);
			else allInserted = false;
			
			// inserting ros orientation
			String rosOrientationURI = Namespaces.rosPrefix + "Orientation_" + uuid;
			insertRosQuaternion(rosOrientationURI, entityOrientationX, entityOrientationY, entityOrientationZ, entityOrientationW);
			
			if(checkPresence(rosOrientationURI, Namespaces.rosPrefix+"Quaternion"))
				insertedInstances.add(rosOrientationURI);
			else allInserted = false;
			
			// inserting pose
			String poseInstanceURI = Namespaces.hsfPrefix  + "Pose_" + uuid;
			insertPose(poseInstanceURI, positionPointInstanceURI, rosOrientationURI);
			
			if(checkPresence(poseInstanceURI, Namespaces.rosPrefix+"Pose"))
				insertedInstances.add(poseInstanceURI);
			else allInserted = false;
			
			addPoseToEntity(entityInstanceURI, poseInstanceURI);
		}
		catch (JSONException e) {}
		
		if(!allInserted) {
			removeAllInstancesFromKB(insertedInstances);
			return lineEntity;
		}
		else return null;
		
	}
	
	public JSONObject insertVolumeEntity(JSONObject volumeEntity) {
		// TODO this has to return like this because null is used for
		// successful insertions
		return new JSONObject();
	}
	
	public JSONArray updateWithSensing(JSONObject sensingData) throws InterruptedException {
				
		JSONArray detections = sensingData.getJSONArray("detections");
		JSONArray ret = new JSONArray();
		
		for (int i = 0; i < detections.length(); ++i) {
			JSONObject detection = detections.getJSONObject(i);
			String geomClass = detection.getJSONObject("geometry").getString("geom_class");
			
			if(geomClass.equals("point")) {
				JSONObject point = insertPointEntity(detection);
				
				if(point != null)
					ret.put(point);
			}
			else if(geomClass.equals("line")) {
				JSONObject line =  insertLineEntity(detection);
				
				if(line != null)
					ret.put(line);
			}
			else if(geomClass.equals("area")) {
				JSONObject area = insertAreaEntity(detection);
				
				if(area != null)
					ret.put(area);
			}
			else if(geomClass.equals("volume")) {
				// TODO add a log that says that volumes are not added
				insertVolumeEntity(detection);
			}		
		}
		
		return ret;
	}
	
	public void saveKBSnapshot(String file) throws IOException {
		
		// composing the name of the temporary knowledge base
		String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
		String fileExtension = file.substring(file.lastIndexOf("."), file.length());
		String fileName = file.substring(0, file.lastIndexOf("."));
		String tmpKbFile = fileName + "_" + timeStamp + fileExtension;
		
		System.out.println("Saving kb snapshot in " + tmpKbFile);
		FileOutputStream fos = new FileOutputStream(tmpKbFile);
		RDFDataMgr.write(fos, this.dataModel, RDFFormat.TURTLE);
		fos.flush();
		fos.close();
		System.out.println("done");
	}

	private void insertEntity(String entityClassURI, String entityInstanceURI, long entityTimestamp, String entityGeometryURI, String entityId, String entityName) {
		if(entityName == null || entityName.isEmpty()) {
			entityName = SplitIRI.localname(entityInstanceURI);
		}
		
		String query = String.format(
				"  prefix schema: <http://schema.org/> "
				+ "prefix hsf: <http://data.open.ac.uk/kmi/hans#> "
				+ "prefix geo: <http://www.opengis.net/ont/geosparql#> "
				+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> "
				+ "INSERT DATA { "
				+ "		<%s>   a <%s> ; "
				+ "		schema:name \"%s\" ;"
				+ "		hsf:hasTimestamp \"%d\"^^xsd:unsignedLong ; "
				+ "		geo:hasGeometry <%s>  "
				+ ".}", entityInstanceURI, entityClassURI, entityName, entityTimestamp, entityGeometryURI);
		
		if(entityId != null && !entityId.isEmpty()) {
			query = String.format(
					"  prefix hsf: <http://data.open.ac.uk/kmi/hans#> "
					+ "prefix geo: <http://www.opengis.net/ont/geosparql#> "
					+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#> "
					+ "prefix schema: <http://schema.org/> "
					+ "INSERT DATA { "
					+ "		<%s>   a <%s> ; "
					+ "		schema:name \"%s\" ;"
					+ "		hsf:hasTimestamp \"%d\"^^xsd:unsignedLong ; "
					+ "		geo:hasGeometry <%s> ; "
					+ "		hsf:hasId \"%s\"^^xsd:string"
					+ ".}", entityInstanceURI, entityClassURI, entityName, entityTimestamp, entityGeometryURI, entityId);
		}
		
		UpdateAction.parseExecute(query, dataModel);
	}
	
	private void addPoseToEntity(String entityInstanceURI, String poseInstanceURI) {
		String query = String.format(
				"  prefix hsf: <http://data.open.ac.uk/kmi/hans#>"
				+ "INSERT DATA { "
				+ "		<%s>   hsf:hasPose <%s> ; "
				+ ".}", entityInstanceURI, poseInstanceURI);

		UpdateAction.parseExecute(query, dataModel);
	}
	
	private void insertPose(String poseInstanceURI, String posePositionURI, String poseOrientationURI) {
		String query = String.format(
				"  prefix ros: <http://data.open.ac.uk/kmi/ros#>"
				+ "INSERT DATA { "
				+ "		<%s>   a ros:Pose ; "
				+ "		ros:hasPosition <%s> ;"
				+ "		ros:hasOrientation <%s>; "
				+ ".}", poseInstanceURI, posePositionURI, poseOrientationURI);
		
		UpdateAction.parseExecute(query, dataModel);
	}
	
	private void insertRosQuaternion(String rosQuaternionInstanceURI, double x, double y, double z, double w) {
		String query = String.format(
				"  prefix ros: <http://data.open.ac.uk/kmi/ros#>"
				+ "prefix xsd: <http://www.w3.org/2001/XMLSchema#>"
				+ "INSERT DATA { "
				+ "		<%s>   a ros:Quaternion ; "
				+ "		ros:hasX \"%f\"^^xsd:double ;"
				+ "		ros:hasY \"%f\"^^xsd:double ;"
				+ "		ros:hasZ \"%f\"^^xsd:double ;"
				+ "		ros:hasW \"%f\"^^xsd:double ;"
				+ ".}", rosQuaternionInstanceURI, x, y, z, w);
		
		UpdateAction.parseExecute(query, dataModel);
	}
	
	private void insertGeometry(String geometryInstanceURI, String geometryClassURI, String geometryWkt) {
		String query = String.format(
				"  prefix geo: <http://www.opengis.net/ont/geosparql#>"
				+ "INSERT DATA { "
				+ "		<%s>   a <%s> ; "
				+ "		geo:asWKT \"%s\"^^geo:WktLiteral .}", geometryInstanceURI, geometryClassURI, geometryWkt);
		
		UpdateAction.parseExecute(query, dataModel);
	}
	
	// TODO this heavy method can be changed with a ModelChangedListener
	// even because when I delete, I can be sure I deleted them?
	private boolean checkPresence(String instanceURI, String classURI) {
		String query = String.format(
				"  prefix hsf: <http://data.open.ac.uk/kmi/hans#> "
				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "ASK { "
				+ "		<%s>   rdf:type/rdfs:subClassOf* <%s> "
				+ ".}", instanceURI, classURI);
		QueryExecution qexec = QueryExecutionFactory.create(query, this.dataModel);

		return qexec.execAsk();
	}
	
	private void removeAllInstancesFromKB(ArrayList<String> instances) {
		
		for(String instanceURI : instances) {
			String query = String.format(
					"  prefix hsf: <http://data.open.ac.uk/kmi/hans#> "
					+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
					+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
					+ "DELETE WHERE { "
					+ "		<%s> ?a ?b  "
					+ ".}", instanceURI);
			
			UpdateAction.parseExecute(query, dataModel);
		}
	}

	// TODO this is a little bit over-fitted on the demo
	public JSONObject queryEntityList(String entityClassURI) throws JSONException, UnsupportedEncodingException {
		
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
			+ " }", entityClassURI);
		
		QueryExecution qexec = QueryExecutionFactory.create(query, dataModel);
					
		ResultSet results = qexec.execSelect();	
		return JsonUtils.resultSetToJsonLD(results);
	}

	public JSONObject queryEntity(String entityURI) throws JSONException, UnsupportedEncodingException {
		
		String query = String.format(
				"   prefix sh: <http://www.w3.org/ns/shacl#> "
				+ " prefix hsf: <http://data.open.ac.uk/kmi/hans#> "
				+ " prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ " prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ " prefix schema: <http://schema.org/> "
				+ " prefix geo: <http://www.opengis.net/ont/geosparql#> "
				+ " SELECT ?entity ?name ?class ?wkt WHERE {"
				+ " 	<%s> rdf:type ?class ;"
				+ " 		 schema:name ?name ; "
				+ "			 geo:hasGeometry ?geometry ."
				+ "		?geometry geo:asWKT ?wkt ."
				+ "		BIND(<%s> AS ?entity)"
				+ " }", entityURI, entityURI);
		
		QueryExecution qexec = QueryExecutionFactory.create(query, dataModel);
		
		ResultSet results = qexec.execSelect();	
		return JsonUtils.resultSetToJsonLD(results);
	}

	// TODO send feedback on delete
	public void deleteEntity(String entityURI) {
		
		String query = String.format(
				"  prefix hsf: <http://data.open.ac.uk/kmi/hans#> "
				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "prefix geo: <http://www.opengis.net/ont/geosparql#> "
				+ "prefix ros:   <http://data.open.ac.uk/kmi/ros#> "
				+ "DELETE WHERE { "
				+ "		<%s> ?a ?b ."
				+ "		<%s> geo:hasGeometry ?geom ."
				+ "		<%s> hsf:hasPose ?pose ."
				+ "		?pose ros:hasOrientation ?orientation ."
				+ "		?orientation ?v ?w ."
				+ "   	?pose ros:hasPosition ?position ."
				+ "     ?pose ?l ?m ."
				+ "     ?geom ?c ?d  "
				+ ".}", entityURI, entityURI, entityURI);
		
		UpdateAction.parseExecute(query, dataModel);		
	}

}
