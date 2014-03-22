package org.owasp.appsensor.handler;

import java.net.URI;
import java.util.Collection;
import java.util.GregorianCalendar;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.owasp.appsensor.AppSensorServer;
import org.owasp.appsensor.Response;

/**
 * Test basic rest request handling. 
 * 
 * @author John Melton (jtmelton@gmail.com) http://www.jtmelton.com/
 */
public class RestRequestHandlerTest {

	// Base URI the Grizzly HTTP server will listen on
    public static final String BASE_URI = "http://localhost:9000/myapp/";
    
    private HttpServer server;
    private WebTarget target;

    @Before
    public void setUp() throws Exception {
        // start the server
        server = startServer();
        // create the client
        Client restClient = ClientBuilder.newClient();

        // uncomment the following line if you want to enable
        // support for JSON in the client (you also have to uncomment
        // dependency on jersey-media-json module in pom.xml and Main.startServer())
        // --
//        restClient.register(MoxyJsonFeature.class);

        target = restClient.target(BASE_URI);
    }

    @SuppressWarnings("deprecation")
	@After
    public void tearDown() throws Exception {
        server.stop();
    }

    /**
     * Test to see that the message "Got it!" is sent in the response.
     */
    @Test
    public void testGetIt() {
    	AppSensorServer.bootstrap();
        
        GenericType<Collection<Response>> responseType = new GenericType<Collection<Response>>() {};
        
        Collection<Response> responses = target
		.path("api")
		.path("v1.0")
		.path("responses")
		.queryParam("earliest", (new GregorianCalendar().getTimeInMillis()) - (1000 * 60 * 60 * 2))	//2 hrs ago
		.request()
		.header("X-Appsensor-Client-Application-Name2",  "myclientapp")
		.get(responseType);
        
        System.err.println("responses: " + responses);
        for(Response resp : responses) {
        	System.err.println(resp.getAction() + " / " + resp.getDetectionSystemId() + " / " + resp.getTimestamp());
        }
        
    }
    
    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     * @return Grizzly HTTP server.
     */
    private static HttpServer startServer() {
        // create a resource config that scans for JAX-RS resources and providers
        // in com.example package
        final ResourceConfig rc = new ResourceConfig().packages("org.owasp.appsensor");

//        rc.register(MoxyJsonFeature.class);
        
        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }

}
