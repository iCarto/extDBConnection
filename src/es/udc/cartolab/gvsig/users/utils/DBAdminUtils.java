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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBAdminUtils {
	
	private DBAdminUtils() {
		
	}
	/**
	 * Creates a new user
	 * @param con a connection to the database, createrole privilege or administrador membership is required
	 * @param username the username to create
	 * @param password the password
	 * @throws SQLException if the user has no permission to create user or the username already exists.
	 */
	public static void createUser(Connection con, String username, String password) throws SQLException {

		String sql = "SET ROLE administrador;";
		Statement stat = con.createStatement();
		stat.execute(sql);

		sql = "CREATE USER " + username +";";
		stat = con.createStatement();
		stat.execute(sql);
		
		sql = "ALTER ROLE " + username + " WITH ENCRYPTED PASSWORD ?";
		PreparedStatement stat2 = con.prepareStatement(sql);
		stat2.setString(1, password);
		stat2.execute();
		
		sql = "RESET ROLE";
		stat = con.createStatement();
		stat.execute(sql);

	}
	
	/**
	 * Grants role to the given user
	 * @param con a connection to the database, admin privilege or administrator membership is required.
	 * @param username the username that will be granted with the role.
	 * @param role the role to grant.
	 * @throws SQLException
	 */
	public static void grantRole(Connection con, String username, String role) throws SQLException {
		
		String sql = "SET ROLE administrador;";
		Statement stat = con.createStatement();
		stat.execute(sql);
		
		sql = "GRANT %s TO %s;";
		sql = String.format(sql, role, username);
		stat = con.createStatement();
		stat.execute(sql);
		
		sql = "RESET ROLE";
		stat = con.createStatement();
		stat.execute(sql);
		
	}
	
	/**
	 * Checks if exists a user on the database.
	 * @param con the connection to the database.
	 * @param username the user to check 
	 * @return true if exits, false if not.
	 * @throws SQLException
	 */
	public static boolean existsUser(Connection con, String username) throws SQLException {
		
		String query = "SELECT COUNT(*) AS number FROM pg_user WHERE usename=?";
		PreparedStatement stat = con.prepareStatement(query);
		stat.setString(1, username);
		ResultSet rs = stat.executeQuery();
		rs.next();
		int numUsers = rs.getInt("number");
		return numUsers > 0;
		
	}
	
	/**
	 * Drops the given user
	 * @param con the connection to the database
	 * @param username the user to be removed
	 * @throws SQLException 
	 * 
	 */
	public static void dropUser(Connection con, String username) throws SQLException {
	
		String sql = "SET ROLE administrador";
		Statement stat = con.createStatement();
		stat.execute(sql);
		
		sql = "DROP USER %s";
		sql = String.format(sql, username);
		stat.execute(sql);
		
		sql = "RESET ROLE";
		stat = con.createStatement();
		stat.execute(sql);
		
		
	}

}
