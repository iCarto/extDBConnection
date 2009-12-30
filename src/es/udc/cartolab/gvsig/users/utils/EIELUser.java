package es.udc.cartolab.gvsig.users.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class EIELUser {

	/* roles */
	private final String adminRole = "administrador";
	private final String eielRole = "eiel";
	
	private String username;
	private String password;
	private String userid;
	private boolean isSuper = false;
	private boolean isAdmin = false;
	private boolean isEIEL = false;
	
	public EIELUser(String username, String password, Connection con) {
		this.username = username;
		this.password = password;
		try {
			isSuper = checkSuper(con);
			isAdmin = checkRole(con, adminRole);
			isEIEL = checkRole(con, eielRole);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private boolean checkSuper(Connection con) throws SQLException {
		boolean superuser = false;
		Statement stat;
		stat = con.createStatement();
		String query = "SELECT usesysid, usesuper FROM pg_user WHERE usename='" + username + "';";
		ResultSet rs = stat.executeQuery(query);

		while (rs.next()) {
			superuser = rs.getBoolean("usesuper");
			userid = rs.getString("usesysid");
		}

		return superuser;
	}
	
	private boolean checkRole(Connection con, String role) throws SQLException {
		
		boolean isRole = isSuper || isAdmin;
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
	
	public boolean isEIELUser() {
		return isEIEL;
	}
	
	public boolean checkPassword(String password) {
		return this.password.equals(password);
	}
	
	public void changePassword(String password) throws SQLException {
		
		DBSession dbs = DBSession.getCurrentSession();
		Connection con = dbs.getJavaConnection();
		String sql = "ALTER ROLE " + username + " WITH ENCRYPTED PASSWORD ?";
		PreparedStatement stat = con.prepareStatement(sql);
		stat.setString(1, password);
		stat.execute();
		stat.close();
		
		con.commit();

	}
}
