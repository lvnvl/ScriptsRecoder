package com;

import com.android.uiautomator.core.UiObject;
import com.android.uiautomator.core.UiObjectNotFoundException;
import com.android.uiautomator.core.UiSelector;
import com.android.uiautomator.testrunner.UiAutomatorTestCase;

public class Runner extends UiAutomatorTestCase {

	public void testDemo() throws UiObjectNotFoundException {
		String[] confirmTexts = {"安装", "允许", "确认", "替换", "Install"}; 
		while(true){
			try {
				for(String text:confirmTexts){
					UiObject btn = new UiObject(
							new UiSelector().clickable(true).textContains(text));
					if (btn.exists()) {
						btn.click();
					}
						
				}
			} catch (UiObjectNotFoundException e) {
				e.printStackTrace();
			}

			try {
				Thread.sleep(500);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
	}
}