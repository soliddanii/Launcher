package com.skcraft.launcher.m4e.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

import com.skcraft.launcher.m4e.utils.ResourceUtils;
import com.skcraft.launcher.swing.InstanceTable;

public class M4EInstanceTable extends InstanceTable {

	private static final long serialVersionUID = -6882726039278277341L;

	public M4EInstanceTable() {
		super();
		// Row height
		setRowHeight(Math.max(getRowHeight() + 4, 38));
		// Background color
		setSelectionBackground(new Color(216,216,216,100));
		// Font
		setFont(ResourceUtils.getMinecraftFont(12));
		//Set no border cell renderer for string cells
		setDefaultRenderer(String.class, new ProxyCellRenderer(getDefaultRenderer(String.class)));
		//Set no border cell renderer for imageicon cells
		setDefaultRenderer(ImageIcon.class, new ProxyCellRenderer(getDefaultRenderer(ImageIcon.class))); 
		// Opacity
		setOpaque(false);
	}
	
	public M4EInstanceTable(Color color) {
		this();
		this.setBackground(color);
		this.setForeground(Color.WHITE);
	}
	
	@Override
	public void paintComponent(Graphics g) {
		Color controlColor = new Color(230, 240, 230, 100);
		Graphics2D g2 = (Graphics2D) g;
		for (int row = 0; row < getRowCount(); row++) {
			if (isRowSelected(row)) {
				Rectangle start = getCellRect(row, 0, true);
				Rectangle end = getCellRect(row, getColumnCount() - 1, true);
				g2.setPaint(new GradientPaint(start.x, 0, controlColor, (int) ((end.x + end.width - start.x) * 1.25), 0,
						new Color(68, 85, 218, 100)));
				g2.fillRect(start.x, start.y, end.x + end.width - start.x, start.height);
			} else {
				Rectangle start = getCellRect(row, 0, true);
				Rectangle end = getCellRect(row, getColumnCount() - 1, true);
				g2.setColor(new Color(0, 0, 0, 0));
				g2.fillRect(start.x, start.y, end.x + end.width - start.x, start.height);
			}
		}
		super.paintComponent(g);
	}
	
}

class ProxyCellRenderer implements TableCellRenderer {

	protected static final Border DEFAULT_BORDER = new EmptyBorder(1, 1, 1, 1);
	private TableCellRenderer renderer;

	public ProxyCellRenderer(TableCellRenderer renderer) {
		this.renderer = renderer;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		Component comp = renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		if (comp instanceof JComponent) {
			((JComponent) comp).setBorder(DEFAULT_BORDER);
		}
		return comp;
	}
}
