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
package es.udc.cartolab.gvsig.users.gui;

import java.awt.Component;
import java.sql.Connection;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.drivers.DBException;
import com.jeta.forms.components.panel.FormPanel;

import es.udc.cartolab.gvsig.users.utils.DBAdminUtils;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class CreateUserWindow extends AbstractGVWindow {

	JPanel centerPanel = null;

	JButton okButton, cancelButton;
	JTextField userTF, passTF, repassTF;
	// JCheckBox adminCHB;
	protected JComboBox typeCB;

	public CreateUserWindow() {
		super(425, 200);
		setTitle(PluginServices.getText(this, "new_user"));
	}

	protected JPanel getCenterPanel() {
		if (centerPanel == null) {
			centerPanel = new JPanel();
			FormPanel form = new FormPanel("forms/newUser.jfrm");
			centerPanel.add(form);
			userTF = form.getTextField("userTF");
			passTF = form.getTextField("passTF");
			repassTF = form.getTextField("repassTF");
			// adminCHB = form.getCheckBox("adminCHB");
			typeCB = form.getComboBox("typeCB");

			// Labels
			JLabel userLabel = form.getLabel("userLabel");
			userLabel.setText(PluginServices.getText(this, "user_name"));
			JLabel passLabel = form.getLabel("passLabel");
			passLabel.setText(PluginServices.getText(this, "user_pass"));
			JLabel repassLabel = form.getLabel("repassLabel");
			repassLabel.setText(PluginServices
					.getText(this, "retype_user_pass"));
			JLabel typeLabel = form.getLabel("typeLabel");
			typeLabel.setText(PluginServices.getText(this, "user_type"));

			// adminCHB.setText(PluginServices.getText(this, "create_admin"));
			typeCB.addItem(PluginServices.getText(this, "create_guest"));
			typeCB.addItem(PluginServices.getText(this, "create_admin"));
		}
		return centerPanel;
	}

	protected void onOK() {
		String username = userTF.getText();
		String pass1 = passTF.getText();
		String pass2 = repassTF.getText();
		boolean cont = true;
		if (typeCB.getSelectedIndex() == 2) {
			Object[] options = { PluginServices.getText(this, "ok"),
					PluginServices.getText(this, "cancel") };
			int n = JOptionPane.showOptionDialog(this,
					PluginServices.getText(this, "create_admin_question"), "",
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.WARNING_MESSAGE, null, options, options[1]);
			if (n != 0) {
				cont = false;
			}
		}
		if (cont) {
			if (pass1.equals(pass2)) {
				DBSession dbs = DBSession.getCurrentSession();
				if (dbs != null) {
					Connection con = dbs.getJavaConnection();
					try {
						if (DBAdminUtils.existsUser(con, username)) {
							String message = PluginServices.getText(this,
									"user_exists");
							JOptionPane.showMessageDialog(this, String.format(
									message, username), PluginServices.getText(
									this, "creating_user_error"),
									JOptionPane.ERROR_MESSAGE);
						} else {
							closeWindow();
							try {
								DBAdminUtils.createUser(con, username, pass1);
								grantRole(con, username);
								// force db commit
								con.commit();
							} catch (SQLException e2) {
								String message = PluginServices.getText(this,
										"creating_user_error_message");
								JOptionPane
										.showMessageDialog(this, String.format(
												message, e2.getMessage()),
												PluginServices.getText(this,
														"creating_user_error"),
												JOptionPane.ERROR_MESSAGE);
								try {
									dbs = DBSession.reconnect();
								} catch (DBException e) {
									e.printStackTrace();
								}
							}
						}
					} catch (SQLException e1) {
						try {
							dbs = DBSession.reconnect();
						} catch (DBException e) {
							e.printStackTrace();
						}
					}
				}
			} else {
				JOptionPane.showMessageDialog(this,
						PluginServices.getText(this, "passwords_dont_match"),
						PluginServices.getText(this, "creating_user_error"),
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	protected void grantRole(Connection con, String username)
			throws SQLException {
		// TODO Auto-generated method stub
		int selectedIndex = typeCB.getSelectedIndex();
		switch (selectedIndex) {
		case 0: // Guest
			DBAdminUtils.grantRole(con, username, "guest");
			break;
		case 1: // Admin
			DBAdminUtils.grantRole(con, username, "administrador");
			break;
		}
	}

	protected Component getDefaultFocusComponent() {
		return userTF;
	}

}
