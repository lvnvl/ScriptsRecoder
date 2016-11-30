package com.jack.utils;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class ErrorHandler {

	public ErrorHandler() {
		// TODO Auto-generated constructor stub
	}

    public static void showError(Shell shell, final String msg, final Throwable t) {
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                Status s = new Status(IStatus.ERROR, "Screenshot", msg, t);
                ErrorDialog.openError(
                		Display.getDefault().getActiveShell(), "Error", msg, s);
            }
        });
    }
}
