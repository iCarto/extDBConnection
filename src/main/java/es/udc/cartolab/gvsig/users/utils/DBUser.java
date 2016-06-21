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

public interface DBUser {

	public boolean isAdmin();

	public boolean checkPassword(String password);

	public void changePassword(String password) throws SQLException;

	public boolean canCreateTable(String schema) throws SQLException;

	public boolean canUseSchema(String schema) throws SQLException;

	/**
	 * Use canUseSchema() instead
	 */
	@Deprecated
	public boolean canReadSchema(String schema) throws SQLException;

	public boolean canCreateSchema() throws SQLException;

	public boolean canCreateUser();

	public boolean canDropUser();

	public boolean canChangePass();
}
