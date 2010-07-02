package es.udc.cartolab.gvsig.users.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBUser {


	private String username;
	private String password;
	private String userid;
	private boolean isAdmin = false;

	public DBUser(String username, String password, Connection con) {
		this.username = username;
		this.password = password;
		try {
			isAdmin = checkSuper(con);
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
		String sql = "ALTER ROLE " + username + " WITH ENCRYPTED PASSWORD ?";
		PreparedStatement stat = con.prepareStatement(sql);
		stat.setString(1, password);
		stat.execute();
		stat.close();

		con.commit();

	}

	public boolean canCreateTable(String schema) throws SQLException {

		DBSession dbs = DBSession.getCurrentSession();
		Connection con = dbs.getJavaConnection();

		String query = "SELECT has_schema_privilege('" + schema + "', 'create') as can_create";
		Statement stat = con.createStatement();

		//		PreparedStatement stat = con.prepareStatement(query);
		//		stat.setString(1, schema);
		ResultSet rs = stat.executeQuery(query);

		while (rs.next()) {
			return rs.getBoolean("can_create");
		}

		return false;

	}
}
