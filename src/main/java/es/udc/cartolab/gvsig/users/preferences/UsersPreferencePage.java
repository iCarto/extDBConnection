/*
 * Copyright (c) 2010. CartoLab, Universidad de A Coru�a
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

import static es.icarto.gvsig.commons.i18n.I18n._;

import java.io.InputStream;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.gvsig.andami.PluginServices;
import org.gvsig.andami.preferences.AbstractPreferencePage;
import org.gvsig.andami.preferences.StoreException;
import org.gvsig.utils.XMLEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeta.forms.components.panel.FormPanel;
import com.jeta.forms.gui.common.FormException;

@SuppressWarnings("serial")
public class UsersPreferencePage extends AbstractPreferencePage {

	private static final Logger logger = LoggerFactory.getLogger(UsersPreferencePage.class);
	public static String LOGO = "";

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

	@Override
	public void setChangesApplied() {
		setChanged(false);
	}

	@Override
	public void storeValues() throws StoreException {
		PluginServices ps = PluginServices.getPluginServices(this);
		XMLEntity xml = ps.getPersistentXML();
		xml.putProperty(Persistence.OPEN_CONNECTION_DIALOG_AT_STARTUP, connectDBCB.isSelected());
	}

	@Override
	public String getID() {
		return id;
	}

	@Override
	public ImageIcon getIcon() {
		return icon;
	}

	@Override
	public JPanel getPanel() {

		if (!panelStarted) {
			panelStarted = true;
			InputStream resourceAsStream = this.getClass().getClassLoader()
					.getResourceAsStream("forms/preferences.xml");
			FormPanel form;
			try {
				form = new FormPanel(resourceAsStream);
			} catch (FormException e) {
				logger.error(e.getMessage(), e);
				return this;
			}
			form.setFocusTraversalPolicyProvider(true);

			connectDBCB = form.getCheckBox("connectDBCB");
			connectDBCB.setText(_("connect_startup"));

			addComponent(form);
		}

		return this;
	}

	@Override
	public String getTitle() {
		return _("dbconnection");
	}

	@Override
	public void initializeDefaults() {
		connectDBCB.setSelected(false);
	}

	@Override
	public void initializeValues() {
		if (!panelStarted) {
			getPanel();
		}
		Persistence p = new Persistence();
		connectDBCB.setSelected(p.openDialogAtStartUp);
	}

	@Override
	public boolean isValueChanged() {
		return super.hasChanged();
	}

}
