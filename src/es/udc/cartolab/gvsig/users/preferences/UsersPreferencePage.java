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
package es.udc.cartolab.gvsig.users.preferences;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import com.iver.andami.PluginServices;
import com.iver.andami.preferences.AbstractPreferencePage;
import com.iver.andami.preferences.StoreException;
import com.iver.utiles.XMLEntity;
import com.jeta.forms.components.panel.FormPanel;

public class UsersPreferencePage extends AbstractPreferencePage {

	/* key names */
	public static final String CONNECT_DB_AT_STARTUP_KEY_NAME = "ConnectAtStartup";

	/* default values */
	private static final boolean CONNECT_DB_AT_STARTUP = false;

	protected String id;
	private ImageIcon icon;
	private JCheckBox connectDBCB;

	private boolean panelStarted;

	/**
	 * Creates a new panel containing the db connection preferences settings.
	 *
	 */
	public UsersPreferencePage() {
		super();
		id = this.getClass().getName();
		icon = new ImageIcon(this.getClass().getClassLoader().getResource("images/logo.png"));
		panelStarted = false;
	}


	public void setChangesApplied() {
		setChanged(false);
	}

	public void storeValues() throws StoreException {
		PluginServices ps = PluginServices.getPluginServices(this);
		XMLEntity xml = ps.getPersistentXML();
		xml.putProperty(CONNECT_DB_AT_STARTUP_KEY_NAME, connectDBCB.isSelected());
	}

	public String getID() {
		return id;
	}

	public ImageIcon getIcon() {
		return icon;
	}

	public JPanel getPanel() {

		if (!panelStarted) {
			panelStarted = true;

	    FormPanel form = new FormPanel("forms/preferences.xml");
			form.setFocusTraversalPolicyProvider(true);

			connectDBCB = form.getCheckBox("connectDBCB");
			connectDBCB.setText(PluginServices.getText(this, "connect_startup"));

			addComponent(form);
		}

		return this;
	}

	public String getTitle() {
		return PluginServices.getText(this, "dbconnection");
	}

	public void initializeDefaults() {
		connectDBCB.setSelected(CONNECT_DB_AT_STARTUP);
	}

	public void initializeValues() {
		if (!panelStarted) {
			getPanel();
		}

		PluginServices ps = PluginServices.getPluginServices(this);
		XMLEntity xml = ps.getPersistentXML();

		if (xml.contains(CONNECT_DB_AT_STARTUP_KEY_NAME)) {
			connectDBCB.setSelected(xml.getBooleanProperty(CONNECT_DB_AT_STARTUP_KEY_NAME));
		} else {
			connectDBCB.setSelected(CONNECT_DB_AT_STARTUP);
		}
	}

	public boolean isValueChanged() {
		return super.hasChanged();
	}




}
