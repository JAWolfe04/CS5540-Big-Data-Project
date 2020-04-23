package twitterserver.twitterserver.handler;

import java.io.IOException;
import java.io.PrintWriter;

import twitterserver.twitterserver.SparkFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class BubbleChartHandler extends HttpServlet {

	@Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType("application/json");
		response.setStatus(HttpServletResponse.SC_OK);
		System.out.println("Getting Bubble Chart Data");
		
		PrintWriter out = response.getWriter();
		out.println(SparkFactory.getInstance().getBubbleChartData());
	
		System.out.println("Finished Bubble Chart Data");
	}
}
