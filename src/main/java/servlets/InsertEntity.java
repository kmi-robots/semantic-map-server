package servlets;

import java.io.IOException;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.json.JSONObject;

import geometry.GeometricClasses;
import semanticmap.KBInterface;

public class InsertEntity extends HttpServlet {
	
	private static final long serialVersionUID = 6734749696610986681L;
	public static KBInterface kbInterface;
	
	public InsertEntity() {
		super();
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.getWriter().println("This API is accessible only via POST method.");
		resp.setStatus(HttpStatus.NOT_IMPLEMENTED_501);
		resp.getWriter().flush();
		resp.getWriter().close();
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		String json = req.getParameter("json");
		
		if (json == null) {
			resp.setStatus(HttpStatus.BAD_REQUEST_400);
			resp.getWriter().write("Please specify the parameter 'json'");
			resp.getWriter().flush();
			resp.getWriter().close();
		}
		else {
			try {
				JSONObject object = new JSONObject(json);
				JSONObject geometry = object.getJSONObject("geometry");
				Set<String> keySet = geometry.keySet();
				
				if(keySet.contains("geom_class")) {
					String geomClass = geometry.getString("geom_class");
					
					if(!EnumUtils.isValidEnum(GeometricClasses.class, geomClass.toUpperCase())) {
						resp.setStatus(HttpStatus.BAD_REQUEST_400);
						resp.getWriter().write("'geom_class' field must be in [point|area|line|(volume)]");
						resp.getWriter().flush();
						resp.getWriter().close();
					} 
					else if( (geomClass.equals("point") || geomClass.equals("area") || geomClass.equals("line"))) {
						JSONObject res = processJsonObject(object);
						
						if(res != null) {
							resp.setStatus(HttpStatus.CONFLICT_409);
							resp.getWriter().write(res.toString());
							resp.getWriter().flush();
							resp.getWriter().close();
						}
						else {
							resp.setStatus(HttpStatus.CREATED_201);
							resp.getWriter().flush();
							resp.getWriter().close();
						}
					}
					else if(geomClass.equals("volume")) {
						resp.setStatus(HttpStatus.METHOD_NOT_ALLOWED_405);
						resp.getWriter().write("'volume' geometric class not supported yet");
						resp.getWriter().flush();
						resp.getWriter().close();
					}
				}
				else {
					resp.setStatus(HttpStatus.BAD_REQUEST_400);
					resp.getWriter().write("You must specify a 'geom_class' in your json");
					resp.getWriter().flush();
					resp.getWriter().close();
				}
			}
			catch (Exception e) {
				e.printStackTrace();
				resp.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
				resp.getWriter().write(ExceptionUtils.getStackTrace(e));
				resp.getWriter().flush();
				resp.getWriter().close();
			}
		}
	}

	private JSONObject processJsonObject(JSONObject object) throws InterruptedException {
		String geomClass = object.getJSONObject("geometry").getString("geom_class"); 
		
		if(geomClass.equals("point")) {
			return kbInterface.insertPointEntity(object);
		}
		else if(geomClass.equals("area")) {
			return kbInterface.insertAreaEntity(object);
		}
		else if(geomClass.equals("line")) {
			return kbInterface.insertLineEntity(object);
		}
		// It should be if geomClass == volume, but given
		// the checkings in the body of doPost, here
		// it can be only "volue", and this way I save on
		// catching another exception
		else {
			return kbInterface.insertVolumeEntity(object);
		}
	}

}
