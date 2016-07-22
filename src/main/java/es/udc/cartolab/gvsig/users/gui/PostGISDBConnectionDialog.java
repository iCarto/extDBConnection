/*
 * Copyright (c) 2010 - 2012. CartoLab. Fundaci�n de Intenier�a Civil de Galicia.
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
package es.udc.cartolab.gvsig.users.gui;

import static es.icarto.gvsig.commons.i18n.I18n._;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.InputStream;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.gvsig.andami.PluginServices;
import org.gvsig.app.extension.ProjectExtension;
import org.gvsig.fmap.dal.exception.DataException;
import org.gvsig.utils.XMLEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeta.forms.components.image.ImageComponent;
import com.jeta.forms.components.panel.FormPanel;
import com.jeta.forms.gui.common.FormException;

import es.udc.cartolab.gvsig.users.preferences.Persistence;
import es.udc.cartolab.gvsig.users.preferences.UsersPreferencePage;
import es.udc.cartolab.gvsig.users.utils.DBSession;
import es.udc.cartolab.gvsig.users.utils.DBSessionPostGIS;

/**
 * @author Javier Est�vez
 * @author Francisco Puga <fpuga@cartolab.es>
 *
 */
@SuppressWarnings("serial")
public class PostGISDBConnectionDialog extends AbstractGVWindow {

	private static final Logger logger = LoggerFactory
			.getLogger(PostGISDBConnectionDialog.class);

	private final static int INIT_MIN_HEIGHT = 175;
	private final static int INIT_MAX_HEIGHT = 350;

	private int minHeight;
	private int maxHeight;

	private JPanel centerPanel = null;

	private JCheckBox advCHB;
	private JTextField serverTF, userTF, passTF, schemaTF, dbTF, portTF;
	private JComponent advForm;

	public static final String ID_SERVERTF = "serverTF";
	public static final String ID_PORTTF = "portTF";
	public static final String ID_USERTF = "userTF";
	public static final String ID_PASSTF = "passTF"; // javax.swing.JPasswordField
	public static final String ID_DBTF = "dbTF";
	public static final String ID_SCHEMATF = "schemaTF";
	public static final String ID_ADVF = "advancedForm";
	public static final String ID_ADVCHB = "advancedCHB";
	public static final String ID_SERVERL = "serverLabel";
	public static final String ID_PORTL = "portLabel";
	public static final String ID_USERL = "userLabel";
	public static final String ID_PASSL = "passLabel";
	public static final String ID_DBL = "dbLabel";
	public static final String ID_SCHEMAL = "schemaLabel";

	public PostGISDBConnectionDialog() {
		super(425, INIT_MIN_HEIGHT);
		setTitle(_("login"));
	}

	@Override
	protected JPanel getCenterPanel() {

		if (centerPanel == null) {
			centerPanel = new JPanel();

			InputStream resourceAsStream = this.getClass().getClassLoader()
					.getResourceAsStream("forms/postgresDbConnection.xml");
			FormPanel form;
			try {
				form = new FormPanel(resourceAsStream);
			} catch (FormException e) {
				logger.error(e.getMessage(), e);
				return centerPanel;
			}
			centerPanel.add(form);
			serverTF = form.getTextField(ID_SERVERTF);
			portTF = form.getTextField(ID_PORTTF);
			userTF = form.getTextField(ID_USERTF);
			passTF = form.getTextField(ID_PASSTF);
			dbTF = form.getTextField(ID_DBTF);
			schemaTF = form.getTextField(ID_SCHEMATF);
			advForm = (JComponent) form.getComponentByName(ID_ADVF);
			advCHB = form.getCheckBox(ID_ADVCHB);
			showAdvancedProperties(false);
			advCHB.addActionListener(this);

			initLogo(form);

			// localization
			JLabel serverLabel = form.getLabel(ID_SERVERL);
			JLabel portLabel = form.getLabel(ID_PORTL);
			JLabel userLabel = form.getLabel(ID_USERL);
			JLabel passLabel = form.getLabel(ID_PASSL);
			JLabel schemaLabel = form.getLabel(ID_SCHEMAL);
			JLabel dbLabel = form.getLabel(ID_DBL);

			serverLabel.setText(_("server"));
			portLabel.setText(_("port"));
			dbLabel.setText(_("data_base"));
			userLabel.setText(_("user_name"));
			passLabel.setText(_("user_pass"));
			schemaLabel.setText(_("schema"));
			advCHB.setText(_("advanced_options"));

			DBSession dbs = DBSession.getCurrentSession();
			if ((dbs != null) && (dbs instanceof DBSessionPostGIS)) {
				serverTF.setText(((DBSessionPostGIS) dbs).getServer());
				portTF.setText(Integer.toString(((DBSessionPostGIS) dbs)
						.getPort()));
				userTF.setText(((DBSessionPostGIS) dbs).getUserName());
				dbTF.setText(((DBSessionPostGIS) dbs).getDatabase());
				schemaTF.setText(((DBSessionPostGIS) dbs).getSchema());
				advCHB.setSelected(false);
			} else {
				fillDialogFromPluginPersistence();
			}

			passTF.requestFocusInWindow();

		}
		return centerPanel;
	}

	private void initLogo(FormPanel form) {
		if (!UsersPreferencePage.LOGO.isEmpty()) {
			File logo = new File(UsersPreferencePage.LOGO);
			if (logo.isFile()) {
				ImageComponent image = (ImageComponent) form
						.getComponentByName("image");
				ImageIcon icon = new ImageIcon(logo.getAbsolutePath());
				image.setIcon(icon);
			}
		}
	}

	private void fillDialogFromPluginPersistence() {
		Persistence p = new Persistence();
		if (p.paramsAreSet()) {
			serverTF.setText(p.host);
			portTF.setText(p.port + "");
			dbTF.setText(p.database);
			userTF.setText(p.user);
			schemaTF.setText(p.schema);
		} else {
			showAdvancedProperties(true);
			advCHB.setSelected(true);
		}
	}

	private void saveConfig(String host, String port, String database,
			String schema, String user) {
		// TODO: fpuga: If in the future we will want save more than one
		// configuration this approach is not valid. Whe should store each
		// connection in a different XMLEntity and in the main XMLEntity store a
		// "lastConnectionUsed" value

		XMLEntity xml = PluginServices.getPluginServices(this)
				.getPersistentXML();
		xml.putProperty(Persistence.HOST_KEY, host);
		xml.putProperty(Persistence.PORT_KEY, port);
		xml.putProperty(Persistence.DATABASE_KEY, database);
		xml.putProperty(Persistence.USER_KEY, user);
		xml.putProperty(Persistence.SCHEMA_KEY, schema);
		PluginServices.getMDIManager().restoreCursor();
		String title = " " + _("connectedTitlePostGIS", user, host);
		PluginServices.getMainFrame().setTitle(title);
	}

	@Override
	protected JPanel getNorthPanel() {
		if (headerImg != null) {
			maxHeight = INIT_MAX_HEIGHT + headerImg.getIconHeight();
			minHeight = INIT_MIN_HEIGHT + headerImg.getIconHeight();
		} else {
			maxHeight = INIT_MAX_HEIGHT;
			minHeight = INIT_MIN_HEIGHT;
		}
		return super.getNorthPanel();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		if (e.getSource() == advCHB) {
			showAdvancedProperties(advCHB.isSelected());
		}
	}

	private void showAdvancedProperties(boolean show) {
		int height;
		if (show) {
			height = maxHeight;
		} else {
			height = minHeight;
		}
		setHeight(height);
		advForm.setVisible(show);
	}

	private boolean activeSession() {

		DBSession dbs = DBSession.getCurrentSession();
		if (dbs != null) {
			ProjectExtension pExt = (ProjectExtension) PluginServices
					.getExtension(ProjectExtension.class);
			pExt.execute("application-project-new");
			try {
				dbs.close();
			} catch (DataException e) {
				logger.error(e.getMessage(), e);
			}
		}
		return true;

	}

	@Override
	protected void onOK() {

		try {

			if (!activeSession()) {
				return;
			}

			PluginServices.getMDIManager().setWaitCursor();

			String portS = portTF.getText().trim();
			int port = Integer.parseInt(portS);
			String server = serverTF.getText().trim();
			String username = userTF.getText().trim();
			String password = passTF.getText();
			String schema = schemaTF.getText();
			String database = dbTF.getText();

			DBSessionPostGIS.createConnection(server, port, database, schema,
					username, password);

			closeWindow();

			saveConfig(server, portS, database, schema, username);
			PluginServices.getMainFrame().enableControls();
		} catch (NumberFormatException e2) {
			JOptionPane.showMessageDialog(this, _("portError"), _("dataError"),
					JOptionPane.ERROR_MESSAGE);
		} catch (DataException e1) {
			// Login error
			logger.error(e1.getMessage(), e1);
			JOptionPane.showMessageDialog(this, _("databaseConnectionError"),
					_("connectionError"), JOptionPane.ERROR_MESSAGE);

		} finally {
			PluginServices.getMDIManager().restoreCursor();
			passTF.setText("");
		}
	}

	@Override
	protected Component getDefaultFocusComponent() {
		return passTF;
	}

}
