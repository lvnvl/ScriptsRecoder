package com.android.uiautomator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.android.ddmlib.IDevice;

public class RunScriptDialog extends Dialog {
    private static final int FIXED_TEXT_FIELD_WIDTH = 300;
    private static final int DEFAULT_LAYOUT_SPACING = 10;
	private Text mScriptSaveText;
	private Text mAPKText;
    private Text mDeviceText;
	private static File sScriptFile;
	private static File sAPKFile;
    private static ArrayList<IDevice> sIDevices = null;
//	private int sSelectedDeviceIndex = 0;
	public RunScriptDialog(Shell shell) {
		super(shell);
		sIDevices = new ArrayList<IDevice>();
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

        //open script composite
        Group openScriptGroup = new Group(container, SWT.NONE);
        openScriptGroup.setLayout(new GridLayout(2, false));
        openScriptGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        openScriptGroup.setText("Open Script");

        mScriptSaveText = new Text(openScriptGroup, SWT.BORDER | SWT.READ_ONLY);
        if (sScriptFile != null) {
            mScriptSaveText.setText(sScriptFile.getAbsolutePath());
        }
        GridData gd_ScriptSaveText = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        gd_ScriptSaveText.minimumWidth = FIXED_TEXT_FIELD_WIDTH;
        gd_ScriptSaveText.widthHint = FIXED_TEXT_FIELD_WIDTH;
        mScriptSaveText.setLayoutData(gd_ScriptSaveText);

        Button openScriptButton = new Button(openScriptGroup, SWT.NONE);
        openScriptButton.setText("...");
        openScriptButton.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                handleOpenScriptFile();
            }
        });
        //select APK composite
        Group selectAPKGroup = new Group(container, SWT.NONE);
        selectAPKGroup.setLayout(new GridLayout(2, false));
        selectAPKGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        selectAPKGroup.setText("Select app");

        mAPKText = new Text(selectAPKGroup, SWT.BORDER | SWT.READ_ONLY);
        if (sAPKFile != null) {
        	mAPKText.setText(sAPKFile.getAbsolutePath());
        }
        GridData gd_selectAPKText = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        gd_selectAPKText.minimumWidth = FIXED_TEXT_FIELD_WIDTH;
        gd_selectAPKText.widthHint = FIXED_TEXT_FIELD_WIDTH;
        mAPKText.setLayoutData(gd_selectAPKText);

        Button selectAPKButton = new Button(selectAPKGroup, SWT.NONE);
        selectAPKButton.setText("...");
        selectAPKButton.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                handleSelectAPKFile();
            }
        });
      
        //Devices choose composite
        final List<IDevice> devices = DebugBridge.getDevices();
        Group chooseDeviceGroup = new Group(container, SWT.NONE);
        chooseDeviceGroup.setLayout(new GridLayout((int)Math.ceil((double)devices.size()/2), true));
        chooseDeviceGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        chooseDeviceGroup.setText("select device");
        String[] mDeviceNames = null;
        final Button[] mDeviceBtns = new Button[devices.size()];
        final boolean[] mDeviceSelected = new boolean[devices.size()];
        mDeviceNames = new String[devices.size()];
//        mDeviceBtns = new Button[devices.size()];
        for (int i = 0; i < devices.size(); i++) {
        	mDeviceNames[i] = devices.get(i).getName();
        	mDeviceBtns[i] = new Button(chooseDeviceGroup, SWT.CHECK);
        	mDeviceBtns[i].setText(mDeviceNames[i]);
        	mDeviceBtns[i].setData("index", i);
//        	System.out.println("\tdevice :"+ mDeviceNames[i] +";index:"+mDeviceBtns[i].getData("index"));
        	mDeviceSelected[i] = false;
        }
        Button checkAll = new Button(chooseDeviceGroup, SWT.BUTTON1);
        checkAll.setText("select all");
        checkAll.addMouseListener(new MouseListener(){

			@Override
			public void mouseDown(MouseEvent e) {
				// TODO Auto-generated method stub
				for(int btn = 0; btn < mDeviceBtns.length; btn ++){
					mDeviceBtns[btn].setSelection(true);
					mDeviceSelected[btn] = true;
				}
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseUp(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
        });
        for(int j = 0;j < devices.size();j ++){
        	mDeviceBtns[j].addListener(SWT.Selection, new Listener(){

    			@Override
    			public void handleEvent(Event event) {
    				// TODO Auto-generated method stub
//    				System.out.println("detected selection event, and item index:" + event.widget.getData("index"));
    				int index = (int)event.widget.getData("index");
    				mDeviceSelected[index] = ! mDeviceSelected[index];
    			}});
        }
        container.addListener(SWT.Dispose, new Listener(){

			@Override
			public void handleEvent(Event event) {
				// TODO Auto-generated method stub
				for(int btn = 0; btn < devices.size(); btn ++){
					if(mDeviceSelected[btn]){
//						devices.get(btn);
						sIDevices.add(devices.get(btn));
						System.out.println("device test: " + devices.get(btn).getSerialNumber());
					}
				}
			}});
		return container;
	}
    private void handleSelectAPKFile() {
        FileDialog fd = new FileDialog(getShell(), SWT.OPEN);
        fd.setText("Select APK File");
        File initialFile = sAPKFile;
        // if file has never been selected before, try to base initial path on the mXmlDumpFile
        if (initialFile != null) {
            if (initialFile.isFile()) {
                fd.setFileName(initialFile.getAbsolutePath());
            } else if (initialFile.isDirectory()) {
                fd.setFilterPath(initialFile.getAbsolutePath());
            }
        }
        String[] filter = {"*.apk"};
        fd.setFilterExtensions(filter);
        String selected = fd.open();
        if (selected != null) {
            sAPKFile = new File(selected);
            mAPKText.setText(selected);
        }
    }
//    private void handleOpenScriptDirectory(){
//    	DirectoryDialog dialog = new DirectoryDialog(getShell());
//    	System.out.println("SWT platform: " + SWT.getPlatform());
//		dialog.setFilterPath(sScriptFileDirectory != null ? 
//				sScriptFileDirectory : 
//					SWT.getPlatform().equals("win32") ? "d:\\" : "/");
////		dialog.setFilterPath(SWT.getPlatform().equals("win32")?"d:\\":"/");
//		String directory = null;
//		boolean done = false;
//		while(!done){
//	        directory = dialog.open();
//	        System.out.println("directory is" + directory);
//			if(directory == null){
//				done = true;
//			}else{
//				mScriptSaveText.setText(directory);
//				sScriptFileDirectory = directory;
//				File dir = new File(directory);
//				if(dir.list().length > 0){
//					MessageBox mb = new MessageBox(dialog.getParent(), SWT.ICON_ERROR);
//					mb.setMessage(directory + " is not empty. please select an empty case diretory.");
//					mb.open();
//					sScriptFileDirectory = null;
////					done = mb.open() == SWT.YES;
//				}else{
//					done = true;
//				}
//			}
//		}
//    }
    private void handleOpenScriptFile() {
        FileDialog fd = new FileDialog(getShell(), SWT.OPEN);
        fd.setText("Open Script File");
        File initialFile = sScriptFile;
        // if file has never been selected before, try to base initial path on the mXmlDumpFile
        if (initialFile != null) {
            if (initialFile.isFile()) {
                fd.setFileName(initialFile.getAbsolutePath());
            } else if (initialFile.isDirectory()) {
                fd.setFilterPath(initialFile.getAbsolutePath());
            }
        }
        String[] filter = {"*.xml"};
        fd.setFilterExtensions(filter);
        String selected = fd.open();
        if (selected != null) {
            sScriptFile = new File(selected);
            mScriptSaveText.setText(selected);
        }
    }

	/**
	 * @return the mDeviceText
	 */
	public Text getmDeviceText() {
		return mDeviceText;
	}

	/**
	 * @return the sScriptFile
	 */
	public static File getsScriptFile() {
		return sScriptFile;
	}
	/**
	 * @param sScriptFile the sScriptFile to set
	 */
	public static void setsScriptFile(File sScriptFile) {
		RunScriptDialog.sScriptFile = sScriptFile;
	}

	/**
	 * @return the sAPKFile
	 */
	public static File getsAPKFile() {
		return sAPKFile;
	}

	/**
	 * @param sAPKFile the sAPKFile to set
	 */
	public static void setsAPKFile(File sAPKFile) {
		RunScriptDialog.sAPKFile = sAPKFile;
	}

	/**
	 * @return the sIDevices
	 */
	public static ArrayList<IDevice> getsIDevices() {
		return sIDevices;
	}
}