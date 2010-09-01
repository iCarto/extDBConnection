package es.udc.cartolab.gvsig.users.gui;

import java.awt.Color;
import java.sql.SQLException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiManager.WindowInfo;
import com.iver.cit.gvsig.fmap.drivers.DBException;
import com.jeta.forms.components.panel.FormPanel;

import es.udc.cartolab.gvsig.users.utils.DBSession;
import es.udc.cartolab.gvsig.users.utils.DBUser;

public class ChangePassDialog extends AbstractGVWindow {

	WindowInfo viewInfo = null;
	JPanel centerPanel = null;
	JPanel northPanel = null;
	JPanel southPanel = null;
	JButton okButton, cancelButton;
	JTextField currentPassTF, newPassTF, reNewPassTF;

	public ChangePassDialog(ImageIcon headerImage, Color headerBgColor) {
		super(425, 160, headerImage, headerBgColor);
		setTitle(PluginServices.getText(this, "change_password"));
	}

	public ChangePassDialog() {
		this(null, null);
	}

	@Override
	protected JPanel getCenterPanel() {
		if (centerPanel == null) {
			centerPanel = new JPanel();
			FormPanel form = new FormPanel("forms/changePass.jfrm");
			form.setFocusTraversalPolicyProvider(true);
			centerPanel.add(form);

			JLabel currentPassLabel = form.getLabel("currentPassLabel");
			currentPassLabel.setText(PluginServices.getText(this, "current_pass"));
			JLabel newPassLabel = form.getLabel("newPassLabel");
			newPassLabel.setText(PluginServices.getText(this, "new_pass"));
			JLabel reNewPassLabel = form.getLabel("reNewPassLabel");
			reNewPassLabel.setText(PluginServices.getText(this, "retype_user_pass"));

			currentPassTF = form.getTextField("currentPassTF");
			currentPassTF.addActionListener(this);
			newPassTF = form.getTextField("newPassTF");
			newPassTF.addActionListener(this);
			reNewPassTF = form.getTextField("reNewPassTF");
			reNewPassTF.addActionListener(this);
		}
		return centerPanel;
	}

	@Override
	protected void onOK() {
		String currentPass = currentPassTF.getText();
		String newPass = newPassTF.getText();
		String newPass2 = reNewPassTF.getText();
		if (newPass.equals(newPass2)) {
			DBSession dbs = DBSession.getCurrentSession();
			if (dbs!=null) {
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

}
