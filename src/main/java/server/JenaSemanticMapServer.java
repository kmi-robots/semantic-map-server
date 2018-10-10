package server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Properties;
import java.util.Scanner;

import javax.servlet.DispatcherType;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.function.FunctionRegistry;
import org.apache.jena.util.FileUtils;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.jenax.util.JenaUtil;

import semanticmap.KBInterface;
import semanticmap.SemanticMapModelChangedListener;
import servlets.DeleteEntity;
import servlets.InsertEntity;
import servlets.QueryEntity;
import servlets.QueryEntityList;
import servlets.QueryRuleList;
import servlets.QueryRuleWaypointList;
import servlets.SubmitSensing;
import servlets.Validate;
import servlets.ValidateSingleRule;
import sparql.functions.Contains;
import sparql.functions.Covers;
import sparql.functions.Crosses;
import sparql.functions.Disjoint;
import sparql.functions.Distance;
import sparql.functions.Equals;
import sparql.functions.Intersects;
import sparql.functions.Overlaps;
import sparql.functions.Touches;
import sparql.functions.Within;
import tests.testJenaSHACL;

public class JenaSemanticMapServer {

	public static void main(String[] args) {
		try {
			JenaSemanticMapServer server = new JenaSemanticMapServer("./resources/server.properties");
			server.start();
			
			Runtime.getRuntime().addShutdownHook(new Thread() {
			    public void run() {
			    	Scanner reader = new Scanner(System.in);  // Reading from System.in
			    	String choice = "";
			    	
			    	while(!(choice.equals("y") || choice.equals("n"))) {
				    	
				    	System.out.print("Save the actual semantic map model [y/n]? ");
				    	choice = reader.nextLine();
				    	
				    	if(choice.equals("y")) {
				    		try {
								server.kbInterface.saveKBSnapshot(server.getInstancesFile());
							} catch (IOException e) {
								System.out.println("Unable to save kb snapshot.");
								choice = "";
								e.printStackTrace();
							}
				    	}
				    	else if(choice.equals("n")) {
				    		System.out.println("Closing the server without saving.");
				    	}
				    	else {
				    		System.out.println("Option unavailable.");
				    	}
			    	}
			    	reader.close();
			    }
			 });
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
	
	public static Logger logger = LoggerFactory.getLogger(testJenaSHACL.class);
	public static Model dataModel;
	private int port = 7070;
	private Server server;
	private KBInterface kbInterface;
	private SemanticMapModelChangedListener modelListener;
	private String ontologyFile = "./resources/hans-ontology.ttl";
	private String rulesFile = "./resources/hans-rules.ttl";
	private String instancesFile = "./resources/hans-instances.ttl";
	
	JenaSemanticMapServer(String configFile) throws IOException, SQLException {
		
		// loading property files
		Properties prop = new Properties();
		InputStream input = new FileInputStream(configFile);
		prop.load(input);
		
		System.out.println("Initialising semantic map server");
		
		if(prop.containsKey("port")) {
			this.port = Integer.parseInt(prop.getProperty("port"));
			System.out.println("Using port " + this.port);
		}
		else {
			System.out.println("Using default port 7070");
		}
		
		if(prop.containsKey("ontology_file")) {
			this.ontologyFile = prop.getProperty("ontology_file");
			System.out.println("Using ontology " + this.ontologyFile);
		}
		else {
			System.out.println("Using default kb ./resources/hans-shacl.ttl");
		}
		
		if(prop.containsKey("rules_file")) {
			this.rulesFile = prop.getProperty("rules_file");
			System.out.println("Using rules " + this.rulesFile);
		}
		else {
			System.out.println("Using default kb ./resources/hans-shacl.ttl");
		}
		
		if(prop.containsKey("instances_file")) {
			this.instancesFile = prop.getProperty("instances_file");
			System.out.println("Using kb instances " + this.instancesFile);
		}
		else {
			System.out.println("Using default kb ./resources/hans-shacl.ttl");
		}
		
		System.out.println("Loading kb");
		dataModel = JenaUtil.createMemoryModel();
		InputStream inOntology = new FileInputStream(new File(this.ontologyFile));
		dataModel.read(inOntology, null, FileUtils.langTurtle);
		InputStream inRules = new FileInputStream(new File(this.rulesFile));
		dataModel.read(inRules, null, FileUtils.langTurtle);
		InputStream inInstances = new FileInputStream(new File(this.instancesFile));
		dataModel.read(inInstances, null, FileUtils.langTurtle);
		
		this.modelListener = new SemanticMapModelChangedListener();
		dataModel.register(this.modelListener);
		this.kbInterface = new KBInterface(dataModel, new GeometryFactory(), new WKTWriter(3));
		
		initialiseFunctionRegistry();
		initialiseServer();
	}
	
	private void initialiseFunctionRegistry() {
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
		FunctionRegistry.get().put("http://data.open.ac.uk/kmi/hans#distance", Distance.class);
		Distance.model = dataModel;
		Distance.wktReader = wktReader;
	}
	
	private void initialiseServer() {
		this.server = new Server(this.port);
		
		ArrayList<ServletContextHandler> handlersArrayList = new ArrayList<>();
		System.out.print("Initializing servlets ...");
		
		// insert servlet handlers
		ServletContextHandler insertEntityHandler = new ServletContextHandler(server,"/insert/entity");
		handlersArrayList.add(insertEntityHandler);

		// delete servlet handlers
		ServletContextHandler deleteEntityHandler = new ServletContextHandler(server,"/delete/entity");
		handlersArrayList.add(deleteEntityHandler);
		
		// submit servlet handlers
		// submit is a general api to submit some processing from the robot
		// this can be a list of classification of objects, with or without their positions as points,
		// or 2d-3d shapes, and so on...
		ServletContextHandler submitSensingHandler = new ServletContextHandler(server,"/submit/sensing");
		handlersArrayList.add(submitSensingHandler);
		
		// semantic map query servlet handlers
		// TODO choose between entity/object/whatever
//		ServletContextHandler queryObjectHandler = new ServletContextHandler(server,"/query/object");
//		handlersArrayList.add(queryObjectHandler);
//		ServletContextHandler queryAreaHandler = new ServletContextHandler(server,"/query/area");
//		handlersArrayList.add(queryAreaHandler);
//		ServletContextHandler queryConnectionHandler = new ServletContextHandler(server,"/query/connection");
//		handlersArrayList.add(queryConnectionHandler);
//		ServletContextHandler queryListObjectsHandler = new ServletContextHandler(server,"/query/list/objects");
//		handlersArrayList.add(queryListObjectsHandler);
//		ServletContextHandler queryListAreasHandler = new ServletContextHandler(server,"/query/list/areas");
//		handlersArrayList.add(queryListAreasHandler);
//		ServletContextHandler queryListConnectionsHandler = new ServletContextHandler(server,"/query/list/connections");
//		handlersArrayList.add(queryListConnectionsHandler);
		ServletContextHandler queryEntityHandler = new ServletContextHandler(server,"/query/entity");
		handlersArrayList.add(queryEntityHandler);
		ServletContextHandler queryListEntitiesHandler = new ServletContextHandler(server,"/query/list/entities");
		handlersArrayList.add(queryListEntitiesHandler);
		ServletContextHandler queryListRulesHandler = new ServletContextHandler(server,"/query/list/rules");
		handlersArrayList.add(queryListRulesHandler);
		ServletContextHandler queryListRuleWaypointsHandler = new ServletContextHandler(server,"/query/list/rule-waypoints");
		handlersArrayList.add(queryListRuleWaypointsHandler);

		// validate servlet handlers
		ServletContextHandler validateHanlder = new ServletContextHandler(server,"/validate");
		handlersArrayList.add(validateHanlder);
		ServletContextHandler validateSingleRuleHanlder = new ServletContextHandler(server,"/validate/rule");
		handlersArrayList.add(validateSingleRuleHanlder);
		
		// update servlet handlers
		
		// delete servlet handlers
		
		// adding annoying headers
		for(ServletContextHandler svc : handlersArrayList) {
			FilterHolder cors = svc.addFilter(CrossOriginFilter.class,"/*",EnumSet.of(DispatcherType.REQUEST));
			cors.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
			cors.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "*");
			cors.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "POST, GET, OPTIONS, PUT, DELETE, HEAD");
			cors.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM, "Origin, X-Requested-With, Content-Type, Accept");			
		}
		
		InsertEntity.kbInterface = this.kbInterface;
		DeleteEntity.kbInterface = this.kbInterface;
		QueryEntity.kbInterface = this.kbInterface;
		QueryEntityList.kbInterface = this.kbInterface;
		QueryRuleList.kbInterface = this.kbInterface;
		QueryRuleWaypointList.kbInterface = this.kbInterface;
		SubmitSensing.kbInterface = this.kbInterface;
		Validate.kbInterface = this.kbInterface;
		ValidateSingleRule.kbInterface = this.kbInterface;
		
		insertEntityHandler.addServlet(InsertEntity.class, "/");
		insertEntityHandler.setAllowNullPathInfo(true);
		
		deleteEntityHandler.addServlet(DeleteEntity.class, "/");
		deleteEntityHandler.setAllowNullPathInfo(true);
		
		queryEntityHandler.addServlet(QueryEntity.class, "/");
		queryEntityHandler.setAllowNullPathInfo(true);
		queryListEntitiesHandler.addServlet(QueryEntityList.class, "/");
		queryListEntitiesHandler.setAllowNullPathInfo(true);
		queryListRulesHandler.addServlet(QueryRuleList.class, "/");
		queryListRulesHandler.setAllowNullPathInfo(true);
		queryListRuleWaypointsHandler.addServlet(QueryRuleWaypointList.class, "/");
		queryListRuleWaypointsHandler.setAllowNullPathInfo(true);
		
		submitSensingHandler.addServlet(SubmitSensing.class, "/");
		submitSensingHandler.setAllowNullPathInfo(true);
		
		validateHanlder.addServlet(Validate.class, "/");
		validateHanlder.setAllowNullPathInfo(true);
		validateSingleRuleHanlder.addServlet(ValidateSingleRule.class, "/");
		validateSingleRuleHanlder.setAllowNullPathInfo(true);
		
		ContextHandlerCollection contexts = new ContextHandlerCollection();
		Handler[] servlets = handlersArrayList.toArray(new Handler[handlersArrayList.size()]);
		contexts.setHandlers(servlets);
		this.server.setHandler(contexts);
		System.out.println("All servlets initialised and loaded");
	}
	
	public void start() {
		try {
			System.out.println("Starting semantic-map server");
			this.server.start();					
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Model getDataModel() {
		return dataModel;
	}

	public String getInstancesFile() {
		return instancesFile;
	}

	public void setInstancesFile(String instancesFile) {
		this.instancesFile = instancesFile;
	}

}
