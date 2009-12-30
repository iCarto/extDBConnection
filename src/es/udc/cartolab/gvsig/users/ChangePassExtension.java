package es.udc.cartolab.gvsig.users;

import com.iver.andami.PluginServices;
import com.iver.andami.plugins.Extension;

import es.udc.cartolab.gvsig.users.gui.ChangePassDialog;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class ChangePassExtension extends Extension {

	public void execute(String actionCommand) {
		ChangePassDialog dialog = new ChangePassDialog();
		PluginServices.getMDIManager().addCentredWindow(dialog);
	}

	public void initialize() {
		
	}

	public boolean isEnabled() {
		return true;
	}

	public boolean isVisible() {
		DBSession dbs = DBSession.getCurrentSession();
		return dbs!=null;
	}

}
