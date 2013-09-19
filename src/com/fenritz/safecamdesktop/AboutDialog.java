package com.fenritz.safecamdesktop;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

public class AboutDialog extends Dialog {
	
	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public AboutDialog(Shell parentShell) {
		super(parentShell);
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(2, false));
		
		Label lblNewLabel_1 = new Label(container, SWT.FILL);
		lblNewLabel_1.setImage(SWTResourceManager.getImage(AboutDialog.class, "/com/fenritz/safecamdesktop/safe-camera64.png"));
		
		Label lblNewLabel = new Label(container, SWT.NONE);
		lblNewLabel.setAlignment(SWT.CENTER);
		GridData gd_lblNewLabel = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_lblNewLabel.widthHint = 229;
		lblNewLabel.setLayoutData(gd_lblNewLabel);
		lblNewLabel.setText("Safe Camera v1.0\n\nGPL v3");
		new Label(container, SWT.NONE);
		
		Text siteUrl = new Text(container, SWT.READ_ONLY | SWT.CENTER);
		GridData gd_siteUrl = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_siteUrl.widthHint = 232;
		siteUrl.setLayoutData(gd_siteUrl);
		siteUrl.setText("http://www.safecamera.org/");
		
		getShell().setText("About");
		return container;
	}

	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.OK_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(317, 206);
	}

}
