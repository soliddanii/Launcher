package com.skcraft.launcher.m4e.swing;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JTextPane;

public class HyperlinkJTextPane extends JTextPane implements MouseListener {
	private static final long serialVersionUID = -7255565605484566071L;
	private static final long CLICK_DELAY = 250L;
	
	private long lastClick = System.currentTimeMillis();
	private String url;

	public HyperlinkJTextPane(String text, String url) {
		this.setText(text);
		this.url = url;
		super.addMouseListener(this);
		this.setHighlighter(null);
		this.setCaretColor(new Color(255, 255, 255, 0));
		setOpaque(false);
	}

	@Override
	protected void paintComponent(Graphics g) {
		g.setColor(getBackground());
		Insets insets = getInsets();
		int x = insets.left;
		int y = insets.top;
		int width = getWidth() - (insets.left + insets.right);
		int height = getHeight() - (insets.top + insets.bottom);
		g.fillRect(x, y, width, height);
		super.paintComponent(g);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (lastClick + CLICK_DELAY > System.currentTimeMillis()) {
			System.out.println("click");
			return;
		}
		lastClick = System.currentTimeMillis();
		browse(url);
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}
	
	public static void browse(String url) {
		try {
			URI uri = new java.net.URI(url);
			Desktop.getDesktop().browse(uri);
		} catch (IOException ex) {
			ex.printStackTrace(System.err);
		} catch (URISyntaxException e) {
			e.printStackTrace(System.err);
		}
	}
}
