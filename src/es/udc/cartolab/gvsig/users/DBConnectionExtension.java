package es.udc.cartolab.gvsig.users;

import java.awt.Color;
import java.io.File;

import javax.swing.ImageIcon;

import com.iver.andami.PluginServices;
import com.iver.andami.plugins.Extension;
import com.iver.andami.preferences.IPreference;
import com.iver.andami.preferences.IPreferenceExtension;
import com.iver.cit.gvsig.About;
import com.iver.cit.gvsig.gui.panels.FPanelAbout;
import com.iver.utiles.XMLEntity;

import es.udc.cartolab.gvsig.users.gui.DBConnectionDialog;
import es.udc.cartolab.gvsig.users.preferences.EielPage;


public class DBConnectionExtension extends Extension implements IPreferenceExtension {

	public static EielPage eielPreferencesPage = new EielPage();
	private final String imagePath = "gvSIG/extensiones/es.udc.cartolab.gvsig.users/images/header_cartolab.png";

	public void execute(String actionCommand) {

		//with header image (Pontevedra)
		ImageIcon icon = new ImageIcon(imagePath);
		Color bgColor = new Color(36, 46, 109);
		DBConnectionDialog dialog = new DBConnectionDialog(icon, bgColor);

		//without header image
		//		DBConnectionDialog dialog = new DBConnectionDialog();
		PluginServices.getMDIManager().addCentredWindow(dialog);
	}

	public void initialize() {
		About about=(About)PluginServices.getExtension(About.class);
		FPanelAbout panelAbout=about.getAboutPanel();
		java.net.URL aboutURL = this.getClass().getResource("/about.htm");
		panelAbout.addAboutUrl("OGE", aboutURL);

		//poner if
		PluginServices ps = PluginServices.getPluginServices(this);
		XMLEntity xml = ps.getPersistentXML();
		if (xml.contains(EielPage.CONNECT_DB_AT_STARTUP_KEY_NAME)) {
			if (xml.getBooleanProperty(EielPage.CONNECT_DB_AT_STARTUP_KEY_NAME)) {
				execute(null);
			}
		}

		//Creating config Dir
		String symbolsDirStr = System.getProperty("user.dir") + File.separator + "Leyendas";
		File symbolsDir = new File(symbolsDirStr);
		symbolsDir.mkdir();
	}

	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return true;
	}

	public boolean isVisible() {
		// TODO Auto-generated method stub
		return true;
	}

	public IPreference[] getPreferencesPages() {
		// TODO Auto-generated method stub
		IPreference[] preferences=new IPreference[1];
		preferences[0]=eielPreferencesPage;
		return preferences;
	}

}
