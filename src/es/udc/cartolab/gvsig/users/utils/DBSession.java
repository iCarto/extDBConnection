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
import java.sql.SQLException;

import javax.swing.JOptionPane;

import org.cresques.cts.IProjection;

import com.iver.andami.Launcher;
import com.iver.andami.Launcher.TerminationProcess;
import com.iver.andami.PluginServices;
import com.iver.andami.ui.wizard.UnsavedDataPanel;
import com.iver.cit.gvsig.ProjectExtension;
import com.iver.cit.gvsig.fmap.drivers.ConnectionJDBC;
import com.iver.cit.gvsig.fmap.drivers.DBException;
import com.iver.cit.gvsig.fmap.drivers.db.utils.ConnectionWithParams;
import com.iver.cit.gvsig.fmap.drivers.db.utils.SingleDBConnectionManager;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.project.Project;


public abstract class DBSession {

	protected static DBSession instance = null;
	protected DBUser user;
	protected ConnectionWithParams conwp;
	protected String database = "", username = "", password = "", server = "";
	protected int port = 0;

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


	/* GET TABLE */

	protected abstract String[] getColumnNames(String tablename, String schema)
			throws SQLException;

	protected abstract int getColumnType(String tablename, String schema,
			String column) throws SQLException;

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

	/**
	 * Checks if there is an active and unsaved project and asks the user to
	 * save resources.
	 * 
	 * @return true if there's no unsaved data
	 */
	public boolean askSave() {

		ProjectExtension pExt = (ProjectExtension) PluginServices
				.getExtension(ProjectExtension.class);
		Project p = pExt.getProject();

		if (p != null && p.hasChanged()) {
			TerminationProcess process = Launcher.getTerminationProcess();
			UnsavedDataPanel panel = process.getUnsavedDataPanel();
			panel.setHeaderText(PluginServices.getText(this,
					"Select_resources_to_save_before_closing_current_project"));
			panel.setAcceptText(
					PluginServices.getText(this, "save_resources"),
					PluginServices
							.getText(this,
									"Save_the_selected_resources_and_close_current_project"));
			panel.setCancelText(PluginServices.getText(this, "Dont_close"),
					PluginServices.getText(this, "Return_to_current_project"));
			int closeCurrProj = process.manageUnsavedData();
			if (closeCurrProj == JOptionPane.NO_OPTION) {
				// the user chose to return to current project
				return false;
			} else if (closeCurrProj == JOptionPane.YES_OPTION) {
				// trick to avoid ask twice for modified data
				p.setModified(false);
			}
		}
		return true;
	}

	public abstract String getCompleteTableName(String name, String schema);

	public abstract String getDriverName();

	public abstract String getAlphanumericDriverName();

}
