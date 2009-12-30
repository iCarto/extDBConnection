package es.udc.cartolab.gvsig.users;

import com.iver.andami.PluginServices;
import com.iver.andami.plugins.Extension;

import es.udc.cartolab.gvsig.users.gui.CreateUserWindow;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class CreateUserExtension extends Extension {

	public void execute(String actionCommand) {
		// TODO Auto-generated method stub
		CreateUserWindow window = new CreateUserWindow();
		PluginServices.getMDIManager().addCentredWindow(window);
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
			return session.getEIELUser().isAdmin();
		}
		return false;
	}

}
