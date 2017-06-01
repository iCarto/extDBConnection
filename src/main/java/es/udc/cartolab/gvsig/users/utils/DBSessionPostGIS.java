/*
 * Copyright (c) 2010. CartoLab, Universidad de A Coru�a
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

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
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
import org.gvsig.fmap.dal.exception.DataException;
import org.gvsig.fmap.dal.store.jdbc.JDBCStoreParameters;
import org.gvsig.fmap.mapcontext.MapContextLocator;
import org.gvsig.fmap.mapcontext.MapContextManager;
import org.gvsig.fmap.mapcontext.layers.FLayer;
import org.gvsig.tools.exception.BaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.icarto.gvsig.commons.gvsig2.SingleDBConnectionManager;

public class DBSessionPostGIS extends DBSession {

	private static final Logger logger = LoggerFactory
			.getLogger(DBSessionPostGIS.class);

	public static final String POSTGRESQL_STORE_PROVIDER_NAME = "PostgreSQL";
	private static final String POSTGRESQL_SERVER_EXPLORER_NAME = "PostgreSQLExplorer";
	private static final String POSTGRESQL_RESOURCE = "PostgreSQLResource";
	// public static final String ALPHANUMERIC_DRIVER_NAME =
	// "PostgreSQL Alphanumeric";

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
			String username, String password) throws DataException {
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
	 * @throws DataException
	 * @throws DBException
	 *             if there's any problem (server error or login error)
	 */
	public static DBSession createConnection(String server, int port,
			String database, String schema, String username, String password)
					throws DataException {
		if (instance != null) {
			instance.close();
		}
		instance = new DBSessionPostGIS(server, port, database, schema,
				username, password);
		instance.connect();
		return instance;
	}

	@Override
	protected void connect() throws DataException {
		try {
			conwp = SingleDBConnectionManager.instance().getConnection(
					POSTGRESQL_STORE_PROVIDER_NAME,
					POSTGRESQL_SERVER_EXPLORER_NAME, POSTGRESQL_RESOURCE,
					username, password, "ELLE_connection", server, port,
					database, "", true);
			user = new DBUserPostGIS(username, password, conwp.getConnection());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			if (conwp != null) {
				SingleDBConnectionManager.instance().closeAndRemove(conwp);
			}
			instance = null;
		}
	}

	public String getSchema() {
		return schema;
	}

	public void changeSchema(String schema) {
		this.schema = schema;
	}

	@Override
	public FLayer getLayer(String layerName, String tableName, String schema,
			String whereClause, IProjection projection) throws BaseException {

		if (whereClause == null) {
			whereClause = "";
		}

		MapContextManager mapContextManager = MapContextLocator
				.getMapContextManager();
		JDBCStoreParameters params = (JDBCStoreParameters) conwp
				.getStoreParams().getCopy();
		params.setSchema(schema);
		params.setTable(tableName);
		params.setCRS(projection);
		params.setDynValue("schema", schema);
		params.setDynValue("table", tableName);
		if (whereClause.compareTo("") != 0) {
			params.setBaseFilter(whereClause);
		}

		FLayer layer = mapContextManager.createLayer(layerName, params);

		return layer;
	}

	@Override
	public FLayer getLayer(String layerName, String tableName,
			String whereClause, IProjection projection) throws BaseException {
		return getLayer(layerName, tableName, schema, whereClause, projection);
	}

	@Override
	public FLayer getLayer(String tableName, String whereClause,
			IProjection projection) throws BaseException {
		return getLayer(tableName, tableName, schema, whereClause, projection);
	}

	@Override
	public FLayer getLayer(String tableName, IProjection projection)
			throws BaseException {
		return getLayer(tableName, null, projection);
	}

	/* GET METADATA */

	@Override
	protected String[] getColumnNames(String tablename, String schema) {

		Connection con = conwp.getConnection();
		try {
			String query = "SELECT * FROM " + schema + "." + tablename
					+ " LIMIT 1";
			Statement st = con.createStatement();
			ResultSet resultSet = st.executeQuery(query);
			ResultSetMetaData md = resultSet.getMetaData();
			String[] cols = new String[md.getColumnCount()];
			for (int i = 0; i < md.getColumnCount(); i++) {
				cols[i] = md.getColumnLabel(i + 1);
			}
			return cols;
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		} finally {
			conwp.close(con);
		}
	}

	@Override
	protected int getColumnType(String tablename, String schema, String column)
			throws SQLException {

		Connection con = conwp.getConnection();
		try {
			DatabaseMetaData meta = con.getMetaData();
			ResultSet rsColumns = meta.getColumns(null, schema, tablename,
					column);
			while (rsColumns.next()) {
				if (column.equalsIgnoreCase(rsColumns.getString("COLUMN_NAME"))) {
					return rsColumns.getInt("COLUMN_TYPE");
				}
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		} finally {
			conwp.close(con);
		}
		return -1;
	}

	/* GET TABLE AS STRING[][] */

	@Override
	public String[][] getTable(String tableName, String schema,
			String whereClause, String[] orderBy, boolean desc)
			throws SQLException {

		String[] columnNames = getColumnNames(tableName, schema);

		return getTable(tableName, schema, columnNames, whereClause, orderBy,
				desc);
	}

	@Override
	public String[][] getTable(String tableName, String schema,
			String[] fieldNames, String whereClause, String[] orderBy,
			boolean desc) {
		Connection con = conwp.getConnection();
		ArrayList<String[]> rows = new ArrayList<String[]>();
		try {
			ResultSet rs = getTableResultSet(tableName, schema, fieldNames,
					whereClause, orderBy, desc, con);

			while (rs.next()) {
				String[] row = new String[fieldNames.length];
				for (int i = 0; i < fieldNames.length; i++) {
					// elle styles in table elle._map_style are defined as 'xml'
					// columns
					// for some reasons rs.getObject returns null and
					// con.setTypeMap
					// is not working to set a custom mapping. So this
					// workaround is used
					if (rs.getMetaData().getColumnType(i + 1) == java.sql.Types.OTHER) {
						row[i] = rs.getString(i + 1);
					} else {
						row[i] = format.toString(rs.getObject(i + 1));
					}
				}
				rows.add(row);
			}
			rs.close();
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		} finally {
			conwp.close(con);
		}

		return rows.toArray(new String[0][0]);

	}

	@Override
	public String[][] getTable(String tableName, String schema,
			String[] orderBy, boolean desc) throws SQLException {
		return getTable(tableName, schema, null, orderBy, desc);
	}

	@Override
	public String[][] getTable(String tableName, String schema,
			String whereClause) throws SQLException {
		return getTable(tableName, schema, whereClause, null, false);
	}

	@Override
	public String[][] getTable(String tableName, String whereClause)
			throws SQLException {
		return getTable(tableName, schema, whereClause, null, false);
	}

	@Override
	public String[][] getTable(String tableName) throws SQLException {
		return getTable(tableName, schema, null, null, false);
	}

	/* GET TABLE AS OBJECT[][] */

	@Override
	public Object[][] getTableAsObjects(String tableName, String schema,
			String whereClause, String[] orderBy, boolean desc)
			throws SQLException {

		String[] columnNames = getColumnNames(tableName, schema);

		return getTableAsObjects(tableName, schema, columnNames, whereClause,
				orderBy, desc);
	}

	@Override
	public Object[][] getTableAsObjects(String tableName, String schema,
			String[] fieldNames, String whereClause, String[] orderBy,
			boolean desc) throws SQLException {
		Connection con = conwp.getConnection();
		ArrayList<Object[]> rows = new ArrayList<Object[]>();
		try {
			ResultSet rs = getTableResultSet(tableName, schema, fieldNames,
					whereClause, orderBy, desc, con);

			while (rs.next()) {
				Object[] row = new Object[fieldNames.length];
				for (int i = 0; i < fieldNames.length; i++) {
					Object val = rs.getObject(fieldNames[i]);
					if (val == null) {
						val = "";
					}
					row[i] = val;
				}
				rows.add(row);
			}
			rs.close();
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		} finally {
			conwp.close(con);
		}

		return rows.toArray(new Object[0][0]);
	}

	@Override
	public Object[][] getTableAsObjects(String tableName, String schema,
			String[] orderBy, boolean desc) throws SQLException {
		return getTableAsObjects(tableName, schema, null, orderBy, desc);
	}

	@Override
	public Object[][] getTableAsObjects(String tableName, String schema,
			String whereClause) throws SQLException {
		return getTableAsObjects(tableName, schema, whereClause, null, false);
	}

	@Override
	public Object[][] getTableAsObjects(String tableName, String whereClause)
			throws SQLException {
		return getTableAsObjects(tableName, schema, whereClause, null, false);
	}

	@Override
	public Object[][] getTableAsObjects(String tableName) throws SQLException {
		return getTableAsObjects(tableName, schema, null, null, false);
	}

	/* GET TABLE AS RESULTSET */

	@Override
	public ResultSet getTableAsResultSet(String tableName, String schema,
			String whereClause, String[] orderBy, boolean desc)
			throws SQLException {

		String[] columnNames = getColumnNames(tableName, schema);

		return getTableAsResultSet(tableName, schema, columnNames, whereClause,
				orderBy, desc);
	}

	@Override
	public ResultSet getTableAsResultSet(String tableName, String schema,
			String[] fieldNames, String whereClause, String[] orderBy,
			boolean desc) throws SQLException {
		Connection con = conwp.getConnection();
		return getTableResultSet(tableName, schema, fieldNames, whereClause,
				orderBy, desc, con);

	}

	@Override
	public ResultSet getTableAsResultSet(String tableName, String schema,
			String[] orderBy, boolean desc) throws SQLException {
		return getTableAsResultSet(tableName, schema, null, orderBy, desc);
	}

	@Override
	public ResultSet getTableAsResultSet(String tableName, String schema,
			String whereClause) throws SQLException {
		return getTableAsResultSet(tableName, schema, whereClause, null, false);
	}

	@Override
	public ResultSet getTableAsResultSet(String tableName, String whereClause)
			throws SQLException {
		return getTableAsResultSet(tableName, schema, whereClause, null, false);
	}

	@Override
	public ResultSet getTableAsResultSet(String tableName) throws SQLException {
		return getTableAsResultSet(tableName, schema, null, null, false);
	}

	private ResultSet getTableResultSet(String tableName, String schema,
			String[] fieldNames, String whereClause, String[] orderBy,
			boolean desc, Connection con) throws SQLException {

		if (whereClause == null) {
			whereClause = "";
		} else {
			whereClause = whereClause.trim();
		}

		String query = "SELECT ";

		for (int i = 0; i < fieldNames.length; i++) {
			query = query + fieldNames[i] + ", ";
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

		query += builder.getOrderByClause(orderBy, desc);

		PreparedStatement stat = con.prepareStatement(query);
		for (int i = 0; i < whereValues.size(); i++) {
			stat.setString(i + 1, whereValues.get(i));
		}

		return stat.executeQuery();
	}

	/* GET BINARY STREAM */

	@Override
	public InputStream getBinaryStream(String tableName, String schema,
			String fieldName, String whereClause) throws SQLException {
		String[] fieldNames = { fieldName };
		InputStream is = null;
		try {
			Class.forName("org.postgresql.Driver");
			Connection con = DriverManager.getConnection(getConnectionString(),
					username, password);
			ResultSet rs = getTableResultSet(tableName, schema, fieldNames,
					whereClause, new String[0], false, con);
			if (rs.next()) {
				is = rs.getBinaryStream(1);
			}
			con.close();
			rs.close();
		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage(), e);
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}
		return is;

	}

	/* SET BINARY STREAM */

	@Override
	public void updateWithBinaryStream(String tableName, String schema,
			String fieldName, InputStream is, int length, String[] columns,
			Object[] values, String whereClause) throws SQLException {

		if (columns.length == values.length) {
			try {
				Class.forName("org.postgresql.Driver");
				Connection con = DriverManager.getConnection(
						getConnectionString(), username, password);
				String sql = "UPDATE " + schema + "." + tableName + " SET "
						+ fieldName + " = ?";
				for (String column : columns) {
					sql += ", " + column + " = ?";
				}
				sql += " " + whereClause;

				PreparedStatement statement = con.prepareStatement(sql);
				int pos = 1;
				statement.setBinaryStream(pos++, is, length);
				for (Object value : values) {
					statement.setObject(pos++, value);
				}
				statement.executeUpdate();
				con.close();
			} catch (ClassNotFoundException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	@Override
	public void insertWithBinaryStream(String tableName, String schema,
			String fieldName, InputStream is, int length, String[] columns,
			Object[] values) throws SQLException {

		if (columns.length == values.length) {
			try {
				Class.forName("org.postgresql.Driver");
				Connection con = DriverManager.getConnection(
						getConnectionString(), username, password);
				String sql = "INSERT INTO " + schema + "." + tableName + " ("
						+ fieldName;
				String sqlValues = "VALUES (?";
				for (String column : columns) {
					sql += ", " + column;
					sqlValues += ", ?";
				}
				sql += ") " + sqlValues + ")";

				PreparedStatement statement = con.prepareStatement(sql);
				int pos = 1;
				statement.setBinaryStream(pos++, is, length);
				for (Object value : values) {
					statement.setObject(pos++, value);
				}
				statement.executeUpdate();
				con.close();
			} catch (ClassNotFoundException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	/* GET TABLES WITH JOIN */

	/*
	 * NOTES ONTO ALL THE JOIN RELATED METHODS:
	 * 
	 * Inside tableNames we must put all the tables we want to join, which will
	 * be assigned the alphabet letters in order as alias (a, b, c...) so we can
	 * avoid field names conflicts in all the other parameters (mainly
	 * joinFields, the whereClause and the fields we retrieve). Then we pass the
	 * schemas for all those tables in the same order. And inside joinFields we
	 * must put the fields we want to use for joining the tables. We must
	 * specify two fields for each table besides the first one, which will be
	 * the base. Field names will probably repeat in this case, so remember that
	 * we can use the aliases inside them as well (e.g. "a.cod_com",
	 * "b.cod_com").
	 * 
	 * To summarize, if we have N table names we must have N schemas and (N-1)*2
	 * join fields.
	 * 
	 * 
	 * EXAMPLE: we want to join the tables 'viviendas', 'parcelas' and
	 * 'comunidades', being the three in the same schema, 'data'. Both
	 * 'viviendas' and 'parcelas' are related by 'cod_viv', and 'comunidades'
	 * and 'viviendas' by 'cod_com'. We must pass the next parameters (in its
	 * precise order):
	 * 
	 * 
	 * tableNames = {"viviendas", "parcelas", "comunidades"}
	 * 
	 * schemas = {"data", "data", "data"}
	 * 
	 * joinFields = {"a.cod_viv", "b.cod_viv", "a.cod_com", "c.cod_com"}
	 */

	@Override
	public String[][] getTableWithJoin(String[] tableNames, String[] schemas,
			String[] joinFields, String whereClause, String[] orderBy,
			boolean desc) throws SQLException {

		List<String> fields = new ArrayList<String>();
		for (int i = 0, len = tableNames.length; i < len; i++) {
			String[] tempFields = getColumnNames(tableNames[i], schemas[i]);
			for (String tempField : tempFields) {
				fields.add(tableNames[i] + "." + tempField);
			}
		}

		return getTableWithJoin(tableNames, schemas, joinFields,
				fields.toArray(new String[0]), whereClause, orderBy, desc);
	}

	@Override
	public String[][] getTableWithJoin(String[] tableNames, String[] schemas,
			String[] joinFields, String[] fieldNames, String whereClause,
			String[] orderBy, boolean desc) throws SQLException {
		Connection con = conwp.getConnection();

		if (whereClause == null) {
			whereClause = "";
		} else {
			whereClause = whereClause.trim();
		}

		String query = "SELECT ";
		Set<String> queriedFields = new HashSet<String>();
		for (int i = 0; i < fieldNames.length; i++) {
			if (!queriedFields.contains(fieldNames[i])) {
				query = query + fieldNames[i] + " AS \"" + fieldNames[i]
						+ "\", ";
				queriedFields.add(fieldNames[i]);
			}
		}

		query = query.substring(0, query.length() - 2) + " FROM " + schemas[0]
				+ "." + tableNames[0] + " " + getCharForNumber(1);
		for (int i = 1, len = tableNames.length; i < len; i++) {
			query += " JOIN " + schemas[i] + "." + tableNames[i] + " "
					+ getCharForNumber(i + 1) + " ON "
					+ joinFields[(i - 1) * 2] + " = "
					+ joinFields[((i - 1) * 2) + 1];
		}

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

		query += builder.getOrderByClause(orderBy, desc);

		PreparedStatement stat = con.prepareStatement(query);
		for (int i = 0; i < whereValues.size(); i++) {
			stat.setString(i + 1, whereValues.get(i));
		}

		ResultSet rs = stat.executeQuery();

		ArrayList<String[]> rows = new ArrayList<String[]>();
		while (rs.next()) {
			String[] row = new String[fieldNames.length];
			for (int i = 0; i < fieldNames.length; i++) {
				// elle styles in table elle._map_style are defined as 'xml'
				// columns
				// for some reasons rs.getObject returns null and con.setTypeMap
				// is not working to set a custom mapping. So this workaround is
				// used
				if (rs.getMetaData().getColumnType(i + 1) == java.sql.Types.OTHER) {
					row[i] = rs.getString(i + 1);
				} else {
					row[i] = format.toString(rs.getObject(i + 1));
				}
			}
			rows.add(row);
		}
		rs.close();

		return rows.toArray(new String[0][0]);

	}

	@Override
	public String[][] getTableWithJoin(String[] tableNames, String[] schemas,
			String[] joinFields, String[] orderBy, boolean desc)
			throws SQLException {
		return getTableWithJoin(tableNames, schemas, joinFields, null, orderBy,
				desc);
	}

	@Override
	public String[][] getTableWithJoin(String[] tableNames, String[] schemas,
			String[] joinFields, String whereClause) throws SQLException {
		return getTableWithJoin(tableNames, schemas, joinFields, whereClause,
				null, false);
	}

	@Override
	public String[][] getTableWithJoin(String[] tableNames,
			String[] joinFields, String whereClause) throws SQLException {
		String[] schemas = new String[tableNames.length];
		for (int i = 0, len = schemas.length; i < len; i++) {
			schemas[i] = schema;
		}
		return getTableWithJoin(tableNames, schemas, joinFields, whereClause,
				null, false);
	}

	@Override
	public String[][] getTableWithJoin(String[] tableNames, String[] joinFields)
			throws SQLException {
		String[] schemas = new String[tableNames.length];
		for (int i = 0, len = schemas.length; i < len; i++) {
			schemas[i] = schema;
		}
		return getTableWithJoin(tableNames, schemas, joinFields, null, null,
				false);
	}

	/* GET DISTINCT VALUES FROM A COLUMN */

	@Override
	public String[] getDistinctValues(String tableName, String schema,
			String fieldName, boolean sorted, boolean desc, String whereClause)
			throws SQLException {

		Connection con = conwp.getConnection();

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

	@Override
	public String[] getDistinctValues(String tableName, String schema,
			String fieldName, boolean sorted, boolean desc) throws SQLException {
		return getDistinctValues(tableName, schema, fieldName, sorted, desc,
				null);
	}

	@Override
	public String[] getDistinctValues(String tableName, String schema,
			String fieldName) throws SQLException {
		return getDistinctValues(tableName, schema, fieldName, false, false,
				null);
	}

	@Override
	public String[] getDistinctValues(String tableName, String fieldName)
			throws SQLException {
		return getDistinctValues(tableName, schema, fieldName, false, false,
				null);
	}

	@Override
	public String[] getTables(boolean onlyGeospatial) throws SQLException {

		Connection con = conwp.getConnection();
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

	@Override
	public String[] getColumns(String table) throws SQLException {

		return getColumns(null, table);

	}

	@Override
	public String[] getColumns(String schema, String table) throws SQLException {

		Connection con = conwp.getConnection();
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

	@Override
	/**
	 * Only returns the columns of the table that has some not null values in it
	 */
	public List<String> getColumnsWithNotNulls(String schema, String table)
			throws SQLException {

		Connection con = conwp.getConnection();
		DatabaseMetaData metadataDB = con.getMetaData();

		ResultSet columns = metadataDB.getColumns(null, schema, table, "%");
		List<String> cols = new ArrayList<String>();
		while (columns.next()) {
			String columnName = columns.getString("Column_name");
			String sql = String.format(
					"SELECT count(*) FROM \"%s\".\"%s\" WHERE %s IS NOT NULL",
					schema, table, columnName);
			Statement statement = con.createStatement();
			ResultSet rs = statement.executeQuery(sql);
			rs.next();
			if (rs.getInt(1) > 0) {
				cols.add(columnName);
			}
		}
		return cols;
	}

	@Override
	public void deleteRows(String schema, String table, String whereClause) {

		Connection con = conwp.getConnection();
		String sql = String.format("DELETE FROM \"%s\".\"%s\" %s", schema,
				table, whereClause);
		try {
			Statement statement = con.createStatement();
			statement.executeUpdate(sql);
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		} finally {
			conwp.close(con);
		}
	}

	@Override
	public void insertRow(String schema, String table, Object[] values)
			throws SQLException {

		String[] columns = getColumnNames(table, schema);
		insertRow(schema, table, columns, values);
	}

	@Override
	public void insertRow(String schema, String table, String[] columns,
			Object[] values) {

		Connection con = conwp.getConnection();

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

			try {
				PreparedStatement statement = con.prepareStatement(sql);
				for (int i = 0; i < columns.length; i++) {
					statement.setObject(i + 1, values[i]);
				}
				statement.executeUpdate();
			} catch (SQLException e) {
				logger.error(e.getMessage(), e);
				throw new RuntimeException(e);
			} finally {
				conwp.close(con);
			}
		}
	}

	@Override
	public void updateRows(String schema, String tablename, String[] columns,
			Object[] values, String whereClause) {

		Connection con = conwp.getConnection();

		if (columns.length == values.length) {
			String sql = "UPDATE " + schema + "." + tablename + " SET ";
			for (String column : columns) {
				sql = sql + column + "=?, ";
			}
			sql = sql.substring(0, sql.length() - 2) + " " + whereClause;

			try {
				PreparedStatement statement = con.prepareStatement(sql);
				for (int i = 0; i < values.length; i++) {
					statement.setObject(i + 1, values[i]);
				}
				statement.executeUpdate();
			} catch (SQLException e) {
				logger.error(e.getMessage(), e);
				throw new RuntimeException(e);
			} finally {
				conwp.close(con);
			}
		}
	}

	@Override
	public boolean tableExists(String schema, String tablename) {

		Connection con = conwp.getConnection();

		String query = "select count(*) as count from pg_tables where schemaname='"
				+ schema + "' and tablename='" + tablename + "'";

		try {
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
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		} finally {
			conwp.close(con);
		}
	}

	@Override
	public boolean schemaExists(String schema) {
		Connection con = conwp.getConnection();
		String sqlHasSchema = String
				.format("SELECT COUNT(*) AS schemaCount FROM information_schema.schemata WHERE schema_name = '%s'",
						schema);
		try {
			Statement stat = con.createStatement();
			ResultSet rs = stat.executeQuery(sqlHasSchema);
			rs.next();
			int schemaCount = rs.getInt("schemaCount");
			if (schemaCount == 1) {
				return true;
			}
			return false;
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		} finally {
			conwp.close(con);
		}
	}

	@Override
	public void createSchema(String schema) {
		Connection con = conwp.getConnection();
		String sqlCreateSchema = String.format("CREATE SCHEMA %s", schema);
		try {
			Statement stat = con.createStatement();
			stat.execute(sqlCreateSchema);
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		} finally {
			conwp.close(con);
		}
	}

	@Override
	protected DBSession restartConnection() throws DataException {
		return createConnection(server, port, database, schema, username,
				password);
	}

	@Override
	public String getConnectionString() {
		return CONNECTION_STRING_BEGINNING + "//" + server + ":" + port + "/"
				+ database;
	}

	// @Override
	// public String getAlphanumericDriverName() {
	// return ALPHANUMERIC_DRIVER_NAME;
	// }

	@Override
	public String getCompleteTableName(String name, String schema) {
		return schema + "." + name;
	}

}
