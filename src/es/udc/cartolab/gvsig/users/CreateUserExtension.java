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
package es.udc.cartolab.gvsig.users;

import com.iver.andami.plugins.Extension;

import es.udc.cartolab.gvsig.users.gui.CreateUserWindow;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class CreateUserExtension extends Extension {

	public void execute(String actionCommand) {

		CreateUserWindow window = new CreateUserWindow();
		window.openWindow();
	}

	public void initialize() {
		// TODO Auto-generated method stub

	}

	public boolean isEnabled() {
		return true;
	}

	public boolean isVisible() {
		DBSession session = DBSession.getCurrentSession();
		if (session != null) {
			return session.getDBUser().isAdmin();
		}
		return false;
	}

}
