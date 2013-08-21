package com.fenritz.safecamdesktop;

import java.io.File;
import java.util.prefs.Preferences;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import utils.Helpers;

public class PreferencesDialog extends Dialog {
	private Text mainFolder;
	private Text outputFolder;
	
	private String mainFolderPath, outFolderPath;

	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public PreferencesDialog(Shell parentShell) {
		super(parentShell);
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(3, false));
		
		Label lblSafecameraFolder = new Label(container, SWT.NONE);
		lblSafecameraFolder.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblSafecameraFolder.setText("SafeCamera folder:");
		
		mainFolder = new Text(container, SWT.BORDER);
		mainFolder.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		mainFolderPath = Helpers.getMainFolderPath();
		outFolderPath = Helpers.getOutputFolderPath();
		
		mainFolder.setText(mainFolderPath);
		
		Button mainFolderBrowse = new Button(container, SWT.NONE);
		mainFolderBrowse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(getShell());
				String dir = dialog.open();
				if (dir != null) {
					File currentDir = new File(dir);
					if (currentDir.isDirectory()) {
						mainFolderPath = currentDir.getPath();
						mainFolder.setText(mainFolderPath);
					}
				}
			}
		});
		mainFolder.addKeyListener(new KeyListener() {
			
			@Override
			public void keyReleased(KeyEvent e) {
				mainFolderPath = mainFolder.getText();
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				mainFolderPath = mainFolder.getText();
			}
		});
		mainFolderBrowse.setText("Browse");
		
		Label lblOutputFolder = new Label(container, SWT.NONE);
		lblOutputFolder.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblOutputFolder.setText("Output folder:");
		
		outputFolder = new Text(container, SWT.BORDER);
		outputFolder.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		outputFolder.setText(outFolderPath);
		
		Button outputFolderBrowse = new Button(container, SWT.NONE);
		outputFolderBrowse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(getShell());
				String dir = dialog.open();
				if (dir != null) {
					File currentDir = new File(dir);
					if (currentDir.isDirectory()) {
						outFolderPath = currentDir.getPath();
						outputFolder.setText(outFolderPath);
					}
				}
			}
		});
		outputFolder.addKeyListener(new KeyListener() {
			
			@Override
			public void keyReleased(KeyEvent e) {
				outFolderPath = outputFolder.getText();
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				
			}
		});
		outputFolderBrowse.setText("Browse");
		getShell().setText("Preferences");
		return container;
	}

	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		Button button = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Preferences prefs = Preferences.userNodeForPackage(com.fenritz.safecamdesktop.SafeCamera.class);
				prefs.put(SafeCamera.PREF_MAINDIR, mainFolderPath);
				prefs.put(SafeCamera.PREF_OUTDIR, outFolderPath);
			}
		});
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(450, 300);
	}

}
