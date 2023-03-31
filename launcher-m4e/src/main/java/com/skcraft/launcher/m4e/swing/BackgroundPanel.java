package com.skcraft.launcher.m4e.swing;

import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;

import com.skcraft.launcher.m4e.utils.ResourceUtils;

public class BackgroundPanel extends JPanel {

	private static final long serialVersionUID = -6905754207686885148L;
	
	private Image background;

	public BackgroundPanel(String image, int width, int height) {
		this.setBounds(0, 0, width, height);
		background = ResourceUtils.getImage(image);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (background != null) {
			g.drawImage(background, 0, 0, getWidth(), getHeight(), null);
		}
	}

}
