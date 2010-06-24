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
