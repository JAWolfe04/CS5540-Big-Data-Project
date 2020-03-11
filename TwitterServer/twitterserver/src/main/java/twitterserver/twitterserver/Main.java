package twitterserver.twitterserver;

import org.eclipse.jetty.server.Server;

import twitterserver.twitterserver.SparkFactory;
import twitterserver.twitterserver.WebServer;
import twitterserver.twitterserver.Config;

public class Main 
{	
    public static void main( String[] args ) throws Exception
    {
    	SparkFactory.getInstance();
    	WebServer webServer = new WebServer();
    	Server server = webServer.createServer();
        server.start();
        System.out.println("Server online at http://" + Config.host + ":" + Config.port + "/");
        server.join();
        
    }
}
