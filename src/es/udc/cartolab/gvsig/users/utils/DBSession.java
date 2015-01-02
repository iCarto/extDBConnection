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
import java.sql.ResultSet;
import java.sql.SQLException;

import org.cresques.cts.IProjection;

import com.iver.cit.gvsig.fmap.drivers.ConnectionJDBC;
import com.iver.cit.gvsig.fmap.drivers.DBException;
import com.iver.cit.gvsig.fmap.drivers.db.utils.ConnectionWithParams;
import com.iver.cit.gvsig.fmap.drivers.db.utils.SingleDBConnectionManager;
import com.iver.cit.gvsig.fmap.layers.FLayer;


public abstract class DBSession {

	protected static DBSession instance = null;
	protected static IFormatter formatter = new Formatter();
	protected DBUser user;
	protected ConnectionWithParams conwp;
	protected String database = "", username = "", password = "", server = "";
	protected int port = 0;
	public static String CONNECTION_STRING_BEGINNING;

	/**
	 * Creates a new PostGIS DB Connection or changes the current one.
	 * 
	 * Method created in order to retain compatibility. We should instantiate
	 * the specific session we need because each one may accept a different set
	 * of parameters.
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
	@Deprecated
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

	public static DBSession createConnection(String connString,
			String username, String password) throws DBException {
		if (connString.startsWith(DBSessionPostGIS.CONNECTION_STRING_BEGINNING)) {
			return DBSessionPostGIS.createConnectionFromConnString(connString,
					username,
					password);
		}
		if (connString
				.startsWith(DBSessionSpatiaLite.CONNECTION_STRING_BEGINNING)) {
			return DBSessionSpatiaLite
					.createConnectionFromConnString(connString);
		}
		return null;

	}

	/**
	 * 
	 * @return the DB Connection or null if there isn't any
	 */
	public static DBSession getCurrentSession() {
		return instance;
	}

	public static boolean isActive() {
		return (instance != null);
	}

	/**
	 * To be used only when there's any error (SQLException) that is not handled
	 * by gvSIG
	 * 
	 * @return the session
	 * @throws DBException
	 */
	public static DBSession reconnect() throws DBException {
		if (instance!=null) {
			return instance.restartConnection();
		}
		return null;
	}

	protected abstract void connect() throws DBException;

	protected abstract DBSession restartConnection() throws DBException;

	public Connection getJavaConnection() {
		return ((ConnectionJDBC) conwp.getConnection()).getConnection();
	}

	public void close() throws DBException {

		user = null;
		if (conwp!=null) {
			SingleDBConnectionManager.instance().closeAndRemove(conwp);
			conwp = null;
		}
		instance = null;

	}

	public DBUser getDBUser() {
		return user;
	}
	
	public static void setFormatter(IFormatter f) {
	    formatter = f; 
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

	public String getPassword() {
		return password;
	}

	public String getDatabase() {
		return database;
	}

	public abstract String getConnectionString();

	/* GET LAYER */

	public abstract FLayer getLayer(String layerName, String tableName,
			String schema, String whereClause, IProjection projection)
			throws SQLException, DBException;

	public abstract FLayer getLayer(String layerName, String tableName,
			String whereClause, IProjection projection) throws SQLException,
			DBException;

	public abstract FLayer getLayer(String tableName, String whereClause,
			IProjection projection) throws SQLException, DBException;

	public abstract FLayer getLayer(String tableName, IProjection projection)
			throws SQLException, DBException;

	/* GET METADATA */

	protected abstract String[] getColumnNames(String tablename, String schema)
			throws SQLException;

	protected abstract int getColumnType(String tablename, String schema,
			String column) throws SQLException;

	/* GET TABLE AS STRING[][] */

	public abstract String[][] getTable(String tableName, String schema,
			String whereClause, String[] orderBy, boolean desc)
			throws SQLException;

	public abstract String[][] getTable(String tableName, String schema,
			String[] fieldNames, String whereClause, String[] orderBy,
			boolean desc) throws SQLException;

	public abstract String[][] getTable(String tableName, String schema,
			String[] orderBy, boolean desc) throws SQLException;

	public abstract String[][] getTable(String tableName, String schema,
			String whereClause) throws SQLException;

	public abstract String[][] getTable(String tableName, String whereClause)
			throws SQLException;

	public abstract String[][] getTable(String tableName) throws SQLException;

	/* GET TABLE AS OBJECT[][] */

	public abstract Object[][] getTableAsObjects(String tableName,
			String schema, String whereClause, String[] orderBy, boolean desc)
			throws SQLException;

	public abstract Object[][] getTableAsObjects(String tableName,
			String schema, String[] fieldNames, String whereClause,
			String[] orderBy, boolean desc) throws SQLException;

	public abstract Object[][] getTableAsObjects(String tableName,
			String schema, String[] orderBy, boolean desc) throws SQLException;

	public abstract Object[][] getTableAsObjects(String tableName,
			String schema, String whereClause) throws SQLException;

	public abstract Object[][] getTableAsObjects(String tableName,
			String whereClause) throws SQLException;

	public abstract Object[][] getTableAsObjects(String tableName)
			throws SQLException;

	/* GET TABLE AS RESULTSET */

	public abstract ResultSet getTableAsResultSet(String tableName,
			String schema, String whereClause, String[] orderBy, boolean desc)
			throws SQLException;

	public abstract ResultSet getTableAsResultSet(String tableName,
			String schema, String[] fieldNames, String whereClause,
			String[] orderBy, boolean desc) throws SQLException;

	public abstract ResultSet getTableAsResultSet(String tableName,
			String schema, String[] orderBy, boolean desc) throws SQLException;

	public abstract ResultSet getTableAsResultSet(String tableName,
			String schema, String whereClause) throws SQLException;

	public abstract ResultSet getTableAsResultSet(String tableName,
			String whereClause) throws SQLException;

	public abstract ResultSet getTableAsResultSet(String tableName)
			throws SQLException;

	/* GET BINARY STREAM */

	public abstract InputStream getBinaryStream(String tableName,
			String schema, String fieldName, String whereClause)
			throws SQLException;

	/* SET BINARY STREAM */

	public abstract void updateWithBinaryStream(String tableName, String schema,
			String fieldName, InputStream is, int length, String[] columns,
			Object[] values, String whereClause)
			throws SQLException;

	public abstract void insertWithBinaryStream(String tableName, String schema,
			String fieldName, InputStream is, int length, String[] columns,
			Object[] values) throws SQLException;

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

	public abstract String[][] getTableWithJoin(String[] tableNames,
			String[] schemas, String[] joinFields, String whereClause,
			String[] orderBy, boolean desc) throws SQLException;

	public abstract String[][] getTableWithJoin(String[] tableNames,
			String[] schemas, String[] joinFields, String[] fieldNames,
			String whereClause, String[] orderBy, boolean desc)
			throws SQLException;

	public abstract String[][] getTableWithJoin(String[] tableNames,
			String[] schemas, String[] joinFields, String[] orderBy,
			boolean desc)
			throws SQLException;

	public abstract String[][] getTableWithJoin(String[] tableNames,
			String[] schemas, String[] joinFields, String whereClause)
			throws SQLException;

	public abstract String[][] getTableWithJoin(String[] tableNames,
			String[] joinFields, String whereClause) throws SQLException;

	public abstract String[][] getTableWithJoin(String[] tableNames,
			String[] joinFields) throws SQLException;

	/* GET DISTINCT VALUES FROM A COLUMN */

	public abstract String[] getDistinctValues(String tableName, String schema,
			String fieldName, boolean sorted, boolean desc, String whereClause)
			throws SQLException;

	public abstract String[] getDistinctValues(String tableName, String schema,
			String fieldName, boolean sorted, boolean desc) throws SQLException;

	public abstract String[] getDistinctValues(String tableName, String schema,
			String fieldName) throws SQLException;

	public abstract String[] getDistinctValues(String tableName,
			String fieldName) throws SQLException;

	public abstract String[] getTables(boolean onlyGeospatial)
			throws SQLException;

	public abstract String[] getColumns(String table) throws SQLException;

	public abstract String[] getColumns(String schema, String table)
			throws SQLException;

	public abstract void deleteRows(String schema, String table,
			String whereClause) throws SQLException;

	public abstract void insertRow(String schema, String table, Object[] values)
			throws SQLException;

	public abstract void insertRow(String schema, String table,
			String[] columns, Object[] values) throws SQLException;

	public abstract void updateRows(String schema, String tablename,
			String[] columns, Object[] values, String whereClause)
			throws SQLException;

	public abstract boolean tableExists(String schema, String tablename)
			throws SQLException;

	public abstract String getCompleteTableName(String name, String schema);

	public abstract String getDriverName();

	public abstract String getAlphanumericDriverName();

	protected String getCharForNumber(int i) {
		return i > 0 && i < 27 ? String.valueOf((char) (i + 96)) : null;
	}

}
