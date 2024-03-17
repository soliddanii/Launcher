package com.skcraft.launcher.m4e.dialog;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.concurrent.Callable;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.skcraft.concurrency.ObservableFuture;
import com.skcraft.concurrency.ProgressObservable;
import com.skcraft.concurrency.SettableProgress;
import com.skcraft.launcher.Configuration;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.auth.AuthenticationException;
import com.skcraft.launcher.auth.LoginService;
import com.skcraft.launcher.auth.OfflineSession;
import com.skcraft.launcher.auth.SavedSession;
import com.skcraft.launcher.auth.Session;
import com.skcraft.launcher.dialog.LoginDialog;
import com.skcraft.launcher.dialog.ProgressDialog;
import com.skcraft.launcher.m4e.swing.BackgroundPanel;
import com.skcraft.launcher.m4e.swing.BlueButton;
import com.skcraft.launcher.m4e.swing.ImageButton;
import com.skcraft.launcher.m4e.swing.LiteButton;
import com.skcraft.launcher.m4e.swing.RedButton;
import com.skcraft.launcher.m4e.utils.M4EConstants;
import com.skcraft.launcher.m4e.utils.ResourceUtils;
import com.skcraft.launcher.persistence.Persistence;
import com.skcraft.launcher.swing.SwingHelper;
import com.skcraft.launcher.util.SharedLocale;
import com.skcraft.launcher.util.SwingExecutor;

import lombok.RequiredArgsConstructor;

public class M4EAccountSelectDialog extends M4EDialog implements MouseListener, MouseMotionListener {
	private static final long serialVersionUID = 7974404018111500358L;
	
	private static final int FRAME_WIDTH = 509;
	private static final int FRAME_HEIGHT = 302;
	
	private final JList<SavedSession> accountList;

	private final Launcher launcher;
	private Session selected;

	public M4EAccountSelectDialog(Window owner, Launcher launcher) {
		super(owner, ModalityType.DOCUMENT_MODAL);

		this.launcher = launcher;
		this.accountList = new JList<>(launcher.getAccounts());
		
		// GUI SETUP
		setSize(FRAME_WIDTH, FRAME_HEIGHT);
		initComponents();
		setResizable(false);
		setBackground(M4EConstants.CHARCOAL);
		this.setUndecorated(true);
		setLocationRelativeTo(owner);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		// LISTENERS
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	private void initComponents() {
		
		Font minecraft = ResourceUtils.getMinecraftFont(12);
		
		// Background (STATIC FOR NOW)
		JPanel container = createContainerPanel();
		
		// Close button
		ImageButton closeButton = new ImageButton(ResourceUtils.getIcon("login_close.png"));
		closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		closeButton.setBounds(FRAME_WIDTH - M4EConstants.SPACING - 20, M4EConstants.SPACING, 20, 20);
		
		// Title
		JLabel title = new JLabel(SharedLocale.tr("accounts.title"));
		title.setFont(minecraft.deriveFont(14F));
		title.setBounds(M4EConstants.SPACING, M4EConstants.SPACING, 350, 20);
		title.setForeground(Color.WHITE);
		
		// Account list
		accountList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		accountList.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		accountList.setBackground(M4EConstants.CHARCOAL_LIGHT);
		accountList.setCellRenderer(new AccountRenderer());
		accountList.setLayoutOrientation(JList.VERTICAL);
		accountList.setVisibleRowCount(0);

		JScrollPane accountPane = new JScrollPane(accountList);
		accountPane.setBounds(M4EConstants.SPACING, (M4EConstants.SPACING * 2) + 20, 350, 200);
		accountPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		
		// Login buttons
		int linkWidth = FRAME_WIDTH - 350 - (M4EConstants.SPACING * 3);
		int linkHeight = (200 - (M4EConstants.SPACING * 2)) / 3;
		
		LiteButton mojangButton = new LiteButton(SharedLocale.tr("accounts.addMojang"));
		mojangButton.setFont(minecraft.deriveFont(13F));
		mojangButton.setBounds(350 + (M4EConstants.SPACING * 2), 20 + (M4EConstants.SPACING * 2), linkWidth, linkHeight);
		mojangButton.setContentAreaFilled(false);
		mojangButton.setBorderPainted(false);
		
		LiteButton microsoftButton = new LiteButton(SharedLocale.tr("accounts.addMicrosoft"));
		microsoftButton.setFont(minecraft.deriveFont(13F));
		microsoftButton.setBounds(350 + (M4EConstants.SPACING * 2), 20 + (M4EConstants.SPACING * 3) + linkHeight, linkWidth, linkHeight);
		microsoftButton.setContentAreaFilled(false);
		microsoftButton.setBorderPainted(false);
		
		LiteButton removeButton = new LiteButton(SharedLocale.tr("accounts.removeSelected"));
		removeButton.setFont(minecraft.deriveFont(13F));
		removeButton.setBounds(350 + (M4EConstants.SPACING * 2), 20 + (M4EConstants.SPACING * 4) + (linkHeight * 2), linkWidth, linkHeight);
		removeButton.setContentAreaFilled(false);
		removeButton.setBorderPainted(false);
		
		// Play buttons
		BlueButton loginButton = new BlueButton(SharedLocale.tr("accounts.play"));
		loginButton.setBounds(FRAME_WIDTH - linkWidth - M4EConstants.SPACING, 220 + (M4EConstants.SPACING * 4), linkWidth, 40);
		loginButton.setFont(minecraft.deriveFont(14F));
		loginButton.setContentAreaFilled(false);
		loginButton.setBorderPainted(false);
		
		RedButton offlineButton = new RedButton(SharedLocale.tr("login.playOffline"));
		offlineButton.setBounds(FRAME_WIDTH - (linkWidth * 2) - (M4EConstants.SPACING * 2), 220 + (M4EConstants.SPACING * 4), linkWidth, 40);
		offlineButton.setEnabled(launcher.getConfig().isOfflineEnabled());
		offlineButton.setFont(minecraft.deriveFont(14F));
		offlineButton.setContentAreaFilled(false);
		offlineButton.setBorderPainted(false);
		
		
		Container contentPane = getContentPane();
		contentPane.setLayout(null);

		contentPane.add(title);
		contentPane.add(closeButton);
		contentPane.add(accountPane);
		contentPane.add(mojangButton);
		contentPane.add(microsoftButton);
		contentPane.add(removeButton);
		contentPane.add(loginButton);
		contentPane.add(offlineButton);
		contentPane.add(container);
		
		
		closeButton.addActionListener(e -> dispose());
		
		loginButton.addActionListener(ev -> attemptExistingLogin(accountList.getSelectedValue()));
		
		offlineButton.addActionListener(ev -> setResult(new OfflineSession(launcher.getConfig().getOfflineName())));
		
		mojangButton.addActionListener(ev -> {
			Session newSession = M4ELoginDialog.showLoginRequest(this, launcher);
			
			if (newSession != null) {
				launcher.getAccounts().update(newSession.toSavedSession());
				setResult(newSession);
			}
		});
		
		microsoftButton.addActionListener(ev -> attemptMicrosoftLogin());
		
		removeButton.addActionListener(ev -> {
			if (accountList.getSelectedValue() != null) {
				boolean confirmed = SwingHelper.confirmDialog(this, SharedLocale.tr("accounts.confirmForget"), SharedLocale.tr("accounts.confirmForgetTitle"));
				
				if (confirmed) {
					launcher.getAccounts().remove(accountList.getSelectedValue());
				}
			}
		});
		
		accountList.setSelectedIndex(0);
	}

	private JPanel createContainerPanel() {
		return new BackgroundPanel("optionsBackground.png", FRAME_WIDTH, FRAME_HEIGHT);
	}
	
	@Override
	public void dispose() {
		accountList.setModel(new DefaultListModel<>());
		super.dispose();
	}

	public static Session showAccountRequest(Window owner, Launcher launcher) {
		M4EAccountSelectDialog dialog = new M4EAccountSelectDialog(owner, launcher);
		dialog.setVisible(true);

		if (dialog.selected != null && dialog.selected.isOnline()) {
			launcher.getAccounts().update(dialog.selected.toSavedSession());
		}

		Persistence.commitAndForget(launcher.getAccounts());

		return dialog.selected;
	}

	private void setResult(Session result) {
		this.selected = result;
		dispose();
	}

	private void attemptMicrosoftLogin() {
		String status = SharedLocale.tr("login.microsoft.seeBrowser");
		SettableProgress progress = new SettableProgress(status, -1);

		ListenableFuture<?> future = launcher.getExecutor().submit(() -> {
			Session newSession = launcher.getMicrosoftLogin().login(() ->
					progress.set(SharedLocale.tr("login.loggingInStatus"), -1));

			if (newSession != null) {
				launcher.getAccounts().update(newSession.toSavedSession());
				setResult(newSession);

				// Set offline enabled flag to true
				Configuration config = launcher.getConfig();
				if (!config.isOfflineEnabled()) {
					config.setOfflineEnabled(true);
					Persistence.commitAndForget(config);
				}
			}

			return null;
		});

		ProgressDialog.showProgress(this, future, progress,
				SharedLocale.tr("login.loggingInTitle"), status);
		SwingHelper.addErrorDialogCallback(this, future);
	}

	private void attemptExistingLogin(SavedSession session) {
		if (session == null) return;

		LoginService loginService = launcher.getLoginService(session.getType());
		RestoreSessionCallable callable = new RestoreSessionCallable(loginService, session);

		ObservableFuture<Session> future = new ObservableFuture<>(launcher.getExecutor().submit(callable), callable);
		Futures.addCallback(future, new FutureCallback<Session>() {
			@Override
			public void onSuccess(Session result) {
				setResult(result);
			}

			@Override
			public void onFailure(Throwable t) {
				if (t instanceof AuthenticationException) {
					if (((AuthenticationException) t).isInvalidatedSession()) {
						// Just need to log in again
						LoginDialog.ReloginDetails details = new LoginDialog.ReloginDetails(session.getUsername(),
								SharedLocale.tr("login.relogin", t.getLocalizedMessage()));
						Session newSession = M4ELoginDialog.showLoginRequest(M4EAccountSelectDialog.this, launcher, details);

						setResult(newSession);
					}
				} else {
					SwingHelper.showErrorDialog(M4EAccountSelectDialog.this, t.getLocalizedMessage(), SharedLocale.tr("errorTitle"), t);
				}
			}
		}, SwingExecutor.INSTANCE);

		ProgressDialog.showProgress(this, future, SharedLocale.tr("login.loggingInTitle"),
				SharedLocale.tr("login.loggingInStatus"));
	}

	@RequiredArgsConstructor
	private static class RestoreSessionCallable implements Callable<Session>, ProgressObservable {
		private final LoginService service;
		private final SavedSession session;

		@Override
		public Session call() throws Exception {
			return service.restore(session);
		}

		@Override
		public String getStatus() {
			return SharedLocale.tr("accounts.refreshingStatus");
		}

		@Override
		public double getProgress() {
			return -1;
		}
	}

	private static class AccountRenderer extends JLabel implements ListCellRenderer<SavedSession> {
		public AccountRenderer() {
			setFont(ResourceUtils.getMinecraftFont(14));
			setHorizontalAlignment(LEFT);
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends SavedSession> list, SavedSession value, int index, boolean isSelected, boolean cellHasFocus) {
			setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, M4EConstants.CHARCOAL_LIGHT));
			setText(value.getUsername());
			setIconTextGap(10);
			if (value.getAvatarImage() != null) {
				setIcon(new ImageIcon(value.getAvatarImage()));
			} else {
				setIcon(SwingHelper.createIcon(Launcher.class, "default_skin.png", 32, 32));
			}
			
			if (isSelected) {
				setOpaque(true);
				setBackground(M4EConstants.LIGHT);
				setForeground(M4EConstants.CHARCOAL);
			} else {
				setOpaque(false);
				setForeground(M4EConstants.LIGHT);
			}

			return this;
		}
	}
}
