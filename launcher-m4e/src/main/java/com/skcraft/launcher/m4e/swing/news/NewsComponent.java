
package com.skcraft.launcher.m4e.swing.news;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.net.URL;

import javax.swing.JComponent;

import org.apache.commons.lang.StringUtils;

import com.skcraft.launcher.m4e.swing.HyperlinkJTextPane;
import com.skcraft.launcher.m4e.swing.SquareBox;
import com.skcraft.launcher.m4e.utils.M4EConstants;
import com.skcraft.launcher.m4e.utils.ResourceUtils;

public class NewsComponent extends JComponent {

	private static final long serialVersionUID = -6887891346187363121L;

	public NewsComponent(final URL url) {
		GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(ResourceUtils.getMinecraftFont(10));
		new Thread("News Thread") {
			@Override
			public void run() {
				loadArticles(url);
			}
		}.start();
	}

	private void loadArticles(URL url) {
		Font articleFont = ResourceUtils.getMinecraftFont(12);
		int width = getWidth() - 16;
		int height = getHeight() / 2 - 16;

		Feed feed = new Feed(url, 2);

		for (int i = 0; i < 2; i++) {
			Article article = feed.getArticle(i);
			String date = article.getDate();
			String title = article.getTitle();
			HyperlinkJTextPane link = new HyperlinkJTextPane(StringUtils.capitalize(date) + "\n" + title, article.getLink()); // article.getUrl()
			link.setFont(articleFont);
			link.setForeground(Color.WHITE);
			link.setBackground(new Color(255, 255, 255, 0));
			link.setBounds(8, 8 + ((height + 8) * i), width, height);
			link.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			this.add(link);
		}

		SquareBox background = new SquareBox(M4EConstants.TRANSPARENT);
		background.setBounds(0, 0, getWidth(), getHeight());
		this.add(background);
		this.repaint();
	}

}
