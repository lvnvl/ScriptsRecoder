package com.jack.appium;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import com.jack.model.Constant;

import io.appium.java_client.AppiumDriver;

public class WebElementUtil {
	
	/**
	 * @author  Jack
	 * @param driver AndroidDriver of the current context
	 * @param secs  max seconds that will wait for the element
	 * @param id    id of the search element
	 * @return      null or the giving id of the element
	 */
	public static WebElement waitForWebElementByID(AppiumDriver<WebElement> driver,int secs,String id){
		WebElement we = null;
		int sleptSeconds = 0;
		boolean elementExist = false;
		while(!elementExist && sleptSeconds <= secs){
			try{
				By by = By.id(id);
				if(by != null){
					we = driver.findElement(by);
				}
				if(null != we){
					elementExist = true;
				}
			}catch(NoSuchElementException e){
//				e.printStackTrace();
//				System.out.println(e.fillInStackTrace());
				try {
					Thread.sleep(1000L * 1);
					sleptSeconds++;
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
//				System.out.println("学生专区 未找到"+(null == we));
			}
		}
		return we;
	}
	
	/**
	 * @author  Jack
	 * @param driver
	 * @param secs  max seconds that will wait for the element
	 * @param id    id of the search element
	 * @return      null or the giving id of the element
	 */
	/**
	 * 
	 * @author ZYC
	 * @param driver AndroidDriver of the current context
	 * @param by    the element's by
	 * @param secs  max seconds that will wait for the element
	 * @return
	 * 2016年11月28日
	 */
	public static WebElement waitForElementWithPromptCheck(AppiumDriver<WebElement> driver, By by, int secs){
		WebElement we = null;
		int sleptSeconds = 0;
		boolean elementExist = false;
		while(!elementExist && sleptSeconds <= secs){
			try{
//				By by = By.id(id);
				if(by != null){
					we = driver.findElement(by);
				}
				if(null != we){
					elementExist = true;
				}
			}catch(NoSuchElementException e){
				System.out.println("waitForElementWithPromptCheck throws NoSuchElementException, start to check where exists a prompt.");
				for (String confirmText:Constant.CONFIRM_TEXT){
					try {
						By confirmXpath = By.xpath("//*[contains(@text, '" + confirmText + "')]");
						driver.findElement(confirmXpath).click();
						System.out.println("(((((((((((((((((found text " + confirmText+" and performed click");
					} catch (NoSuchElementException e2) {
						continue;
					}
				}
				try {
					Thread.sleep(1000L * 1);
					sleptSeconds++;
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
//				System.out.println("学生专区 未找到"+(null == we));
			}
		}
		return we;
	}

	public static void checkPrompt(AppiumDriver<WebElement> driver) {
		System.out.println("check whether prompts exist.");
		for (String confirmText:Constant.CONFIRM_TEXT){
			try {
				By confirmXpath = By.xpath("//*[contains(@text, '" + confirmText + "')]");
				driver.findElement(confirmXpath).click();
				System.out.println("((((((((found text " + confirmText+" and performed click");
			} catch (NoSuchElementException e2) {
				continue;
			}
		}
		try {
			Thread.sleep(1000L * 1);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}