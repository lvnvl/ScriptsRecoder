package com.android.uiautomator.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;

import com.android.ddmlib.IDevice;
import com.android.uiautomator.DebugBridge;
import com.android.uiautomator.NewScriptDialog;
import com.android.uiautomator.UiAutomatorHelper;
import com.android.uiautomator.UiAutomatorViewer;
import com.android.uiautomator.UiAutomatorHelper.UiAutomatorException;
import com.android.uiautomator.UiAutomatorHelper.UiAutomatorResult;
import com.android.uiautomator.UiAutomatorView;
import com.jack.model.AppiumConfig;
import com.jack.model.Constant;
import com.jack.utils.ErrorHandler;
import com.jack.utils.MyImageUtil;

public class NewScriptAction extends Action {

	private UiAutomatorViewer mViewer;
	
	public NewScriptAction(UiAutomatorViewer viewer){
		super("&New Script");
        mViewer = viewer;
//		System.out.println("constructor done!");
	}
	

	@Override
	public ImageDescriptor getImageDescriptor() {
		// TODO Auto-generated method stub
//		System.out.println("now go to get the new.png!");
//        return ImageHelper.loadImageDescriptorFromResource("images/new.png");
		return ImageDescriptor.createFromFile(null, "images/new.png");
	}

	@Override
	public void run() {
		List<IDevice> devices = DebugBridge.getDevices();
        if(devices.size() < 1){
        	MessageBox mb = new MessageBox(Display.getDefault().getActiveShell(), SWT.ICON_ERROR);
			mb.setMessage("no devices detected,please check and try again latter");
			mb.open();
			return;
        }
		// TODO Auto-generated method stub
		NewScriptDialog d = new NewScriptDialog(Display.getDefault().getActiveShell());
        if (d.open() != NewScriptDialog.OK) {
            return;
        }
        

        ProgressMonitorDialog dialog = new ProgressMonitorDialog(mViewer.getShell());
        try {
            dialog.run(true, false, new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException,
                                                                        InterruptedException {
                    UiAutomatorResult result = null;
                    UiAutomatorView mView = mViewer.getView();
                    System.out.println(this.getClass()+" try to start appium");
                    try {
                    	mView.setAppiumConfig(
                    			new AppiumConfig(NewScriptDialog.getsAPKFile(), 
                    			NewScriptDialog.getsIDevice(),
                    			NewScriptDialog.getmPort()));
                        System.out.println(this.getClass()+" appium init done");
                    	if(mView.getAppiumConfig().getDriver() == null){
                    		monitor.done();
                    		ErrorHandler.showError(mViewer.getShell()
                    				, "please check the params and retry!"
                    				, new Exception("appium inint error"));
                    		return;
                    	}
                    	UiAutomatorView.count = 0;
                    	monitor.worked(30);
                    	Thread.sleep(1*1000);
                    	monitor.worked(40);
                    	Thread.sleep(1*1000);
                    	monitor.worked(50);
                    	Thread.sleep(1*1000);
                        System.out.println(this.getClass()+" appium ok, go to set model");
                        result = UiAutomatorHelper.takeSnapshot(monitor, mView.getAppiumConfig().getDriver());
                        MyImageUtil.saveSWTImage(result.screenshot, Constant.SAVE);
                    } catch (UiAutomatorException e) {
                        monitor.done();
                        mView.getAppiumConfig().getDriver().quit();
                        ErrorHandler.showError(mViewer.getShell()
                        		, e.getMessage(), e);
                        return;
                    } catch (Exception e){
                    	e.printStackTrace();
                    	return;
                    }
//                    System.out.println("test driver init;currentActivity:" + AppiumConfig.getDriver().currentActivity());
                    mViewer.setModel(result.model, result.uiHierarchy, result.screenshot);
//                    AppiumConfig.getDriver().quit();
                    monitor.done();
                }
            });
        } catch (Exception e) {
            ErrorHandler.showError(mViewer.getShell()
            		, "Unexpected error while obtaining UI hierarchy", e);
        }
	}
}