package car.tp2;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.net.ftp.FTPSClient;

@Path("/restftp")
public class RestFTP {

	
	 // Pour tester:
	 // curl http://localhost:8080/rest/api/helloworld/getfile
	 @GET
	 @Path ("/getfile")
	 @Produces("application/octet-stream")
	 public StreamingOutput getFile() {
		 StreamingOutput so = null;
		 final FTPSClient ftpsClient = new FTPSClient(false); // implicit = false
         try {
			final boolean login = ftpsClient.login("anonymous", "bob");
         final String[] filenames = ftpsClient.listNames();
         final boolean containsFile = Arrays.asList(filenames).contains("ftpserver.jks");

		 so = new StreamingOutput() {
			     public void write(OutputStream os) throws java.io.IOException {
			       os.write(filenames.toString().getBytes());
			     }
			   };
         ftpsClient.connect("localhost", 2121);
         ftpsClient.logout();
         } catch (IOException e) {
        	 // TODO Auto-generated catch block
        	 e.printStackTrace();
         }
		return so;
	 }
}
