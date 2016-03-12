package car.tp2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.net.ftp.FTPClient;

@Path("/restftp")
public class RestFTP {

	// Pour tester:
	// curl http://localhost:8080/rest/api/helloworld/getfile
	@GET
	@Path("/getfile/{var: .*}")
	@Produces("application/octet-stream")
	public StreamingOutput getFile(@PathParam("var") String file) {
		StreamingOutput so = null;
		final FTPClient ftpClient = new FTPClient();
		try {
			ftpClient.connect("localhost", 1234);
			final boolean login = ftpClient.login("anonymous", "bob");

			if (login) {
				final InputStream is = ftpClient.retrieveFileStream(file);

				so = new StreamingOutput() {
					public void write(OutputStream os) throws java.io.IOException {
						while (is.available() > 0)
							os.write(is.read());
						is.close();
						os.close();
						ftpClient.logout();
					}
				};

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return so;
	}

	// Pour tester:
	// curl -H "Content-Type: application/octet-stream" -X POST -d "tralala"
	// http://localhost:8080/rest/api/helloworld/postfile
	@POST
	@Path("/postfile/{var: .*}")
	@Consumes("application/octet-stream")
	public String postFile(InputStream is, @PathParam("var") String file) throws java.io.IOException {		
		final FTPClient ftpClient = new FTPClient();
		try {
			ftpClient.connect("localhost", 1234);
			final boolean login = ftpClient.login("anonymous", "bob");

			if (login) {

				OutputStream os = ftpClient.storeFileStream(file);
				os.write(is.read());
				
				ftpClient.logout();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ("Envoi fichier OK (POST)\n");
	}

	// Pour tester:
	// curl -H "Content-Type: application/octet-stream" -X PUT -d "tralala"
	// http://localhost:8080/rest/api/helloworld/putfile
	@PUT
	@Path("/putfile/{var: .*}")
	@Consumes("application/octet-stream")
	public String putFile(InputStream is, @PathParam("var") String file) throws java.io.IOException {	
		final FTPClient ftpClient = new FTPClient();
		try {
			ftpClient.connect("localhost", 1234);
			final boolean login = ftpClient.login("anonymous", "bob");

			if (login) {

				ftpClient.storeFile(file, is);
				ftpClient.logout();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ("Envoi fichier OK (PUT)\n");
	}
}
