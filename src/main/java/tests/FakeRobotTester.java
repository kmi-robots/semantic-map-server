package tests;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math3.complex.Quaternion;
import org.json.JSONArray;
import org.json.JSONObject;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import geometry.GeometricClasses;
import geometry.GeometricEntity;
import geometry.GeometryTypeException;
import geometry.Pose;

public class FakeRobotTester {
	
	/**
	 * this program simulates a robot driving around the environment and submitting sensing to the KB
	 * asking for validation every time it submit a sensing 
	 * @throws GeometryTypeException 
	 * @throws InterruptedException 
	 * @throws IOException 
	 * */
	public static void main(String[] args) throws GeometryTypeException, InterruptedException, IOException {
		
		GeometryFactory gf = new GeometryFactory();
		
		System.out.println("Starting patrolling routine");
		System.out.println("Patrolling ...");
		
		//for (int i = 0; i < 100; ++i) {
			
			// playroom coordinates (0 0, 4 0, 4 4, 0 4, 0 0)
			ArrayList<GeometricEntity> playroomObjects = generatePlayroomObjects(gf);
			navigateAndCheck("Playroom", playroomObjects, 2000);
						
			// activity2 coordinates (0 -9, 3 -9, 3 -1.5, 0 -1.5, 0 -9)
			ArrayList<GeometricEntity> activity2Objects = generateActivity2Objects(gf);
			navigateAndCheck("Activity 2", activity2Objects, 4000);
			
			// KMi main entrance coordinates (20 -5.3, 21.2 -5.3, 21.2 -3, 20 -3, 20 -5.3)
			ArrayList<GeometricEntity> mainEntranceObjects = generateKMIEntranceObjects(gf);
			navigateAndCheck("KMi main entrance", mainEntranceObjects, 4000);
			
			// Podium entrance coordinates (-33 -20.8, -31.8 -20.8, -31.8 -19, -33 -19, -33 -20.8)
			ArrayList<GeometricEntity> podiumEntranceObjects = generatePodiumEntranceObjects(gf);
			navigateAndCheck("Podium entrance", podiumEntranceObjects, 2000);
			
			// Podium's back coordinates (-34.5 -8, -32.9 -8, -32.9 -3.7, -34.5 -3.7, -34.5 -8)
			ArrayList<GeometricEntity> podiumBackObjects = generatePodiumBackObjects(gf);
			navigateAndCheck("Podium back", podiumBackObjects, 4000);
		//}
	}

	private static HttpURLConnection sendSensing(ArrayList<GeometricEntity> objects) throws IOException {
		
		URL url = new URL("http://localhost:7070/submit/sensing");
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		
		JSONObject sensing = new JSONObject();
		JSONArray detections = new JSONArray();
		
		for(GeometricEntity ge : objects) {
			detections.put(ge.toJson());
		}
		
		sensing.put("detections", detections);
		
		Map<String, String> parameters = new HashMap<>();
		parameters.put("sensing", sensing.toString());
		
		System.out.println("Submitting " + sensing.toString());
		
		con.setDoOutput(true);
		DataOutputStream out = new DataOutputStream(con.getOutputStream());
		out.writeBytes(getParamsString(parameters));
		out.flush();
		out.close();
		
		return con;
	
	}
	
	private static HttpURLConnection askValidation() throws IOException {
		
		URL url = new URL("http://localhost:7070/validate");
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");				 
		con.setDoOutput(true);
		
		return con;
	
	}
	
    private static String getParamsString(Map<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
 
        for (Map.Entry<String, String> entry : params.entrySet()) {
          result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
          result.append("=");
          result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
          result.append("&");
        }
 
        String resultString = result.toString();
        return resultString.length() > 0
          ? resultString.substring(0, resultString.length() - 1)
          : resultString;
    }

	private static void navigateAndCheck(String destination, ArrayList<GeometricEntity> objects, long sleepMs) throws InterruptedException, IOException {
		System.out.println("Driving to the " + destination);
		// sleep X seconds
		Thread.sleep(sleepMs);
		
		// the robot is in the playroom
		System.out.println(destination + " reached. Inspecting.");
		Thread.sleep(sleepMs/2);
		
		// send some sensing: a heater, a chair
		HttpURLConnection resp = sendSensing(objects);
		
		if(resp.getResponseCode() != 200) {
			System.out.println(resp.getResponseCode() + " - " + resp.getResponseMessage());
		}
		
		// validate
		HttpURLConnection validationResp = askValidation();
		
		BufferedReader in = new BufferedReader(new InputStreamReader(validationResp.getInputStream()));
		String inputLine;
		StringBuffer content = new StringBuffer();
		
		while ((inputLine = in.readLine()) != null) {
		    content.append(inputLine);
		}
		in.close();
		
		System.out.println("Validation feedback: " + content.toString());
		
	}
	
	private static ArrayList<GeometricEntity> generatePlayroomObjects(GeometryFactory gf) throws GeometryTypeException, InterruptedException {

		GeometricEntity heater = new GeometricEntity("object", "Heater", Calendar.getInstance().getTimeInMillis(), GeometricClasses.POINT);
		Quaternion heaterOrientation = new Quaternion(1, 0, 0, 0);
		Point heaterPosition = gf.createPoint(new Coordinate(0.6, 0.13, 0));
		Pose heaterPose = new Pose(heaterPosition, heaterOrientation);
		heater.setPose(heaterPose);
		Point heaterGeometry = gf.createPoint(new Coordinate(0.6, 0.13, 0));
		heater.setGeometry(heaterGeometry);
		
		GeometricEntity chair = new GeometricEntity("object", "Chair", Calendar.getInstance().getTimeInMillis(), GeometricClasses.POINT);
		Quaternion chairOrientation = new Quaternion(1, 0, 0, 0);
		Point chairPosition = gf.createPoint(new Coordinate(1.1, 0.85, 0));
		Pose chairPose = new Pose(chairPosition, chairOrientation);
		chair.setPose(chairPose);
		Point chairGeometry = gf.createPoint(new Coordinate(1.1, 0.85, 0));
		chair.setGeometry(chairGeometry);
		
		ArrayList<GeometricEntity> ret = new ArrayList<>();
		ret.add(heater);
		ret.add(chair);

		return ret;
	}

	private static ArrayList<GeometricEntity> generateActivity2Objects(GeometryFactory gf) throws GeometryTypeException, InterruptedException {

		GeometricEntity chair1 = new GeometricEntity("object", "Chair", Calendar.getInstance().getTimeInMillis(), GeometricClasses.POINT);
		Quaternion chair1Orientation = new Quaternion(1, 0, 0, 0);
		Point chair1Position = gf.createPoint(new Coordinate(2.1, -2.5, 0));
		Pose chair1Pose = new Pose(chair1Position, chair1Orientation);
		chair1.setPose(chair1Pose);
		Point chair1Geometry = gf.createPoint(new Coordinate(2.1, -2.5, 0));
		chair1.setGeometry(chair1Geometry);
		
		GeometricEntity chair = new GeometricEntity("object", "Chair", Calendar.getInstance().getTimeInMillis(), GeometricClasses.POINT);
		Quaternion chairOrientation = new Quaternion(1, 0, 0, 0);
		Point chairPosition = gf.createPoint(new Coordinate(2.8, -7.3, 0));
		Pose chairPose = new Pose(chairPosition, chairOrientation);
		chair.setPose(chairPose);
		Point chairGeometry = gf.createPoint(new Coordinate(2.8, -7.3, 0));
		chair.setGeometry(chairGeometry);
		
		ArrayList<GeometricEntity> ret = new ArrayList<>();
		ret.add(chair1);
		ret.add(chair);

		return ret;
	}
	
	// (20 -5.3, 21.2 -5.3, 21.2 -3, 20 -3, 20 -5.3)
	private static ArrayList<GeometricEntity> generateKMIEntranceObjects(GeometryFactory gf) throws GeometryTypeException, InterruptedException {

		GeometricEntity fan = new GeometricEntity("object", "FireActionNotice", Calendar.getInstance().getTimeInMillis(), GeometricClasses.POINT);
		Quaternion fanOrientation = new Quaternion(1, 0, 0, 0);
		Point fanPosition = gf.createPoint(new Coordinate(20.5, -4, 0));
		Pose fanPose = new Pose(fanPosition, fanOrientation);
		fan.setPose(fanPose);
		Point fanGeometry = gf.createPoint(new Coordinate(20.5, -4, 0));
		fan.setGeometry(fanGeometry);
		
		GeometricEntity fe = new GeometricEntity("object", "FireExtinguisher", Calendar.getInstance().getTimeInMillis(), GeometricClasses.POINT);
		Quaternion feOrientation = new Quaternion(1, 0, 0, 0);
		Point fePosition = gf.createPoint(new Coordinate(20.1, -3.8, 0));
		Pose fePose = new Pose(fePosition, feOrientation);
		fe.setPose(fePose);
		Point feGeometry = gf.createPoint(new Coordinate(20.1, -3.8, 0));
		fe.setGeometry(feGeometry);
		
		ArrayList<GeometricEntity> ret = new ArrayList<>();
		ret.add(fan);
		ret.add(fe);

		return ret;
	}
	
	// (-33 -20.8, -31.8 -20.8, -31.8 -19, -33 -19, -33 -20.8)
	private static ArrayList<GeometricEntity> generatePodiumEntranceObjects(GeometryFactory gf) throws GeometryTypeException, InterruptedException {

		GeometricEntity fel = new GeometricEntity("object", "FireExtinguisherLabel", Calendar.getInstance().getTimeInMillis(), GeometricClasses.POINT);
		Quaternion felOrientation = new Quaternion(1, 0, 0, 0);
		Point felPosition = gf.createPoint(new Coordinate(-32, -20.1, 0));
		Pose felPose = new Pose(felPosition, felOrientation);
		fel.setPose(felPose);
		Point felGeometry = gf.createPoint(new Coordinate(-32, -20.1, 0));
		fel.setGeometry(felGeometry);
		
		GeometricEntity fe = new GeometricEntity("object", "FireExtinguisher", Calendar.getInstance().getTimeInMillis(), GeometricClasses.POINT);
		Quaternion feOrientation = new Quaternion(1, 0, 0, 0);
		Point fePosition = gf.createPoint(new Coordinate(-32.4, -19.5, 0));
		Pose fePose = new Pose(fePosition, feOrientation);
		fe.setPose(fePose);
		Point feGeometry = gf.createPoint(new Coordinate(-32.4, -19.5, 0));
		fe.setGeometry(feGeometry);
		
		ArrayList<GeometricEntity> ret = new ArrayList<>();
		ret.add(fel);
		ret.add(fe);

		return ret;
	}
	
	// (-34.5 -8, -32.9 -8, -32.9 -3.7, -34.5 -3.7, -34.5 -8)
	private static ArrayList<GeometricEntity> generatePodiumBackObjects(GeometryFactory gf) throws GeometryTypeException, InterruptedException {

		GeometricEntity heater = new GeometricEntity("object", "Heater", Calendar.getInstance().getTimeInMillis(), GeometricClasses.POINT);
		Quaternion heaterOrientation = new Quaternion(1, 0, 0, 0);
		Point heaterPosition = gf.createPoint(new Coordinate(-33, -5.9, 0));
		Pose heaterPose = new Pose(heaterPosition, heaterOrientation);
		heater.setPose(heaterPose);
		Point heaterGeometry = gf.createPoint(new Coordinate(-33, -5.9, 0));
		heater.setGeometry(heaterGeometry);
		
		ArrayList<GeometricEntity> ret = new ArrayList<>();
		ret.add(heater);
		
		return ret;
	}

	
}
