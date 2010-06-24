package es.udc.cartolab.gvsig.users;

import com.iver.andami.PluginServices;
import com.iver.andami.plugins.Extension;

import es.udc.cartolab.gvsig.users.gui.DropUserDialog;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class DropUserExtension extends Extension {

	public void execute(String actionCommand) {
		DropUserDialog dialog = new DropUserDialog();
		PluginServices.getMDIManager().addWindow(dialog);
	}

	public void initialize() {
		// TODO Auto-generated method stub
		
	}

	public boolean isEnabled() {
		// TODO Auto-generated method stub
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
