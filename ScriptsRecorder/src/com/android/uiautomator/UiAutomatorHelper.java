/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.uiautomator;

import com.android.ddmlib.CollectingOutputReceiver;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.SyncService;
import io.appium.java_client.android.AndroidDriver;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebElement;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

public class UiAutomatorHelper {
	public static final int UIAUTOMATOR_MIN_API_LEVEL = 16;

	private static final String UIAUTOMATOR = "/system/bin/uiautomator"; //$NON-NLS-1$
	private static final String UIAUTOMATOR_DUMP_COMMAND = "dump"; //$NON-NLS-1$
	private static final String UIDUMP_DEVICE_PATH = "/data/local/tmp/uidump.xml"; //$NON-NLS-1$

	@SuppressWarnings("unused")
	private static boolean supportsUiAutomator(IDevice device) {
		String apiLevelString = device.getProperty(IDevice.PROP_BUILD_API_LEVEL);
		int apiLevel;
		try {
			apiLevel = Integer.parseInt(apiLevelString);
		} catch (NumberFormatException e) {
			apiLevel = UIAUTOMATOR_MIN_API_LEVEL;
		}

		return apiLevel >= UIAUTOMATOR_MIN_API_LEVEL;
	}

	@SuppressWarnings("unused")
	private static void getUiHierarchyFile(IDevice device, File dst, IProgressMonitor monitor) {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		monitor.subTask("Deleting old UI XML snapshot ...");
		String command = "rm " + UIDUMP_DEVICE_PATH;

		try {
			CountDownLatch commandCompleteLatch = new CountDownLatch(1);
			device.executeShellCommand(command, new CollectingOutputReceiver(commandCompleteLatch));
			commandCompleteLatch.await(5, TimeUnit.SECONDS);
		} catch (Exception e1) {
			// ignore exceptions while deleting stale files
		}

		monitor.subTask("Taking UI XML snapshot...");
		command = String.format("%s %s %s", UIAUTOMATOR, UIAUTOMATOR_DUMP_COMMAND, UIDUMP_DEVICE_PATH);
		CountDownLatch commandCompleteLatch = new CountDownLatch(1);

		try {
			device.executeShellCommand(command, new CollectingOutputReceiver(commandCompleteLatch));
			commandCompleteLatch.await(20, TimeUnit.SECONDS);

			monitor.subTask("Pull UI XML snapshot from device...");
			device.getSyncService().pullFile(UIDUMP_DEVICE_PATH, dst.getAbsolutePath(),
					SyncService.getNullProgressMonitor());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static UiAutomatorResult takeSnapshot(IProgressMonitor monitor, AndroidDriver<WebElement> driver) throws UiAutomatorException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.subTask("Obtaining UI hierarcy");
		String uiHierarcy = driver.getPageSource();
		UiAutomatorModel model = new UiAutomatorModel(uiHierarcy);

		monitor.subTask("Obtaining device screenshot");
		byte[] imageBytes = driver.getScreenshotAs(OutputType.BYTES);
		BufferedImage bi = null;
		try {
			bi = ImageIO.read(new ByteArrayInputStream(imageBytes));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BufferedImage scaledImage = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_ARGB);// 把现有的bufferedImage的颜色模式转换为DirectColorModel
		Graphics2D localGraphics2D = scaledImage.createGraphics();
		localGraphics2D.drawImage(bi, 0, 0, scaledImage.getWidth(), scaledImage.getHeight(), null);
		localGraphics2D.dispose();
		ImageData imageData = convertToSWT(scaledImage);
		Image screenshot = new Image(Display.getDefault(), imageData);
		return new UiAutomatorResult(uiHierarcy, model, screenshot);
	}

	public static ImageData convertToSWT(BufferedImage bufferedImage) {
		if (bufferedImage.getColorModel() instanceof DirectColorModel) {
			DirectColorModel colorModel = (DirectColorModel) bufferedImage.getColorModel();
			PaletteData palette = new PaletteData(colorModel.getRedMask(), colorModel.getGreenMask(),
					colorModel.getBlueMask());
			ImageData data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(),
					colorModel.getPixelSize(), palette);
			WritableRaster raster = bufferedImage.getRaster();
			int[] pixelArray = new int[4];
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					raster.getPixel(x, y, pixelArray);
					int pixel = palette.getPixel(new RGB(pixelArray[0], pixelArray[1], pixelArray[2]));
					data.setPixel(x, y, pixel);
				}
			}
			return data;
		} else if (bufferedImage.getColorModel() instanceof IndexColorModel) {
			IndexColorModel colorModel = (IndexColorModel) bufferedImage.getColorModel();
			int size = colorModel.getMapSize();
			byte[] reds = new byte[size];
			byte[] greens = new byte[size];
			byte[] blues = new byte[size];
			colorModel.getReds(reds);
			colorModel.getGreens(greens);
			colorModel.getBlues(blues);
			RGB[] rgbs = new RGB[size];
			for (int i = 0; i < rgbs.length; i++) {
				rgbs[i] = new RGB(reds[i] & 0xFF, greens[i] & 0xFF, blues[i] & 0xFF);
			}
			PaletteData palette = new PaletteData(rgbs);
			ImageData data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(),
					colorModel.getPixelSize(), palette);
			data.transparentPixel = colorModel.getTransparentPixel();
			WritableRaster raster = bufferedImage.getRaster();
			int[] pixelArray = new int[1];
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					raster.getPixel(x, y, pixelArray);
					data.setPixel(x, y, pixelArray[0]);
				}
			}
			return data;
		}
		return null;
	}

	@SuppressWarnings("serial")
	public static class UiAutomatorException extends Exception {
		public UiAutomatorException(String msg, Throwable t) {
			super(msg, t);
		}
	}

	public static class UiAutomatorResult {
		public final String uiHierarchy;
		public final UiAutomatorModel model;
		public final Image screenshot;

		public UiAutomatorResult(String pageSource, UiAutomatorModel m, Image s) {
			uiHierarchy = pageSource;
			model = m;
			screenshot = s;
		}
	}
}
