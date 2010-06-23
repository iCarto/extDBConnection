package es.udc.cartolab.gvsig.users.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;



public class ConfigFile {

	private String preferencesFile;
	private static ConfigFile instance = null;
	private String server;
	private String database;
	private String schema;
	private String username;
	private String port;

	private boolean fileExists = true;

	private ConfigFile() {
		String dir = System.getProperty("user.dir");
		if (dir.endsWith(File.separator)) {
			preferencesFile = dir + "dbconnection.cfg";
		} else {
			preferencesFile = dir + File.separator + "dbconnection.cfg";
		}

		try {
			getProperties();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static ConfigFile getInstance() {
		if (instance == null) {
			instance = new ConfigFile();
		}
		return instance;
	}

	private void getProperties() throws IOException {
		File configFile = new File(preferencesFile);
		String line;
		try {
			//leer el archivo en busca de los datos
			BufferedReader fileReader = new BufferedReader(new FileReader(configFile));
			while ((line = fileReader.readLine())!=null) {
				if (line.charAt(0) != '#') {
					int spacePos = line.indexOf(" ");
					if (spacePos>0) {
						String configWord = line.substring(0, spacePos);
						if (configWord.toLowerCase().compareTo("server")==0) {
							//get value without the quotation marks
							server = line.substring(line.indexOf('"'));
							server = server.substring(1);
							server = server.substring(0, server.indexOf('"'));
						}
						if (configWord.toLowerCase().compareTo("port")==0) {
							//get value without the quotation marks
							port = line.substring(line.indexOf('"'));
							port = port.substring(1);
							port = port.substring(0, port.indexOf('"'));
						}
						if (configWord.toLowerCase().compareTo("database")==0) {
							//get value without the quotation marks
							database = line.substring(line.indexOf('"'));
							database = database.substring(1);
							database = database.substring(0, database.indexOf('"'));
						}
						if (configWord.toLowerCase().compareTo("schema")==0) {
							//get value without the quotation marks
							schema = line.substring(line.indexOf('"'));
							schema = schema.substring(1);
							schema = schema.substring(0, schema.indexOf('"'));
						}
						if (configWord.toLowerCase().compareTo("username")==0) {
							//get value without the quotation marks
							username = line.substring(line.indexOf('"'));
							username = username.substring(1);
							username = username.substring(0, username.indexOf('"'));
						}
					}
				}
			}
		} catch (FileNotFoundException fnfe) {
			server = "";
			port = "";
			username = "";
			schema = "";
			database = "";
			fileExists = false;
		}
	}

	public String getServer() {
		return server;
	}

	public String getPort() {
		return port;
	}

	public String getDatabase() {
		return database;
	}

	public String getSchema() {
		return schema;
	}

	public String getUsername() {
		return username;
	}

	private void saveProperties() throws IOException {
		//save the file
		File configFile = new File(preferencesFile);
		if (!configFile.exists()) {
			configFile.createNewFile();
			fileExists = true;
		}
		if (configFile.canWrite()) {
			FileWriter fileWriter = new FileWriter(configFile);
			//TODO: añadir cabecera o descripción de los campos
			String[] lines = new String[5];
			lines[0] = "server \"" + server + "\"";
			lines[1] = "port \"" + port + "\"";
			lines[2] = "database \"" + database + "\"";
			lines[3] = "schema \"" + schema + "\"";
			lines[4] = "username \"" + username + "\"";
			for (int i = 0; i<lines.length; i++) {
				fileWriter.write(lines[i]+"\n");
			}
			fileWriter.flush();
		} else {
			throw new IOException("The file can not be edited");
		}
	}

	public void setProperties(String server, String port,
			String database, String schema, String username) throws IOException {

		this.server = server;
		this.port = port;
		this.database = database;
		this.schema = schema;
		this.username = username;

		saveProperties();
	}

	public boolean fileExists() {
		return fileExists;
	}
}
