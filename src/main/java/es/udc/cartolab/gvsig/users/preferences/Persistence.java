package es.udc.cartolab.gvsig.users.preferences;

import org.gvsig.andami.PluginServices;
import org.gvsig.utils.XMLEntity;

public class Persistence {

	public static final String HOST_KEY = "host";
	public static final String PORT_KEY = "port";
	public static final String DATABASE_KEY = "database";
	public static final String USER_KEY = "user";
	public static final String PWD_KEY = "pwd";
	public static final String SCHEMA_KEY = "schema";
	public static final String OPEN_CONNECTION_DIALOG_AT_STARTUP = "OPEN_CONNECTION_DIALOG_AT_STARTUP";
	private static final String AUTOCONNECT_AT_STARTUP = "AUTOCONNECT_AT_STARTUP";

	public final String host;
	public final String port;
	public int portInt;
	public final String database;
	public final String user;
	public final String pwd;
	public final String schema;

	public final boolean openDialogAtStartUp;
	public final boolean autoConnectAtStartUp;

	private final XMLEntity xml;

	public Persistence() {
		PluginServices ps = PluginServices.getPluginServices(this);
		xml = ps.getPersistentXML();

		host = getStringValue(HOST_KEY);
		port = getStringValue(PORT_KEY);
		try {
			portInt = Integer.parseInt(port);
		} catch (Exception e) {
			portInt = -1;
		}
		database = getStringValue(DATABASE_KEY);
		user = getStringValue(USER_KEY);
		pwd = getStringValue(PWD_KEY);
		schema = getStringValue(SCHEMA_KEY);

		openDialogAtStartUp = getBooleanValue(OPEN_CONNECTION_DIALOG_AT_STARTUP);
		autoConnectAtStartUp = getBooleanValue(AUTOCONNECT_AT_STARTUP);
	}

	public boolean paramsAreSet() {
		if (!host.isEmpty() && !port.isEmpty() && !database.isEmpty() && !user.isEmpty()) {
			return true;
		}
		return false;
	}

	private String getStringValue(String key) {
		if (xml.contains(key)) {
			return xml.getStringProperty(key);
		}
		return "";
	}

	private boolean getBooleanValue(String key) {
		if (xml.contains(key)) {
			return xml.getBooleanProperty(key);
		}
		return false;
	}

}
