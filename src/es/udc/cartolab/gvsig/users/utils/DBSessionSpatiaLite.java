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

import java.io.InputStream;
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
import org.sqlite.Conn;

import com.hardcode.driverManager.DriverLoadException;
import com.iver.cit.gvsig.fmap.drivers.ConnectionJDBC;
import com.iver.cit.gvsig.fmap.drivers.DBException;
import com.iver.cit.gvsig.fmap.drivers.DBLayerDefinition;
import com.iver.cit.gvsig.fmap.drivers.FieldDescription;
import com.iver.cit.gvsig.fmap.drivers.IVectorialJDBCDriver;
import com.iver.cit.gvsig.fmap.drivers.db.utils.SingleDBConnectionManager;
import com.iver.cit.gvsig.fmap.drivers.db.utils.SingleVectorialDBConnectionManager;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.LayerFactory;

import es.udc.cartolab.cit.gvsig.fmap.drivers.jdbc.spatialite.SpatiaLite;
import es.udc.cartolab.cit.gvsig.fmap.drivers.jdbc.spatialite.SpatiaLiteDriver;
import es.udc.cartolab.com.hardcode.gdbms.driver.sqlite.SQLiteDriver;

public class DBSessionSpatiaLite extends DBSession {

	private final String sqliteFile;
	protected static String CONNECTION_STRING_BEGINNING = "jdbc:sqlite:";

	protected DBSessionSpatiaLite(String sqliteFile) {
		this.sqliteFile = sqliteFile;
	}

	/**
	 * Creates a new DB Connection or changes the current one.
	 * 
	 * @param connString
	 * @return the connection
	 * @throws DBException
	 *             if there's any problem (server error or login error)
	 */
	public static DBSession createConnectionFromConnString(String connString)
			throws DBException {
		if (!connString.startsWith(CONNECTION_STRING_BEGINNING)) {
			return null;
		}
		if (instance != null) {
			instance.close();
		}

		String file = connString.replaceFirst(CONNECTION_STRING_BEGINNING, "");
		instance = new DBSessionSpatiaLite(file);
		instance.connect();
		return instance;
	}

	/**
	 * Creates a new DB Connection or changes the current one.
	 * 
	 * @param sqliteFile
	 * @return the connection
	 * @throws DBException
	 *             if there's any problem (server error or login error)
	 */
	public static DBSession createConnection(String sqliteFile)
			throws DBException {
		if (instance != null) {
			instance.close();
		}
		instance = new DBSessionSpatiaLite(sqliteFile);
		instance.connect();
		return instance;
	}

	protected void connect() throws DBException {
		try {
			conwp = SingleVectorialDBConnectionManager.instance()
					.getConnection(getDriverName(), "", "",
							"SpatiaLite_connection", sqliteFile, "", "", true);
			user = new DBUserSpatiaLite(sqliteFile);
		} catch (DBException e) {
			if (this.conwp != null) {
				SingleVectorialDBConnectionManager.instance().closeAndRemove(
						this.conwp);
			}
			instance = null;
			throw e;
		}

	}

	public void close() throws DBException {
		Connection conn = instance.getJavaConnection();
		user = null;
		if (conwp != null) {
			SingleDBConnectionManager.instance().closeAndRemove(conwp);
			conwp = null;
		}
		if (conn instanceof Conn) {
			try {
				((Conn) conn).realClose();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		instance = null;

	}

	/* GET LAYER */

	public FLayer getLayer(String layerName, String tableName, String schema,
			String whereClause, IProjection projection) throws SQLException,
			DBException {

		if (whereClause == null) {
			whereClause = "";
		}

		String database = this.sqliteFile;
		schema = "";
		database = "";

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
			if (SpatiaLite.isGeometryType(columnas.getString("Type_Name"))) {
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
			dbLayerDef.setFieldID("PK_UID");
		}

		dbLayerDef.setSRID_EPSG(projection.getAbrev());

		FLayer lyr = null;
		try {
			IVectorialJDBCDriver dbDriver = (IVectorialJDBCDriver) LayerFactory
					.getDM().getDriver("SpatiaLite JDBC Driver");

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
		return getLayer(layerName, tableName, "", whereClause, projection);
	}

	public FLayer getLayer(String tableName, String whereClause,
			IProjection projection) throws SQLException, DBException {
		return getLayer(tableName, tableName, "", whereClause, projection);
	}

	public FLayer getLayer(String tableName, IProjection projection)
			throws SQLException, DBException {
		return getLayer(tableName, null, projection);
	}

	/* GET METADATA */

	protected String[] getColumnNames(String tablename, String schema)
			throws SQLException {

		Connection con = ((ConnectionJDBC) conwp.getConnection())
				.getConnection();

		String query = "SELECT * FROM " + tablename + " LIMIT 1";
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
		ResultSet rsColumns = meta.getColumns(null, "", tablename, column);
		while (rsColumns.next()) {
			if (column.equalsIgnoreCase(rsColumns.getString("COLUMN_NAME"))) {
				return rsColumns.getInt("COLUMN_TYPE");
			}
		}
		return -1;
	}

	/* GET TABLE */

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
		ResultSet rs = getTableResultSet(tableName, schema, fieldNames,
				whereClause, orderBy, desc);

		ArrayList<String[]> rows = new ArrayList<String[]>();
		while (rs.next()) {
			String[] row = new String[fieldNames.length];
			for (int i = 0; i < fieldNames.length; i++) {
			 // elle styles in table elle._map_style are defined as 'xml' columns
			    // for some reasons rs.getObject returns null and con.setTypeMap
			    // is not working to set a custom mapping. So this workaround is used
			    if (rs.getMetaData().getColumnType(i+1) == java.sql.Types.OTHER) {
				row[i] = rs.getString(i+1);
			    } else {
				row[i] = formatter.toString(rs.getObject(i+1));
			    }
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
		return getTable(tableName, "", whereClause, null, false);
	}

	public String[][] getTable(String tableName) throws SQLException {
		return getTable(tableName, "", null, null, false);
	}

	/* GET TABLE AS OBJECT[][] */

	public Object[][] getTableAsObjects(String tableName, String schema,
			String whereClause, String[] orderBy, boolean desc)
			throws SQLException {

		String[] columnNames = getColumnNames(tableName, schema);

		return getTableAsObjects(tableName, schema, columnNames, whereClause,
				orderBy, desc);
	}

	public Object[][] getTableAsObjects(String tableName, String schema,
			String[] fieldNames, String whereClause, String[] orderBy,
			boolean desc) throws SQLException {
		ResultSet rs = getTableResultSet(tableName, schema, fieldNames,
				whereClause, orderBy, desc);

		ArrayList<Object[]> rows = new ArrayList<Object[]>();
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

		return rows.toArray(new Object[0][0]);

	}

	public Object[][] getTableAsObjects(String tableName, String schema,
			String[] orderBy, boolean desc) throws SQLException {
		return getTableAsObjects(tableName, schema, null, orderBy, desc);
	}

	public Object[][] getTableAsObjects(String tableName, String schema,
			String whereClause) throws SQLException {
		return getTableAsObjects(tableName, schema, whereClause, null, false);
	}

	public Object[][] getTableAsObjects(String tableName, String whereClause)
			throws SQLException {
		return getTableAsObjects(tableName, "", whereClause, null, false);
	}

	public Object[][] getTableAsObjects(String tableName) throws SQLException {
		return getTableAsObjects(tableName, "", null, null, false);
	}

	/* GET TABLE AS RESULTSET */

	public ResultSet getTableAsResultSet(String tableName, String schema,
			String whereClause, String[] orderBy, boolean desc)
			throws SQLException {

		String[] columnNames = getColumnNames(tableName, schema);

		return getTableAsResultSet(tableName, schema, columnNames, whereClause,
				orderBy, desc);
	}

	public ResultSet getTableAsResultSet(String tableName, String schema,
			String[] fieldNames, String whereClause, String[] orderBy,
			boolean desc) throws SQLException {
		return getTableResultSet(tableName, schema, fieldNames, whereClause,
				orderBy, desc);

	}

	public ResultSet getTableAsResultSet(String tableName, String schema,
			String[] orderBy, boolean desc) throws SQLException {
		return getTableAsResultSet(tableName, schema, null, orderBy, desc);
	}

	public ResultSet getTableAsResultSet(String tableName, String schema,
			String whereClause) throws SQLException {
		return getTableAsResultSet(tableName, schema, whereClause, null, false);
	}

	public ResultSet getTableAsResultSet(String tableName, String whereClause)
			throws SQLException {
		return getTableAsResultSet(tableName, "", whereClause, null, false);
	}

	public ResultSet getTableAsResultSet(String tableName) throws SQLException {
		return getTableAsResultSet(tableName, "", null, null, false);
	}

	private ResultSet getTableResultSet(String tableName, String schema,
			String[] fieldNames, String whereClause, String[] orderBy,
			boolean desc) throws SQLException {
		Connection con = ((ConnectionJDBC) conwp.getConnection())
				.getConnection();

		if (whereClause == null) {
			whereClause = "";
		} else {
			whereClause = whereClause.trim();
		}

		String query = "SELECT ";
		for (int i = 0; i < fieldNames.length; i++) {
		    query = query + fieldNames[i] + ", ";
		}

		query = query.substring(0, query.length() - 2) + " FROM " + tableName;

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

	public InputStream getBinaryStream(String tableName, String schema,
			String fieldName, String whereClause) throws SQLException {
		InputStream is = null;
		String[] fieldNames = { fieldName };
		ResultSet rs = getTableResultSet(tableName, schema, fieldNames,
				whereClause, new String[0], false);
		if (rs.next()) {
			is = rs.getBinaryStream(1);
		}
		rs.close();
		return is;

	}

	/* SET BINARY STREAM */

	public void updateWithBinaryStream(String tableName, String schema,
			String fieldName, InputStream is, int length, String[] columns,
			Object[] values, String whereClause) throws SQLException {
		if (columns.length == values.length) {
			Connection con = ((ConnectionJDBC) conwp.getConnection())
					.getConnection();

			clearTransaction(con);

			String sql = "UPDATE " + tableName + " SET " + fieldName + "=?, ";
			for (String column : columns) {
				sql = sql + column + "=?, ";
			}
			sql = sql.substring(0, sql.length() - 2) + " " + whereClause;

			PreparedStatement statement = con.prepareStatement(sql);
			int pos = 1;
			statement.setBinaryStream(pos++, is, length);
			for (Object value : values) {
				statement.setObject(pos++, value);
			}

			statement.executeUpdate();
			con.commit();
		}
	}

	public void insertWithBinaryStream(String tableName, String schema,
			String fieldName, InputStream is, int length, String[] columns,
			Object[] values) throws SQLException {
		if (columns.length == values.length) {
			Connection con = ((ConnectionJDBC) conwp.getConnection())
					.getConnection();

			clearTransaction(con);

			String sql = "INSERT INTO " + tableName + " (" + fieldName;
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
			con.commit();
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
	 * 'comunidades'. Both 'viviendas' and 'parcelas' are related by 'cod_viv',
	 * and 'comunidades' and 'viviendas' by 'cod_com'. We must pass the next
	 * parameters (in its precise order):
	 * 
	 * 
	 * tableNames = {"viviendas", "parcelas", "comunidades"}
	 * 
	 * schemas = {"", "", ""}
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
				fields.add(tableNames + "." + tempField);
			}
		}

		return getTableWithJoin(tableNames, schemas, joinFields,
				fields.toArray(new String[0]), whereClause, orderBy, desc);
	}

	@Override
	public String[][] getTableWithJoin(String[] tableNames, String[] schemas,
			String[] joinFields, String[] fieldNames, String whereClause,
			String[] orderBy, boolean desc) throws SQLException {
		Connection con = ((ConnectionJDBC) conwp.getConnection())
				.getConnection();

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

		query = query.substring(0, query.length() - 2) + " FROM "
				+ tableNames[0] + " " + getCharForNumber(1);
		for (int i = 1, len = tableNames.length; i < len; i++) {
			query += " JOIN " + tableNames[i] + " " + getCharForNumber(i + 1)
					+ " ON " + joinFields[(i - 1) * 2] + " = "
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
			 // elle styles in table elle._map_style are defined as 'xml' columns
			    // for some reasons rs.getObject returns null and con.setTypeMap
			    // is not working to set a custom mapping. So this workaround is used
			    if (rs.getMetaData().getColumnType(i+1) == java.sql.Types.OTHER) {
				row[i] = rs.getString(i+1);
			    } else {
				row[i] = formatter.toString(rs.getObject(i+1));
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
			schemas[i] = "";
		}
		return getTableWithJoin(tableNames, schemas, joinFields, whereClause,
				null, false);
	}

	@Override
	public String[][] getTableWithJoin(String[] tableNames, String[] joinFields)
			throws SQLException {
		String[] schemas = new String[tableNames.length];
		for (int i = 0, len = schemas.length; i < len; i++) {
			schemas[i] = "";
		}
		return getTableWithJoin(tableNames, schemas, joinFields, null, null,
				false);
	}

	/* GET DISTINCT VALUES FROM A COLUMN */

	public String[] getDistinctValues(String tableName, String schema,
			String fieldName, boolean sorted, boolean desc, String whereClause)
			throws SQLException {

		Connection con = ((ConnectionJDBC) conwp.getConnection())
				.getConnection();

		Statement stat = con.createStatement();

		if (whereClause == null) {
			whereClause = "";
		}

		String query = "SELECT DISTINCT " + fieldName + " FROM " + tableName
				+ " " + whereClause;

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
		return getDistinctValues(tableName, "", fieldName, false, false, null);
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

		ResultSet columns = metadataDB.getColumns(null, "", table, "%");
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
	 * Clear connection of transactions
	 * 
	 * We simply try to commit inside a connection and in case there is no
	 * transaction, simply ignore the Exception.
	 * 
	 * @param con
	 */
	private void clearTransaction(Connection con) {
		try {
			con.commit();
		} catch (SQLException e) {
			// Probably there was no commit created
		}
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

		clearTransaction(con);

		String sql = "DELETE FROM " + table + " " + whereClause;

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

		clearTransaction(con);

		if (columns.length == values.length) {
			String sql = "INSERT INTO " + table + " (";
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

		clearTransaction(con);

		if (columns.length == values.length) {
			String sql = "UPDATE " + tablename + " SET ";
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

		String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name=?;";

		PreparedStatement st = con.prepareStatement(sql);
		st.setString(1, tablename);
		ResultSet rs = st.executeQuery();
		if (rs.next()) {
			return true;
		}
		return false;

	}

	public String getSqliteFile() {
		return sqliteFile;
	}

	@Override
	public String getServer() {
		return sqliteFile;
	}

	@Override
	protected DBSession restartConnection() throws DBException {
		return createConnection(this.sqliteFile);
	}

	@Override
	public String getUserName() {
		return null;
	}

	@Override
	public String getPassword() {
		return null;
	}

	@Override
	public String getConnectionString() {
		return CONNECTION_STRING_BEGINNING + sqliteFile;
	}

	@Override
	public String getDriverName() {
		return SpatiaLiteDriver.NAME;
	}

	@Override
	public String getAlphanumericDriverName() {
		return SQLiteDriver.NAME;
	}

	@Override
	public String getCompleteTableName(String name, String schema) {
		return name;
	}

}
