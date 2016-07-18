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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBUserPostGIS implements DBUser {
	
	
	private static final Logger logger = LoggerFactory
			.getLogger(DBUserPostGIS.class);

	private String username;
	private String password;
	private String userid;
	private boolean isAdmin = false;

	public DBUserPostGIS(String username, String password, Connection con) {
		this.username = username;
		this.password = password;
		try {
			isAdmin = checkSuper(con);
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}

	}

	private boolean checkSuper(Connection con) throws SQLException {
		boolean superuser = false;
		Statement stat = con.createStatement();
		String query = "SELECT usesysid, usesuper FROM pg_user WHERE usename='" + username + "';";
		ResultSet rs = stat.executeQuery(query);

		if (rs.next()) {
			superuser = rs.getBoolean("usesuper");
			userid = rs.getString("usesysid");
			if (!superuser) {
				superuser = checkRoleSuperUserRecursive(con, userid);
			}
		}
		return superuser;
	}

	private boolean checkRoleSuperUserRecursive(Connection con, String memberid) throws SQLException{
		Statement stat = con.createStatement();
		String query = "SELECT a.rolname AS rolname, a.oid AS rolid, a.rolsuper AS rolsuper " +
				"FROM pg_roles a JOIN pg_auth_members b ON a.oid=b.roleid " +
				"WHERE b.member = " + memberid + ";";
		ResultSet rs = stat.executeQuery(query);

		while (rs.next()) {
			if (rs.getBoolean("rolsuper")) {
				stat.execute("SET SESSION ROLE '" + rs.getString("rolname") + "';");
				return true;
			}
			String roleid = rs.getString("rolid");
			if (checkRoleSuperUserRecursive(con, roleid)) {
				return true;
			}
		}
		return false;
	}

	private boolean checkRole(Connection con, String role) throws SQLException {

		boolean isRole = isAdmin;
		if (!isRole) {
			Statement stat = con.createStatement();
			String query = "SELECT member FROM pg_auth_members WHERE member='" +
			userid + "' AND roleid=(SELECT oid FROM pg_roles WHERE rolname='" +
			role + "');";
			ResultSet rs = stat.executeQuery(query);
			int i=0;
			while (rs.next()) {
				i++;
				if (i>1) {
					break;
				}
			}
			isRole = i==1;
		}
		return isRole;
	}

	public boolean isAdmin() {
		return isAdmin;
	}

	public boolean checkPassword(String password) {
		return this.password.equals(password);
	}

	public void changePassword(String password) throws SQLException {

		DBSession dbs = DBSession.getCurrentSession();
		Connection con = dbs.getJavaConnection();
		// http://dba.stackexchange.com/a/78399/15606
		String sanitizedPassword = password.replace("'", "''");
		String sql = "ALTER ROLE " + username + " WITH ENCRYPTED PASSWORD '" + sanitizedPassword + "'";
		Statement st = con.createStatement();
		st.executeUpdate(sql);
		if (!con.getAutoCommit()) {
			con.commit();			
		}
		st.close();
	}

	public boolean canCreateTable(String schema) throws SQLException {

		return canPerform(schema, "create");

	}

	public boolean canUseSchema(String schema) throws SQLException {

		return canPerform(schema, "usage");

	}

	/**
	 * Use canUseSchema() instead
	 */
	@Deprecated
	public boolean canReadSchema(String schema) throws SQLException {
		return canUseSchema(schema);
	}

	private boolean canPerform(String schema, String privilege) throws SQLException {
		if (isAdmin) {
			return true;
		}
		DBSession dbs = DBSession.getCurrentSession();
		Connection con = dbs.getJavaConnection();

		String query = "SELECT has_schema_privilege('" + schema + "', '" + privilege + "') as can_do";
		Statement stat = con.createStatement();

		ResultSet rs = stat.executeQuery(query);

		while (rs.next()) {
			return rs.getBoolean("can_do");
		}

		return false;
	}

	public boolean canCreateSchema() throws SQLException {
		if (isAdmin) {
			return true;
		}
		DBSessionPostGIS dbs = (DBSessionPostGIS) DBSession
				.getCurrentSession();
		Connection con = dbs.getJavaConnection();
		String dbName = dbs.getDatabase();
		String query = "SELECT has_database_privilege('" + dbName + "', 'create') as can_do";
		Statement stat = con.createStatement();
		ResultSet rs = stat.executeQuery(query);
		while (rs.next()) {
			return rs.getBoolean("can_do");
		}
		return false;
	}

	@Override
	public boolean canCreateUser() {
		return isAdmin;
	}

	@Override
	public boolean canDropUser() {
		return isAdmin;
	}

	@Override
	public boolean canChangePass() {
		return true;
	}
}
