package car.tp2;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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
import org.apache.commons.net.ftp.FTPFile;

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
			ftpClient.connect("edel-brau.lifl.fr", 21);
			final boolean login = ftpClient.login("anonymous", "bob");

			if (login) {
				final InputStream is = ftpClient.retrieveFileStream(file);

				if (is != null) {
					so = new StreamingOutput() {
						public void write(OutputStream os)
								throws java.io.IOException {
							while (is.available() > 0)
								os.write(is.read());
							is.close();
							ftpClient.logout();
							os.close();

						}
					};
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return so;
	}

	// Pour tester:
	// curl -H "Content-Type: application/octet-stream" -X POST -d "tralala" http://localhost:8080/rest/api/helloworld/postfile
	@POST
	@Path("/postfile/{var: .*}")
	@Consumes("application/octet-stream")
	public String postFile(InputStream is, @PathParam("var") String file)
			throws java.io.IOException {
		final FTPClient ftpClient = new FTPClient();
		try {
			ftpClient.connect("edel-brau.lifl.fr", 21);
			final boolean login = ftpClient.login("anonymous", "bob");

			if (login) {
				if(ftpClient.storeFile(file, is))
					return ("Envoi fichier OK (POST)\n");
				
				ftpClient.logout();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ("Envoi fichier KO (POST)\n");
	}

	@GET
	@Path("/listfiles/{var: .*}")
	@Consumes("application/octet-stream")
	public String listFile(OutputStream os, @PathParam("var") String file)
			throws java.io.IOException {
		final FTPClient ftpClient = new FTPClient();
		try {
			ftpClient.connect("edel-brau.lifl.fr", 21);
			final boolean login = ftpClient.login("anonymous", "bob");

			if (login) {
				FTPFile files[] = ftpClient.listFiles();
				for(FTPFile f : files){
					os.write(f.getName().getBytes());					
				}
				ftpClient.logout();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ("Envoi fichier KO (POST)\n");
	}
}
