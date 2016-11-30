package com.jack.model;

import com.android.uiautomator.tree.BasicTreeNode;
import com.android.uiautomator.tree.UiNode;

/**
 * 操作描述类
 * 初始化方式为，
 * 	调用构造函数
 * 	根据需要
 * 		设置 节点 信息
 * 		设置  参数 信息
 * @author ZYC
 * 2016年11月28日
 */
public class Action {

	private String type;
	private String itemName;
	private String xPath;
	/**
	 * 格式	TYPE::XPATH|ARG
	 */
	private String operation;
	
	/**
	 * universal constructor, default for click
	 * @param type     operate type
	 * @param itemName item name, like the name in a tree node
	 * @param xPath    item's xpath
	 */
//	public Action(String type, String itemName, String xPath) {
//		// TODO Auto-generated constructor stub
//		this.type = type;
//		this.itemName = itemName;
//		this.xPath = xPath;
//		operation = type + "::" + xPath ;
//	}
	
	/**
	 * 对于不需要指定 操作节点的 操作，使用这个构造函数
	 * @param type
	 */
	public Action(String type) {
		// TODO Auto-generated constructor stub
		this.type = type;
		this.itemName = "";
		this.xPath = "";
		this.operation = type + "::";
	}

	/**
	 * 
	 * @author ZYC
	 * @param node
	 * 2016年11月28日
	 */
	public void setUiNodeInfo(BasicTreeNode node) {
		itemName = ((UiNode)node).toString();
		xPath = ((UiNode)node).getxPath();
		operation += xPath;
	}
	
	/**
	 * 设置操作的参数；操作有参数时调用
	 * @author ZYC
	 * @param arg  操作参数
	 * 2016年11月28日
	 */
	public void setArgument(String arg) {
		operation += "|" + arg;
	}
	
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	/**
	 * @return the itemName
	 */
	public String getItemName() {
		return itemName;
	}
	/**
	 * @return the xPath
	 */
	public String getxPath() {
		return xPath;
	}
	/**
	 * @return the operation
	 */
	public String getOperation() {
		return operation;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	/**
	 * @param itemName the itemName to set
	 */
	public void setItemName(String itemName) {
		this.itemName = itemName;
	}
	/**
	 * @param xPath the xPath to set
	 */
	public void setxPath(String xPath) {
		this.xPath = xPath;
	}
	/**
	 * @param operation the operation to set
	 */
	public void setOperation(String operation) {
		this.operation = operation;
	}
}