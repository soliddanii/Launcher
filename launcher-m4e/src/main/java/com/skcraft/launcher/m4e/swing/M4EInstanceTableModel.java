package com.skcraft.launcher.m4e.swing;

import java.awt.Image;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;

import com.skcraft.launcher.Instance;
import com.skcraft.launcher.InstanceList;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.swing.SwingHelper;
import com.skcraft.launcher.util.SharedLocale;

public class M4EInstanceTableModel extends AbstractTableModel {

	private final InstanceList instances;
	private final Icon instanceIcon;
	private final Icon customInstanceIcon;
	private final Icon downloadIcon;

	public M4EInstanceTableModel(InstanceList instances) {
		this.instances = instances;
		instanceIcon = SwingHelper.createIcon(Launcher.class, "instance_icon.png", 16, 16);
		customInstanceIcon = SwingHelper.createIcon(Launcher.class, "custom_instance_icon.png", 16, 16);
		downloadIcon = SwingHelper.createIcon(Launcher.class, "download_icon.png", 14, 14);
	}

	public void update() {
		instances.sort();
		fireTableDataChanged();
	}

	@Override
	public String getColumnName(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return "";
		case 1:
			return SharedLocale.tr("launcher.modpackColumn");
		default:
			return null;
		}
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return ImageIcon.class;
		case 1:
			return String.class;
		default:
			return null;
		}
	}

	@Override
	public void setValueAt(Object value, int rowIndex, int columnIndex) {
		switch (columnIndex) {
		case 0:
			instances.get(rowIndex).setSelected((boolean) (Boolean) value);
			break;
		case 1:
		default:
			break;
		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		switch (columnIndex) {
		case 0:
			return false;
		case 1:
			return false;
		default:
			return false;
		}
	}

	@Override
	public int getRowCount() {
		return instances.size();
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Instance instance;
		switch (columnIndex) {
		case 0:
			instance = instances.get(rowIndex);
			if (!instance.isLocal()) {
				return downloadIcon;
			} else if (instance.getManifestURL() != null) {
				if (instance.getCustomIconPath().exists()) {
					return new ImageIcon(SwingHelper.readExternalIconImage(instance.getCustomIconPath()).getScaledInstance(16, 16, Image.SCALE_SMOOTH));
				}
				return instanceIcon;
			} else {
				return customInstanceIcon;
			}
		case 1:
			instance = instances.get(rowIndex);
			return "<html>" + SwingHelper.htmlEscape(instance.getTitle()) + getAddendum(instance) + "</html>";
		default:
			return null;
		}
	}

	private String getAddendum(Instance instance) {
		if (!instance.isLocal()) {
			return " <span style=\"color: #cccccc\">" + "<br>" + SharedLocale.tr("launcher.notInstalledHint")
					+ "</span>";
		} else if (!instance.isInstalled()) {
			return " <span style=\"color: red\">" + "<br>" + SharedLocale.tr("launcher.requiresUpdateHint") + "</span>";
		} else if (instance.isUpdatePending()) {
			return " <span style=\"color: #3758DB\">" + "<br>" + SharedLocale.tr("launcher.updatePendingHint")
					+ "</span>";
		} else {
			return "";
		}
	}

}
