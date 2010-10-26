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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.WindowInfo;

public abstract class AbstractGVWindow extends JPanel implements IWindow, ActionListener {

	private int height, width;
	WindowInfo viewInfo = null;
	JPanel northPanel = null;
	JPanel southPanel = null;
	private JButton okButton;
	private JButton cancelButton;
	private String title = "Abstract window";

	public AbstractGVWindow(int width, int height) {
		this(width, height, null, null);
	}

	public AbstractGVWindow(int width, int height, ImageIcon headerImg, Color headerBgColor) {

		this.height = height;
		this.width = width;
		getWindowInfo(); //to avoid a NullPointerException

		GridBagLayout layout = new GridBagLayout();
		setLayout(layout);

		add(getNorthPanel(headerImg, headerBgColor), new GridBagConstraints(0, 0, 1, 1, 0, 0,
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

	public WindowInfo getWindowInfo() {
		// TODO Auto-generated method stub
		if (viewInfo == null) {
			viewInfo = new WindowInfo(WindowInfo.MODELESSDIALOG | WindowInfo.PALETTE);
			viewInfo.setTitle(title);
			viewInfo.setWidth(width);
			viewInfo.setHeight(height);
		}
		return viewInfo;
	}

	public Object getWindowProfile() {
		// TODO Auto-generated method stub
		return null;
	}

	protected JPanel getNorthPanel(ImageIcon headerImg, Color bgColor) {

		if (northPanel == null) {
			northPanel = new JPanel();
			//Set header if any
			if (headerImg != null && bgColor != null) {
				northPanel.setBackground(bgColor);
				JLabel icon = new JLabel();
				icon.setIcon(headerImg);
				northPanel.add(icon, BorderLayout.WEST);
				setHeight(height + headerImg.getIconHeight());
				if (headerImg.getIconWidth() > width) {
					setWidth(headerImg.getIconWidth());
				}
			}
		}
		return northPanel;
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

	public void closeWindow() {
		PluginServices.getMDIManager().closeWindow(this);
	}

	public void openWindow() {
		PluginServices.getMDIManager().addCentredWindow(this);
		getRootPane().setDefaultButton(okButton);
		getRootPane().setFocusTraversalPolicyProvider(true);
		getDefaultFocusComponent().requestFocusInWindow();
	}

	protected abstract JPanel getCenterPanel();

	protected abstract void onOK();

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == okButton) {
			onOK();
		}
		if (e.getSource() == cancelButton) {
			closeWindow();
		}
	}

	protected void setHeight(int height) {
		viewInfo.setHeight(height);
		this.height = height;
	}

	protected void setWidth(int width) {
		viewInfo.setWidth(width);
		this.width = width;
	}

	public void setTitle(String title) {
		this.title = title;
		viewInfo.setTitle(title);
	}

	protected abstract Component getDefaultFocusComponent();
}
