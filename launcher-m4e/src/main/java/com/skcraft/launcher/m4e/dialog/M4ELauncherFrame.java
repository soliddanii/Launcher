/*
 * SKCraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.m4e.dialog;

import static com.skcraft.launcher.util.SharedLocale.tr;
import static javax.swing.BorderFactory.createEmptyBorder;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import com.skcraft.concurrency.ObservableFuture;
import com.skcraft.launcher.Instance;
import com.skcraft.launcher.InstanceList;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.M4ELaunchSupervisor;
import com.skcraft.launcher.dialog.InstanceSettingsDialog;
import com.skcraft.launcher.dialog.LauncherFrame;
import com.skcraft.launcher.dialog.ProgressDialog;
import com.skcraft.launcher.launch.LaunchListener;
import com.skcraft.launcher.launch.LaunchOptions;
import com.skcraft.launcher.launch.LaunchOptions.UpdatePolicy;
import com.skcraft.launcher.m4e.swing.BackgroundPanel;
import com.skcraft.launcher.m4e.swing.ImageButton;
import com.skcraft.launcher.m4e.swing.ImageHyperlinkButton;
import com.skcraft.launcher.m4e.swing.LiteButton;
import com.skcraft.launcher.m4e.swing.M4EInstanceTable;
import com.skcraft.launcher.m4e.swing.MarqueePanel;
import com.skcraft.launcher.m4e.swing.RedButton;
import com.skcraft.launcher.m4e.swing.SquareBox;
import com.skcraft.launcher.m4e.swing.news.NewsComponent;
import com.skcraft.launcher.m4e.utils.M4EConstants;
import com.skcraft.launcher.m4e.utils.ResourceUtils;
import com.skcraft.launcher.swing.ActionListeners;
import com.skcraft.launcher.swing.DoubleClickToButtonAdapter;
import com.skcraft.launcher.swing.InstanceTableModel;
import com.skcraft.launcher.swing.PopupMouseAdapter;
import com.skcraft.launcher.swing.SwingHelper;
import com.skcraft.launcher.swing.WebpagePanel;
import com.skcraft.launcher.util.SharedLocale;
import com.skcraft.launcher.util.SwingExecutor;

import br.com.azalim.mcserverping.MCPing;
import br.com.azalim.mcserverping.MCPingOptions;
import br.com.azalim.mcserverping.MCPingResponse;
import br.com.azalim.mcserverping.MCPingResponse.Players;
import br.com.azalim.mcserverping.MCPingResponse.Version;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.java.Log;

@Log
public class M4ELauncherFrame extends JFrame implements MouseListener, MouseMotionListener {

	private static final long serialVersionUID = 4194888226424658020L;
	
	public static final int FRAME_HEIGHT = 520;
	public static final int FRAME_WIDTH = 880;

	private final Launcher launcher;
	
	private int mouseX = 0, mouseY = 0;
	private NewsComponent news;
	private SquareBox barBox;

	@Getter
	private final M4EInstanceTable instancesTable = new M4EInstanceTable(M4EConstants.TRANSPARENT);
	@Getter
	private final JScrollPane instanceScroll = new JScrollPane(instancesTable);
	
	private final InstanceTableModel instancesModel;
	
	private final M4ELaunchSupervisor launchSupervisor;

	private final JButton launchButton = new LiteButton(SharedLocale.tr("launcher.launch"));
	private final JButton refreshButton = new ImageButton(ResourceUtils.getIcon("updateLinkButton.png"));
	private final ImageButton optionsButton = new ImageButton(ResourceUtils.getIcon("gear.png", 28, 28), ResourceUtils.getIcon("gearInverted.png", 28, 28));
	private final JButton selfUpdateButton = new RedButton(SharedLocale.tr("launcher.updateLauncher"));
	private final ImageButton refreshServer = new ImageButton(ResourceUtils.getIcon("refresh.png", 24, 24), ResourceUtils.getIcon("refreshInverted.png", 24, 24));
	private final boolean updateCheck = true; //Sustitudo el checkbox por un booleano. SIEMPRE COMPROBAR ACTUALIZACIONES

	/**
	 * Create a new frame.
	 *
	 * @param launcher the launcher
	 */
	public M4ELauncherFrame(@NonNull Launcher launcher) {
		super(SharedLocale.tr("launcher.title", launcher.getVersion()));

		this.launcher = launcher;
		this.instancesModel = new InstanceTableModel(launcher.getInstances());
		this.launchSupervisor = new M4ELaunchSupervisor(launcher);

		initComponents();
		setSize(FRAME_WIDTH, FRAME_HEIGHT);
		setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
		setResizable(false);
		setLocationRelativeTo(null);
		setUndecorated(true);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		addMouseListener(this);
		addMouseMotionListener(this);

		SwingHelper.setFrameIcon(this, Launcher.class, "m4e/icon.png");

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				loadInstances();
			}
		});
	}

	private void initComponents() {
		
		Font minecraft = ResourceUtils.getMinecraftFont(12);
		
		//Background (STATIC FOR NOW)
		JPanel container = createContainerPanel();
		
		// Launch button area
		SquareBox launchArea = new SquareBox(M4EConstants.TRANSPARENT);
		launchArea.setBounds(605, 410, 265, 100);
		
		// Update Button
		selfUpdateButton.setFont(minecraft.deriveFont(20f));
		selfUpdateButton.setBounds(launchArea.getX() + 5, launchArea.getY() + 7, launchArea.getWidth() - 10, (launchArea.getHeight() / 2) - 10);

		//Play Button
		launchButton.setFont(minecraft.deriveFont(20f));
		launchButton.setBounds(launchArea.getX() + 5, selfUpdateButton.getY() + selfUpdateButton.getHeight() + 5, launchArea.getWidth() - 10, (launchArea.getHeight() / 2) - 10);

		// Launcher logo
		JLabel logo = new JLabel();
		ImageIcon logoIcon = new ImageIcon(ResourceUtils.scaleWithAspectWidth(ResourceUtils.getImage("header.png"), 275));
		logo.setIcon(logoIcon);
		logo.setBounds(600, 6, logoIcon.getIconWidth(), logoIcon.getIconHeight());

		// Progress Bar Background box
		barBox = new SquareBox(M4EConstants.TRANSPARENT);
		barBox.setVisible(false);
		barBox.setBounds(605, 205, 265, 35);
		
		// News Items
		news = new NewsComponent(launcher.getNewsURL());
		news.setBounds(barBox.getX(), logo.getY() + logo.getHeight(), barBox.getWidth(), 100);

		// Server information
		SquareBox serverArea = new SquareBox(M4EConstants.TRANSPARENT);
		serverArea.setBounds(605, 180, 265, 90);

		refreshServer.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		refreshServer.setDisabledIcon(ResourceUtils.getIcon("refreshDisabled.png", 24, 24));
		refreshServer.setBounds(serverArea.getX() + serverArea.getWidth() - 34, serverArea.getY() + 10, 24, 24);
		
		final JLabel statusIcon = new JLabel("");
		statusIcon.setIcon(ResourceUtils.getIcon("serverOff.png", 24, 24));
		statusIcon.setBounds(serverArea.getX() + 10, serverArea.getY() + 10, 24, 24);
		statusIcon.setVisible(true);

		final JLabel statusText = new JLabel("OFFLINE");
		statusText.setFont(ResourceUtils.getMinecraftFont(18));
		statusText.setForeground(Color.WHITE);
		statusText.setBounds(statusIcon.getX() + statusIcon.getWidth() + 12, serverArea.getY() + 10, serverArea.getWidth() - 73, 24);
		statusText.setVisible(true);

		String tmpI = M4EConstants.SERVER_IP + (M4EConstants.SERVER_PORT != 25565 ? ":" + M4EConstants.SERVER_PORT : "");
		final JLabel serverInformation = new JLabel(tmpI, SwingConstants.LEFT);
		serverInformation.setFont(ResourceUtils.getMinecraftFont(12));
		serverInformation.setForeground(Color.WHITE);
		serverInformation.setBounds(serverArea.getX() + 10, statusIcon.getY() + statusIcon.getHeight(), serverArea.getWidth() - 70, 24);
		serverInformation.setVisible(true);

		final JLabel serverVersion = new JLabel("", SwingConstants.RIGHT);
		serverVersion.setFont(ResourceUtils.getMinecraftFont(12));
		serverVersion.setForeground(Color.WHITE);
		serverVersion.setBounds(serverArea.getX() + serverArea.getWidth() - 70, statusIcon.getY() + statusIcon.getHeight(), 60, 24);
		serverVersion.setVisible(true);

		final MarqueePanel onlinePlayers = new MarqueePanel(62, 1);
		final JLabel message = new JLabel("El servidor está offline");
		message.setFont(ResourceUtils.getMinecraftFont(12));
		message.setForeground(Color.WHITE);
		onlinePlayers.setOpaque(false);
		onlinePlayers.setBackground(new Color(45, 45, 45, 160));
		onlinePlayers.add(message);
		onlinePlayers.setWrap(true);
		onlinePlayers.setWrapAmount(150);
		onlinePlayers.setScrollWhenFocused(true);
		onlinePlayers.setBounds(serverArea.getX() + 10, serverInformation.getY() + serverInformation.getHeight(), serverArea.getWidth() - 20, 24);

		this.refreshServerStatus(statusIcon, statusText, serverVersion, message);
		
		
		// Link background box
		SquareBox linkArea = new SquareBox(M4EConstants.TRANSPARENT);
		linkArea.setBounds(605, 280, 265, 120); // x,y,width,height

		int linkWidth = linkArea.getWidth() - (M4EConstants.SPACING * 2);
		int linkHeight = (linkArea.getHeight() - (M4EConstants.SPACING * 4)) / 3;

		// Forums link
		JButton forums = new ImageHyperlinkButton("https://www.minecraft4ever.com");
		forums.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		forums.setToolTipText(SharedLocale.tr("launcher.visitForums"));
		forums.setBounds(linkArea.getX() + M4EConstants.SPACING, linkArea.getY() + M4EConstants.SPACING, linkWidth, linkHeight);
		forums.setIcon(ResourceUtils.getIcon("forumsLinkButton.png"));
		forums.setRolloverIcon(ResourceUtils.getIcon("forumsLinkButtonBright.png"));
		forums.setContentAreaFilled(false);
		forums.setBorderPainted(false);

		// Donate link
		JButton donate = new ImageHyperlinkButton("https://minecraft4ever.com/misc.php?page=vip");
		donate.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		donate.setToolTipText(SharedLocale.tr("launcher.donate"));
		donate.setBounds(linkArea.getX() + M4EConstants.SPACING, forums.getY() + forums.getHeight() + M4EConstants.SPACING, linkWidth, linkHeight);
		donate.setIcon(ResourceUtils.getIcon("donateLinkButton.png"));
		donate.setRolloverIcon(ResourceUtils.getIcon("donateLinkButtonBright.png"));
		donate.setContentAreaFilled(false);
		donate.setBorderPainted(false);

		// Check for updates
		refreshButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		refreshButton.setToolTipText(SharedLocale.tr("launcher.checkForUpdates"));
		refreshButton.setBounds(linkArea.getX() + M4EConstants.SPACING, donate.getY() + donate.getHeight() + M4EConstants.SPACING, linkWidth, linkHeight);
		refreshButton.setRolloverIcon(ResourceUtils.getIcon("updateLinkButtonBright.png"));
		refreshButton.setContentAreaFilled(false);
		refreshButton.setBorderPainted(false);

		// Options Button
		optionsButton.setBounds(FRAME_WIDTH - 34 * 2, 6, 28, 28);
		optionsButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		// Exit Button
		ImageButton exit = new ImageButton(ResourceUtils.getIcon("quit.png", 28, 28), ResourceUtils.getIcon("quitHover.png", 28, 28));
		exit.setBounds(FRAME_WIDTH - 34, 6, 28, 28);
		exit.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		// YouTube button
		JButton youtube = new ImageHyperlinkButton("http://www.youtube.com/user/Minecraft4everLive");
		youtube.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		youtube.setRolloverIcon(ResourceUtils.getIcon("youtubeInverted.png", 28, 28));
		youtube.setToolTipText("Subscribete a nustro canal");
		youtube.setBounds(215 + 6, 6, 28, 28);
		ResourceUtils.setIcon(youtube, "youtube.png", 28);

		// Discord button
		JButton discord = new ImageHyperlinkButton("http://tiny.cc/discordM4E");
		discord.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		discord.setRolloverIcon(ResourceUtils.getIcon("discordInverted.png", 28, 28));
		discord.setToolTipText("Habla con nosotros en Discord");
		discord.setBounds(215 + 6 + 34, 6, 28, 28);
		ResourceUtils.setIcon(discord, "discord.png", 28);

		// Twitter button
		JButton twitter = new ImageHyperlinkButton("https://twitter.com/m4e_es");
		twitter.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		twitter.setRolloverIcon(ResourceUtils.getIcon("twitterInverted.png", 28, 28));
		twitter.setToolTipText("Siguenos en Twitter");
		twitter.setBounds(215 + 6 + 34 * 2, 6, 28, 28);
		ResourceUtils.setIcon(twitter, "twitter.png", 28);

		// Steam button
		JButton steam = new ImageHyperlinkButton("http://steamcommunity.com/groups/comunidadm4e");
		steam.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		steam.setRolloverIcon(ResourceUtils.getIcon("steamInverted.png", 28, 28));
		steam.setToolTipText("Juega con nosotros en Steam");
		steam.setBounds(215 + 6 + 34 * 3, 6, 28, 28);
		ResourceUtils.setIcon(steam, "steam.png", 28);

		// ModpackContainer Title
		SquareBox modpackContainerTitle = new SquareBox(M4EConstants.TRANSPARENT);
		modpackContainerTitle.setBounds(6, 6, 210, 28);
		JLabel modpackTitle = new JLabel(SharedLocale.tr("launcher.modpackColumn"));
		modpackTitle.setFont(minecraft.deriveFont(14f));
		modpackTitle.setForeground(Color.WHITE);
		modpackTitle.setBounds((modpackContainerTitle.getWidth() / 2) - 45 + 6, (modpackContainerTitle.getHeight() / 2) - 10 + 6, 90, 20);

		// ModpackContainer
		instanceScroll.setBounds(6, 40, 210, FRAME_HEIGHT - 46);
		instanceScroll.setOpaque(false);
		instanceScroll.getViewport().setOpaque(false);
		instanceScroll.setBorder(createEmptyBorder());
		instanceScroll.setViewportBorder(createEmptyBorder());
		instancesTable.setModel(instancesModel);

		selfUpdateButton.setEnabled(launcher.getUpdateManager().getPendingUpdate());

		launcher.getUpdateManager().addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals("pendingUpdate")) {
					selfUpdateButton.setVisible((Boolean) evt.getNewValue());
				}
			}
		});
		
		Container contentPane = getContentPane();
		contentPane.setLayout(null);

		contentPane.add(selfUpdateButton);
		contentPane.add(launchButton);
		contentPane.add(launchArea);
		contentPane.add(barBox);
		contentPane.add(modpackTitle);
		contentPane.add(modpackContainerTitle);
		contentPane.add(instanceScroll);
		// contentPane.add(steam);
		contentPane.add(twitter);
		contentPane.add(discord);
		contentPane.add(youtube);
		contentPane.add(forums);
		contentPane.add(donate);
		contentPane.add(refreshButton);
		contentPane.add(refreshServer);
		contentPane.add(statusText);
		contentPane.add(statusIcon);
		contentPane.add(serverInformation);
		contentPane.add(serverVersion);
		contentPane.add(onlinePlayers);
		contentPane.add(serverArea);
		contentPane.add(linkArea);
		contentPane.add(logo);
		contentPane.add(news);
		contentPane.add(optionsButton);
		contentPane.add(exit);
		contentPane.add(container);

		instancesModel.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				if (instancesTable.getRowCount() > 0) {
					instancesTable.setRowSelectionInterval(0, 0);
				}
			}
		});

		instancesTable.addMouseListener(new DoubleClickToButtonAdapter(launchButton));

		refreshButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				loadInstances();
				launcher.getUpdateManager().checkForUpdate(M4ELauncherFrame.this);
			}
		});

		selfUpdateButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				launcher.getUpdateManager().performUpdate(M4ELauncherFrame.this);
			}
		});

		optionsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showOptions();
			}
		});

		launchButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				launch();
			}
		});
		
		exit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});

		instancesTable.addMouseListener(new PopupMouseAdapter() {
			@Override
			protected void showPopup(MouseEvent e) {
				int index = instancesTable.rowAtPoint(e.getPoint());
				Instance selected = null;
				if (index >= 0) {
					instancesTable.setRowSelectionInterval(index, index);
					selected = launcher.getInstances().get(index);
				}
				popupInstanceMenu(e.getComponent(), e.getX(), e.getY(), selected);
			}
		});
		
		refreshServer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				refreshServerStatus(statusIcon, statusText, serverVersion, message);
				onlinePlayers.stopScrolling();
				onlinePlayers.startScrolling();
			}
		});
	}

	private JPanel createContainerPanel() {
		return new BackgroundPanel("background.jpg", FRAME_WIDTH, FRAME_HEIGHT);
	}

	/**
	 * Return the news panel.
	 *
	 * @return the news panel
	 */
	protected WebpagePanel createNewsPanel() {
		WebpagePanel panel = WebpagePanel.forURL(launcher.getNewsURL(), false);
		panel.setBrowserBorder(BorderFactory.createEmptyBorder());
		return panel;
	}

	/**
	 * Set the server information on the swing components
	 *
	 * @param statusIcon the status icon (on/off)
	 */
	private void refreshServerStatus(final JLabel statusIcon, final JLabel statusText, final JLabel serverVersion,
			final JLabel onlinePlayers) {

		refreshServer.setEnabled(false);

		new Thread(new Runnable() {

			@Override
			public void run() {

				log.info("Refreshing server status");

				MCPingOptions options = MCPingOptions.builder().hostname("minecraft4ever.es").port(25600).timeout(1000)
						.build();

				MCPingResponse reply;

				try {
					reply = MCPing.getPing(options);

					// Set the status icon
					statusIcon.setIcon(ResourceUtils.getIcon("serverOn.png", 24, 24));

					// Set the status text
					Players players = reply.getPlayers();
					statusText.setText("ONLINE       " + players.getOnline() + "/" + players.getMax());

					// Set the server information
					Version version = reply.getVersion();
					serverVersion.setText(version.getName().replace("Paper ", ""));

					String text = "No hay jugadores online";

					if (players.getSample() != null && !players.getSample().isEmpty()) {
						String jugadores = players.getSample().stream().map(player -> player.getName())
								.collect(Collectors.joining(",  "));
						text = "Jugadores:  " + jugadores;
					}

					// Set the online players message
					onlinePlayers.setText(text);
				} catch (IOException ex) {
					log.warning(options.getHostname() + " is down or unreachable.");

					// Set the status icon
					statusIcon.setIcon(ResourceUtils.getIcon("serverOff.png", 24, 24));
					// Set the status text
					statusText.setText("OFFLINE");
					// Set the server information
					serverVersion.setText("");
					// Set the online players message
					onlinePlayers.setText("El servidor está offline");
				}

				log.info("Server status refreshed");

				// Cooldown for the button in seconds
				int BUTTON_COOLDOWN = 4;

				try {
					Thread.sleep(BUTTON_COOLDOWN * 1000L);
				} catch (InterruptedException ignored) {
					log.warning("Refresh button cooldown failed");
				}

				refreshServer.setEnabled(true);

			}

		}).start();

	}

	/**
	 * Popup the menu for the instances.
	 *
	 * @param component the component
	 * @param x         mouse X
	 * @param y         mouse Y
	 * @param selected  the selected instance, possibly null
	 */
	private void popupInstanceMenu(Component component, int x, int y, final Instance selected) {
		JPopupMenu popup = new JPopupMenu();
		JMenuItem menuItem;

		if (selected != null) {
			menuItem = new JMenuItem(!selected.isLocal() ? tr("instance.install") : tr("instance.launch"));
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					launch();
				}
			});
			popup.add(menuItem);

			if (selected.isLocal()) {
				popup.addSeparator();

				menuItem = new JMenuItem(SharedLocale.tr("instance.openFolder"));
				menuItem.addActionListener(ActionListeners.browseDir(M4ELauncherFrame.this, selected.getContentDir(), true));
				popup.add(menuItem);

				menuItem = new JMenuItem(SharedLocale.tr("instance.openSaves"));
				menuItem.addActionListener(ActionListeners.browseDir(M4ELauncherFrame.this, new File(selected.getContentDir(), "saves"), true));
				popup.add(menuItem);

				menuItem = new JMenuItem(SharedLocale.tr("instance.openResourcePacks"));
				menuItem.addActionListener(ActionListeners.browseDir(M4ELauncherFrame.this, new File(selected.getContentDir(), "resourcepacks"), true));
				popup.add(menuItem);

				menuItem = new JMenuItem(SharedLocale.tr("instance.openScreenshots"));
				menuItem.addActionListener(ActionListeners.browseDir(M4ELauncherFrame.this, new File(selected.getContentDir(), "screenshots"), true));
				popup.add(menuItem);

				menuItem = new JMenuItem(SharedLocale.tr("instance.copyAsPath"));
				menuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						File dir = selected.getContentDir();
						dir.mkdirs();
						SwingHelper.setClipboard(dir.getAbsolutePath());
					}
				});
				popup.add(menuItem);

				menuItem = new JMenuItem(SharedLocale.tr("instance.openSettings"));
				menuItem.addActionListener(e -> {
					InstanceSettingsDialog.open(this, selected);
				});
				popup.add(menuItem);

				popup.addSeparator();

				if (!selected.isUpdatePending()) {
					menuItem = new JMenuItem(SharedLocale.tr("instance.forceUpdate"));
					menuItem.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							selected.setUpdatePending(true);
							launch();
							instancesModel.update();
						}
					});
					popup.add(menuItem);
				}

				menuItem = new JMenuItem(SharedLocale.tr("instance.hardForceUpdate"));
				menuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						confirmHardUpdate(selected);
					}
				});
				popup.add(menuItem);

				menuItem = new JMenuItem(SharedLocale.tr("instance.deleteFiles"));
				menuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						confirmDelete(selected);
					}
				});
				popup.add(menuItem);
			}

			popup.addSeparator();
		}

		menuItem = new JMenuItem(SharedLocale.tr("launcher.refreshList"));
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				loadInstances();
			}
		});
		popup.add(menuItem);

		popup.show(component, x, y);

	}

	private void confirmDelete(Instance instance) {
		if (!SwingHelper.confirmDialog(this, tr("instance.confirmDelete", instance.getTitle()),
				SharedLocale.tr("confirmTitle"))) {
			return;
		}

		ObservableFuture<Instance> future = launcher.getInstanceTasks().delete(this, instance);

		// Update the list of instances after updating
		future.addListener(new Runnable() {
			@Override
			public void run() {
				loadInstances();
			}
		}, SwingExecutor.INSTANCE);
	}

	private void confirmHardUpdate(Instance instance) {
		if (!SwingHelper.confirmDialog(this, SharedLocale.tr("instance.confirmHardUpdate"),
				SharedLocale.tr("confirmTitle"))) {
			return;
		}

		ObservableFuture<Instance> future = launcher.getInstanceTasks().hardUpdate(this, instance);

		// Update the list of instances after updating
		future.addListener(new Runnable() {
			@Override
			public void run() {
				launch();
				instancesModel.update();
			}
		}, SwingExecutor.INSTANCE);
	}

	private void loadInstances() {
		ObservableFuture<InstanceList> future = launcher.getInstanceTasks().reloadInstances(this);

		future.addListener(new Runnable() {
			@Override
			public void run() {
				instancesModel.update();
				if (instancesTable.getRowCount() > 0) {
					instancesTable.setRowSelectionInterval(0, 0);
				}
				requestFocus();
			}
		}, SwingExecutor.INSTANCE);

		ProgressDialog.showProgress(this, future, SharedLocale.tr("launcher.checkingTitle"),
				SharedLocale.tr("launcher.checkingStatus"));
		SwingHelper.addErrorDialogCallback(this, future);
	}

	private void showOptions() {
		//ConfigurationDialog configDialog = new ConfigurationDialog(this, launcher);
		M4EConfigurationDialog configDialog = new M4EConfigurationDialog(this, launcher);
		configDialog.setVisible(true);
	}

	private void launch() {
		boolean permitUpdate = updateCheck; //sustituido el checkbox por un booleano: updateCheck.isSelected();
		Instance instance = launcher.getInstances().get(instancesTable.getSelectedRow());

		LaunchOptions options = new LaunchOptions.Builder()
				.setInstance(instance)
				.setListener(new LaunchListenerImpl(this))
				.setUpdatePolicy(permitUpdate ? UpdatePolicy.UPDATE_IF_SESSION_ONLINE : UpdatePolicy.NO_UPDATE)
				.setWindow(this)
				.build();
		
		this.launchSupervisor.launch(options);
	}
	
	private static class LaunchListenerImpl implements LaunchListener {
		private final WeakReference<M4ELauncherFrame> frameRef;
		private final Launcher launcher;

		private LaunchListenerImpl(M4ELauncherFrame frame) {
			this.frameRef = new WeakReference<M4ELauncherFrame>(frame);
			this.launcher = frame.launcher;
		}

		@Override
		public void instancesUpdated() {
			M4ELauncherFrame frame = frameRef.get();
			if (frame != null) {
				frame.instancesModel.update();
			}
		}

		@Override
		public void gameStarted() {
			M4ELauncherFrame frame = frameRef.get();
			if (frame != null) {
				frame.dispose();
			}
		}

		@Override
		public void gameClosed() {
			Window newLauncherWindow = launcher.showLauncherWindow();
			launcher.getUpdateManager().checkForUpdate(newLauncherWindow);
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		this.setLocation(e.getXOnScreen() - mouseX, e.getYOnScreen() - mouseY);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		//nothing
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		//nothing
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		//nothing
	}

	@Override
	public void mouseExited(MouseEvent e) {
		//nothing
	}

	@Override
	public void mousePressed(MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		//nothing
	}

}
