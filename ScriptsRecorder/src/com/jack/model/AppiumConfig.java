package com.jack.model;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.android.ddmlib.IDevice;
import com.jack.utils.CmdConfig;
import com.jack.utils.CmdUtil;
import com.jack.utils.UiautomatorUtil;

import io.appium.java_client.android.AndroidDriver;

public class AppiumConfig {
	private File APKFile;
	private IDevice mDevice;
	public static String mPackage;
	public static String mActivity;
	private int port;
	private AndroidDriver<WebElement> driver;

	public AppiumConfig() {
		super();
	}

	public AppiumConfig(File apkFile, IDevice device, int p) {
		// TODO Auto-generated constructor stub
		APKFile = apkFile;
		mDevice = device;
		String apkUri = apkFile.toURI().toString();
		String apkPath = "";
		if(CmdUtil.isWindows()){
			apkPath = apkUri.substring(6, apkUri.length());
		}else{
			apkPath = APKFile.getAbsolutePath();
		}
		String appInfo = CmdUtil.run(CmdConfig.APP_INFO.replaceAll("#apk#", apkPath));
		mPackage = CmdUtil.stringFilter.grep(appInfo, "package:").split("'")[1].trim();
		mActivity = CmdUtil.stringFilter.grep(appInfo, "launchable-").split("'")[1].trim();
		port = p;
//		System.out.println(mPackage+"===");System.out.println(mActivity);

		/**
		 * check appium port, if occupied release the port , then start appium at the port
		 * */
		if(CmdUtil.isWindows()){
			String portInfo = CmdUtil.run(CmdConfig.CHECK_APPIUM_SERVER_WIN.replaceAll("#port#", String.valueOf(p)));
			if(portInfo != null && portInfo.length() > 0){
				// get pid of the process
				String[] info = portInfo.split("\\s+");
				String pid = info[info.length - 1];
				String pidInfo = CmdUtil.run(CmdConfig.QUERY_PROCESS_INFO_WIN.replaceAll("#pid#", pid));
				// close the port and kill the process if the process is a type of node.exe
				if(pidInfo.contains("node")){
					System.out.println("port " + p + " is occupied by a subprocess of node!");
					CmdUtil.run(CmdConfig.KILL_APPIUM_SERVER_WIN.replaceAll("#pid#", pid));
					System.out.println("process " + pidInfo.split("\\s+")[0] + ":" + p + " is killed!");
					initAppiumDriver(true);
				}else{
					System.out.println("port "+p+" is not occupied by a subprocess of node!");
					System.out.println("process is " + pidInfo.split("\\s+")[0]);
					initAppiumDriver(false);
				}
			}else{
				initAppiumDriver(true);
			}
		}else{
			String portInfo = CmdUtil.run(CmdConfig.CHECK_APPIUM_SERVER_LINUX);
			if(portInfo != null && portInfo.trim().length() > 0){
				if(portInfo.contains("node")){
					System.out.println("port " + p + " is occupied by a subprocess of node!");
					CmdUtil.run(CmdConfig.KILL_APPIUM_SERVER_LINUX.replaceAll("#pid#", portInfo.split("/")[0]));
					System.out.println("process " + portInfo.split("/")[0] + ":" + p + " is killed!");
					initAppiumDriver(true);
				}else{
					System.out.println("port " + p + " is not occupied by a subprocess of node!");
					System.out.println("process is " + portInfo.split("/")[0]);
					initAppiumDriver(false);
				}
			}else{
				initAppiumDriver(true);
			}
		}
	}
	
	public void close(){
		String portInfo = CmdUtil.run((
				CmdUtil.isWindows()?CmdConfig.CHECK_APPIUM_SERVER_WIN
						:CmdConfig.CHECK_APPIUM_SERVER_LINUX).replaceAll("#port#", String.valueOf(port)));
		if(portInfo != null && portInfo.length() > 0){
			// get pid of the process
			if(CmdUtil.isWindows()){
				String[] info = portInfo.split("\\s+");
				String pid = info[info.length - 1];
				CmdUtil.run(CmdConfig.KILL_APPIUM_SERVER_WIN.replaceAll("#pid#", pid));
			}else{
				CmdUtil.run(CmdConfig.KILL_APPIUM_SERVER_LINUX.replaceAll("#pid#", portInfo.split("/")[0]));
			}
		}
		UiautomatorUtil.endInstallDeamon(mDevice.getSerialNumber());
	}
	
	private void initAppiumDriver(boolean portReady){
        System.out.println(this.getClass()+" appium init method");
		if(portReady){
			System.out.println("\t--------------port:"+port);
			try {
				// start appium at port
				new Thread(new Runnable(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						CmdUtil.run(CmdConfig.START_APPIUM_SERVER.replaceAll("#port#", String.valueOf(port)));
					}
					
				}).start();
				System.out.println("\t\t--------------");
				String portInfo = CmdUtil.run((
						CmdUtil.isWindows()?CmdConfig.CHECK_APPIUM_SERVER_WIN
								:CmdConfig.CHECK_APPIUM_SERVER_LINUX).replaceAll("#port#", String.valueOf(port)));
				System.out.println("\t--------------"+portInfo);
				while( portInfo == null || portInfo.trim().length() < 1){
					System.out.println("\t++++++++++\t--waiting " + portInfo + " --");
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					portInfo = CmdUtil.run((
							CmdUtil.isWindows()?CmdConfig.CHECK_APPIUM_SERVER_WIN
									:CmdConfig.CHECK_APPIUM_SERVER_LINUX).replaceAll("#port#", String.valueOf(port)));
				}
				System.out.println(this.getClass()+" start to init driver");
				UiautomatorUtil.startInstallDeamon(mDevice.getSerialNumber());
				// init appium driver
				DesiredCapabilities capabilities = new DesiredCapabilities();
				capabilities.setCapability("platformName", "Android");
				capabilities.setCapability("platformVersion", "5.1");
				String udid = mDevice.getSerialNumber();
				capabilities.setCapability("deviceName", mDevice.getName().split("-")[1]);// 设备名（必填，可任意内容）
				if (udid != null)
					capabilities.setCapability("udid", udid);
				// 支持中文
				capabilities.setCapability("unicodeKeyboard", true);
				capabilities.setCapability("resetKeyboard", true);// 不需重置
				capabilities.setCapability("noSign", true);
				capabilities.setCapability("app", APKFile.getAbsolutePath());
				capabilities.setCapability("appPackage", mPackage);
				capabilities.setCapability("appActivity", mActivity);
				capabilities.setCapability("newCommandTimeout", 7200);//session spire time during two command
				capabilities.setCapability("noReset", true);//don't wipe the app's cache data

				System.out.println("DesiredCapabilities:" + capabilities.toString());

				System.out.println(this.getClass()+" DesiredCapabilities init done");
				driver = new AndroidDriver<>(new URL("http://127.0.0.1:" + port + "/wd/hub"), capabilities);
				
				System.out.println(this.getClass()+" driver init done");
				
				if(!driver.isAppInstalled(mPackage)){
					System.out.println("app not installed!");
					driver.installApp(APKFile.getAbsolutePath());
				}
				System.out.println(this.getClass()+" init android driver done!");
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			driver = null;
		}
	}

	/**
	 * @return the aPKFile
	 */
	public File getAPKFile() {
		return APKFile;
	}

	/**
	 * @return the mDevice
	 */
	public IDevice getmDevice() {
		return mDevice;
	}

	/**
	 * @return the mPackage
	 */
	public static String getmPackage() {
		return mPackage;
	}

	/**
	 * @return the mActivity
	 */
	public static String getmActivity() {
		return mActivity;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @return the driver
	 */
	public AndroidDriver<WebElement> getDriver() {
		return driver;
	}

	/**
	 * @param aPKFile the aPKFile to set
	 */
	public void setAPKFile(File aPKFile) {
		APKFile = aPKFile;
	}

	/**
	 * @param mDevice the mDevice to set
	 */
	public void setmDevice(IDevice mDevice) {
		this.mDevice = mDevice;
	}

	/**
	 * @param mPackage the mPackage to set
	 */
	public void setmPackage(String mPackage) {
		AppiumConfig.mPackage = mPackage;
	}

	/**
	 * @param mActivity the mActivity to set
	 */
	public void setmActivity(String mActivity) {
		AppiumConfig.mActivity = mActivity;
	}

	/**
	 * @param port the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}	
}
