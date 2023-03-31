package com.skcraft.launcher;

import static com.skcraft.launcher.util.SharedLocale.tr;

import java.awt.Window;
import java.util.Date;

import javax.swing.SwingUtilities;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.skcraft.concurrency.ObservableFuture;
import com.skcraft.launcher.auth.Session;
import com.skcraft.launcher.dialog.ProgressDialog;
import com.skcraft.launcher.launch.LaunchListener;
import com.skcraft.launcher.launch.LaunchOptions;
import com.skcraft.launcher.launch.LaunchOptions.UpdatePolicy;
import com.skcraft.launcher.launch.LaunchSupervisor;
import com.skcraft.launcher.m4e.dialog.M4EAccountSelectDialog;
import com.skcraft.launcher.persistence.Persistence;
import com.skcraft.launcher.swing.SwingHelper;
import com.skcraft.launcher.update.Updater;
import com.skcraft.launcher.util.SharedLocale;
import com.skcraft.launcher.util.SwingExecutor;

public class M4ELaunchSupervisor extends LaunchSupervisor {

	public M4ELaunchSupervisor(Launcher launcher) {
		super(launcher);
	}

	@Override
	public void launch(LaunchOptions options) {
		final Window window = options.getWindow();
		final Instance instance = options.getInstance();
		final LaunchListener listener = options.getListener();

		try {
			boolean update = options.getUpdatePolicy().isUpdateEnabled() && instance.isUpdatePending();

			// Store last access date
			Date now = new Date();
			instance.setLastAccessed(now);
			Persistence.commitAndForget(instance);

			// Perform login
			final Session session;
			if (options.getSession() != null) {
				session = options.getSession();
			} else {
				session = M4EAccountSelectDialog.showAccountRequest(window, launcher);
				if (session == null) {
					return;
				}
			}

			// If we have to update, we have to update
			if (!instance.isInstalled()) {
				update = true;
			}

			if (update) {
				// Execute the updater
				Updater updater = new Updater(launcher, instance);
				updater.setOnline(options.getUpdatePolicy() == UpdatePolicy.ALWAYS_UPDATE || session.isOnline());
				ObservableFuture<Instance> future = new ObservableFuture<Instance>(
						launcher.getExecutor().submit(updater), updater);

				// Show progress
				ProgressDialog.showProgress(window, future, SharedLocale.tr("launcher.updatingTitle"),
						tr("launcher.updatingStatus", instance.getTitle()));
				SwingHelper.addErrorDialogCallback(window, future);

				// Update the list of instances after updating
				future.addListener(new Runnable() {
					@Override
					public void run() {
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								listener.instancesUpdated();
							}
						});
					}
				}, SwingExecutor.INSTANCE);

				// On success, launch also
				Futures.addCallback(future, new FutureCallback<Instance>() {
					@Override
					public void onSuccess(Instance result) {
						launch(window, instance, session, listener);
					}

					@Override
					public void onFailure(Throwable t) {
					}
				}, SwingExecutor.INSTANCE);
			} else {
				launch(window, instance, session, listener);
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			SwingHelper.showErrorDialog(window, SharedLocale.tr("launcher.noInstanceError"),
					SharedLocale.tr("launcher.noInstanceTitle"));
		}
	}
}
