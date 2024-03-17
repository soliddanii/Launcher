package com.skcraft.launcher.m4e.dialog;

import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Window;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.Callable;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.skcraft.concurrency.ObservableFuture;
import com.skcraft.concurrency.ProgressObservable;
import com.skcraft.launcher.Configuration;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.auth.AuthenticationException;
import com.skcraft.launcher.auth.Session;
import com.skcraft.launcher.auth.YggdrasilLoginService;
import com.skcraft.launcher.dialog.LoginDialog.ReloginDetails;
import com.skcraft.launcher.dialog.ProgressDialog;
import com.skcraft.launcher.m4e.swing.BackgroundPanel;
import com.skcraft.launcher.m4e.swing.BlueButton;
import com.skcraft.launcher.m4e.swing.ImageButton;
import com.skcraft.launcher.m4e.swing.RedButton;
import com.skcraft.launcher.m4e.utils.M4EConstants;
import com.skcraft.launcher.m4e.utils.ResourceUtils;
import com.skcraft.launcher.persistence.Persistence;
import com.skcraft.launcher.swing.ActionListeners;
import com.skcraft.launcher.swing.LinkButton;
import com.skcraft.launcher.swing.SwingHelper;
import com.skcraft.launcher.swing.TextFieldPopupMenu;
import com.skcraft.launcher.util.SharedLocale;
import com.skcraft.launcher.util.SwingExecutor;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * The login dialog.
 */
public class M4ELoginDialog extends M4EDialog {

	private static final long serialVersionUID = 5553905344424894886L;
	
	private static final int FRAME_HEIGHT = 335;
	private static final int FRAME_WIDTH = 347;
	
	private final Launcher launcher;
	@Getter private Session session;

	private final JLabel message = new JLabel(SharedLocale.tr("login.defaultMessage"));
	private final JTextField usernameText = new JTextField();
	private final JPasswordField passwordText = new JPasswordField();


	/**
	 * Create a new login dialog.
	 *
	 * @param owner    the owner
	 * @param launcher the launcher
	 */
	public M4ELoginDialog(Window owner, @NonNull Launcher launcher, Optional<ReloginDetails> reloginDetails) {
		super(owner, ModalityType.DOCUMENT_MODAL);

		this.launcher = launcher;

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
		
		reloginDetails.ifPresent(details -> message.setText(details.getMessage()));
	}
	
	private void initComponents() {
		
		// Background (STATIC FOR NOW)
		JPanel container = createContainerPanel();
		
		Font largeFont = ResourceUtils.getFrameFont(17);
		Font smallFont = ResourceUtils.getFrameFont(15);
		Font veryLargeFont = ResourceUtils.getFrameFont(24);
		Font verySmallFont = ResourceUtils.getFrameFont(13);
		
		// Close button
		ImageButton closeButton = new ImageButton(ResourceUtils.getIcon("login_close.png"));
		closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		closeButton.setBounds(FRAME_WIDTH - 28, 8, 20, 21);
		
		message.setBounds(25, 21, FRAME_WIDTH - 50, 30);
		message.setForeground(Color.white);
		message.setFont(smallFont);
		
		JLabel nameLabel = new JLabel(SharedLocale.tr("login.idEmail"));
		nameLabel.setBounds(25, 51, FRAME_WIDTH - 50, 30);
		nameLabel.setForeground(Color.white);
		nameLabel.setFont(largeFont);
		
		usernameText.setFont(largeFont);
		usernameText.setBounds(25, nameLabel.getY() + nameLabel.getHeight(), FRAME_WIDTH - 50, 30);
		
		JLabel passLabel = new JLabel(SharedLocale.tr("login.password"));
		passLabel.setBounds(25, usernameText.getY() + usernameText.getHeight() + 5, FRAME_WIDTH - 50, 30);
		passLabel.setForeground(Color.white);
		passLabel.setFont(largeFont);
		
		passwordText.setComponentPopupMenu(TextFieldPopupMenu.INSTANCE);
		passwordText.setBounds(25, passLabel.getY() + passLabel.getHeight(), FRAME_WIDTH - 50, 30);
		passwordText.setFont(largeFont);
		passwordText.setEchoChar('*');
		
		// Login Button
		JButton loginButton = new BlueButton(SharedLocale.tr("login.login"));
		loginButton.setBounds(25, passwordText.getY() + passwordText.getHeight() + 20, FRAME_WIDTH - 50, 40);
		loginButton.setFont(ResourceUtils.getMinecraftFont(18));
		
		// Cancel Button
		JButton cancelButton = new RedButton(SharedLocale.tr("button.cancel"));
		cancelButton.setBounds(25, loginButton.getY() + loginButton.getHeight() + M4EConstants.SPACING, FRAME_WIDTH - 50, 40);
		cancelButton.setFont(ResourceUtils.getMinecraftFont(18));
		
		// Recover Button
		LinkButton recoverButton = new LinkButton(SharedLocale.tr("login.recoverAccount"));
		recoverButton.setBounds((FRAME_WIDTH / 2) - 60, cancelButton.getY() + cancelButton.getHeight() + 22, 120, 20);
		recoverButton.setForeground(Color.white);
		recoverButton.setFont(verySmallFont);
		recoverButton.setVisible(true);
		
		Container contentPane = getContentPane();
		contentPane.setLayout(null);
		
		contentPane.add(closeButton);
		contentPane.add(message);
		contentPane.add(nameLabel);
		contentPane.add(usernameText);
		contentPane.add(passLabel);
		contentPane.add(passwordText);
		contentPane.add(loginButton);
		contentPane.add(cancelButton);
		contentPane.add(recoverButton);
		contentPane.add(container);
		
		recoverButton.addActionListener(ActionListeners.openURL(recoverButton, launcher.getProperties().getProperty("resetPasswordUrl")));
		loginButton.addActionListener(e -> prepareLogin());
		cancelButton.addActionListener(e -> dispose());
		closeButton.addActionListener(e -> dispose());
	}

	private JPanel createContainerPanel() {
		return new BackgroundPanel("login_background.png", FRAME_WIDTH, FRAME_HEIGHT);
	}

	private void prepareLogin() {
		if (!usernameText.getText().isEmpty()) {
			String password = String.valueOf(passwordText.getPassword());

			if (password == null || password.isEmpty()) {
				SwingHelper.showErrorDialog(this, SharedLocale.tr("login.noPasswordError"),
						SharedLocale.tr("login.noPasswordTitle"));
			} else {
				attemptLogin(usernameText.getText(), password);
			}
		} else {
			SwingHelper.showErrorDialog(this, SharedLocale.tr("login.noLoginError"),
					SharedLocale.tr("login.noLoginTitle"));
		}
	}

	private void attemptLogin(String username, String password) {
		LoginCallable callable = new LoginCallable(username, password);
		ObservableFuture<Session> future = new ObservableFuture<Session>(launcher.getExecutor().submit(callable),
				callable);

		Futures.addCallback(future, new FutureCallback<Session>() {
			@Override
			public void onSuccess(Session result) {
				setResult(result);
			}

			@Override
			public void onFailure(Throwable t) {
			}
		}, SwingExecutor.INSTANCE);

		ProgressDialog.showProgress(this, future, SharedLocale.tr("login.loggingInTitle"),
				SharedLocale.tr("login.loggingInStatus"));
		SwingHelper.addErrorDialogCallback(this, future);
	}

	private void setResult(Session session) {
		this.session = session;
		dispose();
	}

	public static Session showLoginRequest(Window owner, Launcher launcher) {
		return showLoginRequest(owner, launcher, null);
	}

	public static Session showLoginRequest(Window owner, Launcher launcher, ReloginDetails reloginDetails) {
		M4ELoginDialog dialog = new M4ELoginDialog(owner, launcher, Optional.ofNullable(reloginDetails));
		dialog.setVisible(true);
		return dialog.getSession();
	}

	@RequiredArgsConstructor
	private class LoginCallable implements Callable<Session>, ProgressObservable {
		private final String username;
		private final String password;

		@Override
		public Session call() throws AuthenticationException, IOException, InterruptedException {
			YggdrasilLoginService service = launcher.getYggdrasil();
			Session identity = service.login(username, password);

			// The presence of the identity (profile in Mojang terms) corresponds to whether
			// the account
			// owns the game, so we need to check that
			if (identity != null) {
				// Set offline enabled flag to true
				Configuration config = launcher.getConfig();
				if (!config.isOfflineEnabled()) {
					config.setOfflineEnabled(true);
					Persistence.commitAndForget(config);
				}

				return identity;
			} else {
				throw new AuthenticationException("Minecraft not owned",
						SharedLocale.tr("login.minecraftNotOwnedError"));
			}
		}

		@Override
		public double getProgress() {
			return -1;
		}

		@Override
		public String getStatus() {
			return SharedLocale.tr("login.loggingInStatus");
		}
	}

}
