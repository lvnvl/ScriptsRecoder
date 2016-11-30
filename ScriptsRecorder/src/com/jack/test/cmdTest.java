package com.jack.test;

import java.util.Properties;

import org.openqa.selenium.firefox.FirefoxDriver.SystemProperty;

import com.jack.utils.CmdConfig;
import com.jack.utils.CmdUtil;

import pers.quq.filedb.core.StringFilterImpl;

public class cmdTest {

	public static StringFilterImpl stringFilter = new StringFilterImpl();
	
	public cmdTest() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println(System.getProperty("user.dir"));
		String str = CmdUtil.run("adb shell ps flymed");
		System.out.println(str);
		System.out.println(stringFilter.grep(str, "flymed"));
		System.out.println(stringFilter.grep(str, "flymed").split("\\s+")[1]);
//		int p = 49155;
//		/**
//		 * chech appium port, if occupied release the port , then start appium at the port
//		 * */
//		String portInfo = CmdUtil.run(CmdConfig.CHECK_APPIUM_SERVER_WIN.replaceAll("#port#", String.valueOf(p)));
//		if(portInfo != null && portInfo.length()>0){
//			// get pid of the process
//			System.out.println(portInfo);
//			System.out.println("++++++++++");
//			String[] info = portInfo.split("\\s+");
//			for(String str:info){
//				System.out.println("-----------:" + str + ":-------");
//			}
//			String pid = info[info.length - 1];
//			String pidInfo = CmdUtil.run(CmdConfig.QUERY_PROCESS_INFO_WIN.replaceAll("#pid#", pid));
//			// close the port and kill the process if the process is a type of node.exe
//			if(pidInfo.contains("node")){
//				System.out.println("port " + p + " is occupied by a subprocess of node!");
//				CmdUtil.run(CmdConfig.KILL_APPIUM_SERVER_WIN.replaceAll("#pid#", pid));
//			}else{
//				System.out.println("port "+p+" is not occupied by a subprocess of node!");
//				System.out.println("process is " + pidInfo.split("\\s+")[0]);
//			}
//		}
	}

}
