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

import javax.swing.JOptionPane;

import com.iver.andami.Launcher;
import com.iver.andami.Launcher.TerminationProcess;
import com.iver.andami.PluginServices;
import com.iver.andami.plugins.Extension;
import com.iver.andami.ui.wizard.UnsavedDataPanel;
import com.iver.cit.gvsig.ProjectExtension;
import com.iver.cit.gvsig.fmap.drivers.DBException;
import com.iver.cit.gvsig.project.Project;

import es.udc.cartolab.gvsig.users.utils.DBSession;

public class CloseSessionExtension extends Extension {

	public void execute(String actionCommand) {
		DBSession dbs = DBSession.getCurrentSession();
		if (dbs!=null) {
			try {

				if (!askSave()) {
					return;
				}
				dbs.close();

				ProjectExtension pExt = (ProjectExtension) PluginServices.getExtension(ProjectExtension.class);
				pExt.execute("NUEVO");

			} catch (DBException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private boolean askSave() {

		ProjectExtension pExt = (ProjectExtension) PluginServices.getExtension(ProjectExtension.class);
		Project p = pExt.getProject();

		if (p != null && p.hasChanged()) {
			TerminationProcess process = Launcher.getTerminationProcess();
			UnsavedDataPanel panel = process.getUnsavedDataPanel();
			panel.setHeaderText(PluginServices.getText(this, "Select_resources_to_save_before_closing_current_project"));
			panel.setAcceptText(
					PluginServices.getText(this, "save_resources"),
					PluginServices.getText(this, "Save_the_selected_resources_and_close_current_project"));
			panel.setCancelText(
					PluginServices.getText(this, "Dont_close"),
					PluginServices.getText(this, "Return_to_current_project"));
			int closeCurrProj = process.manageUnsavedData();
			if (closeCurrProj==JOptionPane.NO_OPTION) {
				// the user chose to return to current project
				return false;
			} else if (closeCurrProj==JOptionPane.YES_OPTION) {
				//trick to avoid ask twice for modified data
				p.setModified(false);
			}
		}
		return true;
	}


	public void initialize() {
		registerIcons();
	}

	protected void registerIcons() {
		PluginServices.getIconTheme().registerDefault(
				"DBClose",
				this.getClass().getClassLoader().getResource("images/sessiondisc.png")
			);
	}

	public boolean isEnabled() {
		return true;
	}

	public boolean isVisible() {
		DBSession dbs = DBSession.getCurrentSession();
		return dbs!=null;
	}

}
