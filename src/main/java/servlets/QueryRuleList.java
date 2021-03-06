package servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.json.JSONObject;

import semanticmap.KBInterface;
import utils.JsonUtils;

public class QueryRuleList extends HttpServlet {
	
	private static final long serialVersionUID = -7752504552123153330L;
	public static KBInterface kbInterface;
	
	public QueryRuleList() {
		super();	
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		
		try {
			JSONObject rules = kbInterface.queryRuleList();
			JSONObject rulesFormatted = JsonUtils.bindJsonLDResultSet(rules);
			resp.setStatus(HttpStatus.OK_200);
			resp.getWriter().write(rulesFormatted.toString());
			resp.getWriter().flush();
			resp.getWriter().close();
		}
		catch (Exception e) {
			e.printStackTrace();
			resp.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
			resp.getWriter().write(ExceptionUtils.getStackTrace(e));
			resp.getWriter().flush();
			resp.getWriter().close();
		}
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		resp.getWriter().println("This API is accessible only via GET method.");
		resp.setStatus(HttpStatus.NOT_IMPLEMENTED_501);
		resp.getWriter().flush();
		resp.getWriter().close();
	}
}
