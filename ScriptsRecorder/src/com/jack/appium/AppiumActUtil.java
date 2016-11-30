package com.jack.appium;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import bsh.This;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidKeyCode;

public class AppiumActUtil {

	public AppiumActUtil() {
		// TODO Auto-generated constructor stub
	}

	public static WebElement waitForElement(AndroidDriver<WebElement> driver,String xpath,int timeOut){
		//wait for 60s if WebElemnt show up less than 60s , then return , until 60s
        WebElement e = new AndroidDriverWait(driver, timeOut)
        		.until(new ExpectedCondition<WebElement>() {
        			public WebElement apply(AndroidDriver d) {
        				return d.findElement(By.xpath(xpath));
                    }
               });
        return e;
	}
	
	public static boolean swipe(AndroidDriver<WebElement> driver,String dire){
		int width = driver.manage().window().getSize().width;
        int height = driver.manage().window().getSize().height;
        try {
        	switch(dire){
        	case "up":
        		System.out.println(This.class + " swipe to up");
                driver.swipe(width / 2, height * 8 / 10, width / 2, height * 2 / 10, 1000);
        		break;
        	case "right":
        		System.out.println(This.class + " swipe to right");
                driver.swipe(width * 2 / 10, height / 2, width * 8 / 10, height / 2, 1000);
        		break;
        	case "left":
        		System.out.println(This.class + " swipe to left");
                driver.swipe(width * 8 / 10, height / 2, width * 2 / 10, height / 2, 1000);
            	break;
        	case "down":
        		System.out.println(This.class + " swipe to down");
                driver.swipe(width / 2, height * 2 / 10, width / 2, height * 8 / 10, 1000);
        		break;
        	default:
        		System.out.println(This.class + " go into default");
        		break;
        	}
        } catch (Exception e) {
        	//TODO process exception
//        	throw new SwipeException();
        	System.err.println(e.getMessage());
        	e.printStackTrace();
            return false;
        }
        return true;
	}
	public static void sendKeyCode(AndroidDriver<WebElement> driver,String des) {
		if("back".equalsIgnoreCase(des)){
			driver.pressKeyCode(AndroidKeyCode.BACK);			
		}
	}
}