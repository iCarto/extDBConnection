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

import org.gvsig.andami.IconThemeHelper;
import org.gvsig.andami.PluginServices;
import org.gvsig.andami.plugins.Extension;
import org.gvsig.app.extension.ProjectExtension;
import org.gvsig.fmap.dal.exception.DataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.udc.cartolab.gvsig.users.utils.DBSession;

public class CloseSessionExtension extends Extension {
	
	
	private static final Logger logger = LoggerFactory
			.getLogger(CloseSessionExtension.class);

	public void execute(String actionCommand) {
		DBSession dbs = DBSession.getCurrentSession();
		if (dbs!=null) {
			ProjectExtension pExt = (ProjectExtension) PluginServices.getExtension(ProjectExtension.class);
			pExt.execute("application-project-new");
			try {
				dbs.close();
			} catch (DataException e) {
				logger.error(e.getMessage(), e);
			}			
		}
	}

	public void initialize() {
		registerIcons();
	}

	protected void registerIcons() {
		final String id = this.getClass().getName();
		IconThemeHelper.registerIcon("action", id, this);
	}

	public boolean isEnabled() {
		return true;
	}

	public boolean isVisible() {
		DBSession dbs = DBSession.getCurrentSession();
		return dbs!=null;
	}

}
