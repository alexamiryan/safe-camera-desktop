package com.fenritz.safecamdesktop;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import utils.AESCrypt;
import utils.AESCrypt.CryptoProgress;
import utils.AESCryptException;
import utils.Helpers;

public class SafeCamera extends ApplicationWindow {
	private Label imgLabel = null;
	private ProgressBar progressBar;
	private File currentFile = null;
	private String currentPath = null;
	private final ArrayList<File> files = new ArrayList<File>();
	
	public final static String PREF_MAINDIR = "maindir";
	public final static String PREF_OUTDIR = "outdir";

	private Image currentImage = null;

	public static AESCrypt crypto = null;

	private Menu menu;
	private MenuItem decryptItem, decryptAsItem;

	/**
	 * Create the application window.
	 */
	public SafeCamera() {
		super(null);
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		createActions();
		/*
		 * addToolBar(SWT.FLAT | SWT.WRAP); addMenuBar(); addStatusLine();
		 */
	}

	/**
	 * Create contents of the application window.
	 * 
	 * @param parent
	 */
	@Override
	protected Control createContents(final Composite parent) {
		setStatus("");
		final Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(4, false));

		progressBar = new ProgressBar(container, SWT.NONE);
		progressBar.setVisible(false);
		GridData gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalAlignment = SWT.WRAP;
		gridData.grabExcessVerticalSpace = false;
		progressBar.setLayoutData(gridData);

		imgLabel = new Label(container, SWT.CENTER);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalAlignment = SWT.FILL;
		gridData.grabExcessVerticalSpace = true;
		imgLabel.setLayoutData(gridData);

		ResizeListener listener = new ResizeListener();
		imgLabel.addControlListener(listener);
		imgLabel.getDisplay().addFilter(SWT.MouseDown, listener);
		imgLabel.getDisplay().addFilter(SWT.MouseUp, listener);

		Display.getCurrent().addFilter(SWT.KeyDown, new Listener() {

			@Override
			public void handleEvent(Event e) {
				if (e.keyCode == 16777220 || e.keyCode == 32) {
					showNextImage();
				}
				else if (e.keyCode == 16777219 || e.keyCode == 8) {
					showPrevImage();
				}
				/*
				 * if(((e.stateMask & SWT.CTRL) == SWT.CTRL) && (e.keyCode ==
				 * 'f')){ System.out.println("From Display I am the Key down !!"
				 * + e.keyCode); }
				 */
			}
		});

		return container;
	}

	public boolean ensurePassword() {
		return ensurePassword(false);
	}

	public boolean ensurePassword(boolean retryCurrentImage) {

		if (SafeCamera.crypto != null) {
			return true;
		}
		Shell parent = getParentShell();
		final Shell shell = new Shell(parent, SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL | SWT.CENTER);
		shell.setText("Please enter password");
		shell.setSize(380, 150);
		shell.setLayout(new GridLayout(2, true));

		centerShell(shell);

		Label label = new Label(shell, SWT.NULL);
		label.setText("Please enter password:");

		final Text text = new Text(shell, SWT.SINGLE | SWT.BORDER);
		text.setEchoChar('*');
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		text.setLayoutData(gridData);

		text.addKeyListener(new KeyListener() {

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.character == SWT.CR || e.character == SWT.LF) {
					try {
						SafeCamera.crypto = new AESCrypt(AESCrypt.byteToHex(AESCrypt.getHash(new String(text.getText()))));
						shell.dispose();
					}
					catch (AESCryptException e1) {
						e1.printStackTrace();
					}
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {

			}
		});

		final Button buttonOK = new Button(shell, SWT.PUSH);
		buttonOK.setText("Ok");
		buttonOK.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		Button buttonCancel = new Button(shell, SWT.PUSH);
		buttonCancel.setText("Cancel");

		buttonOK.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				try {
					SafeCamera.crypto = new AESCrypt(AESCrypt.byteToHex(AESCrypt.getHash(new String(text.getText()))));
					shell.dispose();
				}
				catch (AESCryptException e) {
					e.printStackTrace();
				}
			}
		});

		buttonCancel.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {

				shell.dispose();
			}
		});
		shell.addListener(SWT.Traverse, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (event.detail == SWT.TRAVERSE_ESCAPE) event.doit = false;
			}
		});

		text.setText("");
		shell.pack();
		shell.open();

		Display display = Display.getDefault();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}

		if (SafeCamera.crypto != null) {
			if (retryCurrentImage) {
				showCurrentImage();
			}
			return true;
		}
		else {
			return false;
		}
	}

	private void showNextImage() {
		if (files != null && files.size() > 0) {
			int currentIndex = files.indexOf(currentFile);
			if (currentIndex + 1 < files.size()) {
				currentFile = files.get(currentIndex + 1);
				showCurrentImage();
			}
		}
	}

	private void showPrevImage() {
		if (files != null && files.size() > 0) {
			int currentIndex = files.indexOf(currentFile);
			if (currentIndex - 1 >= 0) {
				currentFile = files.get(currentIndex - 1);
				showCurrentImage();
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void fillFilesList() {
		if (currentFile != null && currentFile.isFile() && currentFile.exists()) {
			if (currentPath == null || !currentPath.equals(currentFile.getParent())) {
				File dir = new File(currentFile.getParent());
				File[] folderFiles = dir.listFiles();

				Arrays.sort(folderFiles, (new utils.NaturalOrderComparator()));

				files.clear();

				int maxFileSize = Helpers.MAX_FILE_SIZE * 1024 * 1024;
				for (File file : folderFiles) {
					if (file.getName().endsWith(Helpers.SC_EXTENSION) && file.length() < maxFileSize) {
						files.add(file);

					}
				}

				currentPath = currentFile.getParent();
			}
		}
	}

	private void showCurrentImage() {
		fillFilesList();
		if (ensurePassword()) {
			Thread decrypt = new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						FileInputStream input = new FileInputStream(currentFile);

						AESCrypt.CryptoProgress cryptProgress = new CryptoProgress(input.getChannel().size()) {
							@Override
							public void setProgress(long pCurrent) {
								super.setProgress(pCurrent);
								progressBar.setSelection(this.getProgressPercents());
							}
						};

						byte[] decryptedImage = SafeCamera.crypto.decrypt(input, cryptProgress);

						if (decryptedImage != null) {
							ByteArrayInputStream istream = new ByteArrayInputStream(decryptedImage);
							ImageData imageData = new ImageData(istream);
							
							Integer rotation = Helpers.getAltExifRotation(new BufferedInputStream(new ByteArrayInputStream(decryptedImage)));

							int finalRotation = 0;
							
							switch(rotation){
								case 90:
									finalRotation = SWT.RIGHT;
									break;
								case 180:
									finalRotation = SWT.DOWN;
									break;
								case 270:
									finalRotation = SWT.LEFT;
									break;
							}
							if(finalRotation != 0){
								imageData = Helpers.rotate(imageData, finalRotation);
							}
							currentImage = new Image(Display.getDefault(), imageData);
							if (imgLabel != null) {
								imgLabel.setImage(resize(currentImage, imgLabel.getSize().x, imgLabel.getSize().y));
								progressBar.setVisible(false);
								getShell().setText(Helpers.decryptFilename(currentFile.getName()) + " - SafeCamera Desktop");
								decryptItem.setEnabled(true);
								decryptAsItem.setEnabled(true);
							}
						}
						else {
							// System.out.println("Unable to decrypt");
							SafeCamera.crypto = null;
							ensurePassword(true);
						}
					}
					catch (FileNotFoundException e1) {
						e1.printStackTrace();
					}
					catch (IOException e) {
						e.printStackTrace();
					}
					catch (Exception e) {
						e.printStackTrace();
						SafeCamera.crypto = null;
						ensurePassword(true);
					}
				}
			});
			progressBar.setVisible(true);
			decrypt.run();
		}
	}

	private SelectionAdapter openSelect() {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(getShell(), SWT.NULL);
				dialog.setFilterPath(Helpers.getMainFolderPath());
				String path = dialog.open();
				if (path != null) {
					currentFile = new File(path);
					if (currentFile.isFile()) {
						showCurrentImage();
					}
				}
			}
		};
	}
	
	private SelectionAdapter encryptSelect() {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(getShell(), SWT.MULTI);
				dialog.setFilterPath(Helpers.getMainFolderPath());
				String path = dialog.open();
				if (path != null) {
					final File outputFolder = new File(Helpers.getMainFolderPath());
					if(!outputFolder.exists()){
						outputFolder.mkdirs();
					}
					
					String[] files = dialog.getFileNames();
					if (files.length > 0 && ensurePassword()) {
						for(String filePath : files){
							final File fileToEncrypt = new File(dialog.getFilterPath() + "/" + filePath);
							final String destinationPath = Helpers.getNewDestinationPath(outputFolder.getPath(), fileToEncrypt.getName(), SafeCamera.crypto);
							if(destinationPath.length() - outputFolder.getPath().length() > 256){
								Helpers.errorDialog(getShell(), "Sorry filename "+fileToEncrypt.getName()+" is too long");
							}
							else{
								if (fileToEncrypt.isFile()) {
									Thread encryptFile = new Thread(new Runnable() {
										@Override
										public void run() {
											try {
												FileInputStream inputStream = new FileInputStream(fileToEncrypt);
												FileOutputStream outputStream = new FileOutputStream(destinationPath);
	
												AESCrypt.CryptoProgress cryptProgress = new CryptoProgress(inputStream.getChannel().size()) {
													@Override
													public void setProgress(long pCurrent) {
														super.setProgress(pCurrent);
														progressBar.setSelection(this.getProgressPercents());
													}
												};
	
												SafeCamera.crypto.encrypt(inputStream, outputStream, cryptProgress);
												progressBar.setVisible(false);
	
											}
											catch (FileNotFoundException e1) {
												e1.printStackTrace();
											}
											catch (IOException e) {
												e.printStackTrace();
											}
										}
									});
									progressBar.setVisible(true);
									encryptFile.run();
								}
							}
						}
					}
				}
			}
		};
	}

	private SelectionAdapter decryptCurrent(final boolean toDefaultLocation) {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (currentFile != null && currentFile.isFile() && currentFile.exists()) {
					String path;
					if(toDefaultLocation){
						path = Helpers.ensureLastSlash(Helpers.getOutputFolderPath()) + Helpers.decryptFilename(currentFile.getName());
						File decDir = new File(Helpers.getOutputFolderPath());
						if(!decDir.exists()){
							decDir.mkdirs();
						}
					}
					else{
						FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
						dialog.setFileName(Helpers.decryptFilename(currentFile.getName()));
						path = dialog.open();
					}
					if (path != null) {

						final File destFile = new File(path);

						if (ensurePassword()) {

							Thread decryptFile = new Thread(new Runnable() {

								@Override
								public void run() {
									try {
										FileInputStream inputStream = new FileInputStream(currentFile);
										FileOutputStream outputStream = new FileOutputStream(destFile);

										AESCrypt.CryptoProgress cryptProgress = new CryptoProgress(inputStream.getChannel().size()) {
											@Override
											public void setProgress(long pCurrent) {
												super.setProgress(pCurrent);
												progressBar.setSelection(this.getProgressPercents());
											}
										};

										SafeCamera.crypto.decrypt(inputStream, outputStream, cryptProgress);
										progressBar.setVisible(false);

									}
									catch (FileNotFoundException e1) {
										e1.printStackTrace();
									}
									catch (IOException e) {
										e.printStackTrace();
									}
								}
							});
							progressBar.setVisible(true);
							decryptFile.run();
						}
					}
				}
			}
		};
	}

	/**
	 * Create the actions.
	 */
	private void createActions() {
		// Create the actions
	}

	private Image resize(Image image, int width, int height) {
		int origWidth = image.getBounds().width;
		int origHeight = image.getBounds().height;

		int resultingWidth = 0, resultingHeight = 0;

		int mode = 0;

		if (width == 0 && height == 0) {
			return image;
		}

		if (width == 0) {
			mode = 2;
		}
		else if (height == 0) {
			mode = 1;
		}
		else {
			int future_width = origWidth * height / origHeight;
			int future_height = origHeight * width / origWidth;
			if (origWidth >= width || origHeight >= height) {
				if (origWidth >= origHeight) {
					if (future_height <= height) {
						mode = 1;
					}
					else {
						mode = 2;
					}
				}
				else if (origWidth < origHeight) {
					if (future_width <= width) {
						mode = 2;
					}
					else {
						mode = 1;
					}
				}
			}
		}
		if (mode == 1) {
			resultingWidth = width;
			resultingHeight = origHeight * width / origWidth;
		}
		else if (mode == 2) {
			resultingWidth = origWidth * height / origHeight;
			resultingHeight = height;
		}
		else {
			return image;
		}

		Image scaled = new Image(Display.getDefault(), resultingWidth, resultingHeight);
		GC gc = new GC(scaled);
		gc.setAntialias(SWT.ON);
		gc.setInterpolation(SWT.HIGH);

		gc.drawImage(image, 0, 0, origWidth, origHeight, 0, 0, resultingWidth, resultingHeight);
		gc.dispose();
		// image.dispose(); // don't forget about me!
		return scaled;
	}

	/**
	 * Create the menu manager.
	 * 
	 * @return the menu manager
	 */

	/*@Override
	protected MenuManager createMenuManager() {
		MenuManager menuManager = new MenuManager("menu");
		return menuManager;
	}*/

	/**
	 * Create the toolbar manager.
	 * 
	 * @return the toolbar manager
	 */
	/*
	 * @Override protected ToolBarManager createToolBarManager(int style) {
	 * ToolBarManager toolBarManager = new ToolBarManager(style); return
	 * toolBarManager; }
	 */

	/**
	 * Create the status line manager.
	 * 
	 * @return the status line manager
	 */
	/*
	 * @Override protected StatusLineManager createStatusLineManager() {
	 * StatusLineManager statusLineManager = new StatusLineManager(); return
	 * statusLineManager; }
	 */

	/**
	 * Launch the application.
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			SafeCamera window = new SafeCamera();
			window.setBlockOnOpen(true);
			window.open();
			Display.getCurrent().dispose();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Configure the shell.
	 * 
	 * @param newShell
	 */
	@Override
	protected void configureShell(final Shell shell) {
		super.configureShell(shell);
		shell.setText("SafeCamera Desktop");
		shell.setSize(640, 480);

		centerShell(shell);

		menu = new Menu(shell, SWT.BAR);
		MenuItem fileItem = new MenuItem(menu, SWT.CASCADE);
		fileItem.setText("File");
		
		MenuItem toolsItem = new MenuItem(menu, SWT.CASCADE);
		toolsItem.setText("Tools");

		MenuItem helpItem = new MenuItem(menu, SWT.CASCADE);
		helpItem.setText("Help");

		Menu fileMenu = new Menu(menu);
		fileItem.setMenu(fileMenu);
		
		Menu toolsMenu = new Menu(menu);
		toolsItem.setMenu(toolsMenu);
		
		MenuItem openItem = new MenuItem(fileMenu, SWT.NONE);
		openItem.setText("Open...");
		openItem.addSelectionListener(openSelect());
		
		decryptItem = new MenuItem(fileMenu, SWT.NONE);
		decryptItem.setText("Decrypt");
		decryptAsItem = new MenuItem(fileMenu, SWT.NONE);
		decryptAsItem.setText("Decrypt As...");
		
		MenuItem prefsItem = new MenuItem(fileMenu, SWT.NONE);
		prefsItem.setText("Preferences");
				
		MenuItem exitItem = new MenuItem(fileMenu, SWT.NONE);
		exitItem.setText("Exit");

		decryptItem.addSelectionListener(decryptCurrent(true));
		decryptAsItem.addSelectionListener(decryptCurrent(false));
		
		decryptItem.setEnabled(false);
		decryptAsItem.setEnabled(false);
		
		MenuItem encryptItem = new MenuItem(toolsMenu, SWT.NONE);
		encryptItem.setText("Encrypt Files...");
		encryptItem.addSelectionListener(encryptSelect());
		
		prefsItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				new PreferencesDialog(getShell()).open();
			}
		});
		
		exitItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shell.getDisplay().dispose();
				System.exit(0);
			}
		});

		shell.setMenuBar(menu);
	}

	/**
	 * Return the initial size of the window.
	 */
	/*
	 * @Override protected Point getInitialSize() { return new Point(743, 619);
	 * }
	 */

	private class ResizeListener implements ControlListener, Runnable, Listener {

		private long lastEvent = 0;

		private boolean mouse = true;

		@Override
		public void controlMoved(ControlEvent e) {
		}

		@Override
		public void controlResized(ControlEvent e) {
			lastEvent = System.currentTimeMillis();
			Display.getDefault().timerExec(500, this);
		}

		@Override
		public void run() {
			if ((lastEvent + 500) < System.currentTimeMillis() && mouse) {
				if (currentImage != null) {
					imgLabel.setImage(resize(currentImage, imgLabel.getSize().x, imgLabel.getSize().y));
				}
			}
			else {
				Display.getDefault().timerExec(500, this);
			}
		}

		@Override
		public void handleEvent(Event event) {
			mouse = event.type == SWT.MouseUp;
		}

	}

	public static void centerShell(Shell shell) {
		Monitor primary = Display.getCurrent().getPrimaryMonitor();
		Rectangle bounds = primary.getBounds();
		Rectangle rect = shell.getBounds();
		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y + (bounds.height - rect.height) / 2;
		shell.setLocation(x, y);
	}
}
