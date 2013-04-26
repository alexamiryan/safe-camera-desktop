import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.Security;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import utils.AESCrypt;
import utils.AESCryptException;

public class SafeCamera extends ApplicationWindow {
	private Text text;
	private Label imgLabel = null;

	/**
	 * Create the application window.
	 */
	public SafeCamera() {
		super(null);
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		createActions();
		addToolBar(SWT.FLAT | SWT.WRAP);
		addMenuBar();
		addStatusLine();
	}

	/**
	 * Create contents of the application window.
	 * 
	 * @param parent
	 */
	@Override
	protected Control createContents(final Composite parent) {
		final Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		Composite composite = new Composite(container, SWT.NONE);
		composite.setBounds(0, 0, 634, 364);
				composite.setLayout(new GridLayout(2, false));
		
				text = new Text(composite, SWT.BORDER);
				text.setEchoChar('*');
				Button btnOpen = new Button(composite, SWT.NONE);
				btnOpen.setText("Open");
				
						
						imgLabel = new Label(composite, SWT.NONE);
						new Label(composite, SWT.NONE);

		btnOpen.addSelectionListener(openSelect());

		return container;
	}

	private SelectionAdapter openSelect(){
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(getShell(), SWT.NULL);
				String path = dialog.open();
				if (path != null) {
					
					try{
						File file = new File(path);
						if (file.isFile()){
							String enteredPassword = new String(text.getText());
							String enteredPasswordHash = AESCrypt.byteToHex(AESCrypt.getHash(enteredPassword));
							AESCrypt crypt = new AESCrypt(enteredPasswordHash);
							byte[] decryptedImage = crypt.decrypt(new FileInputStream(file));
							
							if(decryptedImage != null){
								InputStream istream = new ByteArrayInputStream(decryptedImage);
								
								ImageData imageData = new ImageData(istream);
								Image image = new Image(Display.getDefault(), imageData);
								if(imgLabel != null){
									imgLabel.setImage(image);
								}
							}
							else{
								System.out.println("Unable to decrypt");
							}
							
						}
						else{
							
						}
					}
					catch(FileNotFoundException e1){
						e1.printStackTrace();
					}
					catch (AESCryptException e2) {
						e2.printStackTrace();
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

	/**
	 * Create the menu manager.
	 * 
	 * @return the menu manager
	 */
	@Override
	protected MenuManager createMenuManager() {
		MenuManager menuManager = new MenuManager("menu");
		return menuManager;
	}

	/**
	 * Create the toolbar manager.
	 * 
	 * @return the toolbar manager
	 */
	@Override
	protected ToolBarManager createToolBarManager(int style) {
		ToolBarManager toolBarManager = new ToolBarManager(style);
		return toolBarManager;
	}

	/**
	 * Create the status line manager.
	 * 
	 * @return the status line manager
	 */
	@Override
	protected StatusLineManager createStatusLineManager() {
		StatusLineManager statusLineManager = new StatusLineManager();
		return statusLineManager;
	}

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
	@Override
	protected Point getInitialSize() {
		return new Point(743, 619);
	}
	
}
