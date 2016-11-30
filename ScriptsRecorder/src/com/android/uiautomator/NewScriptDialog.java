package com.android.uiautomator;

import java.io.File;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.android.ddmlib.IDevice;

public class NewScriptDialog extends Dialog {
    private static final int FIXED_TEXT_FIELD_WIDTH = 300;
    private static final int DEFAULT_LAYOUT_SPACING = 10;
    private Text mScriptSaveText;
    private Text mAPKText;
    private Text mPackageText;
    private Text mActivityText;
    private Text mDeviceText;
    private Text mPortText;
	private Button mOkButton;

//    private static String sScriptFilePath;
    private static String sScriptFileDirectory;
    private static File sAPKFile;
//    private static String mPackage;
//    private static String mActivity;
    private static int mPort;
    private static IDevice sIDevice;
	private int sSelectedDeviceIndex = 0;
    
	public NewScriptDialog(Shell parentShell) {
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

        //Save script composite
        Group saveScriptGroup = new Group(container, SWT.NONE);
        saveScriptGroup.setLayout(new GridLayout(2, false));
        saveScriptGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        saveScriptGroup.setText("Save");

        mScriptSaveText = new Text(saveScriptGroup, SWT.BORDER | SWT.READ_ONLY);
        if (sScriptFileDirectory != null) {
            mScriptSaveText.setText(sScriptFileDirectory);
        }
        GridData gd_ScriptSaveText = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        gd_ScriptSaveText.minimumWidth = FIXED_TEXT_FIELD_WIDTH;
        gd_ScriptSaveText.widthHint = FIXED_TEXT_FIELD_WIDTH;
        mScriptSaveText.setLayoutData(gd_ScriptSaveText);

        Button openScriptSaveButton = new Button(saveScriptGroup, SWT.NONE);
        openScriptSaveButton.setText("...");
        openScriptSaveButton.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
            	// 以文件形式选择保存脚本     9:27 2016/11/28  。 
            	// 修改为以文件夹的形式保存用例，每个用例选择一个独立的文件夹存储
//                handleOpenScriptSaveFile();
            	handleOpenScriptSaveDirectoryDialog();
            }
        });

        //App args composite
        Group setAppInfoGroup = new Group(container, SWT.NONE);
        setAppInfoGroup.setLayout(new GridLayout(3, false));
        setAppInfoGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 3));
        setAppInfoGroup.setText("APP info");
        
        Label apkLabel = new Label(setAppInfoGroup,SWT.NONE);
        apkLabel.setText("apk:");
        mAPKText = new Text(setAppInfoGroup, SWT.BORDER | SWT.READ_ONLY);
        //test for aliyun app
        sAPKFile = new File("E:/app/calculator/longbin.helloworld_3.8.1_37.apk");
        if (sAPKFile != null) {
            mAPKText.setText(sAPKFile.getAbsolutePath());
        }
        mAPKText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Button openSetAPKButton = new Button(setAppInfoGroup, SWT.NONE);
        openSetAPKButton.setText("open");
        openSetAPKButton.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                handleOpenSetAPKFile();
            }
        });
//        
//        Label packageLabel = new Label(setAppInfoGroup,SWT.NONE);
//        packageLabel.setText("Package:");
//        mPackageText = new Text(setAppInfoGroup, SWT.BORDER);
//        //test for aliyun app
//        mPackageText.setText("com.alibaba.aliyun");
//        mPackageText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
//        mPackageText.addListener(SWT.Dispose, new Listener(){
//			@Override
//			public void handleEvent(Event event) {
//				// TODO Auto-generated method stub
//				mPackage = mPackageText.getText();
//			}
//        	
//        });
//        Label activityLabel = new Label(setAppInfoGroup,SWT.NONE);
//        activityLabel.setText("Activity:");
//        mActivityText = new Text(setAppInfoGroup, SWT.BORDER);
//        //test for aliyun app
//        mActivityText.setText("com.alibaba.aliyun.biz.login.WelcomeActivity");
//        mActivityText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
//        mActivityText.addListener(SWT.Dispose, new Listener(){
//
//			@Override
//			public void handleEvent(Event event) {
//				// TODO Auto-generated method stub
//				mActivity = mActivityText.getText();
//			}
//        	
//        });
        //Devices choose composite
        Group chooseDeviceGroup = new Group(container, SWT.NONE);
        chooseDeviceGroup.setLayout(new GridLayout(1, false));
        chooseDeviceGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        chooseDeviceGroup.setText("select device");
        List<IDevice> devices = DebugBridge.getDevices();
        String[] mDeviceNames;
        mDeviceNames = new String[devices.size()];
        for (int i = 0; i < devices.size(); i++) {
            mDeviceNames[i] = devices.get(i).getName();
        }
        final Combo combo = new Combo(chooseDeviceGroup, SWT.BORDER | SWT.READ_ONLY);
        combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        combo.setItems(mDeviceNames);
        int defaultSelection =
                sSelectedDeviceIndex < devices.size() ? sSelectedDeviceIndex : 0;
        combo.select(defaultSelection);
        sSelectedDeviceIndex = defaultSelection;
        sIDevice = devices.get(sSelectedDeviceIndex);

        combo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                sSelectedDeviceIndex = combo.getSelectionIndex();
                sIDevice = devices.get(sSelectedDeviceIndex);
            }
        });

        
        //Appium port composite
        Group setAppiumPortGroup = new Group(container, SWT.NONE);
        setAppiumPortGroup.setLayout(new GridLayout(1, false));
        setAppiumPortGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        setAppiumPortGroup.setText("Appium port");
        
        mPortText = new Text(setAppiumPortGroup, SWT.BORDER);
        mPortText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        mPortText.addListener(SWT.Dispose, new Listener(){
			@Override
			public void handleEvent(Event event) {
				// TODO Auto-generated method stub
				mPort = Integer.parseInt(mPortText.getText());
			}
        	
        });
        //test for aliyun app
        mPortText.setText("4723");
        return container;
	}
	protected void handleOpenSetAPKFile() {
		FileDialog fd = new FileDialog(getShell(), SWT.OPEN);
        fd.setText("Open APK File");
        File initialFile = sAPKFile;
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
	protected void handleOpenScriptSaveDirectoryDialog() {
		DirectoryDialog dialog = new DirectoryDialog(getShell());
		System.out.println("SWT platform: " + SWT.getPlatform());
		dialog.setFilterPath(sScriptFileDirectory != null ? 
				sScriptFileDirectory : 
					SWT.getPlatform().equals("win32") ? "d:\\" : "/");
//		dialog.setFilterPath(SWT.getPlatform().equals("win32")?"d:\\":"/");
		String directory = null;
		boolean done = false;
		while(!done){
	        directory = dialog.open();
	        System.out.println("directory is" + directory);
			if(directory == null){
				done = true;
			}else{
				mScriptSaveText.setText(directory);
				sScriptFileDirectory = directory;
				File dir = new File(directory);
				if(dir.list().length > 0){
					MessageBox mb = new MessageBox(dialog.getParent(), SWT.ICON_ERROR);
					mb.setMessage(directory + " is not empty. please select an empty case diretory.");
					mb.open();
					sScriptFileDirectory = null;
//					done = mb.open() == SWT.YES;
				}else{
					done = true;
				}
			}
		}

	}
	protected void handleOpenScriptSaveFile() {
		FileDialog fd = new FileDialog(getShell(), SWT.SAVE);
		fd.setFilterExtensions(new String[]{".xml"});
		String fileName = null;
		boolean done = false;
		while(!done){
	        fileName = fd.open();
//	        System.out.println("filename is" + fileName);
			if(fileName == null){
				done = true;
			}else{
				mScriptSaveText.setText(fileName);
				sScriptFileDirectory = fileName;
				File file = new File(fileName);
				if(file.exists()){
					MessageBox mb = new MessageBox(fd.getParent(), SWT.ICON_WARNING
							| SWT.YES | SWT.NO);
					mb.setMessage(fileName + "already exists.Do you want to replace it?");
					done = mb.open() == SWT.YES;
				}else{
					done = true;
				}
			}
		}

//        System.out.println("filename is" + fileName + "\n path is " + sScriptFile.getAbsolutePath());
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
        newShell.setText("New Script config");
    }

    /**
     * Create contents of the button bar.
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        setmOkButton(createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true));
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

	/**
	 * @return the mScriptSaveText
	 */
	public Text getmScriptSaveText() {
		return mScriptSaveText;
	}

	/**
	 * @return the mPackageText
	 */
	public Text getmPackageText() {
		return mPackageText;
	}

	/**
	 * @return the mActivityText
	 */
	public Text getmActivityText() {
		return mActivityText;
	}

	/**
	 * @return the mDeviceText
	 */
	public Text getmDeviceText() {
		return mDeviceText;
	}

	/**
	 * @return the mPortText
	 */
	public Text getmPortText() {
		return mPortText;
	}

	/**
	 * @return the sScriptSaveFile
	 */
	public static String getsScriptSaveDirectory() {
		return sScriptFileDirectory;
	}

	/**
	 * @return the sAPKFile
	 */
	public static File getsAPKFile() {
		return sAPKFile;
	}
	
	/**
	 * @return the sAPKFile
	 */
	public static IDevice getsIDevice() {
		return sIDevice;
	}
//
//	public static String getmActivity() {
//		return mActivity;
//	}
//
//	public static String getmPackage() {
//		return mPackage;
//	}

	public static int getmPort() {
		return mPort;
	}

	public Button getmOkButton() {
		return mOkButton;
	}

	public void setmOkButton(Button mOkButton) {
		this.mOkButton = mOkButton;
	}

}