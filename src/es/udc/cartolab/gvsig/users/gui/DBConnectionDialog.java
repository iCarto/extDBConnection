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
import java.io.IOException;

import javax.swing.ImageIcon;
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

import es.udc.cartolab.gvsig.users.utils.ConfigFile;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class DBConnectionDialog extends JPanel implements IWindow, ActionListener {

	WindowInfo viewInfo = null;
	private JPanel northPanel = null;
	private JPanel centerPanel = null;
	private JPanel southPanel = null;
	private JButton okButton;
	private JButton cancelButton;
	private JTextField serverTF, portTF, userTF, passTF;

	public static final String ID_SERVERTF = "serverTF";  //javax.swing.JTextField
	public static final String ID_PORTTF = "portTF";  //javax.swing.JTextField
	public static final String ID_USERTF = "userTF";  //javax.swing.JTextField
	public static final String ID_PASSTF = "passTF";  //javax.swing.JPasswordField
	public static final String ID_SERVERL = "serverLabel";
	public static final String ID_PORTL = "portLabel";
	public static final String ID_USERL = "userLabel";
	public static final String ID_PASSL = "passLabel";


	public DBConnectionDialog() {
		init();
	}

	public WindowInfo getWindowInfo() {

		if (viewInfo == null) {
			viewInfo = new WindowInfo(WindowInfo.MODELESSDIALOG | WindowInfo.PALETTE);
			viewInfo.setTitle(PluginServices.getText(this, "Login"));
			viewInfo.setWidth(425);
			viewInfo.setHeight(275);
		}
		return viewInfo;
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
			FormPanel form = new FormPanel("forms/dbConnection.jfrm");
			form.setFocusTraversalPolicyProvider(true);
			centerPanel.add(form);
			serverTF = form.getTextField(ID_SERVERTF);
			serverTF.addActionListener(this);
			portTF = form.getTextField(ID_PORTTF);
			portTF.addActionListener(this);
			userTF = form.getTextField(ID_USERTF);
			userTF.addActionListener(this);
			passTF = form.getTextField(ID_PASSTF);
			passTF.addActionListener(this);

			JLabel serverLabel = form.getLabel(ID_SERVERL);
			JLabel portLabel = form.getLabel(ID_PORTL);
			JLabel userLabel = form.getLabel(ID_USERL);
			JLabel passLabel = form.getLabel(ID_PASSL);

			serverLabel.setText(PluginServices.getText(this, "server"));
			portLabel.setText(PluginServices.getText(this, "port"));
			userLabel.setText(PluginServices.getText(this, "user_name"));
			passLabel.setText(PluginServices.getText(this, "eiel_pass"));

			DBSession dbs = DBSession.getCurrentSession();
			if (dbs!=null) {
				serverTF.setText(dbs.getServer());
				portTF.setText((new Integer(dbs.getPort())).toString());
				userTF.setText(dbs.getUserName());
			} else {
				ConfigFile cf = ConfigFile.getInstance();
				serverTF.setText(cf.getServer());
				portTF.setText(cf.getPort());
				userTF.setText(cf.getUsername());
			}

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

	public void actionPerformed(ActionEvent e) {

		if ((e.getSource() == okButton) || (e.getSource() instanceof JTextField)) {
			PluginServices.getMDIManager().setWaitCursor();
			try {
				String portS = portTF.getText().trim();
				int port = new Integer(portS).intValue();
				String server = serverTF.getText().trim();
				String username = userTF.getText().trim();
				String password = passTF.getText();

				DBSession dbc = DBSession.createConnection(server, port, username, password);

				ConfigFile cf = ConfigFile.getInstance();
				dbc.changeDatabase(cf.getDatabase());
				dbc.changeSchema(cf.getSchema());

				PluginServices.getMDIManager().closeWindow(this);

				//save config file
				cf.setProperties(server, portS, dbc.getDatabase(), dbc.getSchema(), username);
				PluginServices.getMDIManager().restoreCursor();
				String title = " " + String.format(PluginServices.getText(this, "connectedTitle"), username, server);
				PluginServices.getMainFrame().setTitle(title);

			} catch (DBException e1) {
				// Login error
				e1.printStackTrace();
				PluginServices.getMDIManager().restoreCursor();
				JOptionPane.showMessageDialog(this,
						PluginServices.getText(this, "databaseConnectionError"),
						PluginServices.getText(this, "connectionError"),
						JOptionPane.ERROR_MESSAGE);

			} catch (NumberFormatException e2) {
				PluginServices.getMDIManager().restoreCursor();
				JOptionPane.showMessageDialog(this,
						PluginServices.getText(this, "portError"),
						PluginServices.getText(this, "dataError"),
						JOptionPane.ERROR_MESSAGE);
			} catch (IOException e3) {
				//TODO show error in log
				PluginServices.getMDIManager().restoreCursor();
				System.out.println("No se pudo guardar el archivo: " + e3.getMessage());
			} finally {

				passTF.setText("");
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
