package twitterserver.twitterserver.handler;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import twitterserver.twitterserver.SparkFactory;

@SuppressWarnings("serial")
public class TweetFreqHandler extends HttpServlet {
	@Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType("application/json");
		response.setStatus(HttpServletResponse.SC_OK);
		System.out.println("Getting Tweet Frequency Data");
		
		PrintWriter out = response.getWriter();
		out.println(SparkFactory.getInstance().getTweetFreq());
	
		System.out.println("Finished Tweet Frequency Data");
	}
}
