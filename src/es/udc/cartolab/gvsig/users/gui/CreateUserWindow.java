package es.udc.cartolab.gvsig.users.gui;

import java.awt.Color;
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
import com.jeta.forms.components.panel.FormPanel;

import es.udc.cartolab.gvsig.users.utils.DBAdminUtils;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class CreateUserWindow extends AbstractGVWindow {


	JPanel centerPanel = null;

	JButton okButton, cancelButton;
	JTextField userTF, passTF, repassTF;
	//JCheckBox adminCHB;
	JComboBox typeCB;


	public CreateUserWindow() {
		this(null, null);
	}

	public CreateUserWindow(ImageIcon headerImg, Color headerBgColor) {
		super(425, 200, headerImg, headerBgColor);
		setTitle(PluginServices.getText(this, "new_user"));
	}

	@Override
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

	@Override
	protected void onOK() {
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
						if (DBAdminUtils.existsUser(con, username)){
							String message = PluginServices.getText(this, "user_exists");
							JOptionPane.showMessageDialog(this,
									String.format(message, username),
									PluginServices.getText(this, "creating_user_error"),
									JOptionPane.ERROR_MESSAGE);
						} else {
							closeWindow();
							try {
								DBAdminUtils.createUser(con, username, pass1);
								switch (typeCB.getSelectedIndex()) {
								case 0 : //Guest
									DBAdminUtils.grantRole(con, username, "guest");
									break;
								case 1 : //EIEL user
									DBAdminUtils.grantRole(con, username, "eiel");
									break;
								case 2 : //Admin
									DBAdminUtils.grantRole(con, username, "administrador");
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

}
