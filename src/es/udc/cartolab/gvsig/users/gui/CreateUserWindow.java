package es.udc.cartolab.gvsig.users.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.WindowInfo;
import com.jeta.forms.components.panel.FormPanel;

import es.udc.cartolab.gvsig.users.utils.DBSession;
import es.udc.cartolab.gvsig.users.utils.EIELAdminUtils;

public class CreateUserWindow extends JPanel implements IWindow, ActionListener {

	WindowInfo viewInfo = null;
	JPanel centerPanel = null;
	JPanel northPanel = null;
	JPanel southPanel = null;
	JButton okButton, cancelButton;
	JTextField userTF, passTF, repassTF;
	//JCheckBox adminCHB;
	JComboBox typeCB;


	public WindowInfo getWindowInfo() {
		// TODO Auto-generated method stub
		if (viewInfo == null) {
			viewInfo = new WindowInfo(WindowInfo.MODELESSDIALOG | WindowInfo.PALETTE);
			viewInfo.setTitle(PluginServices.getText(this, "new_user"));
			viewInfo.setWidth(425);
			viewInfo.setHeight(275);
		}
		return viewInfo;
	}

	public CreateUserWindow() {
		init();
	}

	private void init() {

		GridBagLayout layout = new GridBagLayout();
		setLayout(layout);

		add(getNorthPanel(), new GridBagConstraints(0, 0, 1, 1, 0, 0,
				GridBagConstraints.NORTH, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));

		add(getCenterPanel(), new GridBagConstraints(0, 1, 1, 1, 0, 1,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));

		add(getSouthPanel(), new GridBagConstraints(0, 2, 1, 1, 10, 0,
				GridBagConstraints.SOUTH, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));

		//enables tabbing navigation
		setFocusCycleRoot(true);
	}

	protected JPanel getNorthPanel() {

		//Set header if any
		//Current header (Pontevedra) size: 425x79
		if (northPanel == null) {
			northPanel = new JPanel();
			File iconPath = new File("gvSIG/extensiones/es.udc.cartolab.gvsig.elle/images/header.png");
			if (iconPath.exists()) {
				northPanel.setBackground(new Color(36, 46, 109));
				ImageIcon logo = new ImageIcon(iconPath.getAbsolutePath());
				JLabel icon = new JLabel();
				icon.setIcon(logo);
				northPanel.add(icon, BorderLayout.WEST);
			}
		}
		return northPanel;
	}

	protected JPanel getCenterPanel() {
		if (centerPanel == null) {
			centerPanel = new JPanel();
			FormPanel form = new FormPanel("forms/newUser.jfrm");
			form.setFocusTraversalPolicyProvider(true);
			centerPanel.add(form);
			userTF = form.getTextField("userTF");
			passTF = form.getTextField("passTF");
			repassTF = form.getTextField("repassTF");
			//			adminCHB = form.getCheckBox("adminCHB");
			typeCB = form.getComboBox("typeCB");

			//Labels
			JLabel userLabel = form.getLabel("userLabel");
			userLabel.setText(PluginServices.getText(this, "user_name"));
			JLabel passLabel = form.getLabel("passLabel");
			passLabel.setText(PluginServices.getText(this, "eiel_pass"));
			JLabel repassLabel = form.getLabel("repassLabel");
			repassLabel.setText(PluginServices.getText(this, "retype_eiel_pass"));
			JLabel typeLabel = form.getLabel("typeLabel");
			typeLabel.setText(PluginServices.getText(this, "user_type"));

			//adminCHB.setText(PluginServices.getText(this, "create_admin"));
			typeCB.addItem(PluginServices.getText(this, "create_guest"));
			typeCB.addItem(PluginServices.getText(this, "create_eiel"));
			typeCB.addItem(PluginServices.getText(this, "create_admin"));
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

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == okButton) {
			String username = userTF.getText();
			String pass1 = passTF.getText();
			String pass2 = repassTF.getText();
			boolean cont = true;
			if (typeCB.getSelectedIndex()==2) {
				Object[] options = {PluginServices.getText(this, "ok"),
						PluginServices.getText(this, "cancel")};
				int n = JOptionPane.showOptionDialog(this,
						PluginServices.getText(this, "create_admin_question"),
						"",
						JOptionPane.YES_NO_CANCEL_OPTION,
						JOptionPane.WARNING_MESSAGE,
						null,
						options,
						options[1]);
				if (n!=0) {
					cont = false;
				}
			}
			if (cont) {
				if (pass1.equals(pass2)) {
					DBSession dbs = DBSession.getCurrentSession();
					if (dbs!=null) {
						Connection con = dbs.getJavaConnection();
						try {
							if (EIELAdminUtils.existsUser(con, username)){
								String message = PluginServices.getText(this, "user_exists");
								JOptionPane.showMessageDialog(this,
										String.format(message, username),
										PluginServices.getText(this, "creating_user_error"),
										JOptionPane.ERROR_MESSAGE);
							} else {
								PluginServices.getMDIManager().closeWindow(this);
								try {
									EIELAdminUtils.createUser(con, username, pass1);
									switch (typeCB.getSelectedIndex()) {
									case 0 : //Guest
										EIELAdminUtils.grantRole(con, username, "guest");
										break;
									case 1 : //EIEL user
										EIELAdminUtils.grantRole(con, username, "eiel");
										break;
									case 2 : //Admin
										EIELAdminUtils.grantRole(con, username, "administrador");
										break;
									}
									//force db commit
									con.commit();
								} catch (SQLException e2) {
									String message = PluginServices.getText(this, "creating_user_error_message");
									JOptionPane.showMessageDialog(this,
											String.format(message, e2.getMessage()),
											PluginServices.getText(this, "creating_user_error"),
											JOptionPane.ERROR_MESSAGE);
								}
							}
						} catch (SQLException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
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
		if (e.getSource() == cancelButton) {
			PluginServices.getMDIManager().closeWindow(this);
		}
	}

	public Object getWindowProfile() {
		// TODO Auto-generated method stub
		return null;
	}

}
