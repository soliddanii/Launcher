package com.skcraft.launcher.m4e.dialog;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;

import com.google.common.base.Strings;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.skcraft.concurrency.ObservableFuture;
import com.skcraft.concurrency.ProgressObservable;
import com.skcraft.launcher.Configuration;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.auth.AuthenticationException;
import com.skcraft.launcher.auth.LoginService;
import com.skcraft.launcher.auth.OfflineSession;
import com.skcraft.launcher.auth.Session;
import com.skcraft.launcher.dialog.ProgressDialog;
import com.skcraft.launcher.dialog.LoginDialog.ReloginDetails;
import com.skcraft.launcher.m4e.swing.BlueButton;
import com.skcraft.launcher.m4e.swing.ImageButton;
import com.skcraft.launcher.m4e.swing.RedButton;
import com.skcraft.launcher.m4e.utils.M4EConstants;
import com.skcraft.launcher.m4e.utils.ResourceUtils;
import com.skcraft.launcher.persistence.Persistence;
import com.skcraft.launcher.swing.ActionListeners;
import com.skcraft.launcher.swing.LinkButton;
import com.skcraft.launcher.swing.PopupMouseAdapter;
import com.skcraft.launcher.swing.SwingHelper;
import com.skcraft.launcher.swing.TextFieldPopupMenu;
import com.skcraft.launcher.util.SharedLocale;
import com.skcraft.launcher.util.SwingExecutor;

import lombok.Getter;
import lombok.NonNull;

/**
 * The login dialog.
 */
public class M4ELoginDialog extends M4EDialog implements MouseListener, MouseMotionListener {

	private static final long serialVersionUID = 5553905344424894886L;
	
	private static final int FRAME_HEIGHT = 409;
	private static final int FRAME_WIDTH = 347;
	
	private final Launcher launcher;
	@Getter private Session session;

    private final JComboBox idCombo = new JComboBox(); //campo usuario
    private final JPasswordField passwordText = new JPasswordField(); //campo contraseña
    private final JCheckBox rememberIdCheck = new JCheckBox(SharedLocale.tr("login.rememberId")); //checkbox recordar usuario
    private final JCheckBox rememberPassCheck = new JCheckBox(SharedLocale.tr("login.rememberPassword")); //checkbox recordar contraseña
    private final JButton  loginButton = new BlueButton(SharedLocale.tr("login.login")); //boton de login
    private final JButton offlineButton = new RedButton(SharedLocale.tr("login.playOffline")); // boton de jugar offline
    private final LinkButton recoverButton = new LinkButton(SharedLocale.tr("login.recoverAccount")); //boton de recuperar contraseña
    

    
    private JLabel instructionText;
    private JLabel nameLabel;
    private JLabel passLabel;
    private JLabel background;
    private ImageButton closeButton;

	/**
	 * Create a new login dialog.
	 *
	 * @param owner    the owner
	 * @param launcher the launcher
	 */
	public M4ELoginDialog(Window owner, @NonNull Launcher launcher, Optional<ReloginDetails> reloginDetails) {
		super(owner, ModalityType.DOCUMENT_MODAL);

		this.launcher = launcher;
		//this.accounts = launcher.getAccounts();

		// GUI SETUP
		setSize(FRAME_WIDTH, FRAME_HEIGHT);
		//initComponents();
		setResizable(false);
		setBackground(M4EConstants.CHARCOAL);
		this.setUndecorated(true);
		setLocationRelativeTo(owner);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		// LISTENERS
		addMouseListener(this);
		addMouseMotionListener(this);

		/*
		 * setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		 * addWindowListener(new WindowAdapter() {
		 * 
		 * @Override public void windowClosing(WindowEvent event) { removeListeners();
		 * dispose(); } });
		 */
		
		//reloginDetails.ifPresent(details -> message.setText(details.message));
	}
/*
    @SuppressWarnings("unchecked")
    private void removeListeners() {
        idCombo.setModel(new DefaultComboBoxModel());
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {
        
        idCombo.setModel(getAccounts());
        updateSelection();
        
        Font largeFont = ResourceUtils.getFrameFont(17);
        Font smallFont = ResourceUtils.getFrameFont(15);
        Font veryLargeFont = ResourceUtils.getFrameFont(24);
        Font verySmallFont = ResourceUtils.getFrameFont(13);

        Container contentPane = getContentPane();
	contentPane.setLayout(null);
      
        instructionText = new JLabel(SharedLocale.tr("login.instructionText"));
        instructionText.setFont(smallFont);
        instructionText.setBounds(25, 21, FRAME_WIDTH - 50, 30);
        instructionText.setForeground(Color.white);

        nameLabel = new JLabel(SharedLocale.tr("login.idPassword"));
        nameLabel.setFont(largeFont);
        nameLabel.setBounds(25, 51, FRAME_WIDTH - 60, 30);
        nameLabel.setForeground(Color.white);
        
        idCombo.setBounds(25, nameLabel.getY()+nameLabel.getHeight(), 297, 32);
	idCombo.setFont(largeFont);
	idCombo.setEditable(true);
        idCombo.getEditor().selectAll();

        passLabel = new JLabel(SharedLocale.tr("login.password"));
        passLabel.setFont(largeFont);
        passLabel.setBounds(25, idCombo.getY()+idCombo.getHeight()+5, FRAME_WIDTH - 60, 30);
        passLabel.setForeground(Color.white);
        
        passwordText.setComponentPopupMenu(TextFieldPopupMenu.INSTANCE);
        passwordText.setBounds(25, passLabel.getY()+passLabel.getHeight(), 297, 30);
	passwordText.setFont(largeFont);
        passwordText.setEchoChar('*');
        
         //Remember id check
        rememberIdCheck.setFont(smallFont);
        rememberIdCheck.setForeground(Color.white);
        rememberIdCheck.setOpaque(false);
        rememberIdCheck.setBounds(25, 186, 300, 30);
        rememberIdCheck.setHorizontalTextPosition(SwingConstants.LEFT);
        rememberIdCheck.setHorizontalAlignment(SwingConstants.RIGHT);
        rememberIdCheck.setIconTextGap(12);
        
        //Remember pass check
        rememberPassCheck.setFont(smallFont);
        rememberPassCheck.setForeground(Color.white);
        rememberPassCheck.setOpaque(false);
        rememberPassCheck.setBounds(25, rememberIdCheck.getY()+rememberIdCheck.getHeight()+10, 300, 30);
        rememberPassCheck.setHorizontalTextPosition(SwingConstants.LEFT);
        rememberPassCheck.setHorizontalAlignment(SwingConstants.RIGHT);
        rememberPassCheck.setIconTextGap(12);
        
        //Login Button
        loginButton.setFont(veryLargeFont);
        loginButton.setBounds(25, rememberPassCheck.getY()+rememberPassCheck.getHeight()+10, FRAME_WIDTH - 50, 40);
        
        //Offline Button
        offlineButton.setFont(veryLargeFont);
        offlineButton.setBounds(25, loginButton.getY()+loginButton.getHeight()+10, FRAME_WIDTH - 50, 40);

        // Recover Button
	recoverButton.setFont(verySmallFont);
	recoverButton.setBounds((FRAME_WIDTH / 2) - 60, offlineButton.getY()+offlineButton.getHeight()+20, 120, 20);
        recoverButton.setVisible(true);
        
        //Close button
        closeButton = new ImageButton(ResourceUtils.getIcon("login_close.png"));
        closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeButton.setBounds(FRAME_WIDTH - 28, 8, 20, 21);
                
        background = new JLabel();
        background.setBounds(0, 0, FRAME_WIDTH, FRAME_HEIGHT);
        
        ResourceUtils.setIcon(background, "login_background.png", background.getWidth(), background.getHeight());



        //getRootPane().setDefaultButton(loginButton);
        //SwingUtilities.getRootPane(this).setDefaultButton(loginButton);

        idCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateSelection();
            }
        });

        idCombo.getEditor().getEditorComponent().addMouseListener(new PopupMouseAdapter() {
            @Override
            protected void showPopup(MouseEvent e) {
                popupManageMenu(e.getComponent(), e.getX(), e.getY());
            }
        });

        recoverButton.addActionListener(ActionListeners.openURL(recoverButton, launcher.getProperties().getProperty("resetPasswordUrl")));

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                prepareLogin();
            }
        });

        offlineButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //setResult(new OfflineSession(launcher.getProperties().getProperty("offlinePlayerName")));
                setResult(new OfflineSession(launcher.getConfig().getOfflineName()));
                removeListeners();
                dispose();
            }
        });

        rememberPassCheck.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (rememberPassCheck.isSelected()) {
                    rememberIdCheck.setSelected(true);
                }
            }
        });

        rememberPassCheck.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (rememberPassCheck.isSelected()) {
                    rememberIdCheck.setSelected(true);
                }
            }
        });
        
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeListeners();
                dispose();
            }
        });
        
        contentPane.add(offlineButton);
        if (!launcher.getConfig().isOfflineEnabled()) offlineButton.setEnabled(false);
        contentPane.add(recoverButton);
        contentPane.add(idCombo);
        contentPane.add(passwordText);
        contentPane.add(loginButton);
        contentPane.add(closeButton);
        contentPane.add(rememberIdCheck);
        contentPane.add(rememberPassCheck);
        contentPane.add(instructionText);
        contentPane.add(nameLabel);
        contentPane.add(passLabel);
        //contentPane.add(platformImage);
        contentPane.add(background);
    }

    private void popupManageMenu(Component component, int x, int y) {
        Object selected = idCombo.getSelectedItem();
        JPopupMenu popup = new JPopupMenu();
        JMenuItem menuItem;

        if (selected != null && selected instanceof Account) {
            final Account account = (Account) selected;

            menuItem = new JMenuItem(SharedLocale.tr("login.forgetUser"));
            menuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    accounts.remove(account);
                    Persistence.commitAndForget(accounts);
                }
            });
            popup.add(menuItem);

            if (!Strings.isNullOrEmpty(account.getPassword())) {
                menuItem = new JMenuItem(SharedLocale.tr("login.forgetPassword"));
                menuItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        account.setPassword(null);
                        Persistence.commitAndForget(accounts);
                    }
                });
                popup.add(menuItem);
            }
        }

        menuItem = new JMenuItem(SharedLocale.tr("login.forgetAllPasswords"));
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (SwingHelper.confirmDialog(M4ELoginDialog.this,
                        SharedLocale.tr("login.confirmForgetAllPasswords"),
                        SharedLocale.tr("login.forgetAllPasswordsTitle"))) {
                    accounts.forgetPasswords();
                    Persistence.commitAndForget(accounts);
                }
            }
        });
        popup.add(menuItem);

        popup.show(component, x, y);
    }

    private void updateSelection() {
        Object selected = idCombo.getSelectedItem();

        if (selected != null && selected instanceof Account) {
            Account account = (Account) selected;
            String password = account.getPassword();

            rememberIdCheck.setSelected(true);
            if (!Strings.isNullOrEmpty(password)) {
                rememberPassCheck.setSelected(true);
                passwordText.setText(password);
            } else {
                rememberPassCheck.setSelected(false);
            }
        } else {
            passwordText.setText("");
            rememberIdCheck.setSelected(true);
            rememberPassCheck.setSelected(false);
        }
    }

    @SuppressWarnings("deprecation")
    private void prepareLogin() {
        Object selected = idCombo.getSelectedItem();

        if (selected != null && selected instanceof Account) {
            Account account = (Account) selected;
            String password = passwordText.getText();

            if (password == null || password.isEmpty()) {
                SwingHelper.showErrorDialog(this, SharedLocale.tr("login.noPasswordError"), SharedLocale.tr("login.noPasswordTitle"));
            } else {
                if (rememberPassCheck.isSelected()) {
                    account.setPassword(password);
                } else {
                    account.setPassword(null);
                }

                if (rememberIdCheck.isSelected()) {
                    accounts.add(account);
                } else {
                    accounts.remove(account);
                }

                account.setLastUsed(new Date());

                Persistence.commitAndForget(accounts);

                attemptLogin(account, password);
            }
        } else {
            SwingHelper.showErrorDialog(this, SharedLocale.tr("login.noLoginError"), SharedLocale.tr("login.noLoginTitle"));
        }
    }

    private void attemptLogin(Account account, String password) {
        LoginCallable callable = new LoginCallable(account, password);
        ObservableFuture<Session> future = new ObservableFuture<Session>(
                launcher.getExecutor().submit(callable), callable);

        Futures.addCallback(future, new FutureCallback<Session>() {
            @Override
            public void onSuccess(Session result) {
                setResult(result);
            }

            @Override
            public void onFailure(Throwable t) {
            }
        }, SwingExecutor.INSTANCE);

        ProgressDialog.showProgress(this, future, SharedLocale.tr("login.loggingInTitle"), SharedLocale.tr("login.loggingInStatus"));
        SwingHelper.addErrorDialogCallback(this, future);
    }

    private void setResult(Session session) {
        this.session = session;
        removeListeners();
        dispose();
    }

    public static Session showLoginRequest(Window owner, Launcher launcher) {
        M4ELoginDialog dialog = new M4ELoginDialog(owner, launcher);
        dialog.setVisible(true);
        return dialog.getSession();
    }

    private class LoginCallable implements Callable<Session>,ProgressObservable {
        private final Account account;
        private final String password;

        private LoginCallable(Account account, String password) {
            this.account = account;
            this.password = password;
        }

        @Override
        public Session call() throws AuthenticationException, IOException, InterruptedException {
            LoginService service = launcher.getLoginService();
            List<? extends Session> identities = service.login(launcher.getProperties().getProperty("agentName"), account.getId(), password);

            // The list of identities (profiles in Mojang terms) corresponds to whether the account
            // owns the game, so we need to check that
            if (identities.size() > 0) {
                // Set offline enabled flag to true
                Configuration config = launcher.getConfig();
                if (!config.isOfflineEnabled()) {
                    config.setOfflineEnabled(true);
                    Persistence.commitAndForget(config);
                }

                Persistence.commitAndForget(getAccounts());
                return identities.get(0);
            } else {
                throw new AuthenticationException("Minecraft not owned", SharedLocale.tr("login.minecraftNotOwnedError"));
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
*/
}
