package com.jack.model;

import java.util.ArrayList;

public class ScriptConfig {
	private String apkPath;
	private String packageName;
	private String activity;
	private String port;
	private ArrayList<String> operations;

	public ScriptConfig(String apkPath, String packageName, String activity, String port) {
		super();
		this.apkPath = apkPath;
		this.packageName = packageName;
		this.activity = activity;
		this.port = port;
		operations = new ArrayList<String>();
	}
	
	public ScriptConfig() {
		// TODO Auto-generated constructor stub
		operations = new ArrayList<String>();
	}
	public void addAction(String s){
		operations.add(s);
	}
	
	/**
	 * @return the apkPath
	 */
	public String getApkPath() {
		return apkPath;
	}
	/**
	 * @return the packageName
	 */
	public String getPackageName() {
		return packageName;
	}
	/**
	 * @return the activity
	 */
	public String getActivity() {
		return activity;
	}
	/**
	 * @return the port
	 */
	public String getPort() {
		return port;
	}
	/**
	 * @return the actions
	 */
	public ArrayList<String> getOperations() {
		return operations;
	}
	/**
	 * @param apkPath the apkPath to set
	 */
	public void setApkPath(String apkPath) {
		this.apkPath = apkPath;
	}
	/**
	 * @param packageName the packageName to set
	 */
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	/**
	 * @param activity the activity to set
	 */
	public void setActivity(String activity) {
		this.activity = activity;
	}
	/**
	 * @param port the port to set
	 */
	public void setPort(String port) {
		this.port = port;
	}
}