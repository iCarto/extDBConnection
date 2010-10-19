package es.udc.cartolab.gvsig.users;

import com.iver.andami.PluginServices;
import com.iver.andami.plugins.Extension;
import com.iver.cit.gvsig.fmap.drivers.DBException;

import es.udc.cartolab.gvsig.users.utils.DBSession;

public class CloseSessionExtension extends Extension {

	public void execute(String actionCommand) {
		DBSession dbs = DBSession.getCurrentSession();
		if (dbs!=null) {
			try {
				dbs.close();
			} catch (DBException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void initialize() {
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
