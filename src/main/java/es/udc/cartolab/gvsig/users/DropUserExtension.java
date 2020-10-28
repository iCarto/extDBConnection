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
package es.udc.cartolab.gvsig.users;


import org.gvsig.andami.PluginServices;
import org.gvsig.andami.plugins.Extension;
import org.gvsig.andami.ui.mdiManager.MDIManagerFactory;

import es.udc.cartolab.gvsig.users.gui.DropUserDialog;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class DropUserExtension extends Extension {

	public void execute(String actionCommand) {
		DropUserDialog dialog = new DropUserDialog();
		MDIManagerFactory.getManager().addWindow(dialog);
	}

	public void initialize() {	
	}

	public boolean isEnabled() {
		return true;
	}

	public boolean isVisible() {
		DBSession session = DBSession.getCurrentSession();
		if (session != null) {
			return session.getDBUser().canDropUser();
		}
		return false;
	}

}
