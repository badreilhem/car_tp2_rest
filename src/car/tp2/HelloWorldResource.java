package car.tp2;

import java.io.OutputStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.StreamingOutput;

/**
 * Exemple de ressource REST accessible a l'adresse :
 * 
 * 		http://localhost:8080/rest/tp2/helloworld
 * 
 * @author Lionel Seinturier <Lionel.Seinturier@univ-lille1.fr>
 */
@Path("/helloworld")
public class HelloWorldResource {

	@GET
	@Produces("text/html")
	public String sayHello() {
		return "<h1>Hello World</h1>";
	}

	 @GET
	 @Path("/book/{isbn}")
	 public String getBook( @PathParam("isbn") String isbn ) {
		 return "Book: "+isbn;		 
	 }

	 @GET
	 @Path("{var: .*}/stuff")
	 public String getStuff( @PathParam("var") String stuff ) {
		 return "Stuff: "+stuff;
	 }
	 // Pour tester:
	 // curl http://localhost:8080/rest/api/helloworld/getfile
	 @GET
	 @Path ("/getfile")
	 @Produces("application/octet-stream")
	 public StreamingOutput getFile() {
	   StreamingOutput so = new StreamingOutput() {
	     public void write(OutputStream os) throws java.io.IOException {
	       os.write("Coucou!\n".getBytes());
	     }
	   };
	   return so;
	 }
}

