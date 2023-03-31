/*
 * This file is part of Technic Launcher.
 * Copyright (C) 2013 Syndicate, LLC
 *
 * Technic Launcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Technic Launcher is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Technic Launcher.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.skcraft.launcher.m4e.swing;

import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.JButton;

import com.skcraft.launcher.swing.ActionListeners;


public class ImageHyperlinkButton extends JButton {

	private static final long serialVersionUID = 8518935148076492525L;

	public ImageHyperlinkButton(String url) {
		try {
			this.addActionListener(ActionListeners.openURL(this, (new URL(url).toString())));
		} catch (MalformedURLException ex) {
			ex.printStackTrace(System.err);
		}
		setBorder(null);
		setOpaque(false);
		setFocusable(false);
		setContentAreaFilled(false);
	}
}
