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

import java.sql.SQLException;

public class DBUserSpatiaLite implements DBUser {

	private final String sqliteFile;

	public DBUserSpatiaLite(String sqliteFile) {
		this.sqliteFile = sqliteFile;
	}

	public boolean isAdmin() {
		return true;
	}

	public boolean checkPassword(String password) {
		return false;
	}

	public void changePassword(String password) throws SQLException {

	}

	public boolean canCreateTable(String schema) throws SQLException {
		return true;
	}

	public boolean canUseSchema(String schema) throws SQLException {
		return true;
	}

	/**
	 * Use canUseSchema() instead
	 */
	@Deprecated
	public boolean canReadSchema(String schema) throws SQLException {
		return true;
	}

	private boolean canPerform(String schema, String privilege) throws SQLException {
		return true;
	}

	public boolean canCreateSchema() throws SQLException {
		return true;
	}

	@Override
	public boolean canCreateUser() {
		return false;
	}

	@Override
	public boolean canDropUser() {
		return false;
	}

	@Override
	public boolean canChangePass() {
		return false;
	}
}
