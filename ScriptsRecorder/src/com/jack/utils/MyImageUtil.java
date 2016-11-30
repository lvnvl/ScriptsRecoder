package com.jack.utils;

import java.io.File;
import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;

import com.android.uiautomator.NewScriptDialog;
import com.android.uiautomator.UiAutomatorView;
import com.jack.model.Constant;

public class MyImageUtil {

	public static void saveSWTImage(Image img, int saveMode) throws IOException {
		ImageData imageData = img.getImageData( );
        ImageLoader imageLoader = new ImageLoader();
        imageLoader.data = new ImageData[1];
        imageLoader.data[0] = imageData;
        String scriptDir = NewScriptDialog.getsScriptSaveDirectory();
		String imageSavePath = scriptDir + File.separator + "imgs";
		switch (saveMode) {
		case Constant.SAVE:
			File imgFile = new File(imageSavePath + File.separator + (UiAutomatorView.count++) + ".jpg");
			FileUtil.createFile(imgFile);
			imageLoader.save(imgFile.getAbsolutePath(), SWT.IMAGE_JPEG);
			break;
		case Constant.REPLACE:
			File imgFile2 = new File(imageSavePath + File.separator + (--UiAutomatorView.count) + ".jpg");
			imageLoader.save(imgFile2.getAbsolutePath(), SWT.IMAGE_JPEG);
			UiAutomatorView.count++;
			break;
		case Constant.IGNORE:
			break;
		default:
			break;
		}
	}
}
