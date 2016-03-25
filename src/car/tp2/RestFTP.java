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

	String restURL = "http://localhost:8080/rest/tp2/restftp";
	//String ftpAddr = "192.168.0.10";
		//int ftpPort = 12345;
	String ftpAddr = "edel-braut.lifl.fr";
	int ftpPort = 21;
		
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
			ftpClient.connect(ftpAddr, ftpPort);
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
			ftpClient.connect(ftpAddr, ftpPort);
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
			ftpClient.connect(ftpAddr, ftpPort);
			final boolean login = ftpClient.login(userName, passWord);

			if (login) {
				if (file.equals("home")) {
					FTPFile files[] = ftpClient.listFiles();
					String nameFiles = "<h2>Home</h2>\n<ul>";
					for (FTPFile f : files) {
						if(f.isDirectory())
							nameFiles += "<li><a href=\"" + restURL + "/listfiles/" + f.getName() + "\">" + f.getName() + "</a></li>";
						else if(f.isFile())
							nameFiles += "<li><a href=\"" + restURL + "/getfile/" + f.getName() + "\">" + f.getName() + "</a></li>";
					}
					ftpClient.logout();
					nameFiles += "</ul>";
					return nameFiles;
				} else if (ftpClient.changeWorkingDirectory(file)) {
					FTPFile files[] = ftpClient.listFiles();
					String nameFiles = "<h2>" + file + "</h2>\n<ul>";
					for (FTPFile f : files) {
						if(f.isDirectory())
							nameFiles += "<li><a href=\"" + restURL + "/listfiles/" + file + "/" + f.getName() + "\">" + f.getName() + "</a></li>";
						else if(f.isFile())
							nameFiles += "<li><a href=\"" + restURL + "/getfile/" + file + "/" + f.getName() + "\">" + f.getName() + "</a></li>";

					}
					ftpClient.logout();
					nameFiles = "</ul>";
					return nameFiles;
				} else {
					return ("<h2>Incorrect path<h2>");
				}
			} else {
				return "You are not logged in";
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
			ftpClient.connect(ftpAddr, ftpPort);
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
				+ "<input type=\"text\" name=\"name\" value=\"anonymous\"><br>"
				+ "Password:<br>"
				+ "<input type=\"password\" name=\"pass\" value=\"bob\"><br><br>"
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
