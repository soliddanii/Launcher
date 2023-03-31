package com.skcraft.launcher.m4e.swing;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class LiteButton extends JButton implements MouseListener {

	private static final long serialVersionUID = -4843665311305388826L;
	private boolean clicked = false;
	private boolean hovering = false;
	private boolean enabled = true;

	private Color disabledBackColor;
	private Color disabledForeColor;
	private Color unclickedBackColor;
	private Color unclickedForeColor;
	private Color clickedBackColor;
	private Color clickedForeColor;
	private Color hoverBackColor;
	private Color hoverForeColor;
	
	private JLabel label = null;

	public LiteButton(String label) {
		this(label, new Color(220, 220, 220), Color.black, new Color(220, 220, 220), Color.black, new Color(220, 220, 220), Color.black);
	}

	public LiteButton(String label, Color unclickedBackColor, Color clickedBackColor, Color hoverBackColor,
			Color unclickedForeColor, Color clickedForeColor, Color hoverForeColor) {

		this(label, unclickedBackColor, clickedBackColor, hoverBackColor, unclickedForeColor, clickedForeColor,
				hoverForeColor, new Color(190, 190, 190), new Color(122, 122, 122));
		// grey and darker grey for unespecified disabled colors
	}

	public LiteButton(String label, Color unclickedBackColor, Color clickedBackColor, Color hoverBackColor,
			Color unclickedForeColor, Color clickedForeColor, Color hoverForeColor, Color disabledBackColor,
			Color disabledForeColor) {

		this.unclickedBackColor = unclickedBackColor;
		this.clickedBackColor = clickedBackColor;
		this.hoverBackColor = hoverBackColor;
		this.unclickedForeColor = unclickedForeColor;
		this.clickedForeColor = clickedForeColor;
		this.hoverForeColor = hoverForeColor;
		this.disabledBackColor = disabledBackColor; // Grey
		this.disabledForeColor = disabledForeColor; // Darker Grey

		this.setText(label);
		this.setBackground(this.unclickedBackColor);
		this.setBorder(new LiteBorder(5, getBackground()));
		this.addMouseListener(this);

		if (enabled) {
			setCursor(new Cursor(Cursor.HAND_CURSOR));
		}

	}

	@Override
	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		Color old = g2d.getColor();
		// Draw box
		g2d.setColor(!enabled ? this.disabledBackColor : (clicked ? this.clickedBackColor : (hovering ? this.hoverBackColor : this.unclickedBackColor)));
		g2d.fillRect(0, 0, getWidth(), getHeight());
		g2d.setColor(old);
		
		// Draw label
		paintHtmlString(g2d, getText(), getWidth(), getHeight());
	}
	
	private void paintHtmlString(Graphics g, String text, int width, int height) {
		getJLabel().setText("<html><center>" + text + "</center></html>");
		getJLabel().setForeground(!enabled ? this.disabledForeColor : (clicked ? this.clickedForeColor : (hovering ? this.hoverForeColor : this.unclickedForeColor)));
		getJLabel().setSize(width, height);
		getJLabel().setFont(getFont());
		getJLabel().paint(g);
	}
	
	private JLabel getJLabel() {
		if (label == null) {
			label = new JLabel();
			label.setHorizontalAlignment(SwingConstants.CENTER);
			label.setVerticalAlignment(SwingConstants.CENTER);
		}
		return label;
	}


	@Override
	public void mouseClicked(MouseEvent e) {
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		clicked = true;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		clicked = false;
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		hovering = true;
	}

	@Override
	public void mouseExited(MouseEvent e) {
		hovering = false;
	}

	@Override
	public void setEnabled(boolean b) {
		super.setEnabled(b);
		enabled = b;
	}
}
