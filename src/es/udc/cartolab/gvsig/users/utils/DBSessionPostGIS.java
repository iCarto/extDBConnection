/*
 * Copyright (c) 2010. CartoLab, Universidad de A Coruña
 *
 * This file is part of extDBConnection
 *
 * extDBConnection is free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or any later version.
 *
 * extDBConnection is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with extDBConnection.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package es.udc.cartolab.gvsig.users.utils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cresques.cts.IProjection;

import com.hardcode.driverManager.Driver;
import com.hardcode.driverManager.DriverLoadException;
import com.iver.cit.gvsig.fmap.drivers.ConnectionJDBC;
import com.iver.cit.gvsig.fmap.drivers.DBException;
import com.iver.cit.gvsig.fmap.drivers.DBLayerDefinition;
import com.iver.cit.gvsig.fmap.drivers.FieldDescription;
import com.iver.cit.gvsig.fmap.drivers.IVectorialJDBCDriver;
import com.iver.cit.gvsig.fmap.drivers.db.utils.SingleDBConnectionManager;
import com.iver.cit.gvsig.fmap.drivers.jdbc.postgis.PostGisDriver;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.LayerFactory;

public class DBSessionPostGIS extends DBSession {

	private String schema = "";
	protected static String CONNECTION_STRING_BEGINNING = "jdbc:postgresql:";

	protected DBSessionPostGIS(String server, int port, String database,
			String schema, String username, String password) {
		this.server = server;
		this.port = port;
		this.username = username;
		this.password = password;

		this.database = database;
		this.schema = schema;

	}

	/**
	 * Creates a new DB Connection or changes the current one.
	 * 
	 * @param connString
	 * @param username
	 * @param password
	 * @return the connection
	 * @throws DBException
	 *             if there's any problem (server error or login error)
	 */
	public static DBSession createConnectionFromConnString(String connString,
			String username, String password) throws DBException {
		if (!connString.startsWith(CONNECTION_STRING_BEGINNING)) {
			return null;
		}
		if (instance != null) {
			instance.close();
		}

		connString = connString.replaceFirst(
				CONNECTION_STRING_BEGINNING + "//", "");
		String[] split = connString.split(":");
		String server = split[0];
		if (split.length > 1) {
			split = split[1].split("/");
			int port = Integer.parseInt(split[0]);
			if (split.length > 1) {
				String database = split[1];
				instance = new DBSessionPostGIS(server, port, database,
						"public", username, password);
				instance.connect();
				return instance;
			}
		}
		return null;
	}

	/**
	 * Creates a new DB Connection or changes the current one.
	 * 
	 * @param server
	 * @param port
	 * @param database
	 * @param schema
	 * @param username
	 * @param password
	 * @return the connection
	 * @throws DBException
	 *             if there's any problem (server error or login error)
	 */
	public static DBSession createConnection(String server, int port,
			String database, String schema, String username, String password)
			throws DBException {
		if (instance != null) {
			instance.close();
		}
		instance = new DBSessionPostGIS(server, port, database, schema,
				username, password);
		instance.connect();
		return instance;
	}

	protected void connect() throws DBException {
		try {
			conwp = SingleDBConnectionManager.instance().getConnection(
					getDriverName(), username, password, "ELLE_connection",
					server, (new Integer(port)).toString(), database, "", true);
			user = new DBUserPostGIS(username, password,
					((ConnectionJDBC) conwp.getConnection()).getConnection());
		} catch (DBException e) {
			if (conwp != null) {
				SingleDBConnectionManager.instance().closeAndRemove(conwp);
			}
			instance = null;
			throw e;
		}
	}

	public String getSchema() {
		return schema;
	}

	public void changeSchema(String schema) {
		this.schema = schema;
	}

	/* GET LAYER */

	public FLayer getLayer(String layerName, String tableName, String schema,
			String whereClause, IProjection projection) throws SQLException,
			DBException {
		// Code by Sergio Piñón (gvsig_desarrolladores)

		if (whereClause == null) {
			whereClause = "";
		}

		String database = this.database;
		if (this.server.compareTo("") != 0) {
			if (schema == null || schema.compareTo("") == 0) {
				schema = this.schema;
			}
		} else {
			schema = "";
			database = "";
		}

		DBLayerDefinition dbLayerDef = new DBLayerDefinition();
		dbLayerDef.setCatalogName(database); // Nombre de la base de datos
		dbLayerDef.setSchema(schema); // Nombre del esquema
		dbLayerDef.setTableName(tableName); // Nombre de la tabla
		dbLayerDef.setWhereClause("");
		dbLayerDef.setConnection(conwp.getConnection());

		Connection con = ((ConnectionJDBC) conwp.getConnection())
				.getConnection();
		DatabaseMetaData metadataDB = con.getMetaData();

		String tipos[] = new String[1];
		tipos[0] = "TABLE";
		ResultSet tablas = metadataDB.getTables(null, schema, tableName, tipos);
		tablas.next();
		// String t = tablas.getString(tablas.findColumn( "TABLE_NAME" ));

		ResultSet columnas = metadataDB
				.getColumns(null, schema, tableName, "%");
		ResultSet claves = metadataDB.getPrimaryKeys(null, schema, tableName);

		// ResultSetMetaData aux = columnas.getMetaData();

		ArrayList<FieldDescription> descripciones = new ArrayList<FieldDescription>();
		ArrayList<String> nombres = new ArrayList<String>();

		while (columnas.next()) {
			// log.info("Tratando atributo: \""+columnas.getString("Column_Name")+"\" de la tabla: "+nombreTabla);
			if (columnas.getString("Type_Name").equalsIgnoreCase("geometry")) {
				/* si es la columna de geometria */
				// log.info("Encontrado atributo de geometria para la tabla: "+nombreTabla);
				dbLayerDef.setFieldGeometry(columnas.getString("Column_Name"));
			} else {
				FieldDescription fieldDescription = new FieldDescription();
				fieldDescription
						.setFieldName(columnas.getString("Column_Name"));
				fieldDescription.setFieldType(columnas.getType());
				descripciones.add(fieldDescription);
				nombres.add(columnas.getString("Column_Name"));
			}
		}
		FieldDescription fields[] = new FieldDescription[descripciones.size()];
		String s[] = new String[nombres.size()];
		for (int i = 0; i < descripciones.size(); i++) {
			fields[i] = descripciones.get(i);
			s[i] = nombres.get(i);
		}

		dbLayerDef.setFieldsDesc(fields);
		dbLayerDef.setFieldNames(s);

		if (whereClause.compareTo("") != 0) {
			dbLayerDef.setWhereClause(whereClause);
		}

		/* buscamos clave primaria y la añadimos a la definicion de la capa */
		// OJO, esta solución no vale con claves primarias de más de una
		// columna!!!
		while (claves.next()) {
			dbLayerDef.setFieldID(claves.getString("Column_Name"));
		}

		if (dbLayerDef.getFieldID() == null) {
			if (this.database.compareTo("") != 0) {
				dbLayerDef.setFieldID("gid");
			} else {
				dbLayerDef.setFieldID("PK_UID");
			}
		}

		dbLayerDef.setSRID_EPSG(projection.getAbrev());

		Driver drv;
		FLayer lyr = null;
		try {
			drv = LayerFactory.getDM().getDriver("PostGIS JDBC Driver");
			IVectorialJDBCDriver dbDriver = (IVectorialJDBCDriver) drv;

			dbDriver.setData(conwp.getConnection(), dbLayerDef);

			lyr = LayerFactory.createDBLayer(dbDriver, layerName, projection);
			/* asignamos proyección a la capa y al ViewPort */
			// dbLayerDef.setSRID_EPSG(projection.getAbrev());
		} catch (DriverLoadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return lyr;
	}

	public FLayer getLayer(String layerName, String tableName,
			String whereClause, IProjection projection) throws SQLException,
			DBException {
		return getLayer(layerName, tableName, schema, whereClause, projection);
	}

	public FLayer getLayer(String tableName, String whereClause,
			IProjection projection) throws SQLException, DBException {
		return getLayer(tableName, tableName, schema, whereClause, projection);
	}

	public FLayer getLayer(String tableName, IProjection projection)
			throws SQLException, DBException {
		return getLayer(tableName, null, projection);
	}

	/* GET TABLE */

	protected String[] getColumnNames(String tablename, String schema)
			throws SQLException {

		Connection con = ((ConnectionJDBC) conwp.getConnection())
				.getConnection();

		String query = "SELECT * FROM " + schema + "." + tablename + " LIMIT 1";
		Statement st = con.createStatement();
		ResultSet resultSet = st.executeQuery(query);
		ResultSetMetaData md = resultSet.getMetaData();
		String[] cols = new String[md.getColumnCount()];
		for (int i = 0; i < md.getColumnCount(); i++) {
			cols[i] = md.getColumnLabel(i + 1);
		}
		return cols;
	}

	protected int getColumnType(String tablename, String schema, String column)
			throws SQLException {

		Connection con = ((ConnectionJDBC) conwp.getConnection())
				.getConnection();

		DatabaseMetaData meta = con.getMetaData();
		ResultSet rsColumns = meta.getColumns(null, schema, tablename, column);
		while (rsColumns.next()) {
			if (column.equalsIgnoreCase(rsColumns.getString("COLUMN_NAME"))) {
				return rsColumns.getInt("COLUMN_TYPE");
			}
		}
		return -1;
	}

	public String[][] getTable(String tableName, String schema,
			String whereClause, String[] orderBy, boolean desc)
			throws SQLException {

		String[] columnNames = getColumnNames(tableName, schema);

		return getTable(tableName, schema, columnNames, whereClause, orderBy,
				desc);
	}

	public String[][] getTable(String tableName, String schema,
			String[] fieldNames, String whereClause, String[] orderBy,
			boolean desc) throws SQLException {
		Connection con = ((ConnectionJDBC) conwp.getConnection())
				.getConnection();

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
		Set<String> queriedFields = new HashSet<String>();
		for (int i = 0; i < fieldNames.length; i++) {
			if (!queriedFields.contains(fieldNames[i])) {
				query = query + fieldNames[i] + ", ";
				queriedFields.add(fieldNames[i]);
			}
		}

		query = query.substring(0, query.length() - 2) + " FROM " + schema
				+ "." + tableName;

		List<String> whereValues = new ArrayList<String>();

		if (whereClause.compareTo("") != 0) {

			int quoteIdx = whereClause.indexOf('\'');
			while (quoteIdx > -1) {
				int endQuote = whereClause.indexOf('\'', quoteIdx + 1);
				String subStr = whereClause.substring(quoteIdx + 1, endQuote);
				whereValues.add(subStr);
				quoteIdx = whereClause.indexOf('\'', endQuote + 1);
			}

			for (int i = 0; i < whereValues.size(); i++) {
				whereClause = whereClause.replaceFirst("'" + whereValues.get(i)
						+ "'", "?");
			}

			if (whereClause.toUpperCase().startsWith("WHERE")) {
				query = query + " " + whereClause;
			} else {
				query = query + " WHERE " + whereClause;
			}
		}

		if (numFieldsOrder > 0) {
			query = query + " ORDER BY ";
			for (int i = 0; i < numFieldsOrder - 1; i++) {
				query = query + orderBy[i] + ", ";
			}
			query = query + orderBy[orderBy.length - 1];

			if (desc) {
				query = query + " DESC";
			}
		}

		PreparedStatement stat = con.prepareStatement(query);
		for (int i = 0; i < whereValues.size(); i++) {
			stat.setString(i + 1, whereValues.get(i));
		}

		ResultSet rs = stat.executeQuery();

		ArrayList<String[]> rows = new ArrayList<String[]>();
		while (rs.next()) {
			String[] row = new String[fieldNames.length];
			for (int i = 0; i < fieldNames.length; i++) {
				String val = rs.getString(fieldNames[i]);
				if (val == null) {
					val = "";
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

	public String[][] getTable(String tableName, String whereClause)
			throws SQLException {
		return getTable(tableName, schema, whereClause, null, false);
	}

	public String[][] getTable(String tableName) throws SQLException {
		return getTable(tableName, schema, null, null, false);
	}

	/* GET DISTINCT VALUES FROM A COLUMN */

	public String[] getDistinctValues(String tableName, String schema,
			String fieldName, boolean sorted, boolean desc, String whereClause)
			throws SQLException {

		Connection con = ((ConnectionJDBC) conwp.getConnection())
				.getConnection();

		Statement stat = con.createStatement();

		if (schema == null | schema.length() == 0) {
			schema = this.schema;
		}

		if (whereClause == null) {
			whereClause = "";
		}

		String query = "SELECT DISTINCT " + fieldName + " FROM " + schema + "."
				+ tableName + " " + whereClause;

		if (sorted) {
			query = query + " ORDER BY " + fieldName;
			if (desc) {
				query = query + " DESC";
			}
		}

		ResultSet rs = stat.executeQuery(query);

		List<String> resultArray = new ArrayList<String>();
		while (rs.next()) {
			String val = rs.getString(fieldName);
			resultArray.add(val);
		}
		rs.close();

		String[] result = new String[resultArray.size()];
		for (int i = 0; i < resultArray.size(); i++) {
			result[i] = resultArray.get(i);
		}

		return result;

	}

	public String[] getDistinctValues(String tableName, String schema,
			String fieldName, boolean sorted, boolean desc) throws SQLException {
		return getDistinctValues(tableName, schema, fieldName, sorted, desc,
				null);
	}

	public String[] getDistinctValues(String tableName, String schema,
			String fieldName) throws SQLException {
		return getDistinctValues(tableName, schema, fieldName, false, false,
				null);
	}

	public String[] getDistinctValues(String tableName, String fieldName)
			throws SQLException {
		return getDistinctValues(tableName, schema, fieldName, false, false,
				null);
	}

	public String[] getTables(boolean onlyGeospatial) throws SQLException {

		Connection con = ((ConnectionJDBC) conwp.getConnection())
				.getConnection();
		DatabaseMetaData metadataDB = con.getMetaData();
		ResultSet rs = metadataDB.getTables(null, null, null,
				new String[] { "TABLE" });
		List<String> tables = new ArrayList<String>();
		while (rs.next()) {
			String tableName = rs.getString("TABLE_NAME");
			if (onlyGeospatial) {
				boolean geometry = false;
				ResultSet columns = metadataDB.getColumns(null, null,
						tableName, "%");
				while (columns.next()) {
					if (columns.getString("Type_name").equalsIgnoreCase(
							"geometry")) {
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
		for (int i = 0; i < tables.size(); i++) {
			result[i] = tables.get(i);
		}
		return result;
	}

	public String[] getColumns(String table) throws SQLException {

		return getColumns(null, table);

	}

	public String[] getColumns(String schema, String table) throws SQLException {

		Connection con = ((ConnectionJDBC) conwp.getConnection())
				.getConnection();
		DatabaseMetaData metadataDB = con.getMetaData();

		ResultSet columns = metadataDB.getColumns(null, schema, table, "%");
		List<String> cols = new ArrayList<String>();
		while (columns.next()) {
			cols.add(columns.getString("Column_name"));
		}
		String[] result = new String[cols.size()];
		for (int i = 0; i < cols.size(); i++) {
			result[i] = cols.get(i);
		}
		return result;
	}

	/**
	 * Be careful!
	 * 
	 * @param table
	 * @param whereClause
	 * @throws SQLException
	 */
	public void deleteRows(String schema, String table, String whereClause)
			throws SQLException {

		Connection con = ((ConnectionJDBC) conwp.getConnection())
				.getConnection();

		String sql = "DELETE FROM " + schema + "." + table + " " + whereClause;

		Statement statement = con.createStatement();
		statement.executeUpdate(sql);
		con.commit();
	}

	public void insertRow(String schema, String table, Object[] values)
			throws SQLException {

		String[] columns = getColumnNames(table, schema);
		insertRow(schema, table, columns, values);
	}

	public void insertRow(String schema, String table, String[] columns,
			Object[] values) throws SQLException {

		Connection con = ((ConnectionJDBC) conwp.getConnection())
				.getConnection();

		if (columns.length == values.length) {
			String sql = "INSERT INTO " + schema + "." + table + " (";
			for (String col : columns) {
				sql = sql + col + ", ";
			}
			sql = sql.substring(0, sql.length() - 2) + ") VALUES (";
			for (int i = 0; i < columns.length; i++) {
				sql = sql + "?, ";
			}
			sql = sql.substring(0, sql.length() - 2) + ")";

			PreparedStatement statement = con.prepareStatement(sql);

			for (int i = 0; i < columns.length; i++) {
				statement.setObject(i + 1, values[i]);
			}

			statement.executeUpdate();
			con.commit();

		}

	}

	/**
	 * Be careful!
	 * 
	 * @param schema
	 * @param tablename
	 * @param fields
	 * @param values
	 * @param whereClause
	 * @throws SQLException
	 */
	public void updateRows(String schema, String tablename, String[] columns,
			Object[] values, String whereClause) throws SQLException {

		Connection con = ((ConnectionJDBC) conwp.getConnection())
				.getConnection();

		if (columns.length == values.length) {
			String sql = "UPDATE " + schema + "." + tablename + " SET ";
			for (String column : columns) {
				sql = sql + column + "=?, ";
			}
			sql = sql.substring(0, sql.length() - 2) + " " + whereClause;

			PreparedStatement statement = con.prepareStatement(sql);
			for (int i = 0; i < values.length; i++) {
				statement.setObject(i + 1, values[i]);
			}

			statement.executeUpdate();
			con.commit();
		}

	}

	public boolean tableExists(String schema, String tablename)
			throws SQLException {

		Connection con = ((ConnectionJDBC) conwp.getConnection())
				.getConnection();

		if (this.server.compareTo("") != 0) {
			String query = "select count(*) as count from pg_tables where schemaname='"
					+ schema + "' and tablename='" + tablename + "'";

			Statement stat = con.createStatement();
			ResultSet rs = stat.executeQuery(query);

			while (rs.next()) {
				int count = rs.getInt("count");
				if (count != 1) {
					return false;
				} else {
					return true;
				}
			}
			return false;
		} else {
			String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name=?;";

			PreparedStatement st = con.prepareStatement(sql);
			st.setString(1, tablename);
			ResultSet rs = st.executeQuery();
			if (rs.next()) {
				return true;
			}
			return false;
		}

	}

	@Override
	protected DBSession restartConnection() throws DBException {
		return createConnection(server, port, database, schema, username,
				password);
	}

	@Override
	public String getConnectionString() {
		return CONNECTION_STRING_BEGINNING + "//" + server + ":" + port
				+ "/" + database;
	}

	@Override
	public String getDriverName() {
		return PostGisDriver.NAME;
	}

	@Override
	public String getAlphanumericDriverName() {
		return "PostgreSQL Alphanumeric";
	}

	@Override
	public String getCompleteTableName(String name, String schema) {
		return schema + "." + name;
	}

}
