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

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.WindowInfo;
import com.iver.cit.gvsig.fmap.drivers.DBException;
import com.jeta.forms.components.panel.FormPanel;

import es.udc.cartolab.gvsig.users.utils.DBAdminUtils;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class DropUserDialog extends JPanel implements IWindow, ActionListener {

	private JPanel southPanel = null, centerPanel = null;
	private JButton okButton, cancelButton;
	private JTextField userTF;
	private WindowInfo viewInfo;

	public DropUserDialog() {

		GridBagLayout layout = new GridBagLayout();
		setLayout(layout);

		add(getCenterPanel(), new GridBagConstraints(0, 0, 1, 1, 0, 1,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));

		add(getSouthPanel(), new GridBagConstraints(0, 1, 1, 1, 10, 0,
				GridBagConstraints.SOUTH, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));

		//enables tabbing navigation
		setFocusCycleRoot(true);
	}

	protected JPanel getCenterPanel() {

		if (centerPanel == null) {
			centerPanel = new JPanel();
			FormPanel form = new FormPanel("forms/dropUser.jfrm");
			form.setFocusTraversalPolicyProvider(true);
			centerPanel.add(form);

			userTF = form.getTextField("userTF");
			userTF.addActionListener(this);

			JLabel userLabel = form.getLabel("userLabel");
			userLabel.setText(PluginServices.getText(this, "user_name"));
		}
		return centerPanel;
	}

	protected JPanel getSouthPanel() {

		if (southPanel == null) {
			southPanel = new JPanel();
			FlowLayout layout = new FlowLayout();
			layout.setAlignment(FlowLayout.RIGHT);
			southPanel.setLayout(layout);
			okButton = new JButton(PluginServices.getText(this, "ok"));
			cancelButton = new JButton(PluginServices.getText(this, "cancel"));
			okButton.addActionListener(this);
			cancelButton.addActionListener(this);
			southPanel.add(okButton);
			southPanel.add(cancelButton);
		}
		return southPanel;
	}

	public WindowInfo getWindowInfo() {
		if (viewInfo == null) {
			viewInfo = new WindowInfo(WindowInfo.MODALDIALOG);
			viewInfo.setTitle(PluginServices.getText(this, "drop_user"));
			viewInfo.setWidth(425);
			viewInfo.setHeight(75);
		}
		return viewInfo;
	}

	public void actionPerformed(ActionEvent event) {
		// TODO Auto-generated method stub
		if (event.getSource() == cancelButton) {
			PluginServices.getMDIManager().closeWindow(this);
		}
		if ((event.getSource() == okButton) || (event.getSource() == userTF)) {
			DBSession dbs = DBSession.getCurrentSession();
			String username = userTF.getText();
			if (dbs != null) {
				try {
					if (!username.equalsIgnoreCase(dbs.getUserName()) && !username.equalsIgnoreCase("postgres")) {
						if (DBAdminUtils.existsUser(dbs.getJavaConnection(), username)) {
							String message = PluginServices.getText(this, "dropping_user_question");
							Object[] options = {PluginServices.getText(this, "ok"),
									PluginServices.getText(this, "cancel")};
							int n = JOptionPane.showOptionDialog(this,
									PluginServices.getText(this, String.format(message, username)),
									" ",
									JOptionPane.YES_NO_CANCEL_OPTION,
									JOptionPane.WARNING_MESSAGE,
									null,
									options,
									options[1]);
							if (n==0) {
								DBAdminUtils.dropUser(dbs.getJavaConnection(), username);
								dbs.getJavaConnection().commit();
							}
						} else {
							String message = PluginServices.getText(this, "user_doesnt_exist");
							JOptionPane.showMessageDialog(this,
									PluginServices.getText(this, String.format(message, username)),
									PluginServices.getText(this, " "),
									JOptionPane.ERROR_MESSAGE);
						}
					} else {
						String message = PluginServices.getText(this, "user_cant_be_dropped");
						JOptionPane.showMessageDialog(this,
								PluginServices.getText(this, String.format(message, username)),
								PluginServices.getText(this, " "),
								JOptionPane.ERROR_MESSAGE);
					}
				} catch (SQLException e) {
					String message = PluginServices.getText(this, "dropping_user_error_message");
					JOptionPane.showMessageDialog(this,
							PluginServices.getText(this, String.format(message, e.getMessage())),
							PluginServices.getText(this, PluginServices.getText(this, "dropping_user_error")),
							JOptionPane.ERROR_MESSAGE);
					try {
						dbs = DBSession.reconnect();
					} catch (DBException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
				}
			}
		}
	}

	public Object getWindowProfile() {
		// TODO Auto-generated method stub
		return WindowInfo.DIALOG_PROFILE;
	}



}
