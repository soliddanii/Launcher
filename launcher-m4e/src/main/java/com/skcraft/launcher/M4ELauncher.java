package com.skcraft.launcher;

import com.google.common.base.Supplier;
import com.skcraft.launcher.m4e.dialog.M4ELauncherFrame;
import com.skcraft.launcher.swing.SwingHelper;
import lombok.extern.java.Log;

import javax.swing.*;

import static com.skcraft.launcher.util.SharedLocale.tr;

import java.awt.*;
import java.util.logging.Level;

@Log
public class M4ELauncher {

	public static void main(final String[] args) {
		Launcher.setupLogger();

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.currentThread().setContextClassLoader(M4ELauncher.class.getClassLoader());
					UIManager.getLookAndFeelDefaults().put("ClassLoader", M4ELauncher.class.getClassLoader());

					Launcher launcher = Launcher.createFromArguments(args);
					SwingHelper.setSwingProperties(tr("launcher.appTitle", launcher.getVersion()));
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					launcher.setMainWindowSupplier(new CustomWindowSupplier(launcher));
					launcher.showLauncherWindow();
				} catch (Throwable t) {
					log.log(Level.WARNING, "Load failure", t);
					SwingHelper.showErrorDialog(null, "Uh oh! The updater couldn't be opened because a " 
							+ "problem was encountered.", "Launcher error", t);
				}
			}
		});
	}

	private static class CustomWindowSupplier implements Supplier<Window> {

		private final Launcher launcher;

		private CustomWindowSupplier(Launcher launcher) {
			this.launcher = launcher;
		}

		@Override
		public Window get() {
			return new M4ELauncherFrame(launcher);
		}
	}

}
