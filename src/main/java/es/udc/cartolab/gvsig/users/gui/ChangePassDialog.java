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
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.gvsig.andami.PluginServices;
import org.gvsig.fmap.dal.exception.DataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeta.forms.components.panel.FormPanel;
import com.jeta.forms.gui.common.FormException;

import es.udc.cartolab.gvsig.users.utils.DBSession;
import es.udc.cartolab.gvsig.users.utils.DBUser;

public class ChangePassDialog extends AbstractGVWindow {

	JPanel centerPanel = null;
	JPanel northPanel = null;
	JPanel southPanel = null;
	JButton okButton, cancelButton;
	JTextField currentPassTF, newPassTF, reNewPassTF;

	
	private static final Logger logger = LoggerFactory
			.getLogger(ChangePassDialog.class);
	
	public ChangePassDialog() {
		super(425, 160);
		setTitle(_("change_password"));
	}

	protected JPanel getCenterPanel() {
		if (centerPanel == null) {
			centerPanel = new JPanel();
			InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("forms/changePass.jfrm");
			FormPanel form;
			try {
			    form = new FormPanel(resourceAsStream);
			} catch (FormException e) {
				logger.error(e.getMessage(), e);
			    return centerPanel;
			}
			centerPanel.add(form);

			JLabel currentPassLabel = form.getLabel("currentPassLabel");
			currentPassLabel.setText(_("current_pass"));
			JLabel newPassLabel = form.getLabel("newPassLabel");
			newPassLabel.setText(_("new_pass"));
			JLabel reNewPassLabel = form.getLabel("reNewPassLabel");
			reNewPassLabel.setText(_("retype_user_pass"));

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
						JOptionPane.showMessageDialog(this,_("wrong_password"),"", JOptionPane.ERROR_MESSAGE);
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					JOptionPane.showMessageDialog(this,_("changin_pass_error"),"", JOptionPane.ERROR_MESSAGE);
					
					try {
						dbs = DBSession.reconnect();
					} catch (DataException e1) {
						logger.error(e1.getMessage(), e1);
					}
					
				}
			}
		} else {
			JOptionPane.showMessageDialog(this, _("passwords_dont_match"), "", JOptionPane.ERROR_MESSAGE);
		}
	}

	protected Component getDefaultFocusComponent() {
		return currentPassTF;
	}

}
