package com.android.uiautomator.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;

import com.android.uiautomator.InputDialog;
import com.android.uiautomator.UiAutomatorView;
import com.jack.model.Operate;

public class AddSleepAction extends Action{
	private UiAutomatorView mView;
	public AddSleepAction(UiAutomatorView view) {
		// TODO Auto-generated constructor stub
		super("&TimeWait");
        mView = view;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		// TODO Auto-generated method stub
		// refresh and repaint the current page
		// insert a sleep action to script
		if(mView.getAppiumConfig() == null){
    		MessageBox mb = new MessageBox(mView.getShell(), SWT.ICON_ERROR);
			mb.setMessage("请先开始录制！");
			mb.open();
    		return;
    	}
		InputDialog idg = new InputDialog(mView.getShell());
		if(idg.open() != InputDialog.OK){
			return;
		}
		if(InputDialog.getInput() == null || InputDialog.getInput().trim().equals("")){
			return;
		}
//		com.jack.model.Action sleepAction = new com.jack.model.Action(Operate.SLEEP);
//		sleepAction.setArgument(InputDialog.getInput());
//		mView.getActions().add(sleepAction);
//		mView.getmListViewer().refresh(false);
		mView.performAction(mView.updateAction(null, Operate.SLEEP, InputDialog.getInput()));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#setImageDescriptor(org.eclipse.jface.resource.ImageDescriptor)
	 */
	@Override
	public ImageDescriptor getImageDescriptor(){
		// TODO Auto-generated method stub
		return ImageDescriptor.createFromFile(null, "images/sleep.png");
	}
}
