package com.android.uiautomator.actions;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import com.android.ddmlib.IDevice;
import com.android.uiautomator.DebugBridge;
import com.android.uiautomator.RunScriptDialog;
import com.android.uiautomator.UiAutomatorViewer;
import com.jack.model.ScriptConfig;
import com.jack.model.ScriptRunner;
import com.jack.utils.FileUtil;
import com.jack.utils.XmlUtils;

public class RunScriptAction extends Action {

	private UiAutomatorViewer mViewer;
	private ArrayList<Thread> threads;
	public RunScriptAction(UiAutomatorViewer viewer) {
		super("&Run Script");
        mViewer = viewer;
        threads = new ArrayList<Thread>();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#getImageDescriptor()
	 */
	@Override
	public ImageDescriptor getImageDescriptor() {
		// TODO Auto-generated method stub
		return ImageDescriptor.createFromFile(null, "images/run.png");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		List<IDevice> devices = DebugBridge.getDevices();
		threads.clear(); // 再同一打开的窗口中第二次回放脚本时会报错，因为，这个list中的线程thread没有释放，会使其二次启动，导致异常。
        if(devices.size() < 1){
        	MessageBox mb = new MessageBox(Display.getDefault().getActiveShell(), SWT.ICON_ERROR);
			mb.setMessage("no devices detected,please check and try again latter");
			mb.open();
			return;
        }
		// TODO Auto-generated method stub
        RunScriptDialog d = new RunScriptDialog(Display.getDefault().getActiveShell());
        if (d.open() != RunScriptDialog.OK) {
            return;
        }

        if(RunScriptDialog.getsIDevices() == null || RunScriptDialog.getsIDevices().size() < 1){
        	showError("no devices selected", new Exception("please select at lease one device"));
        	return;
        }
        System.out.println("selected device num:" + RunScriptDialog.getsIDevices().size());
        
        ProgressMonitorDialog dialog = new ProgressMonitorDialog(mViewer.getShell());
        try {
            dialog.run(true, false, new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException,
                InterruptedException {     
                	monitor.subTask("Creating threads ...Start");
                	/**
                	 * use devices, pages, uiautomatorview and ScriptConfig to create thread
                	 * */
                	ArrayList<IDevice> devices = RunScriptDialog.getsIDevices();
                    ArrayList<String> pages = new ArrayList<String>();
                    String pagesPath = RunScriptDialog.getsScriptFile().getParent() + File.separator + "pages";
                	File pagesFilePath = new File(pagesPath);
                	if(!pagesFilePath.exists()){
                		monitor.done();
                    	showError("page infos not exist in " + pagesPath, new Exception("script pages not found"));
                    	return;
                	}
                	for(int i = 0;i < pagesFilePath.list().length;i ++){
                		try {
							pages.add(FileUtil.readAll(pagesPath + File.separator + String.valueOf(i) + ".xml"));
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                	}
                    ScriptConfig sc = XmlUtils.readScript(RunScriptDialog.getsScriptFile());
                    if(sc == null){
                    	monitor.done();
                    	showError("Unexpected error while parsing script", new Exception("script broken"));
                    	return;
                    }
                    sc.setApkPath(RunScriptDialog.getsAPKFile().getAbsolutePath());
                	monitor.subTask("Creating threads ...Init params done, start to create thread");
                	int count = 0;
                	System.out.println("++++++++++++++++++++++++++++++ go to create thread");
                	for(IDevice device:devices){
//                		System.out.println(this.getClass()+"----"+(device==null?"device is null":device.getName()));
                		threads.add(new ScriptRunner(device, pages, mViewer.getView(), sc, count));
                		threads.get(count).setName(device.getName());
                		monitor.subTask("Creating threads ...created #"+ count +" thread");
                		System.out.println(this.getClass() + "\tCreating threads ...created #"+ count +" thread");
                		count++;
                	}
                	monitor.subTask(count + " Threads Created...to run");
                	System.out.println(this.getClass() + " " + count + " Threads Created...to run");
                	for(Thread thread:threads){
                    	thread.start();
                    }
                    monitor.done();
                	System.out.println(this.getClass() + " monitor done!!!");
                }
            });
        } catch (Exception e) {
        	e.printStackTrace();
            showError(this.getClass() + " Unexpected error while threads executing", e);
            return;
        }
        mViewer.setModel(null, null, null);
//        for(Thread thread:threads){
//        	try {
//				thread.join();
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				System.err.println("Thread is interrupted!name:" + thread.getName());
//				e.printStackTrace();
//				return;
//			}
//        }
//        mViewer.getShell().getDisplay().syncExec(new Runnable() {
//            @Override
//            public void run() {
//                MessageDialog.openInformation(mViewer.getShell(), "RunScriptAction Success", "RunScript Success");
//            }
//        });
	}

    private void showError(final String msg, final Throwable t) {
        mViewer.getShell().getDisplay().syncExec(new Runnable() {
            @Override
            public void run() {
                Status s = new Status(IStatus.ERROR, "RunScriptAction", msg, t);
                ErrorDialog.openError(
                        mViewer.getShell(), "Error", "Error running script", s);
            }
        });
    }
}