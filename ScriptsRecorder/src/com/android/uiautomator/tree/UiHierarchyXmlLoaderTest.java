package com.android.uiautomator.tree;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class UiHierarchyXmlLoaderTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testParseXmlByDOM() {
		String pageSource = null;
//		System.out.println(System.getProperty("user.dir"));
		try {
			pageSource = FileUtils.readFileToString(new File(System.getProperty("user.dir")+"/dump.xml"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		UiHierarchyXmlLoader loader = new UiHierarchyXmlLoader();
		assertTrue("read error", pageSource != null);
		BasicTreeNode root = loader.parseXmlByDOM(pageSource);
		assertTrue("parse error", root != null);
	}

}
