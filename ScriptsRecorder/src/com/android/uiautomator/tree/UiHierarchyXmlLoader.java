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

package com.android.uiautomator.tree;

import org.eclipse.swt.graphics.Rectangle;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class UiHierarchyXmlLoader {

    private BasicTreeNode mRootNode;
    private List<Rectangle> mNafNodes;

    public UiHierarchyXmlLoader() {
    }

    /**
     * Uses a SAX parser to process XML dump
     * @param xmlPath
     * @return
     */
    public BasicTreeNode parseXmlBySAX(String xmlPath) {
        mRootNode = null;
        mNafNodes = new ArrayList<Rectangle>();
        // standard boilerplate to get a SAX parser
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = null;
        try {
            parser = factory.newSAXParser();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            return null;
        } catch (SAXException e) {
            e.printStackTrace();
            return null;
        }
        // handler class for SAX parser to receiver standard parsing events:
        // e.g. on reading "<foo>", startElement is called, on reading "</foo>",
        // endElement is called
        DefaultHandler handler = new DefaultHandler(){
            BasicTreeNode mParentNode;
            BasicTreeNode mWorkingNode;
            @Override
            public void startElement(String uri, String localName, String qName,
                    Attributes attributes) throws SAXException {
                boolean nodeCreated = false;
                // starting an element implies that the element that has not yet been closed
                // will be the parent of the element that is being started here
                mParentNode = mWorkingNode;
                if ("hierarchy".equals(qName)) {
                    mWorkingNode = new RootWindowNode(attributes.getValue("windowName"));
                    nodeCreated = true;
                } else if ("node".equals(qName)) {
                    UiNode tmpNode = new UiNode();
                    for (int i = 0; i < attributes.getLength(); i++) {
                        tmpNode.addAtrribute(attributes.getQName(i), attributes.getValue(i));
                    }
                    mWorkingNode = tmpNode;
                    nodeCreated = true;
                    // check if current node is NAF
                    String naf = tmpNode.getAttribute("NAF");
                    if ("true".equals(naf)) {
                        mNafNodes.add(new Rectangle(tmpNode.x, tmpNode.y,
                                tmpNode.width, tmpNode.height));
                    }
                }
                // nodeCreated will be false if the element started is neither
                // "hierarchy" nor "node"
                if (nodeCreated) {
                    if (mRootNode == null) {
                        // this will only happen once
                        mRootNode = mWorkingNode;
                    }
                    if (mParentNode != null) {
                        mParentNode.addChild(mWorkingNode);
                    }
                }
            }

            @Override
            public void endElement(String uri, String localName, String qName) throws SAXException {
                //mParentNode should never be null here in a well formed XML
                if (mParentNode != null) {
                    // closing an element implies that we are back to working on
                    // the parent node of the element just closed, i.e. continue to
                    // parse more child nodes
                    mWorkingNode = mParentNode;
                    mParentNode = mParentNode.getParent();
                }
            }
        };
        try {
            parser.parse(new File(xmlPath), handler);
        } catch (SAXException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return mRootNode;
    }

    /**
     * recrusively process the nodelist
     * @param nodeList
     * @param parentNode
     */
    protected void parse(NodeList nodeList, BasicTreeNode parentNode){
    	
    	for (int i = 0; i < nodeList.getLength(); i++) {
    		Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
            	UiNode tmpNode = new UiNode();
        		NamedNodeMap map = node.getAttributes();
        		if(null != map){
        			for (int j = 0; j < map.getLength(); j++) {
        				//获得该元素的每一个属性
                        Attr attr = (Attr)map.item(j);
                        tmpNode.addAtrribute(attr.getName(), attr.getValue());
//                        System.out.println("key:" + attr.getName() + ";value:" + attr.getValue());
                    }
        			tmpNode.setxPath(getXPath(node));
        			tmpNode.addAtrribute("XPath", tmpNode.getxPath());
//        			System.out.println(tmpNode.getxPath());
        			tmpNode.mParent = parentNode;
        		}
        		
                if(null != tmpNode){
                	// check if current node is NAF
                    String naf = tmpNode.getAttribute("NAF");
                    if ("true".equals(naf)) {
                        mNafNodes.add(new Rectangle(tmpNode.x, tmpNode.y,
                                tmpNode.width, tmpNode.height));
                    }
                	parentNode.mChildren.add(tmpNode);
                	parse(node.getChildNodes(), tmpNode);
                }
                
            }
    		
    	}
    	
    }
    /**
     * use dom operations to generate uinode and it's absoluate xpath
     * @param xmlPath
     * @return
     */
    public BasicTreeNode parseXmlByDOM(String pageSource){
//    	System.out.println("----------\n"+pageSource+"\n----------");
    	mRootNode = null;
        mNafNodes = new ArrayList<Rectangle>();
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new ByteArrayInputStream(pageSource.getBytes("UTF8")));
//            System.out.println(doc == null);
//            System.out.println(doc.hasChildNodes());
//            System.out.println(doc.getFirstChild().toString());
            //获得根元素结点  
            Element root = doc.getDocumentElement();
            mRootNode = new RootWindowNode(root.getAttribute("windowName"));
            if (doc.hasChildNodes()) {
                parse(root.getChildNodes(), mRootNode);
            }
        } catch (Exception e) {
        	System.out.println("catch exception!");
            e.fillInStackTrace();
        }
        return mRootNode;
    }
    
    /**
     * Returns the list of "Not Accessibility Friendly" nodes found during parsing.
     *
     * Call this function after parsing
     *
     * @return
     */
    public List<Rectangle> getNafNodes() {
        return Collections.unmodifiableList(mNafNodes);
    }
    
    /**
     * 根据属性名获取属性值
     *
     * @param nodeMap
     * @param attributeName
     * @return the value of attribute
     */
    protected String getAttribute(NamedNodeMap nodeMap, String attributeName) {
        Node attrNode;
        attrNode = nodeMap.getNamedItem(attributeName);
        if (null == attrNode)
            return null;
        return attrNode.getNodeValue();
    }
    
    /**
     * 
     * @param Node n
     * @return xPath
     */
    public String getXPath(Node n) {
        String clazz1;
        String clazz2;
        NamedNodeMap nodeMap;

        if (null == n)
            return null;

        Node parent = null;
        Stack<Node> hierarchy = new Stack<Node>();
        StringBuffer buffer = new StringBuffer();

        hierarchy.push(n);

        switch (n.getNodeType()) {
            case Node.ATTRIBUTE_NODE:
                parent = ((Attr) n).getOwnerElement();
                break;
            case Node.ELEMENT_NODE:
                parent = n.getParentNode();
                break;
            case Node.DOCUMENT_NODE:
                break;
            default:
                throw new IllegalStateException("Unexpected Node type" + n.getNodeType());
        }

        while (null != parent && parent.getNodeType() != Node.DOCUMENT_NODE) {
            hierarchy.push(parent);
            parent = parent.getParentNode();
        }

        Object obj;
        while (!hierarchy.isEmpty() && null != (obj = hierarchy.pop())) {
            Node node = (Node) obj;
            boolean handled = false;

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                nodeMap = node.getAttributes();
                clazz1 = getAttribute(nodeMap, "class");

                if (buffer.length() == 0) {
                    buffer.append(clazz1);
                } else {
                    buffer.append("/");
                    buffer.append(clazz1);

                    if (!handled) {
                        int prev_siblings = 1;
                        Node prev_sibling = node.getPreviousSibling();
                        while (null != prev_sibling) {
                            if (prev_sibling.getNodeType() == node.getNodeType()) {
                                nodeMap = prev_sibling.getAttributes();
                                clazz2 = getAttribute(nodeMap, "class");
                                if (clazz2.equalsIgnoreCase(clazz1)) {
                                    prev_siblings++;
                                }
                            }
                            prev_sibling = prev_sibling.getPreviousSibling();
                        }
                        buffer.append("[" + prev_siblings + "]");
                    }
                }
            } else if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
                nodeMap = node.getAttributes();
                clazz1 = getAttribute(nodeMap, "class");
                buffer.append("/@");
                buffer.append(clazz1);
            }
        }
        return buffer.toString().replaceAll("null", "/");
    }
}
