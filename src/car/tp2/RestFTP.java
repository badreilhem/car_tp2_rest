package car.tp2;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

@Path("/restftp")
public class RestFTP {

	String restURL = "http://localhost:2323/rest/tp2/restftp";
	String ftpAddr = "192.168.0.10";
	int ftpPort = 12345;
	// String ftpAddr = "edel-braut.lifl.fr";
	// int ftpPort = 21;

	String targetDir = "";
	String userName = "";
	String passWord = "";
	private boolean authentified;
	private FTPClient ftpClient;

	public RestFTP() {
		ftpClient = new FTPClient();
		authentified = false;
	}

	/**
	 * loginForm
	 * 
	 * @return
	 */
	@GET
	@Path("/login")
	@Produces("text/html")
	public String loginForm() {
		return " <form method=\"post\" action='"
				+ restURL
				+ "/connection'>"
				+ "name:<br>"
				+ "<input type=\"text\" name=\"name\" value=\"anonymous\"><br>"
				+ "Password:<br>"
				+ "<input type=\"password\" name=\"pass\" value=\"bob\"><br><br>"
				+ "<input type=\"submit\" value=\"Submit\">" + "</form> ";
	}

	/*
	 * connection
	 */
	@POST
	@Path("/connection")
	@Consumes("application/x-www-form-urlencoded")
	public String connection(@FormParam("name") String name,
			@FormParam("pass") String pass) throws URISyntaxException,
			IOException {
		userName = name;

		if (ftpClient.isConnected()) {
			ftpClient.disconnect();
		}

		ftpClient.connect(ftpAddr, ftpPort);
		authentified = ftpClient.login(name, pass);

		if (authentified)
			return "<h2>authentification ok, go to <a href='" + restURL
					+ "/list/'>the file list</a></h2>";

		userName = null;
		ftpClient.disconnect();
		return "<h2>Error authentification</h2>";
	}

	/**
	 * deconnection
	 * 
	 * @return
	 * @throws IOException
	 */
	@GET
	@Path("/deconnection")
	@Produces("text/html")
	public String deconnection() throws IOException {
		ftpClient.logout();
		ftpClient.disconnect();
		authentified = false;
		userName = null;
		return "<h3>You are now disconnected, please go the <a href='"
				+ restURL + "/login'>login page</a></h3>";
	}

	/**
	 * list
	 * 
	 * @param path
	 * @return
	 * @throws IOException
	 */
	@GET
	@Path("/list/{path: .*}")
	@Produces("text/html")
	public String list(@PathParam("path") String path) throws IOException {
		if (!authentified)
			return loginForm();

		FTPFile files[] = ftpClient.listFiles(path);

		String nameFiles = "<h3><a href='" + restURL
				+ "/deconnection'>Deconnection</a></h3>\n";
		nameFiles += "<h2>" + path + "</h2>\n";

		nameFiles += "<ul>\n";

		if (!path.isEmpty()) {

			nameFiles += "<li><a href='" + restURL + "/list/" + getParent(path)
					+ "'>[parent folder]</a></li>\n";
		}

		nameFiles += "<li><a href='" + restURL + "/upload/" + path
				+ "'>[Upload a file]</a></li>\n";

		for (FTPFile f : files) {
			nameFiles += "<li><a href=\"" + restURL;

			if (f.isDirectory())
				nameFiles += "/list/";
			else if (f.isFile())
				nameFiles += "/get/";

			nameFiles += path + "/" + f.getName() + "\">" + f.getName();

			nameFiles += "</a> -- <a href='" + restURL + "/delete/" + path
					+ "/" + f.getName() + "'>Delete</a></li>\n";
		}
		nameFiles += "</ul>\n";
		return nameFiles;
	}

	/**
	 * delete
	 * 
	 * @param file
	 * @return
	 * @throws java.io.IOException
	 */
	@GET
	@Path("/delete/{var: .*}")
	@Produces("text/html")
	public String deleteFile(@PathParam("var") String file)
			throws java.io.IOException {

		if (!authentified)
			return loginForm();

		if (ftpClient.deleteFile(file))
			return ("<h2>File " + file + " deleted, return to <a href='"
					+ restURL + "/list/" + getParent(file) + "'>the file list</a></h2>");
		else
			return ("<h2>Can't delete file " + file + ", return to <a href='"
					+ restURL + "/list/" + getParent(file) + "'>the file list</a></h2>");

	}

	/**
	 * get
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	@GET
	@Path("/get/{file: .*}")
	@Produces("application/octet-stream")
	public StreamingOutput get(@PathParam("file") String file)
			throws IOException {
		StreamingOutput so = null;

		// TODO ne fonctionne pas toujours, à revoir
		final InputStream is = ftpClient.retrieveFileStream(file);

		if (is != null) {
			so = new StreamingOutput() {
				public void write(OutputStream os) throws java.io.IOException {
					while (is.available() > 0)
						os.write(is.read());
					is.close();
					os.close();

				}
			};
		}

		return so;
	}

	@POST
	@Path("/postold/{var: .*}")
	@Consumes("application/octet-stream")
	public String postFile(InputStream is, @PathParam("var") String file)
			throws java.io.IOException {

		// TODO à tester après avoir fait le formulaire upload
		if (!authentified)
			return loginForm();

		if (ftpClient.storeFile(file, is))
			return ("Envoi fichier OK (POST), return to <a href='" + restURL
					+ "/list/" + getParent(file) + "'>the file list</a></h2>");

		return ("Envoi fichier KO (POST), return to <a href='" + restURL
				+ "/list/" + getParent(file) + "'>the file list</a></h2>");
	}

	/**
	 * upload
	 * 
	 * @param path
	 * @return
	 */
	@GET
	@Path("/upload/{path : .*}")
	@Produces("text/html")
	public String uploadForm(@PathParam("path") String path) {
		if (!authentified)
			return loginForm();

		return " <form method=\"post\" action='"
				+ restURL
				+ "/post' enctype='multipart/form-data'>"
				+ "file path:<br>"
				+ "<input type=\"file\" name=\"file\"><br>"
				+ "<input type=\"text\" name=\"name\" placeholder=\"enter file name\"><br>"
				+ "<input type=\"hidden\" name=\"path\" value=\"" + path + "/"
				+ "\">" + "<input type=\"submit\" value=\"Submit\">"
				+ "</form> ";
	}

	@SuppressWarnings("resource")
	@POST
	@Path("/post")
	@Consumes("multipart/form-data")
	@Produces("text/html")
	public String post(@Multipart("file") InputStream file,
			@Multipart("name") String name, @Multipart("path") String path)
			throws IOException {

		if (!authentified)
			return loginForm();

		if (ftpClient.storeFile(path + name, file))
			return "<h2>file " + file + " posted, go to <a href='" + restURL
					+ "/list/'>the file list</a></h2>";

		return ("<h2>Une erreur lors de poster le fichier " + file + "</h2>");
	}

	/**
	 * getParent
	 * 
	 * @param path
	 * @return
	 */
	private String getParent(String path) {
		String[] splitString = path.split("/");
		String parent = "";
		for (int i = 0; i < splitString.length - 1; i++) {
			String s = splitString[i];
			if (s != "")
				parent += s + "/";
		}
		return parent;
	}
}
