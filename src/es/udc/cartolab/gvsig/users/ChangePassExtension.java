package es.udc.cartolab.gvsig.users;

import java.awt.Color;

import javax.swing.ImageIcon;

import com.iver.andami.plugins.Extension;

import es.udc.cartolab.gvsig.users.gui.ChangePassDialog;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class ChangePassExtension extends Extension {

	private final String imagePath = "gvSIG/extensiones/es.udc.cartolab.gvsig.users/images/header.png";

	public void execute(String actionCommand) {

		ImageIcon icon = new ImageIcon(imagePath);
		Color bgColor = new Color(36, 46, 109);
		ChangePassDialog dialog = new ChangePassDialog(icon, bgColor);

		dialog.openWindow();
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
