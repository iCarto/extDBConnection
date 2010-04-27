package es.udc.cartolab.gvsig.users.preferences;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.iver.andami.Launcher;
import com.iver.andami.PluginServices;
import com.iver.andami.preferences.AbstractPreferencePage;
import com.iver.andami.preferences.StoreException;
import com.iver.utiles.XMLEntity;
import com.jeta.forms.components.panel.FormPanel;

public class EielPage extends AbstractPreferencePage implements ActionListener {

	/* key names */
	public static final String DEFAULT_LEGEND_DIR_KEY_NAME = "LegendDir";
	public static final String CONNECT_DB_AT_STARTUP_KEY_NAME = "ConnectAtStartup";

	/* default values */
	private static final String DEFAULT_LEGEND_DIR = Launcher.getAppHomeDir();
	private static final boolean CONNECT_DB_AT_STARTUP = false;



	protected String id;
	private ImageIcon icon;
	private JTextField legendDirField;
	private JButton legendDirButton;
	private JCheckBox connectDBCB;

	private boolean panelStarted;

	/**
	 * Creates a new panel containing the EIEL preferences settings.
	 *
	 */
	public EielPage() {
		super();
		id = this.getClass().getName();
		icon = new ImageIcon(this.getClass().getClassLoader().getResource("images/logo.png"));
		panelStarted = false;
	}


	@Override
	public void setChangesApplied() {
		// TODO Auto-generated method stub
		setChanged(false);
	}

	@Override
	public void storeValues() throws StoreException {
		// TODO Auto-generated method stub
		PluginServices ps = PluginServices.getPluginServices(this);
		XMLEntity xml = ps.getPersistentXML();
		String legendDir = legendDirField.getText();
		File f = new File(legendDir);
		if (f.exists() && f.isDirectory() && f.canRead()) {
			xml.putProperty(DEFAULT_LEGEND_DIR_KEY_NAME,
					legendDir);
		} else {
			String message = String.format("%s no es un directorio válido", legendDir);
			throw new StoreException(message);
		}
		xml.putProperty(CONNECT_DB_AT_STARTUP_KEY_NAME, connectDBCB.isSelected());
	}

	public String getID() {
		// TODO Auto-generated method stub
		return id;
	}

	public ImageIcon getIcon() {
		// TODO Auto-generated method stub
		return icon;
	}

	public JPanel getPanel() {

		if (!panelStarted) {
			panelStarted = true;

			//			panel = new JPanel();

			FormPanel form = new FormPanel("forms/preferences.jfrm");
			form.setFocusTraversalPolicyProvider(true);

			connectDBCB = form.getCheckBox("connectDBCB");
			connectDBCB.setText(PluginServices.getText(this, "connect_startup"));

			legendDirField = form.getTextField("legendField");
			legendDirButton = (JButton) form.getComponentByName("legendButton");

			JLabel legendLabel = form.getLabel("legendLabel");
			legendLabel.setText(PluginServices.getText(this, "legend_directory"));

			legendDirButton.addActionListener(this);

			addComponent(form);
		}

		return this;
	}

	public JTextField getLegendDirField () {
		return this.legendDirField;
	}
	
	public String getTitle() {
		// TODO Auto-generated method stub
		return PluginServices.getText(this, "EIEL");
	}

	public void initializeDefaults() {
		// TODO Auto-generated method stub
		legendDirField.setText(DEFAULT_LEGEND_DIR);
		connectDBCB.setSelected(CONNECT_DB_AT_STARTUP);
	}

	public void initializeValues() {
		// TODO Auto-generated method stub
		if (!panelStarted) {
			getPanel();
		}

		PluginServices ps = PluginServices.getPluginServices(this);
		XMLEntity xml = ps.getPersistentXML();

		// Default Projection
		String legendDir = null;
		if (xml.contains(DEFAULT_LEGEND_DIR_KEY_NAME)) {
			legendDir = xml.getStringProperty(DEFAULT_LEGEND_DIR_KEY_NAME);
		} else {
			legendDir = DEFAULT_LEGEND_DIR;
		}

		legendDirField.setText(legendDir);

		if (xml.contains(CONNECT_DB_AT_STARTUP_KEY_NAME)) {
			connectDBCB.setSelected(xml.getBooleanProperty(CONNECT_DB_AT_STARTUP_KEY_NAME));
		} else {
			connectDBCB.setSelected(CONNECT_DB_AT_STARTUP);
		}
	}

	public boolean isValueChanged() {
		// TODO Auto-generated method stub
		return super.hasChanged();
	}


	public void actionPerformed(ActionEvent event) {
		// TODO Auto-generated method stub
		if (event.getSource()==legendDirButton) {
			File currentDirectory = new File(legendDirField.getText());
			JFileChooser chooser;
			if (!(currentDirectory.exists() &&
					currentDirectory.isDirectory() &&
					currentDirectory.canRead())) {
				currentDirectory = new File(DEFAULT_LEGEND_DIR);
			}
			chooser = new JFileChooser(currentDirectory);

			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int returnVal = chooser.showOpenDialog(legendDirField);
			if(returnVal == JFileChooser.APPROVE_OPTION) {
				legendDirField.setText(chooser.getSelectedFile().getAbsolutePath());
			}
		}
	}

}
