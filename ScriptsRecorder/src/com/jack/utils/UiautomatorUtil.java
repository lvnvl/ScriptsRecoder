package com.jack.utils;

import java.io.File;

public class UiautomatorUtil {

	public static void startInstallDeamon(String udid) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				endInstallDeamon(udid);
				String push = "adb -s "+ udid +" push " + System.getProperty("user.dir") + File.separator 
						+ "lib"+File.separator+"AutoInstaller.jar data/local/tmp";
				System.out.println("execute:" + push);
				CmdUtil.run(push);
				String runtest = "adb -s "+ udid +" shell uiautomator runtest AutoInstaller.jar -c com.Runner";
				System.out.println("execute:" + runtest);
				CmdUtil.run(runtest);
			}
		}).start();
	}
	
	public static void endInstallDeamon(String udid){
		String uiautomatorInfo = CmdUtil.run(CmdConfig.FIND_UIAUTOMATOR_PROCESS.replaceAll("#udid#", udid));
		uiautomatorInfo = CmdUtil.stringFilter.grep(uiautomatorInfo, "uiautomator");
		if(uiautomatorInfo.trim().length() > 0){
			System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			System.out.println(uiautomatorInfo);
			System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			String pid = uiautomatorInfo.split("\\s+")[1];
			CmdUtil.run(CmdConfig.KILL_APP_PROCESS.replaceAll("#udid#", udid).replaceAll("#pid#", pid));	
		}
	}
}
