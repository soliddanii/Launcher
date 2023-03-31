package com.skcraft.launcher.m4e.dialog;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.MutableComboBoxModel;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.skcraft.launcher.Configuration;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.dialog.ConsoleFrame;
import com.skcraft.launcher.dialog.component.BetterComboBox;
import com.skcraft.launcher.launch.AddJavaRuntime;
import com.skcraft.launcher.launch.JavaRuntime;
import com.skcraft.launcher.launch.JavaRuntimeFinder;
import com.skcraft.launcher.m4e.swing.BackgroundPanel;
import com.skcraft.launcher.m4e.swing.ImageButton;
import com.skcraft.launcher.m4e.swing.LiteButton;
import com.skcraft.launcher.m4e.swing.SquareBox;
import com.skcraft.launcher.m4e.utils.M4EConstants;
import com.skcraft.launcher.m4e.utils.ResourceUtils;
import com.skcraft.launcher.persistence.Persistence;
import com.skcraft.launcher.swing.ActionListeners;
import com.skcraft.launcher.swing.ObjectSwingMapper;
import com.skcraft.launcher.util.SharedLocale;

import lombok.NonNull;



/**
 * A dialog to modify configuration options.
 */
public class M4EConfigurationDialog extends M4EDialog implements MouseListener, MouseMotionListener{

	private static final long serialVersionUID = -8477694865350149534L;
	
	private static final int FRAME_HEIGHT = 400;
	private static final int FRAME_WIDTH = 700;
	
	private final ObjectSwingMapper mapper;
	private final Configuration config;
	private final File baseDir;

	private final JComboBox<JavaRuntime> jvmRuntime = new BetterComboBox<>();
	private final JTextField jvmArgsText = new JTextField();
	private final JSpinner minMemorySpinner = new JSpinner();
	private final JSpinner maxMemorySpinner = new JSpinner();
	private final JSpinner permGenSpinner = new JSpinner();
	private final JSpinner widthSpinner = new JSpinner();
	private final JSpinner heightSpinner = new JSpinner();
	private final JTextField offlineNameText = new JTextField();
	private final JCheckBox useProxyCheck = new JCheckBox(SharedLocale.tr("options.useProxyCheck"));
	private final JTextField proxyHostText = new JTextField();
	private final JSpinner proxyPortText = new JSpinner();
	private final JTextField proxyUsernameText = new JTextField();
	private final JPasswordField proxyPasswordText = new JPasswordField();
	private final JTextField gameKeyText = new JTextField();
	private final LiteButton editServerURL = new LiteButton(SharedLocale.tr("options.editServerURL"));
	private final LiteButton okButton = new LiteButton(SharedLocale.tr("button.ok"));
	private final LiteButton logButton = new LiteButton(SharedLocale.tr("options.launcherConsole"));
	
	private String version;

	/**
	 * Create a new configuration dialog.
	 *
	 * @param owner    the window owner
	 * @param launcher the launcher
	 */
	public M4EConfigurationDialog(Window owner, @NonNull Launcher launcher) {
		super(owner, ModalityType.DOCUMENT_MODAL);

		this.config = launcher.getConfig();
		this.baseDir = launcher.getBaseDir();
		this.version = launcher.getVersion();
		mapper = new ObjectSwingMapper(config);
		
		JavaRuntime[] javaRuntimes = JavaRuntimeFinder.getAvailableRuntimes().toArray(new JavaRuntime[0]);
		DefaultComboBoxModel<JavaRuntime> model = new DefaultComboBoxModel<>(javaRuntimes);
		
		// Put the runtime from the config in the model if it isn't
		boolean configRuntimeFound = Arrays.stream(javaRuntimes).anyMatch(r -> r.equals(config.getJavaRuntime()));
		if (!configRuntimeFound && config.getJavaRuntime() != null) {
			model.insertElementAt(config.getJavaRuntime(), 0);
		}
		
		jvmRuntime.setModel(model);
		jvmRuntime.addItem(AddJavaRuntime.ADD_RUNTIME_SENTINEL);

		jvmRuntime.setSelectedItem(config.getJavaRuntime());

		// GUI SETUP
		setTitle(SharedLocale.tr("options.title"));
		setSize(FRAME_WIDTH, FRAME_HEIGHT);
		initComponents(); // Must be called after jvmRuntime model setup
		setResizable(false);
		setUndecorated(true);
		setLocationRelativeTo(owner);

		// LISTENERS
		addMouseListener(this);
		addMouseMotionListener(this);

		// OPTIONS MAPPING
		mapper.map(jvmArgsText, "jvmArgs");
		mapper.map(minMemorySpinner, "minMemory");
		mapper.map(maxMemorySpinner, "maxMemory");
		mapper.map(permGenSpinner, "permGen");
		mapper.map(widthSpinner, "windowWidth");
		mapper.map(heightSpinner, "windowHeight");
		mapper.map(offlineNameText, "offlineName");
		mapper.map(useProxyCheck, "proxyEnabled");
		mapper.map(proxyHostText, "proxyHost");
		mapper.map(proxyPortText, "proxyPort");
		mapper.map(proxyUsernameText, "proxyUsername");
		mapper.map(proxyPasswordText, "proxyPassword");
		mapper.map(gameKeyText, "gameKey");

		mapper.copyFromObject();
	}

	private void initComponents() {

		Font minecraft = ResourceUtils.getMinecraftFont(12);

		JPanel container = createContainerPanel();

		ImageButton optionsQuit = new ImageButton(ResourceUtils.getIcon("quit.png", 28, 28), ResourceUtils.getIcon("quit.png", 28, 28));
		optionsQuit.setRolloverIcon(ResourceUtils.getIcon("quitHover.png", 28, 28));
		optionsQuit.setBounds(FRAME_WIDTH - 38, 10, 28, 28);

		// Title
		JLabel title = new JLabel(SharedLocale.tr("options.title"));
		title.setFont(minecraft.deriveFont(14F));
		title.setBounds(FRAME_WIDTH / 2 - 100, 15, 200, 20);
		title.setForeground(Color.WHITE);
		title.setHorizontalAlignment(SwingConstants.CENTER);

		SquareBox javaArea = new SquareBox(M4EConstants.TRANSPARENT);
		javaArea.setBounds(10, 40, FRAME_WIDTH / 2 - 20, FRAME_HEIGHT - 90);

		JLabel javaTitle = new JLabel(SharedLocale.tr("options.javaTab"));
		javaTitle.setFont(minecraft.deriveFont(13F));
		javaTitle.setBounds(javaArea.getX() + javaArea.getWidth() / 2 - 100, javaArea.getY() + 10, 200, 22);
		javaTitle.setForeground(Color.WHITE);
		javaTitle.setHorizontalAlignment(SwingConstants.CENTER);

		JLabel javajvmPath = new JLabel(SharedLocale.tr("options.jvmPath"));
		javajvmPath.setFont(minecraft.deriveFont(12F));
		javajvmPath.setBounds(20, javaTitle.getY() + javaTitle.getHeight() + 10, 200, 25);
		javajvmPath.setForeground(Color.WHITE);
		javajvmPath.setHorizontalAlignment(SwingConstants.LEFT);
		//jvmRuntime.setHorizontalAlignment(SwingConstants.RIGHT);
		jvmRuntime.setFont(minecraft.deriveFont(12F));
		jvmRuntime.setBounds(javaArea.getX() + javaArea.getWidth() - 130, javajvmPath.getY(), 120, 25);

		JLabel javajvmArguments = new JLabel(SharedLocale.tr("options.jvmArguments"));
		javajvmArguments.setFont(minecraft.deriveFont(12F));
		javajvmArguments.setBounds(20, javajvmPath.getY() + javajvmPath.getHeight() + 10, 200, 25);
		javajvmArguments.setForeground(Color.WHITE);
		javajvmArguments.setHorizontalAlignment(SwingConstants.LEFT);
		jvmArgsText.setHorizontalAlignment(SwingConstants.RIGHT);
		jvmArgsText.setFont(minecraft.deriveFont(12F));
		jvmArgsText.setBounds(javaArea.getX() + javaArea.getWidth() - 130, javajvmArguments.getY(), 120, 25);

		JLabel java64BitJavaWarning = new JLabel(SharedLocale.tr("options.64BitJavaWarning"));
		java64BitJavaWarning.setFont(minecraft.deriveFont(11F));
		java64BitJavaWarning.setBounds(20, javajvmArguments.getY() + javajvmArguments.getHeight() + 10, 300, 50);
		java64BitJavaWarning.setForeground(Color.WHITE);
		java64BitJavaWarning.setHorizontalAlignment(SwingConstants.LEFT);

		JLabel javaminMemory = new JLabel(SharedLocale.tr("options.minMemory"));
		javaminMemory.setFont(minecraft.deriveFont(12F));
		javaminMemory.setBounds(20, java64BitJavaWarning.getY() + java64BitJavaWarning.getHeight() + 10, 200, 25);
		javaminMemory.setForeground(Color.WHITE);
		javaminMemory.setHorizontalAlignment(SwingConstants.LEFT);
		minMemorySpinner.setAlignmentX(SwingConstants.RIGHT);
		minMemorySpinner.setFont(minecraft.deriveFont(12F));
		minMemorySpinner.setBounds(javaArea.getX() + javaArea.getWidth() - 130, javaminMemory.getY(), 120, 25);

		JLabel javamaxMemory = new JLabel(SharedLocale.tr("options.maxMemory"));
		javamaxMemory.setFont(minecraft.deriveFont(12F));
		javamaxMemory.setBounds(20, javaminMemory.getY() + javaminMemory.getHeight() + 10, 200, 25);
		javamaxMemory.setForeground(Color.WHITE);
		javamaxMemory.setHorizontalAlignment(SwingConstants.LEFT);
		maxMemorySpinner.setAlignmentX(SwingConstants.RIGHT);
		maxMemorySpinner.setFont(minecraft.deriveFont(12F));
		maxMemorySpinner.setBounds(javaArea.getX() + javaArea.getWidth() - 130, javamaxMemory.getY(), 120, 25);

		JLabel javapermGen = new JLabel(SharedLocale.tr("options.permGen"));
		javapermGen.setFont(minecraft.deriveFont(12F));
		javapermGen.setBounds(20, javamaxMemory.getY() + javamaxMemory.getHeight() + 10, 200, 25);
		javapermGen.setForeground(Color.WHITE);
		javapermGen.setHorizontalAlignment(SwingConstants.LEFT);
		permGenSpinner.setAlignmentX(SwingConstants.RIGHT);
		permGenSpinner.setFont(minecraft.deriveFont(12F));
		permGenSpinner.setBounds(javaArea.getX() + javaArea.getWidth() - 130, javapermGen.getY(), 120, 25);

		SquareBox gameArea = new SquareBox(M4EConstants.TRANSPARENT);
		gameArea.setBounds(FRAME_WIDTH / 2 + 10, 40, FRAME_WIDTH / 2 - 20, FRAME_HEIGHT / 2 - 45);

		// Game Settings
		JLabel gameTitle = new JLabel(SharedLocale.tr("options.minecraftTab"));
		gameTitle.setFont(minecraft.deriveFont(13F));
		gameTitle.setBounds(gameArea.getX() + gameArea.getWidth() / 2 - 100, gameArea.getY() + 10, 200, 22);
		gameTitle.setForeground(Color.WHITE);
		gameTitle.setHorizontalAlignment(SwingConstants.CENTER);

		JLabel windowWidth = new JLabel(SharedLocale.tr("options.windowWidth"));
		windowWidth.setFont(minecraft.deriveFont(12F));
		windowWidth.setBounds(gameArea.getX() + 10, gameTitle.getY() + gameTitle.getHeight() + 10, 200, 25);
		windowWidth.setForeground(Color.WHITE);
		windowWidth.setHorizontalAlignment(SwingConstants.LEFT);
		widthSpinner.setAlignmentX(SwingConstants.RIGHT);
		widthSpinner.setFont(minecraft.deriveFont(12F));
		widthSpinner.setBounds(gameArea.getX() + gameArea.getWidth() - 130, windowWidth.getY(), 120, 25);

		JLabel windowHeight = new JLabel(SharedLocale.tr("options.windowHeight"));
		windowHeight.setFont(minecraft.deriveFont(12F));
		windowHeight.setBounds(gameArea.getX() + 10, windowWidth.getY() + windowWidth.getHeight() + 10, 200, 25);
		windowHeight.setForeground(Color.WHITE);
		windowHeight.setHorizontalAlignment(SwingConstants.LEFT);
		heightSpinner.setAlignmentX(SwingConstants.RIGHT);
		heightSpinner.setFont(minecraft.deriveFont(12F));
		heightSpinner.setBounds(gameArea.getX() + gameArea.getWidth() - 130, windowHeight.getY(), 120, 25);

		JLabel offlineName = new JLabel(SharedLocale.tr("options.offlineName"));
		offlineName.setFont(minecraft.deriveFont(12F));
		offlineName.setBounds(gameArea.getX() + 10, windowHeight.getY() + windowHeight.getHeight() + 10, 200, 25);
		offlineName.setForeground(Color.WHITE);
		offlineName.setHorizontalAlignment(SwingConstants.LEFT);
		offlineNameText.setAlignmentX(SwingConstants.RIGHT);
		offlineNameText.setFont(minecraft.deriveFont(12F));
		offlineNameText.setBounds(gameArea.getX() + gameArea.getWidth() - 130, offlineName.getY(), 120, 25);
		
		/*//Proxy Settings
		JLabel proxyTitle = new JLabel(_("options.proxyTab"));
		proxyTitle.setFont(minecraft.deriveFont(13F));
		proxyTitle.setBounds(FRAME_WIDTH/2 -100, windowHeight.getY()+windowHeight.getHeight()+25, 200, 22);
		proxyTitle.setForeground(Color.WHITE);
		proxyTitle.setHorizontalAlignment(SwingConstants.CENTER);
		
		useProxyCheck.setAlignmentX(SwingConstants.RIGHT);
		useProxyCheck.setFont(minecraft.deriveFont(10F));
		useProxyCheck.setForeground(Color.WHITE);
		useProxyCheck.setOpaque(false);
		useProxyCheck.setBounds(FRAME_WIDTH-210, proxyTitle.getY()+proxyTitle.getHeight()+10, 200, 16);
		
		JLabel proxyHost = new JLabel(_("options.proxyHost"));
		proxyHost.setFont(minecraft.deriveFont(11F));
		proxyHost.setBounds(10, useProxyCheck.getY()+useProxyCheck.getHeight()+10, 200, 16);
		proxyHost.setForeground(Color.WHITE);
		proxyHost.setHorizontalAlignment(SwingConstants.LEFT);
		proxyHostText.setAlignmentX(SwingConstants.RIGHT);
		proxyHostText.setFont(minecraft.deriveFont(10F));
		proxyHostText.setBounds(FRAME_WIDTH-210, proxyHost.getY(), 200, 16);
		
		JLabel proxyPort = new JLabel(_("options.proxyPort"));
		proxyPort.setFont(minecraft.deriveFont(11F));
		proxyPort.setBounds(10, proxyHost.getY()+proxyHost.getHeight()+10, 200, 16);
		proxyPort.setForeground(Color.WHITE);
		proxyPort.setHorizontalAlignment(SwingConstants.LEFT);
		proxyPortText.setAlignmentX(SwingConstants.RIGHT);
		proxyPortText.setFont(minecraft.deriveFont(10F));
		proxyPortText.setBounds(FRAME_WIDTH-210, proxyPort.getY(), 200, 16);
		
		JLabel proxyUsername = new JLabel(_("options.proxyUsername"));
		proxyUsername.setFont(minecraft.deriveFont(11F));
		proxyUsername.setBounds(10, proxyPort.getY()+proxyPort.getHeight()+10, 200, 16);
		proxyUsername.setForeground(Color.WHITE);
		proxyUsername.setHorizontalAlignment(SwingConstants.LEFT);
		proxyUsernameText.setAlignmentX(SwingConstants.RIGHT);
		proxyUsernameText.setFont(minecraft.deriveFont(10F));
		proxyUsernameText.setBounds(FRAME_WIDTH-210, proxyUsername.getY(), 200, 16);
		
		JLabel proxyPassword = new JLabel(_("options.proxyPassword"));
		proxyPassword.setFont(minecraft.deriveFont(11F));
		proxyPassword.setBounds(10, proxyUsername.getY()+proxyUsername.getHeight()+10, 200, 16);
		proxyPassword.setForeground(Color.WHITE);
		proxyPassword.setHorizontalAlignment(SwingConstants.LEFT);
		proxyPasswordText.setAlignmentX(SwingConstants.RIGHT);
		proxyPasswordText.setFont(minecraft.deriveFont(10F));
		proxyPasswordText.setBounds(FRAME_WIDTH-210, proxyPassword.getY(), 200, 16);*/
		
		// Advanced Settings
		// Advanced Area
		SquareBox advancedArea = new SquareBox(M4EConstants.TRANSPARENT);
		advancedArea.setBounds(FRAME_WIDTH / 2 + 10, gameArea.getY() + gameArea.getHeight() + 10, FRAME_WIDTH / 2 - 20, FRAME_HEIGHT / 2 - 55);

		JLabel advancedTitle = new JLabel(SharedLocale.tr("options.advancedTab"));
		advancedTitle.setFont(minecraft.deriveFont(13F));
		advancedTitle.setBounds(advancedArea.getX() + advancedArea.getWidth() / 2 - 100, advancedArea.getY() + 10, 200, 22);
		advancedTitle.setForeground(Color.WHITE);
		advancedTitle.setHorizontalAlignment(SwingConstants.CENTER);

		JLabel gameKey = new JLabel(SharedLocale.tr("options.gameKey"));
		gameKey.setFont(minecraft.deriveFont(12F));
		gameKey.setBounds(advancedArea.getX() + 10, advancedTitle.getY() + advancedTitle.getHeight() + 10, 200, 25);
		gameKey.setForeground(Color.WHITE);
		gameKey.setHorizontalAlignment(SwingConstants.LEFT);
		gameKeyText.setAlignmentX(SwingConstants.RIGHT);
		gameKeyText.setFont(minecraft.deriveFont(12F));
		gameKeyText.setBounds(advancedArea.getX() + advancedArea.getWidth() - 160, gameKey.getY(), 150, 25);

		// Boton de editar URL de .craftbooturl
		editServerURL.setFont(minecraft.deriveFont(12F));
		editServerURL.setBounds(advancedArea.getX() + 10, gameKey.getY() + gameKey.getHeight() + 10, advancedArea.getWidth() - 20, 22);
		editServerURL.setForeground(Color.WHITE);

		JLabel gameVersion = new JLabel(SharedLocale.tr("launcher.version") + "  " + version);
		gameVersion.setFont(minecraft.deriveFont(12F));
		gameVersion.setBounds(advancedArea.getX() + 10, editServerURL.getY() + editServerURL.getHeight() + 10, 200, 25);
		gameVersion.setForeground(Color.WHITE);
		gameVersion.setHorizontalAlignment(SwingConstants.LEFT);

		// Boton de la consola del launcher
		logButton.setFont(minecraft.deriveFont(14F));
		logButton.setBounds(10, FRAME_HEIGHT - 40, FRAME_WIDTH / 2 - 15, 25);
		logButton.setForeground(Color.WHITE);

		// boton de ok, guardar
		okButton.setFont(minecraft.deriveFont(14F));
		okButton.setBounds(FRAME_WIDTH / 2 + 5, FRAME_HEIGHT - 40, FRAME_WIDTH / 2 - 25, 25);

		Container contentPane = getContentPane();

		contentPane.add(title);

		contentPane.add(javajvmPath);
		contentPane.add(jvmRuntime);
		contentPane.add(javajvmArguments);
		contentPane.add(jvmArgsText);
		contentPane.add(java64BitJavaWarning);
		contentPane.add(javaminMemory);
		contentPane.add(minMemorySpinner);
		contentPane.add(javamaxMemory);
		contentPane.add(maxMemorySpinner);
		contentPane.add(javapermGen);
		contentPane.add(permGenSpinner);
		contentPane.add(javaArea);
		contentPane.add(javaTitle);

		contentPane.add(windowWidth);
		contentPane.add(widthSpinner);
		contentPane.add(windowHeight);
		contentPane.add(heightSpinner);
		contentPane.add(offlineName);
		contentPane.add(offlineNameText);
		contentPane.add(gameArea);
		contentPane.add(gameTitle);

		/*
		contentPane.add(proxyTitle);
		contentPane.add(useProxyCheck);
		contentPane.add(proxyHost); contentPane.add(proxyHostText);
		contentPane.add(proxyPort); contentPane.add(proxyPortText);
		contentPane.add(proxyUsername); contentPane.add(proxyUsernameText);
		contentPane.add(proxyPassword); contentPane.add(proxyPasswordText);
		*/

		contentPane.add(gameKey);
		contentPane.add(gameKeyText);
		contentPane.add(editServerURL);
		contentPane.add(gameVersion);
		contentPane.add(advancedArea);
		contentPane.add(advancedTitle);

		contentPane.add(logButton);
		contentPane.add(okButton);
		contentPane.add(optionsQuit);
		contentPane.add(container);

		optionsQuit.addActionListener(ActionListeners.dispose(this));

		editServerURL.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					editCraftbootUrl();
				} catch (IOException ex) {
					ex.printStackTrace(System.err);
				}
			}
		});

		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				save();
			}
		});

		logButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ConsoleFrame.showMessages();
			}
		});
		
		jvmRuntime.addActionListener(e -> {
			// A little fun hack...
			if (jvmRuntime.getSelectedItem() == AddJavaRuntime.ADD_RUNTIME_SENTINEL) {
				jvmRuntime.setSelectedItem(null);
				jvmRuntime.setPopupVisible(false);

				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setFileFilter(new JavaRuntimeFileFilter());
				chooser.setDialogTitle("Choose a Java executable");

				int result = chooser.showOpenDialog(this);
				if (result == JFileChooser.APPROVE_OPTION) {
					JavaRuntime runtime = JavaRuntimeFinder.getRuntimeFromPath(chooser.getSelectedFile().getAbsolutePath());

					MutableComboBoxModel<JavaRuntime> model = (MutableComboBoxModel<JavaRuntime>) jvmRuntime.getModel();
					model.insertElementAt(runtime, 0);
					jvmRuntime.setSelectedItem(runtime);
				}
			}
		});
	}
	
	private JPanel createContainerPanel() {
		return new BackgroundPanel("optionsBackground.png", FRAME_WIDTH, FRAME_HEIGHT);
	}

	/**
	 * Save the configuration and close the dialog.
	 */
	public void save() {
		mapper.copyFromSwing();
		config.setJavaRuntime((JavaRuntime) jvmRuntime.getSelectedItem());
		Persistence.commitAndForget(config);
		dispose();
	}

	/**
	 * Save the configuration and close the dialog.
	 * 
	 * @throws java.io.IOException
	 */
	public void editCraftbootUrl() throws IOException {

		File cbu = new File(this.baseDir, ".craftbooturl");

		if (cbu.exists()) {

			// Get the actual link
			FileInputStream inputStream = new FileInputStream(cbu);
			String url = "";
			try {
				url = IOUtils.toString(inputStream);
			} finally {
				inputStream.close();
			}

			url = (String) JOptionPane.showInputDialog(null, "Edita la url a la que se conecta el launcher al iniciar:",
					"Editar Url del Servidor", JOptionPane.QUESTION_MESSAGE, null, null, url);

			if (url != null && !url.equals("") && url.endsWith("launcher.properties")) {
				FileUtils.writeStringToFile(cbu, url, null);
				JOptionPane.showMessageDialog(null, "La Url se ha modificado con éxito, reinice el launcher.", "Reinicio Necesario", JOptionPane.INFORMATION_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(null, "Url Inválida. No se ha modificado.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		} else {
			JOptionPane.showMessageDialog(null, "Error: .craftbooturl no existe", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	static class JavaRuntimeFileFilter extends FileFilter {
		@Override
		public boolean accept(File f) {
			return f.isDirectory() || f.getName().startsWith("java") && f.canExecute();
		}

		@Override
		public String getDescription() {
			return "Java runtime executables";
		}
	}
}
