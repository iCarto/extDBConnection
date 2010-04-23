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
import java.sql.SQLException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.WindowInfo;
import com.jeta.forms.components.panel.FormPanel;

import es.udc.cartolab.gvsig.users.utils.DBSession;
import es.udc.cartolab.gvsig.users.utils.EIELUser;

public class ChangePassDialog extends JPanel implements IWindow, ActionListener {

	WindowInfo viewInfo = null;
	JPanel centerPanel = null;
	JPanel northPanel = null;
	JPanel southPanel = null;
	JButton okButton, cancelButton;
	JTextField currentPassTF, newPassTF, reNewPassTF;

	public WindowInfo getWindowInfo() {
		// TODO Auto-generated method stub
		if (viewInfo == null) {
			viewInfo = new WindowInfo(WindowInfo.MODELESSDIALOG | WindowInfo.PALETTE);
			viewInfo.setTitle(PluginServices.getText(this, "change_password"));
			viewInfo.setWidth(425);
			viewInfo.setHeight(235);
		}
		return viewInfo;
	}

	public ChangePassDialog() {
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
			FormPanel form = new FormPanel("forms/changePass.jfrm");
			form.setFocusTraversalPolicyProvider(true);
			centerPanel.add(form);

			JLabel currentPassLabel = form.getLabel("currentPassLabel");
			currentPassLabel.setText(PluginServices.getText(this, "current_pass"));
			JLabel newPassLabel = form.getLabel("newPassLabel");
			newPassLabel.setText(PluginServices.getText(this, "new_pass"));
			JLabel reNewPassLabel = form.getLabel("reNewPassLabel");
			reNewPassLabel.setText(PluginServices.getText(this, "retype_eiel_pass"));

			currentPassTF = form.getTextField("currentPassTF");
			currentPassTF.addActionListener(this);
			newPassTF = form.getTextField("newPassTF");
			newPassTF.addActionListener(this);
			reNewPassTF = form.getTextField("reNewPassTF");
			reNewPassTF.addActionListener(this);
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

	public void actionPerformed(ActionEvent event) {
		// TODO Auto-generated method stub
		if (event.getSource() == cancelButton) {
			PluginServices.getMDIManager().closeWindow(this);
		}
		if ((event.getSource() == okButton) || (event.getSource() instanceof JTextField)) {
			String currentPass = currentPassTF.getText();
			String newPass = newPassTF.getText();
			String newPass2 = reNewPassTF.getText();
			if (newPass.equals(newPass2)) {
				DBSession dbs = DBSession.getCurrentSession();
				if (dbs!=null) {
					EIELUser user = dbs.getEIELUser();
					try {
						if (user.checkPassword(currentPass)) {
							user.changePassword(newPass);
							PluginServices.getMDIManager().closeWindow(this);
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
						e.printStackTrace();
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

}
