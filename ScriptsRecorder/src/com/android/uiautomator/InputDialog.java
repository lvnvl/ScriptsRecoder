package com.android.uiautomator;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class InputDialog extends Dialog{
    private static final int FIXED_TEXT_FIELD_WIDTH = 300;
    private static final int DEFAULT_LAYOUT_SPACING = 10;
	private static final int FIXED_TEXT_FIELD_HEIGHT = 50;

    private Text mInputText;
    private static String input;
    
	public InputDialog(Shell parentShell) {
		super(parentShell);
		// TODO Auto-generated constructor stub
		setShellStyle(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
	}

	@Override
	protected Control createDialogArea(Composite parent){
		Composite container = (Composite) super.createDialogArea(parent);
        GridLayout gl_container = new GridLayout(1, false);
        gl_container.verticalSpacing = DEFAULT_LAYOUT_SPACING;
        gl_container.horizontalSpacing = DEFAULT_LAYOUT_SPACING;
        gl_container.marginWidth = DEFAULT_LAYOUT_SPACING;
        gl_container.marginHeight = DEFAULT_LAYOUT_SPACING;
        container.setLayout(gl_container);
      
        //send keys composite
        Group inputGroup = new Group(container, SWT.NONE);
        inputGroup.setLayout(new GridLayout(1, true));
        inputGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        inputGroup.setText("Send Keys");

        mInputText = new Text(inputGroup, SWT.BORDER);
        GridData gd_inputText = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        gd_inputText.minimumWidth = FIXED_TEXT_FIELD_WIDTH;
        gd_inputText.minimumHeight = FIXED_TEXT_FIELD_HEIGHT;
        gd_inputText.widthHint = FIXED_TEXT_FIELD_WIDTH;
        mInputText.setLayoutData(gd_inputText);
        mInputText.addDisposeListener(new DisposeListener(){

			@Override
			public void widgetDisposed(DisposeEvent e) {
				// TODO Auto-generated method stub
				input = mInputText.getText();
			}});
        return container;
	}

	/**
     * Return the initial size of the dialog.
     */
    @Override
    protected Point getInitialSize() {
        return new Point(368, 468);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("input time(ms)");
    }

	public static String getInput() {
		return input;
	}
}