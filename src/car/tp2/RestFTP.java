package car.tp2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

@Path("/restftp")
public class RestFTP {

	String userName = "";
	String passWord = "";
	
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
			final boolean login = ftpClient.login(userName, passWord);

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
			} else {
				System.out.println("erreur to be implemented");
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
	public String postFile(InputStream is, @PathParam("var") String file)
			throws java.io.IOException {
		final FTPClient ftpClient = new FTPClient();
		try {
			ftpClient.connect("edel-brau.lifl.fr", 21);
			final boolean login = ftpClient.login(userName, passWord);

			if (login) {
				if (ftpClient.storeFile(file, is))
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
	@Produces("text/html")
	public String listFile(@PathParam("var") String file)
			throws java.io.IOException {
		final FTPClient ftpClient = new FTPClient();
		try {
			ftpClient.connect("edel-brau.lifl.fr", 21);
			final boolean login = ftpClient.login(userName, passWord);

			if (login) {
				if (file.equals("home")) {
					FTPFile files[] = ftpClient.listFiles();
					String nameFiles = "";
					for (FTPFile f : files) {
						nameFiles += f.getName() + "</br>";
					}
					ftpClient.logout();
					return nameFiles;
				} else if (ftpClient.changeWorkingDirectory(file)) {
					FTPFile files[] = ftpClient.listFiles();
					String nameFiles = "";
					for (FTPFile f : files) {
						nameFiles += f.getName() + "</br>";
					}
					ftpClient.logout();
					return nameFiles;
				} else {
					return ("Incorrect path </br>");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ("Incorrect path </br>");
	}

	@GET
	@Path("/deletefile/{var: .*}")
	@Produces("text/html")
	public String deleteFile(@PathParam("var") String file)
			throws java.io.IOException {
		final FTPClient ftpClient = new FTPClient();
		try {
			ftpClient.connect("edel-brau.lifl.fr", 21);
			final boolean login = ftpClient.login(userName, passWord);

			if (login) {
				if (ftpClient.deleteFile("upload/" + file)) {
					ftpClient.logout();
					return ("File " + file + " deleted </br>");
				} else {
					return ("Incorrect path </br>");
				}
			} 
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ("Incorrect path </br>");
	}


	@GET
	@Produces("text/html")
	public String loginForm() {
		return " <form method=\"post\">"
				+ "name:<br>"
				+ "<input type=\"text\" name=\"name\" value=\"Mickey\"><br>"
				+ "Password:<br>"
				+ "<input type=\"text\" name=\"pass\" value=\"Mouse\"><br><br>"
				+ "<input type=\"submit\" value=\"Submit\">" + "</form> ";
	}

	@POST
	@Consumes("application/x-www-form-urlencoded")
	public Response post(@FormParam("name") String name, @FormParam("pass") String pass) {
		userName = name;
		passWord = pass;
		return Response.ok("connect ok").build();
	}
}
