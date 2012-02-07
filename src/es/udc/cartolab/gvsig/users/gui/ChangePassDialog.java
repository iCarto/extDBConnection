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
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.drivers.DBException;
import com.jeta.forms.components.panel.FormPanel;

import es.udc.cartolab.gvsig.users.utils.DBSession;
import es.udc.cartolab.gvsig.users.utils.DBUser;

public class ChangePassDialog extends AbstractGVWindow {

	JPanel centerPanel = null;
	JPanel northPanel = null;
	JPanel southPanel = null;
	JButton okButton, cancelButton;
	JTextField currentPassTF, newPassTF, reNewPassTF;

	public ChangePassDialog() {
		super(425, 160);
		setTitle(PluginServices.getText(this, "change_password"));
	}

	protected JPanel getCenterPanel() {
		if (centerPanel == null) {
			centerPanel = new JPanel();
			FormPanel form = new FormPanel("forms/changePass.jfrm");
			centerPanel.add(form);

			JLabel currentPassLabel = form.getLabel("currentPassLabel");
			currentPassLabel.setText(PluginServices.getText(this,
					"current_pass"));
			JLabel newPassLabel = form.getLabel("newPassLabel");
			newPassLabel.setText(PluginServices.getText(this, "new_pass"));
			JLabel reNewPassLabel = form.getLabel("reNewPassLabel");
			reNewPassLabel.setText(PluginServices.getText(this,
					"retype_user_pass"));

			currentPassTF = form.getTextField("currentPassTF");
			newPassTF = form.getTextField("newPassTF");
			reNewPassTF = form.getTextField("reNewPassTF");
		}
		return centerPanel;
	}

	protected void onOK() {
		String currentPass = currentPassTF.getText();
		String newPass = newPassTF.getText();
		String newPass2 = reNewPassTF.getText();
		if (newPass.equals(newPass2)) {
			DBSession dbs = DBSession.getCurrentSession();
			if (dbs != null) {
				DBUser user = dbs.getDBUser();
				try {
					if (user.checkPassword(currentPass)) {
						user.changePassword(newPass);
						closeWindow();
					} else {
						JOptionPane.showMessageDialog(this,
								PluginServices.getText(this, "wrong_password"),
								PluginServices.getText(this, "dataError"),
								JOptionPane.ERROR_MESSAGE);
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					JOptionPane.showMessageDialog(this,
							PluginServices.getText(this, "changin_pass_error"),
							PluginServices.getText(this, "dataError"),
							JOptionPane.ERROR_MESSAGE);
					try {
						dbs = DBSession.reconnect();
					} catch (DBException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
				}
			}
		} else {
			JOptionPane.showMessageDialog(this,
					PluginServices.getText(this, "passwords_dont_match"),
					PluginServices.getText(this, "dataError"),
					JOptionPane.ERROR_MESSAGE);
		}
	}

	protected Component getDefaultFocusComponent() {
		return currentPassTF;
	}

}
