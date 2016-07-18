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

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.gvsig.andami.PluginServices;
import org.gvsig.andami.ui.mdiManager.IWindow;
import org.gvsig.andami.ui.mdiManager.WindowInfo;
import org.gvsig.fmap.dal.exception.DataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeta.forms.components.panel.FormPanel;
import com.jeta.forms.gui.common.FormException;

import es.udc.cartolab.gvsig.users.utils.DBAdminUtils;
import es.udc.cartolab.gvsig.users.utils.DBSession;
import es.udc.cartolab.gvsig.users.utils.DBSessionPostGIS;

public class DropUserDialog extends JPanel implements IWindow, ActionListener {

    
	private JPanel southPanel = null, centerPanel = null;
	private JButton okButton, cancelButton;
	private JTextField userTF;
	private WindowInfo viewInfo;

	
	
	private static final Logger logger = LoggerFactory
			.getLogger(DropUserDialog.class);
	
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
			InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("forms/dropUser.jfrm");
			FormPanel form;
			try {
			    form = new FormPanel(resourceAsStream);
			} catch (FormException e) {
				logger.error(e.getMessage(), e);
			    return centerPanel;
			}
			form.setFocusTraversalPolicyProvider(true);
			centerPanel.add(form);

			userTF = form.getTextField("userTF");
			userTF.addActionListener(this);

			JLabel userLabel = form.getLabel("userLabel");
			userLabel.setText(_("user_name"));
		}
		return centerPanel;
	}

	protected JPanel getSouthPanel() {

		if (southPanel == null) {
			southPanel = new JPanel();
			FlowLayout layout = new FlowLayout();
			layout.setAlignment(FlowLayout.RIGHT);
			southPanel.setLayout(layout);
			okButton = new JButton(_("ok"));
			cancelButton = new JButton(_("cancel"));
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
			viewInfo.setTitle(_("drop_user"));
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
			if ((dbs != null) && (dbs instanceof DBSessionPostGIS)) {
				try {
					if (!username.equalsIgnoreCase(((DBSessionPostGIS) dbs)
							.getUserName())
							&& !username.equalsIgnoreCase("postgres")) {
						if (DBAdminUtils.existsUser(dbs.getJavaConnection(), username)) {
							
							Object[] options = {_("ok"), _("cancel")};
							int n = JOptionPane.showOptionDialog(this,
									_("dropping_user_question", username),
									"",
									JOptionPane.YES_NO_CANCEL_OPTION,
									JOptionPane.WARNING_MESSAGE,
									null,
									options,
									options[1]);
							if (n==0) {
								DBAdminUtils.dropUser(dbs.getJavaConnection(), username);
							}
						} else {
							
							JOptionPane.showMessageDialog(this,
									_("user_doesnt_exist", username),
									"",
									JOptionPane.ERROR_MESSAGE);
						}
					} else {
						JOptionPane.showMessageDialog(this,
								_("user_cant_be_dropped", username),
								"",
								JOptionPane.ERROR_MESSAGE);
					}
				} catch (SQLException e) {
					logger.error(e.getMessage(), e);
					JOptionPane.showMessageDialog(this,
							_("dropping_user_error_message"),
							"",
							JOptionPane.ERROR_MESSAGE);
					
					try {
						dbs = DBSession.reconnect();
					} catch (DataException e1) {
						logger.error(e1.getMessage(), e1);
					}
				}
			}
		}
	}

	public Object getWindowProfile() {
		return WindowInfo.DIALOG_PROFILE;
	}



}
