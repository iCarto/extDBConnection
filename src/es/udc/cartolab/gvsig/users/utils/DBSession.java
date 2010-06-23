package es.udc.cartolab.gvsig.users.utils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.cresques.cts.IProjection;

import com.hardcode.driverManager.Driver;
import com.hardcode.driverManager.DriverLoadException;
import com.iver.cit.gvsig.fmap.drivers.ConnectionJDBC;
import com.iver.cit.gvsig.fmap.drivers.DBException;
import com.iver.cit.gvsig.fmap.drivers.DBLayerDefinition;
import com.iver.cit.gvsig.fmap.drivers.FieldDescription;
import com.iver.cit.gvsig.fmap.drivers.IVectorialJDBCDriver;
import com.iver.cit.gvsig.fmap.drivers.db.utils.ConnectionWithParams;
import com.iver.cit.gvsig.fmap.drivers.db.utils.SingleVectorialDBConnectionManager;
import com.iver.cit.gvsig.fmap.drivers.jdbc.postgis.PostGISWriter;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.LayerFactory;


public class DBSession {

	/** datos de la BD ¿mandarlos a archivos de configuracion? **/

	private static DBSession instance = null;
	private final String server, username, password;
	private final int port;
	private String database;
	private String schema;
	private EIELUser user;
	private ConnectionWithParams conwp;

	private DBSession(String server, int port, String username, String password) {
		this.server = server;
		this.port = port;
		this.username = username;
		this.password = password;

		ConfigFile file = ConfigFile.getInstance();
		this.schema = file.getSchema();
		this.database = file.getDatabase();
	}
	/**
	 * 
	 * @return the DB Connection or null if there isn't any
	 */
	public static DBSession getCurrentSession() {
		return instance;
	}

	/**
	 * Creates a new DB Connection or changes the current one.
	 * @param server
	 * @param port
	 * @param username
	 * @param password
	 * @return the connection
	 * @throws DBException if there's any problem (server error or login error)
	 */
	public static DBSession createConnection(String server, int port,
			String username, String password) throws DBException {
		if (instance != null) {
			instance.close();
		}
		instance = new DBSession(server, port, username, password);
		connect();
		return instance;
	}

	public static DBSession createConnection(String server, int port,
			String database, String username, String password) throws DBException {
		DBSession con = createConnection(server, port, username, password);
		instance.database = database;
		return con;
	}

	private static void connect() throws DBException {
		try {
			instance.conwp = SingleVectorialDBConnectionManager.instance().getConnection("PostGIS JDBC Driver",
					instance.username, instance.password, "ELLE_connection", instance.server, (new Integer(instance.port)).toString(), instance.database, true);
			instance.user = new EIELUser(instance.username, instance.password, ((ConnectionJDBC) instance.conwp.getConnection()).getConnection());
		} catch (DBException e) {
			if (instance!=null) {
				if (instance.conwp != null) {
					SingleVectorialDBConnectionManager.instance().closeAndRemove(instance.conwp);
				}
			}
			instance = null;
			throw e;
		}

	}

	public String getServer() {
		return server;
	}

	public int getPort() {
		return port;
	}

	public String getUserName() {
		return username;
	}

	public String getDatabase() {
		return database;
	}

	public String getSchema () {
		return schema;
	}

	public void changeSchema(String schema) {
		this.schema = schema;
	}

	public void changeDatabase(String database) {
		this.database = database;
	}

	public Connection getJavaConnection() {
		return ((ConnectionJDBC) conwp.getConnection()).getConnection();
	}

	public void close() throws DBException {

		user = null;
		if (conwp!=null) {
			SingleVectorialDBConnectionManager.instance().closeAndRemove(conwp);
			conwp = null;
		}
		instance = null;

	}

	public EIELUser getEIELUser() {
		return user;
	}

	/* GET LAYER */

	public FLayer getLayer(String layerName, String tableName, String schema, String whereClause,
			IProjection projection) throws SQLException, DBException {
		//Code by Sergio Piñón (gvsig_desarrolladores)

		if (whereClause == null) {
			whereClause = "";
		}

		if (schema == null || schema.compareTo("")==0) {
			schema = this.schema;
		}

		DBLayerDefinition dbLayerDef = new DBLayerDefinition();
		dbLayerDef.setCatalogName(database); //Nombre de la base de datos
		dbLayerDef.setSchema(schema); //Nombre del esquema
		dbLayerDef.setTableName(tableName); //Nombre de la tabla
		dbLayerDef.setWhereClause("");
		dbLayerDef.setConnection(conwp.getConnection());

		Connection con = ((ConnectionJDBC) conwp.getConnection()).getConnection();
		DatabaseMetaData metadataDB = con.getMetaData();

		String tipos[] = new String[1];
		tipos[0] = "TABLE";
		ResultSet tablas = metadataDB.getTables(null, this.schema, tableName, tipos);
		tablas.next();
		//String t = tablas.getString(tablas.findColumn( "TABLE_NAME" ));

		ResultSet columnas = metadataDB.getColumns(null,this.schema,tableName, "%");
		ResultSet claves = metadataDB.getPrimaryKeys(null, this.schema, tableName);

		//ResultSetMetaData aux = columnas.getMetaData();

		ArrayList<FieldDescription> descripciones = new ArrayList <FieldDescription>();
		ArrayList<String> nombres = new ArrayList<String>();

		while(columnas.next()) {
			//log.info("Tratando atributo: \""+columnas.getString("Column_Name")+"\" de la tabla: "+nombreTabla);
			if(columnas.getString("Type_Name").equalsIgnoreCase("geometry")) {
				/*si es la columna de geometria*/
				//log.info("Encontrado atributo de geometria para la tabla: "+nombreTabla);
				dbLayerDef.setFieldGeometry(columnas.getString("Column_Name"));
			}
			else {
				FieldDescription fieldDescription = new FieldDescription();
				fieldDescription.setFieldName(columnas.getString("Column_Name"));
				fieldDescription.setFieldType(columnas.getType());
				descripciones.add(fieldDescription);
				nombres.add(columnas.getString("Column_Name"));
			}
		}
		FieldDescription fields[] = new FieldDescription[descripciones.size()];
		String s[] = new String[nombres.size()];
		for(int i = 0; i < descripciones.size(); i++)  {
			fields[i] = descripciones.get(i);
			s[i] = nombres.get(i);
		}

		dbLayerDef.setFieldsDesc(fields);
		dbLayerDef.setFieldNames(s);

		if (whereClause.compareTo("")!=0) {
			dbLayerDef.setWhereClause(whereClause);
		}

		/*buscamos clave primaria y la añadimos a la definicion de la capa*/
		//OJO, esta solución no vale con claves primarias de más de una columna!!!
		while(claves.next()) {
			dbLayerDef.setFieldID(claves.getString("Column_Name"));
		}

		if (dbLayerDef.getFieldID() == null) {
			dbLayerDef.setFieldID("gid");
		}

		PostGISWriter writer = new PostGISWriter();

		writer.setWriteAll(false);
		writer.setCreateTable(false);

		Driver drv;
		FLayer lyr = null;
		try {
			drv = LayerFactory.getDM().getDriver("PostGIS JDBC Driver");
			IVectorialJDBCDriver dbDriver = (IVectorialJDBCDriver) drv;

			dbDriver.setData(conwp.getConnection(), dbLayerDef);

			lyr =  LayerFactory.createDBLayer(dbDriver, layerName, projection);
			/*asignamos proyección a la capa y al ViewPort*/
			dbLayerDef.setSRID_EPSG(projection.getAbrev());
		} catch (DriverLoadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return lyr;
	}

	public FLayer getLayer(String layerName, String tableName, String whereClause,
			IProjection projection) throws SQLException, DBException {
		return getLayer(layerName, tableName, schema, whereClause, projection);
	}

	public FLayer getLayer(String tableName, String whereClause,
			IProjection projection) throws SQLException, DBException {
		return getLayer(tableName, tableName, schema, whereClause, projection);
	}

	public FLayer getLayer(String tableName, IProjection projection) throws SQLException, DBException {
		return getLayer(tableName, null, projection);
	}


	/* GET TABLE */

	public String[][] getTable(String tableName, String schema, String whereClause,
			String[] orderBy, boolean desc) throws SQLException {

		Connection con = ((ConnectionJDBC) conwp.getConnection()).getConnection();

		DatabaseMetaData metadataDB = con.getMetaData();

		ResultSet columns = metadataDB.getColumns(null,null,tableName, "%");
		List<String> fieldNames = new ArrayList<String>();

		while (columns.next()) {
			fieldNames.add(columns.getString("Column_Name"));
		}

		return getTable(tableName, schema, fieldNames.toArray(new String[0]), whereClause,
				orderBy, desc);
	}


	public String[][] getTable(String tableName, String schema, String[] fieldNames, String whereClause,
			String[] orderBy, boolean desc) throws SQLException {
		Connection con = ((ConnectionJDBC) conwp.getConnection()).getConnection();

		if (whereClause == null) {
			whereClause = "";
		}

		int numFieldsOrder;

		if (orderBy == null) {
			numFieldsOrder = 0;
		} else {
			numFieldsOrder = orderBy.length;
		}


		String query = "SELECT ";
		for (int i=0; i<fieldNames.length-1; i++) {
			query = query + fieldNames[i] + ", ";
		}

		query = query + fieldNames[fieldNames.length-1] + " FROM " + schema + "." + tableName;

		List<String> whereValues = new ArrayList<String>();

		if (whereClause.compareTo("")==0) {
			query = query + " WHERE 1";
		} else {

			int quoteIdx = whereClause.indexOf('\'');
			while (quoteIdx>-1) {
				int endQuote = whereClause.indexOf('\'', quoteIdx+1);
				String subStr = whereClause.substring(quoteIdx+1, endQuote);
				whereValues.add(subStr);
				quoteIdx = whereClause.indexOf('\'', endQuote+1);
			}

			for (int i=0; i<whereValues.size(); i++) {
				whereClause = whereClause.replaceFirst("'" + whereValues.get(i) + "'", "?");
			}

			if (whereClause.toUpperCase().startsWith("WHERE")){
				query = query + " " + whereClause;
			} else {
				query = query + " WHERE " + whereClause;
			}
		}

		if (numFieldsOrder > 0) {
			query = query + " ORDER BY ";
			for (int i=0; i<numFieldsOrder-1; i++) {
				query = query + orderBy[i] + ", ";
			}
			query = query + orderBy[orderBy.length-1];

			if (desc) {
				query = query + " DESC";
			}
		}

		PreparedStatement stat = con.prepareStatement(query);
		for (int i=0; i<whereValues.size(); i++) {
			stat.setString(i+1, whereValues.get(i));
		}

		ResultSet rs = stat.executeQuery();

		ArrayList<String[]> rows = new ArrayList<String[]>();
		while (rs.next()) {
			String[] row = new String[fieldNames.length];
			for (int i=0; i<fieldNames.length; i++) {
				String val = rs.getString(fieldNames[i]);
				if (val == null || val.compareTo("")==0) {
					val = " ";
				}
				row[i] = val;
			}
			rows.add(row);
		}
		rs.close();

		return rows.toArray(new String[0][0]);

	}

	public String[][] getTable(String tableName, String schema,
			String[] orderBy, boolean desc) throws SQLException {
		return getTable(tableName, schema, null, orderBy, desc);
	}

	public String[][] getTable(String tableName, String schema,
			String whereClause) throws SQLException {
		return getTable(tableName, schema, whereClause, null, false);
	}

	public String[][] getTable(String tableName, String whereClause) throws SQLException {
		return getTable(tableName, schema, whereClause, null, false);
	}

	public String[][] getTable(String tableName) throws SQLException {
		return getTable(tableName, schema, null, null, false);
	}



	/* GET DISTINCT VALUES FROM A COLUMN */

	public String[] getDistinctValues(String tableName, String schema, String fieldName, boolean sorted, boolean desc) throws SQLException {

		Connection con = ((ConnectionJDBC) conwp.getConnection()).getConnection();

		Statement stat = con.createStatement();

		if (schema == null | schema.length() == 0) {
			schema = this.schema;
		}

		String query = "SELECT DISTINCT " + fieldName + " FROM " + schema + "." + tableName;

		if (sorted) {
			query = query + " ORDER BY " + fieldName;
			if (desc) {
				query = query + " DESC";
			}
		}

		ResultSet rs = stat.executeQuery(query);

		List <String>resultArray = new ArrayList<String>();
		while (rs.next()) {
			String val = rs.getString(fieldName);
			resultArray.add(val);
		}
		rs.close();

		String[] result = new String[resultArray.size()];
		for (int i=0; i<resultArray.size(); i++) {
			result[i] = resultArray.get(i);
		}

		return result;

	}

	public String[] getDistinctValues(String tableName, String schema, String fieldName) throws SQLException {
		return getDistinctValues(tableName, schema, fieldName, false, false);
	}

	public String[] getDistinctValues(String tableName, String fieldName) throws SQLException {
		return getDistinctValues(tableName, schema, fieldName, false, false);
	}

	public String[] getTables(boolean onlyGeospatial) throws SQLException {

		Connection con = ((ConnectionJDBC) conwp.getConnection()).getConnection();
		DatabaseMetaData metadataDB = con.getMetaData();
		ResultSet rs = metadataDB.getTables(null, null, null, new String[] {"TABLE"});
		List<String> tables = new ArrayList<String>();
		while (rs.next()) {
			String tableName = rs.getString("TABLE_NAME");
			if (onlyGeospatial) {
				boolean geometry = false;
				ResultSet columns = metadataDB.getColumns(null,null,tableName, "%");
				while (columns.next()) {
					if (columns.getString("Type_name").equalsIgnoreCase("geometry")) {
						geometry = true;
						break;
					}
				}
				if (geometry) {
					tables.add(tableName);
				}
			} else {
				tables.add(tableName);
			}
		}
		String[] result = new String[tables.size()];
		for (int i=0; i<tables.size(); i++) {
			result[i] = tables.get(i);
		}
		return result;
	}

	public String[] getColumns(String table) throws SQLException {

		Connection con = ((ConnectionJDBC) conwp.getConnection()).getConnection();
		DatabaseMetaData metadataDB = con.getMetaData();

		ResultSet columns = metadataDB.getColumns(null,null,table, "%");
		List <String> cols = new ArrayList<String>();
		while (columns.next()) {
			cols.add(columns.getString("Column_name"));
		}
		String[] result = new String[cols.size()];
		for (int i=0; i<cols.size(); i++) {
			result[i] = cols.get(i);
		}
		return result;
	}

}
