package es.udc.cartolab.gvsig.users;

import java.awt.Color;

import javax.swing.ImageIcon;

import com.iver.andami.plugins.Extension;

import es.udc.cartolab.gvsig.users.gui.CreateUserWindow;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class CreateUserExtension extends Extension {

	private final String imagePath = "gvSIG/extensiones/es.udc.cartolab.gvsig.users/images/header.png";
	public void execute(String actionCommand) {

		ImageIcon icon = new ImageIcon(imagePath);
		Color bgColor = new Color(36, 46, 109);
		CreateUserWindow window = new CreateUserWindow(icon, bgColor);

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
			return session.getEIELUser().isAdmin();
		}
		return false;
	}

}
