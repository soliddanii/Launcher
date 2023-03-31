package com.skcraft.launcher.m4e.swing;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
//import java.awt.geom.RoundRectangle2D;

import javax.swing.JComponent;

public class SquareBox extends JComponent {
	private static final long serialVersionUID = -5398986496635307728L;

	public SquareBox(Color color) {
		this.setBackground(color);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		Graphics2D g2d = (Graphics2D) g.create();
		Rectangle2D rect = new Rectangle2D.Float(0, 0, getWidth(), getHeight());
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setColor(this.getBackground());
		g2d.fill(rect);
		g2d.dispose();
	}
}
