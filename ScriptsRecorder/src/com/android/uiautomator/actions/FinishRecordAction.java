package com.android.uiautomator.actions;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;

import com.android.uiautomator.NewScriptDialog;
import com.android.uiautomator.UiAutomatorView;
import com.jack.model.AppiumConfig;
import com.jack.utils.ErrorHandler;
import com.jack.utils.FileUtil;
import com.jack.utils.XmlUtils;

public class FinishRecordAction extends Action{
	private UiAutomatorView mView;
	public FinishRecordAction(UiAutomatorView view) {
		// TODO Auto-generated constructor stub
		super("&Finish Record");
        mView = view;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		if(mView.getAppiumConfig() == null){
    		MessageBox mb = new MessageBox(mView.getShell(), SWT.ICON_ERROR);
			mb.setMessage("请先录制再执行保存操作！");
			mb.open();
    		return;
    	}
		//then ,repaint and refresh the data
    	ProgressMonitorDialog dialog = new ProgressMonitorDialog(mView.getShell());
        try {
            dialog.run(true, false, new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException,
                InterruptedException {
                	monitor.subTask("Save Script ...");
            		//save operations and release resource
            		ArrayList<com.jack.model.Action> actions = mView.getActions();
            		String scriptDir = NewScriptDialog.getsScriptSaveDirectory();
            		XmlUtils.createXml(scriptDir,
            				NewScriptDialog.getsAPKFile().getAbsolutePath(), 
            				AppiumConfig.getmPackage(), 
            				String.valueOf(NewScriptDialog.getmPort()), 
            				AppiumConfig.getmActivity(), 
            				actions);
            		monitor.subTask("Save Script, 10%");
            		String deviceInfoSavePath = scriptDir + File.separator + "info.xml";
                	int width = mView.getAppiumConfig().getDriver().manage().window().getSize().width;
                    int height = mView.getAppiumConfig().getDriver().manage().window().getSize().height;
            		XmlUtils.createXml(deviceInfoSavePath, NewScriptDialog.getsIDevice(), width, height);
            		monitor.subTask("Save Script, 20%");
                	ArrayList<String> pages = mView.getPageSources();
                	if(pages != null && pages.size() > 0){
//                		File scriptFile = new File(script);
                		String pageSavePath = scriptDir + File.separator + "pages";
                		FileUtil.createDir(pageSavePath);
                		int i = 0;
                		for(String page:pages){
                			try {
								FileUtil.writeAll(pageSavePath + File.separator + String.valueOf(i) + ".xml", page);
			                	monitor.subTask("Save Script, " + String.valueOf((i++)*100/pages.size()) + " %");
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
                		}
                	}
//                	mView.getPageSources().clear();
                	monitor.subTask("Save Script done, quit appium...");
//                	mView.getAppiumConfig().getDriver().quit();
            		// kill appium
//                	mView.getAppiumConfig().close();
                	monitor.subTask("quit appium success, release page ..");
                	if (Display.getDefault().getThread() != Thread.currentThread()) {
                        Display.getDefault().syncExec(new Runnable() {
                            @Override
                            public void run() {
//                            	mView.setModel(null, null, null);
                            	mView.empty();
                            }
                        });
                    } else {
//                    	mView.setModel(null, null, null);
                    	mView.empty();
                    }
                	monitor.subTask("done");
//                    AppiumConfig.getDriver().quit();
                    monitor.done();
                    System.out.println("monitor done!!!Finish record!!!!!!!!!!!");
                }
            });
        } catch (Exception e) {
        	e.printStackTrace();
            ErrorHandler.showError(mView.getShell(), "Save script", e);
        }
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#setImageDescriptor(org.eclipse.jface.resource.ImageDescriptor)
	 */
	@Override
	public ImageDescriptor getImageDescriptor(){
		// TODO Auto-generated method stub
		return ImageDescriptor.createFromFile(null, "images/finish.png");
	}
}