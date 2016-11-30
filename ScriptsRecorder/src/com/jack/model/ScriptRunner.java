package com.jack.model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import javax.imageio.stream.FileImageOutputStream;

import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebElement;

import com.android.ddmlib.IDevice;
import com.android.uiautomator.RunScriptDialog;
import com.android.uiautomator.UiAutomatorView;
import com.jack.utils.XmlUtils;
import com.jack.appium.AppiumActUtil;
import com.jack.appium.WebElementUtil;
import com.jack.utils.ErrorHandler;
import com.jack.utils.FileUtil;

import io.appium.java_client.android.AndroidDriver;
import pers.vinken.appiumUtil.elementManager;

public class ScriptRunner extends Thread{

	private ScriptConfig sc;
	private UiAutomatorView view;
//	private ArrayList<String> pages;

	private IDevice device;
	private int index;

	public ScriptRunner(IDevice device, ArrayList<String> pages, UiAutomatorView view, ScriptConfig sc, int index) {
		// TODO Auto-generated constructor stub
		this.device = device;
		this.index = index;
//		this.pages = pages;
		this.view = view;
		this.sc = sc;
	}

	@Override
	public void run() {
		// create log dir and file
		File scriptFile = RunScriptDialog.getsScriptFile();
		int order = 0;
		String baseSavePath = scriptFile.getParent() + File.separator 
				+ "compare" + File.separator + device.getName();
		view.addRunningInfos(new RunningInfo(
				Thread.currentThread().getName()
				, "init"
				, 1D
				, "thread for " + device.getSerialNumber() + " started!"));
//		System.out.println("\t" + runInfo.toString());
		AppiumConfig appiumConfig = new AppiumConfig(new File(sc.getApkPath()), 
				device,
    			47111 + index);
    	int width = appiumConfig.getDriver().manage().window().getSize().width;
        int height = appiumConfig.getDriver().manage().window().getSize().height;
		System.out.println(Thread.currentThread().getName() + " appium opened");
    	if(appiumConfig.getDriver() == null){
    		ErrorHandler.showError(null, "start appium error!", new Exception("appium init error"));
    		return;
    	}
    	savePageData(baseSavePath, appiumConfig.getDriver(), order++);
		view.addRunningInfos(new RunningInfo(
				Thread.currentThread().getName()
				, "init"
				, 5D
				, "appium for " + device.getName() + " started!"));
    	int i = 0;int total = sc.getOperations().size();
    	long initTime = System.currentTimeMillis();
    	boolean checkPrompt = true;
    	for(String s:sc.getOperations()){
    		String type = s.split("::")[0];
    		try{
    			if(Operate.CLICK.equals(type)){
//        			appiumConfig.getDriver().findElement(By.xpath(s.split("::")[1])).click();
    				By by = By.xpath(s.split("::")[1]);
    				WebElement element = null;
    				if(checkPrompt){
    					element = WebElementUtil
        						.waitForElementWithPromptCheck(appiumConfig.getDriver(), by, 20);
    				}else{
    					element = elementManager.waitForElement(appiumConfig.getDriver(), by, 20);
    				}
    				if(element == null){
    					break;
    				}
    				element.click();
        	    	savePageData(baseSavePath, appiumConfig.getDriver(), order++);
        		}else if(Operate.INPUT.equals(type)){
//        			appiumConfig.getDriver().findElement(
//                			By.xpath(s.split("::")[1].split("[|]")[0])).sendKeys(s.split("[|]")[1]);
        			By by = By.xpath(s.split("::")[1].split("[|]")[0]);
        			WebElement element = null;
        			if(checkPrompt){
        				element = WebElementUtil
        						.waitForElementWithPromptCheck(appiumConfig.getDriver(), by, 20);
        			}else{
        				element = elementManager.waitForElement(appiumConfig.getDriver(), by, 20);
        			}
    				if(element == null){
    					break;
    				}
    				element.sendKeys(s.split("[|]")[1]);
        			savePageData(baseSavePath, appiumConfig.getDriver(), order++);
        		}else if(Operate.SWIPE.equals(type)){
        			System.out.println(ScriptRunner.class +" "+type + " to "+s.split("[|]")[1]);
        			if(checkPrompt){
        				WebElementUtil.checkPrompt(appiumConfig.getDriver());
        			}
        			AppiumActUtil.swipe(appiumConfig.getDriver(), s.split("[|]")[1]);
        	    	savePageData(baseSavePath, appiumConfig.getDriver(), order++);
        		}else if(Operate.SLEEP.equals(type)){
        			if(checkPrompt){
        				WebElementUtil.checkPrompt(appiumConfig.getDriver());
        			}
        			Thread.sleep(Integer.valueOf(s.split("[|]")[1]));
        		}else if(Operate.SENDKC.equalsIgnoreCase(type)){
        			if(checkPrompt){
        				WebElementUtil.checkPrompt(appiumConfig.getDriver());
        			}
        			AppiumActUtil.sendKeyCode(appiumConfig.getDriver(), s.split("[|]")[1]);
        			savePageData(baseSavePath, appiumConfig.getDriver(), order++);
        		}
    			view.addRunningInfos(new RunningInfo(
    					Thread.currentThread().getName()
    					, "replay"
    					, (i + 1)*100/total
    					, "steps(" + (i + 1) + "/" + total+");" + type + "operations:" + s));
    			i++;
    			if(System.currentTimeMillis() - initTime > 1000L * 60 * 5){
    				checkPrompt = false;
    			}
    		}catch(Exception e){
    			e.printStackTrace();
    			appiumConfig.getDriver().quit();
    			appiumConfig.close();
    			break;
    		}
    	}
    	if(i < sc.getOperations().size()){
    		// TODO 待处理异常：回放时找不到下一步操作的控件
    		System.out.println("this running jammed at " + i + " step!");
    	}
    	appiumConfig.getDriver().quit();
    	appiumConfig.close();
    	saveDeviceInfo(baseSavePath, width, height);
	}
	/**
	 * left for sometime when need to add some info
	 * @param baseSavePath
	 */
	private void saveDeviceInfo(String baseSavePath, int w, int h) {
		// TODO Auto-generated method stub
		String deviceInfoSavePath = baseSavePath + File.separator + "info.xml";
		XmlUtils.createXml(deviceInfoSavePath, device, w, h);
	}

	private void savePageData(String baseSavePath, AndroidDriver<WebElement> driver, int i) {
		// 手动暂停，等待页面加载完全再截图
		try {
			Thread.sleep(1000L * 2);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String imgsSavePath = baseSavePath + File.separator + "imgs";
		String pagesSavePath = baseSavePath + File.separator + "pages";
//		saveBytesImage(imgsSavePath, driver.getScreenshotAs(OutputType.FILE), i);
		saveBytesImage(imgsSavePath, driver.getScreenshotAs(OutputType.BYTES), i);
		savePageSource(pagesSavePath, driver.getPageSource(), i);
	}

//	private void saveBytesImage(String imgsSavePath, File screenshotAs, int i) {
//		// TODO Auto-generated method stub
//		try {
//			File sourceFile = new File(imgsSavePath + File.separator + i + ".jpg");
//			FileUtil.createFile(sourceFile);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

	private void savePageSource(String pagesSavePath, String pageSource, int i) {
		try {
			File sourceFile = new File(pagesSavePath + File.separator + i + ".xml");
			FileUtil.createFile(sourceFile);
			OutputStream os = new FileOutputStream(sourceFile);
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
	        writer.write(pageSource);
	        writer.close();
//			byte[] contentByte = pageSource.getBytes();
//			os.write(contentByte);
//			os.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void saveBytesImage(String dir, byte[] data, int order){
		if(data.length < 3) return;
		try {
			File imgFile = new File(dir + File.separator + order + ".jpg");
			FileUtil.createFile(imgFile);
			FileImageOutputStream imageOutput = new FileImageOutputStream(imgFile);
		    imageOutput.write(data, 0, data.length);
		    imageOutput.close();
//		    System.out.println("Make Picture success,Please find image in " + path);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
