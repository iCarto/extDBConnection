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
package es.udc.cartolab.gvsig.users.gui;

import static es.icarto.gvsig.commons.i18n.I18n._;

import java.awt.Component;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.gvsig.fmap.dal.exception.DataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeta.forms.components.panel.FormPanel;
import com.jeta.forms.gui.common.FormException;

import es.udc.cartolab.gvsig.users.utils.DBAdminUtils;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class CreateUserWindow extends AbstractGVWindow {

	JPanel centerPanel = null;

	JButton okButton, cancelButton;
	JTextField userTF, passTF, repassTF;
	// JCheckBox adminCHB;
	protected JComboBox typeCB;

	private static final Logger logger = LoggerFactory.getLogger(CreateUserWindow.class);

	public CreateUserWindow() {
		super(425, 200);
		setTitle(_("new_user"));
	}

	protected JPanel getCenterPanel() {
		if (centerPanel == null) {
			centerPanel = new JPanel();
			InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("forms/newUser.jfrm");
			FormPanel form;
			try {
				form = new FormPanel(resourceAsStream);
			} catch (FormException e) {
				logger.error(e.getMessage(), e);
				return centerPanel;
			}
			centerPanel.add(form);
			userTF = form.getTextField("userTF");
			passTF = form.getTextField("passTF");
			repassTF = form.getTextField("repassTF");
			// adminCHB = form.getCheckBox("adminCHB");
			typeCB = form.getComboBox("typeCB");

			// Labels
			JLabel userLabel = form.getLabel("userLabel");
			userLabel.setText(_("user_name"));
			JLabel passLabel = form.getLabel("passLabel");
			passLabel.setText(_("user_pass"));
			JLabel repassLabel = form.getLabel("repassLabel");
			repassLabel.setText(_("retype_user_pass"));
			JLabel typeLabel = form.getLabel("typeLabel");
			typeLabel.setText(_("user_type"));

			typeCB.addItem(_("create_guest"));
			typeCB.addItem(_("create_admin"));
		}
		return centerPanel;
	}

	protected void onOK() {
		String username = userTF.getText();
		String pass1 = passTF.getText();
		String pass2 = repassTF.getText();
		boolean cont = true;
		if (typeCB.getSelectedIndex() == 2) {
			Object[] options = { _("ok"), _("cancel") };
			int n = JOptionPane.showOptionDialog(this, _("create_admin_question"), "", JOptionPane.YES_NO_CANCEL_OPTION,
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
							JOptionPane.showMessageDialog(this, _("user_exists", username), _("creating_user_error"),
									JOptionPane.ERROR_MESSAGE);
						} else {
							closeWindow();
							try {
								DBAdminUtils.createUser(con, username, pass1);
								grantRole(con, username);
							} catch (SQLException e2) {
								logger.error(e2.getMessage(), e2);

								JOptionPane.showMessageDialog(this, _("creating_user_error"), _("creating_user_error"),
										JOptionPane.ERROR_MESSAGE);
								try {
									dbs = DBSession.reconnect();
								} catch (DataException e) {
									logger.error(e.getMessage(), e);
								}

							}
						}
					} catch (SQLException e1) {
						try {
							dbs = DBSession.reconnect();
						} catch (DataException e) {
							logger.error(e.getMessage(), e);
						}
					}
				}
			} else {
				JOptionPane.showMessageDialog(this, _("passwords_dont_match"), _("creating_user_error"),
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	protected void grantRole(Connection con, String username) throws SQLException {
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
