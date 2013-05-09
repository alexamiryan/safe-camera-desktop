import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Security;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
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
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import utils.AESCrypt;
import utils.AESCrypt.CryptoProgress;
import utils.AESCryptException;

public class SafeCamera extends ApplicationWindow {
	private Text text;
	private Label imgLabel = null;
	private ProgressBar progressBar;
	private File currentFile = null;

	private Image currentImage = null;

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
		new Label(container, SWT.NONE);

		text = new Text(container, SWT.BORDER);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		text.setLayoutData(gridData);
		text.setEchoChar('*');

		Button btnOpen = new Button(container, SWT.NONE);
		btnOpen.setText("Open");

		btnOpen.addSelectionListener(openSelect());

		Button btnDecrypt = new Button(container, SWT.NONE);
		btnDecrypt.setText("Decrypt");
		btnDecrypt.addSelectionListener(decryptCurrent());

		progressBar = new ProgressBar(container, SWT.NONE);
		progressBar.setVisible(false);
		gridData = new GridData();
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
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);

		ResizeListener listener = new ResizeListener();
		imgLabel.addControlListener(listener);
		imgLabel.getDisplay().addFilter(SWT.MouseDown, listener);
		imgLabel.getDisplay().addFilter(SWT.MouseUp, listener);

		return container;
	}

	private SelectionAdapter openSelect() {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(getShell(), SWT.NULL);
				String path = dialog.open();
				if (path != null) {
					currentFile = new File(path);
					if (currentFile.isFile()) {

						final String enteredPassword = new String(text.getText());

						Thread decrypt = new Thread(new Runnable() {

							@Override
							public void run() {
								try {
									String enteredPasswordHash = AESCrypt.byteToHex(AESCrypt.getHash(enteredPassword));
									AESCrypt crypt = new AESCrypt(enteredPasswordHash);

									FileInputStream input = new FileInputStream(currentFile);

									AESCrypt.CryptoProgress cryptProgress = new CryptoProgress(input.getChannel().size()) {
										@Override
										public void setProgress(long pCurrent) {
											super.setProgress(pCurrent);
											progressBar.setSelection(this.getProgressPercents());
										}
									};

									byte[] decryptedImage = crypt.decrypt(input, cryptProgress);

									if (decryptedImage != null) {
										InputStream istream = new ByteArrayInputStream(decryptedImage);

										ImageData imageData = new ImageData(istream);
										currentImage = new Image(Display.getDefault(), imageData);
										if (imgLabel != null) {
											imgLabel.setImage(resize(currentImage, imgLabel.getSize().x, imgLabel.getSize().y));
											progressBar.setVisible(false);
										}
									}
									else {
										System.out.println("Unable to decrypt");
									}
								}
								catch (FileNotFoundException e1) {
									e1.printStackTrace();
								}
								catch (AESCryptException e2) {
									e2.printStackTrace();
								}
								catch (IOException e) {
									e.printStackTrace();
								}
							}
						});
						progressBar.setVisible(true);
						decrypt.run();
					}
				}
			}
		};
	}

	private SelectionAdapter decryptCurrent() {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
				String path = dialog.open();
				if (path != null) {

					final File destFile = new File(path);
					if (currentFile!= null && currentFile.isFile() && currentFile.exists()) {

						final String enteredPassword = new String(text.getText());

						Thread decryptFile = new Thread(new Runnable() {

							@Override
							public void run() {
								try {
									String enteredPasswordHash = AESCrypt.byteToHex(AESCrypt.getHash(enteredPassword));
									AESCrypt crypt = new AESCrypt(enteredPasswordHash);

									// String destFileName =
									// Helpers.decryptFilename(activity,
									// file.getName());

									FileInputStream inputStream = new FileInputStream(currentFile);
									FileOutputStream outputStream = new FileOutputStream(destFile);

									AESCrypt.CryptoProgress cryptProgress = new CryptoProgress(inputStream.getChannel().size()) {
										@Override
										public void setProgress(long pCurrent) {
											super.setProgress(pCurrent);
											progressBar.setSelection(this.getProgressPercents());
										}
									};

									crypt.decrypt(inputStream, outputStream, cryptProgress);
									progressBar.setVisible(false);

								}
								catch (FileNotFoundException e1) {
									e1.printStackTrace();
								}
								catch (AESCryptException e2) {
									e2.printStackTrace();
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
	/*
	 * @Override protected MenuManager createMenuManager() { MenuManager
	 * menuManager = new MenuManager("menu"); return menuManager; }
	 */

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
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("SafeCamera Desktop");
		newShell.setSize(640, 480);
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

}
