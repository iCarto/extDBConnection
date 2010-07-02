package es.udc.cartolab.gvsig.users.gui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.drivers.DBException;
import com.jeta.forms.components.panel.FormPanel;

import es.udc.cartolab.gvsig.users.utils.ConfigFile;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class DBConnectionDialog extends AbstractGVWindow {

	private final static int INIT_MIN_HEIGHT = 175;
	private final static int INIT_MAX_HEIGHT = 350;

	private int minHeight;
	private int maxHeight;


	private JPanel centerPanel = null;

	private JCheckBox advCHB;
	private JTextField serverTF, userTF, passTF, schemaTF, dbTF, portTF;
	private JComponent advForm;

	public static final String ID_SERVERTF = "serverTF";  //javax.swing.JTextField
	public static final String ID_PORTTF = "portTF";  //javax.swing.JTextField
	public static final String ID_USERTF = "userTF";  //javax.swing.JTextField
	public static final String ID_PASSTF = "passTF";  //javax.swing.JPasswordField
	public static final String ID_DBTF = "dbTF";
	public static final String ID_SCHEMATF = "schemaTF";
	public static final String ID_ADVF = "advancedForm";
	public static final String ID_ADVCHB = "advancedCHB";
	public static final String ID_SERVERL = "serverLabel";
	public static final String ID_PORTL = "portLabel";
	public static final String ID_USERL = "userLabel";
	public static final String ID_PASSL = "passLabel";
	public static final String ID_DBL = "dbLabel";
	public static final String ID_SCHEMAL = "schemaLabel";


	public DBConnectionDialog(ImageIcon headerImg, Color bgColor) {
		super(425, INIT_MIN_HEIGHT, headerImg, bgColor);
		setTitle(PluginServices.getText(this, "Login"));
	}

	public DBConnectionDialog() {
		this(null, null);
	}


	@Override
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
			dbTF = form.getTextField(ID_DBTF);
			schemaTF = form.getTextField(ID_SCHEMATF);
			advForm = (JComponent) form.getComponentByName(ID_ADVF);
			advCHB = form.getCheckBox(ID_ADVCHB);
			advCHB.addActionListener(this);

			//localization
			JLabel serverLabel = form.getLabel(ID_SERVERL);
			JLabel portLabel = form.getLabel(ID_PORTL);
			JLabel userLabel = form.getLabel(ID_USERL);
			JLabel passLabel = form.getLabel(ID_PASSL);
			JLabel schemaLabel = form.getLabel(ID_SCHEMAL);
			JLabel dbLabel = form.getLabel(ID_DBL);

			serverLabel.setText(PluginServices.getText(this, "server"));
			portLabel.setText(PluginServices.getText(this, "port"));
			userLabel.setText(PluginServices.getText(this, "user_name"));
			passLabel.setText(PluginServices.getText(this, "user_pass"));
			schemaLabel.setText(PluginServices.getText(this, "schema"));
			dbLabel.setText(PluginServices.getText(this, "data_base"));
			advCHB.setText(PluginServices.getText(this, "advanced_options"));

			DBSession dbs = DBSession.getCurrentSession();
			if (dbs!=null) {
				serverTF.setText(dbs.getServer());
				portTF.setText(Integer.toString(dbs.getPort()));
				userTF.setText(dbs.getUserName());
				dbTF.setText(dbs.getDatabase());
				schemaTF.setText(dbs.getSchema());
				advCHB.setSelected(false);
				showAdvancedProperties(false);
			} else {
				ConfigFile cf = ConfigFile.getInstance();
				serverTF.setText(cf.getServer());
				portTF.setText(cf.getPort());
				userTF.setText(cf.getUsername());
				schemaTF.setText(cf.getSchema());
				dbTF.setText(cf.getDatabase());
				boolean showAdvProp = !cf.fileExists();
				showAdvancedProperties(showAdvProp);
				advCHB.setSelected(showAdvProp);
			}

		}
		return centerPanel;
	}

	@Override
	protected JPanel getNorthPanel(ImageIcon headerImg, Color bgColor) {
		if (headerImg != null) {
			maxHeight = INIT_MAX_HEIGHT + headerImg.getIconHeight();
			minHeight = INIT_MIN_HEIGHT + headerImg.getIconHeight();
		} else {
			maxHeight = INIT_MAX_HEIGHT;
			minHeight = INIT_MIN_HEIGHT;
		}
		return super.getNorthPanel(headerImg, bgColor);
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		if (e.getSource() == advCHB) {
			showAdvancedProperties(advCHB.isSelected());
		}
	}

	private void showAdvancedProperties(boolean show) {
		int height;
		if (show) {
			height = maxHeight;
		} else {
			height = minHeight;
		}
		setHeight(height);
		advForm.setVisible(show);
	}

	@Override
	protected void onOK() {
		PluginServices.getMDIManager().setWaitCursor();
		try {
			String portS = portTF.getText().trim();
			int port = Integer.parseInt(portS);
			String server = serverTF.getText().trim();
			String username = userTF.getText().trim();
			String password = passTF.getText();
			String schema = schemaTF.getText();
			String database = dbTF.getText();

			DBSession dbc = DBSession.createConnection(server, port, database, schema, username, password);

			closeWindow();

			//save config file
			ConfigFile cf = ConfigFile.getInstance();
			cf.setProperties(server, portS, database, schema, username);
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

}
