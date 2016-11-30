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

import com.android.uiautomator.UiAutomatorHelper.UiAutomatorException;
import com.android.uiautomator.UiAutomatorHelper.UiAutomatorResult;
import com.android.uiautomator.actions.AddSleepAction;
import com.android.uiautomator.actions.FinishRecordAction;
import com.android.uiautomator.actions.RefreshAction;
import com.android.uiautomator.tree.AttributePair;
import com.android.uiautomator.tree.BasicTreeNode;
import com.android.uiautomator.tree.UiNode;
import com.jack.appium.AppiumActUtil;
import com.jack.model.Action;
import com.jack.model.AppiumConfig;
import com.jack.model.Constant;
import com.jack.model.Operate;
import com.jack.model.RunningInfo;
import com.jack.utils.ErrorHandler;
import com.jack.utils.MyImageUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.ToolBar;
import org.openqa.selenium.By;


public class UiAutomatorView extends Composite {
    private static final int IMG_BORDER = 2;
    public static int count;         // 下一张要保存的图片编号

    // The screenshot area is made of a stack layout of two components: screenshot canvas and
    // a "specify screenshot" button. If a screenshot is already available, then that is displayed
    // on the canvas. If it is not availble, then the "specify screenshot" button is displayed.
    private Composite mScreenshotComposite;
    private Composite mRunningInfoComposite;
    private StackLayout mStackLayout;
    private Canvas mScreenshotCanvas;
    private ListViewer mRIListViewer;

    private TableViewer mTableViewer;

	private AppiumConfig appiumConfig;
    private ListViewer mListViewer;
    
    private float mScale = 1.0f;
    private int mDx, mDy;

    private UiAutomatorModel mModel;
    private ArrayList<Action> actions;
    private ArrayList<RunningInfo> runningInfos;
    private ArrayList<String> pageSources;
//    private File mModelFile;
    private Image mScreenshot;

    public UiAutomatorView(Composite parent, int style) {
        super(parent, SWT.NONE);
        setLayout(new FillLayout());

        SashForm baseSash = new SashForm(this, SWT.HORIZONTAL);

        mScreenshotComposite = new Composite(baseSash, SWT.BORDER);
        mStackLayout = new StackLayout();
        mScreenshotComposite.setLayout(mStackLayout);
        // draw the canvas with border, so the divider area for sash form can be highlighted
        mScreenshotCanvas = new Canvas(mScreenshotComposite, SWT.BORDER);
        mScreenshotCanvas.setMenu(createEditPopup(mScreenshotCanvas.getShell()));
        mStackLayout.topControl = mScreenshotCanvas;
        mScreenshotComposite.layout();
        mScreenshotCanvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseUp(MouseEvent e) {
                if (mModel != null) {
//                    mModel.toggleExploreMode();
//                    updateAction(mModel.getSelectedNode());
//                    redrawScreenshot();
                	if(e.button == 1){
                		//left click; perform click
                		performAction(updateAction(mModel.getSelectedNode(), Operate.CLICK, null));
                	}
//                	performAction(updateAction(mModel.getSelectedNode()));
                }
            }
        });
        mScreenshotCanvas.setBackground(
                getShell().getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
        mScreenshotCanvas.addPaintListener(new PaintListener() {
            @Override
            public void paintControl(PaintEvent e) {
                if (mScreenshot != null) {
                    updateScreenshotTransformation();
                    // shifting the image here, so that there's a border around screen shot
                    // this makes highlighting red rectangles on the screen shot edges more visible
                    Transform t = new Transform(e.gc.getDevice());
                    t.translate(mDx, mDy);
                    t.scale(mScale, mScale);
                    e.gc.setTransform(t);
                    e.gc.drawImage(mScreenshot, 0, 0);
                    // this resets the transformation to identity transform, i.e. no change
                    // we don't use transformation here because it will cause the line pattern
                    // and line width of highlight rect to be scaled, causing to appear to be blurry
                    e.gc.setTransform(null);
                    if (mModel.shouldShowNafNodes()) {
                        // highlight the "Not Accessibility Friendly" nodes
                        e.gc.setForeground(e.gc.getDevice().getSystemColor(SWT.COLOR_YELLOW));
                        e.gc.setBackground(e.gc.getDevice().getSystemColor(SWT.COLOR_YELLOW));
                        for (Rectangle r : mModel.getNafNodes()) {
                            e.gc.setAlpha(50);
                            e.gc.fillRectangle(mDx + getScaledSize(r.x), mDy + getScaledSize(r.y),
                                    getScaledSize(r.width), getScaledSize(r.height));
                            e.gc.setAlpha(255);
                            e.gc.setLineStyle(SWT.LINE_SOLID);
                            e.gc.setLineWidth(2);
                            e.gc.drawRectangle(mDx + getScaledSize(r.x), mDy + getScaledSize(r.y),
                                    getScaledSize(r.width), getScaledSize(r.height));
                        }
                    }
                    // draw the mouseover rects
                    Rectangle rect = mModel.getCurrentDrawingRect();
                    if (rect != null) {
                        e.gc.setForeground(e.gc.getDevice().getSystemColor(SWT.COLOR_RED));
//                        if (mModel.isExploreMode()) {
//                            // when we highlight nodes dynamically on mouse move,
//                            // use dashed borders
//                            e.gc.setLineStyle(SWT.LINE_DASH);
//                            e.gc.setLineWidth(1);
//                        } else {
//                            // when highlighting nodes on tree node selection,
//                            // use solid borders
//                            e.gc.setLineStyle(SWT.LINE_SOLID);
//                            e.gc.setLineWidth(2);
//                        }
                        e.gc.setLineStyle(SWT.LINE_SOLID);
                        e.gc.setLineWidth(2);
                        e.gc.drawRectangle(mDx + getScaledSize(rect.x), mDy + getScaledSize(rect.y),
                                getScaledSize(rect.width), getScaledSize(rect.height));
                    }
                }
            }
        });
        mScreenshotCanvas.addMouseMoveListener(new MouseMoveListener() {
            @Override
            public void mouseMove(MouseEvent e) {
//                if (mModel != null && mModel.isExploreMode()) {
            	if (mModel != null) {
                    BasicTreeNode node = mModel.updateSelectionForCoordinates(
                            getInverseScaledSize(e.x - mDx),
                            getInverseScaledSize(e.y - mDy));
                    if (node != null) {
                    	mModel.setSelectedNode(node);
                    	loadAttributeTable();
                        redrawScreenshot();
                    }
                }
            }
        });
        mRunningInfoComposite = new Composite(mScreenshotComposite, SWT.NONE);
        mRunningInfoComposite.setLayout(new GridLayout());
        mRIListViewer = new ListViewer(mRunningInfoComposite , SWT.V_SCROLL|SWT.H_SCROLL);
        runningInfos = new ArrayList<RunningInfo>();
        mRIListViewer.setContentProvider(new IStructuredContentProvider(){
        	@SuppressWarnings("unchecked")
			public Object[] getElements(Object inputElement){
        		ArrayList<RunningInfo> v = (ArrayList<RunningInfo>)inputElement;
        		return v.toArray();
        	}
        	public void dispose(){
        		System.out.println("running info list view disposing...");
        	}
        	public void inputChanged(Viewer v, Object oldO, Object newO){
        		System.out.println("runing info list view content changed, new info:" + newO);
        	}
        });
        mRIListViewer.setInput(runningInfos);
        mRIListViewer.setLabelProvider(new LabelProvider(){
        	public Image getImage(Object o){
        		return null;
        	}
        	public String getText(Object element){
            	return ((RunningInfo)element).getThreadName() 
            			+ " >> " + String.valueOf(((RunningInfo)element).getPercents()) + "%"
            			+ " >> " + ((RunningInfo)element).getState()
            			+ " >> " + ((RunningInfo)element).getDescription();
            }
        });
        mRIListViewer.setSorter(new ViewerSorter(){
        	public int compare(Viewer v, Object o1, Object o2){
        		return ((RunningInfo)o1).getThreadName().compareTo(((RunningInfo)o1).getThreadName());
        	}
        });
        mRIListViewer.getList().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        
        // right sash is split into 2 parts: upper-right and lower-right
        // both are composites with borders, so that the horizontal divider can be highlighted by
        // the borders
        SashForm rightSash = new SashForm(baseSash, SWT.VERTICAL);
        
        // upper-right base contains the toolbar and the tree
        Composite upperRightBase = new Composite(rightSash, SWT.BORDER);
        upperRightBase.setLayout(new GridLayout(1, false));
        ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
		toolBarManager.add(new FinishRecordAction(this));//finsh record and save the script button
		toolBarManager.add(new RefreshAction(this));//refresh and repaint the current page
		toolBarManager.add(new AddSleepAction(this));//when a page need some time to load, click this to add a time to wait
		ToolBar tb = toolBarManager.createControl(upperRightBase);
		tb.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        actions = new ArrayList<Action>();
        pageSources = new ArrayList<String>();
        appiumConfig = null;
        mListViewer = new ListViewer(upperRightBase, SWT.BORDER);
        mListViewer.setContentProvider(new IStructuredContentProvider(){
        	@SuppressWarnings("unchecked")
			public Object[] getElements(Object inputElement){
        		ArrayList<Action> v = (ArrayList<Action>)inputElement;
        		return v.toArray();
        	}
        	public void dispose(){
        		System.out.println("list view disposing...");
        	}
        	public void inputChanged(Viewer v, Object oldO, Object newO){
        		System.out.println("list view input changed,from "+oldO+"to "+newO);
        	}
        });
        mListViewer.setInput(actions);
        mListViewer.setLabelProvider(new LabelProvider(){
        	public Image getImage(Object o){
        		return null;
        	}
        	public String getText(Object element){
        		if(Operate.CLICK.equals(((Action)element).getType())){
        			return ((Action)element).getType() + ">>" + ((Action)element).getItemName();
        		} else if(Operate.INPUT.equals(((Action)element).getType())){
        			return ((Action)element).getType() + ">>" + ((Action)element).getItemName() + "[|]" + ((Action)element).getOperation().split("[|]")[1];
        		} else if(Operate.SWIPE.equals(((Action)element).getType())){
        			return ((Action)element).getType() + ">>" + ((Action)element).getItemName() + "[|]" + ((Action)element).getOperation().split("[|]")[1];
        		} else if(Operate.SLEEP.equals(((Action)element).getType())){
        			return ((Action)element).getType() + ">>" + ((Action)element).getItemName() + "[|]" + ((Action)element).getOperation().split("[|]")[1] + " s";
        		}
            	return ((Action)element).getType() + ">>" + ((Action)element).getOperation();
            }	
        });
        mListViewer.addFilter(new ViewerFilter(){
        	public boolean select(Viewer v, Object pe, Object e){
//        		if(((Action)e).getType().equals("click")){
//        			return true;
//        		}else{
//        			return false;
//        		}
        		return true;
        	}
        });
        mListViewer.setSorter(new ViewerSorter(){
        	public int compare(Viewer v, Object o1, Object o2){
        		return ((Action)o1).getOperation().compareTo(((Action)o1).getOperation());
        	}
        });
        mListViewer.getList().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        // lower-right base contains the detail group
        Composite lowerRightBase = new Composite(rightSash, SWT.BORDER);
        lowerRightBase.setLayout(new FillLayout());
        Group grpNodeDetail = new Group(lowerRightBase, SWT.NONE);
        grpNodeDetail.setLayout(new FillLayout(SWT.HORIZONTAL));
        grpNodeDetail.setText("Node Detail");

        Composite tableContainer = new Composite(grpNodeDetail, SWT.NONE);

        TableColumnLayout columnLayout = new TableColumnLayout();
        tableContainer.setLayout(columnLayout);

        mTableViewer = new TableViewer(tableContainer, SWT.NONE | SWT.FULL_SELECTION);
        Table table = mTableViewer.getTable();
        table.setLinesVisible(true);
        // use ArrayContentProvider here, it assumes the input to the TableViewer
        // is an array, where each element represents a row in the table
        mTableViewer.setContentProvider(new ArrayContentProvider());

        TableViewerColumn tableViewerColumnKey = new TableViewerColumn(mTableViewer, SWT.NONE);
        TableColumn tblclmnKey = tableViewerColumnKey.getColumn();
        tableViewerColumnKey.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                if (element instanceof AttributePair) {
                    // first column, shows the attribute name
                    return ((AttributePair)element).key;
                }
                return super.getText(element);
            }
        });
        columnLayout.setColumnData(tblclmnKey,
                new ColumnWeightData(1, ColumnWeightData.MINIMUM_WIDTH, true));

        TableViewerColumn tableViewerColumnValue = new TableViewerColumn(mTableViewer, SWT.NONE);
        tableViewerColumnValue.setEditingSupport(new AttributeTableEditingSupport(mTableViewer));
        TableColumn tblclmnValue = tableViewerColumnValue.getColumn();
        columnLayout.setColumnData(tblclmnValue,
                new ColumnWeightData(2, ColumnWeightData.MINIMUM_WIDTH, true));
        tableViewerColumnValue.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                if (element instanceof AttributePair) {
                    // second column, shows the attribute value
                    return ((AttributePair)element).value;
                }
                return super.getText(element);
            }
        });
        // sets the ratio of the vertical split: left 5 vs right 3
        baseSash.setWeights(new int[]{5, 3});
    }

    private int getScaledSize(int size) {
        if (mScale == 1.0f) {
            return size;
        } else {
            return new Double(Math.floor((size * mScale))).intValue();
        }
    }

    private int getInverseScaledSize(int size) {
        if (mScale == 1.0f) {
            return size;
        } else {
            return new Double(Math.floor((size / mScale))).intValue();
        }
    }

    private void updateScreenshotTransformation() {
        Rectangle canvas = mScreenshotCanvas.getBounds();
        Rectangle image = mScreenshot.getBounds();
        float scaleX = (canvas.width - 2 * IMG_BORDER - 1) / (float)image.width;
        float scaleY = (canvas.height - 2 * IMG_BORDER - 1) / (float)image.height;
        // use the smaller scale here so that we can fit the entire screenshot
        mScale = Math.min(scaleX, scaleY);
        // calculate translation values to center the image on the canvas
        mDx = (canvas.width - getScaledSize(image.width) - IMG_BORDER * 2) / 2 + IMG_BORDER;
        mDy = (canvas.height - getScaledSize(image.height) - IMG_BORDER * 2) / 2 + IMG_BORDER;
    }

    private class AttributeTableEditingSupport extends EditingSupport {

        private TableViewer mViewer;

        public AttributeTableEditingSupport(TableViewer viewer) {
            super(viewer);
            mViewer = viewer;
        }

        @Override
        protected boolean canEdit(Object arg0) {
            return true;
        }

        @Override
        protected CellEditor getCellEditor(Object arg0) {
            return new TextCellEditor(mViewer.getTable());
        }

        @Override
        protected Object getValue(Object o) {
            return ((AttributePair)o).value;
        }

        @Override
        protected void setValue(Object arg0, Object arg1) {
        }
    }

    /**
     * Causes a redraw of the canvas.
     *
     * The drawing code of canvas will handle highlighted nodes and etc based on data
     * retrieved from Model
     */
    public void redrawScreenshot() {
    	if(mScreenshot == null){
    		mStackLayout.topControl = mRunningInfoComposite;
    	}else{
    		mStackLayout.topControl = mScreenshotCanvas;
    	}
        mScreenshotComposite.layout();

        mScreenshotCanvas.redraw();
    }

    public void loadAttributeTable() {
        // udpate the lower right corner table to show the attributes of the node
        mTableViewer.setInput(mModel.getSelectedNode().getAttributesArray());
    }

    public void performAction(Action action){
    	try {
    		//first,use appium to perform action
        	if(Operate.CLICK.equals(action.getType())){
        		appiumConfig.getDriver().findElement(By.xpath(action.getxPath())).click();
            }else if(Operate.INPUT.equals(action.getType())){
            	System.out.println("\toperation:"+action.getOperation());
            	System.out.println("\toperation: input " + action.getOperation().split("[|]")[1]);
            	appiumConfig.getDriver().findElement(
            			By.xpath(action.getxPath())).sendKeys(action.getOperation().split("[|]")[1]);
            }else if(Operate.SWIPE.equals(action.getType())){
            	System.out.println("\toperation:"+action.getOperation());
            	System.out.println("\toperation: swipe to" + action.getOperation().split("[|]")[1]);
            	switch( action.getOperation().split("[|]")[1]){
            	case "up":
            		AppiumActUtil.swipe(appiumConfig.getDriver(), "up");
            		break;
            	case "right":
            		AppiumActUtil.swipe(appiumConfig.getDriver(), "right");
            		break;
            	case "left":
            		AppiumActUtil.swipe(appiumConfig.getDriver(), "left");
            		break;
            	case "down":
            		AppiumActUtil.swipe(appiumConfig.getDriver(), "down");
            		break;
            	default:
            		break;
            	}
            }else if (Operate.SENDKC.equals(action.getType())) {
            	System.out.println("\toperation:"+action.getOperation());
            	System.out.println("\toperation: send key code " + action.getOperation().split("[|]")[1]);
            	AppiumActUtil.sendKeyCode(appiumConfig.getDriver(), action.getOperation().split("[|]")[1]);
    		}else if (Operate.SLEEP.equals(action.getType())) {
    			System.out.println("\toperation:"+action.getOperation());
            	System.out.println("\toperation: send key code " + action.getOperation().split("[|]")[1]);
            	Thread.sleep(Integer.valueOf(action.getOperation().split("[|]")[1]));
//            	AppiumActUtil.sendKeyCode(appiumConfig.getDriver(), action.getOperation().split("[|]")[1]);
    		}
		} catch (Exception e) {
			// TODO: handle exception
		}
    	
    	//then ,repaint and refresh the data
    	ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
        try {
            dialog.run(true, true, new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException,
                InterruptedException {
//                    UiAutomatorResult result = null;
                    try {
                    	
                		Thread.sleep(2*1000);
                		
                    	UiAutomatorResult result = UiAutomatorHelper.takeSnapshot(monitor, appiumConfig.getDriver());
                    	MyImageUtil.saveSWTImage(result.screenshot, Constant.SAVE);
//                    	System.out.println("test driver init;currentActivity:" + AppiumConfig.getDriver().currentActivity());
//                      setModel(result.model, result.uiHierarchy, result.screenshot);
                        if (Display.getDefault().getThread() != Thread.currentThread()) {
                            Display.getDefault().syncExec(new Runnable() {
                                @Override
                                public void run() {
                                	setModel(result.model, result.uiHierarchy, result.screenshot);
                                }
                            });
                        } else {
                        	setModel(result.model, result.uiHierarchy, result.screenshot);
                        }
                        System.out.println("set model done!!!");
                    } catch (UiAutomatorException e) {
                        monitor.done();
                        appiumConfig.getDriver().quit();
                        ErrorHandler.showError(getShell(), "Appium obtain page error", e);
                        return;
                    } catch (Exception e){
                    	e.printStackTrace();
                    	return;
                    }
                    
//                    AppiumConfig.getDriver().quit();
                    monitor.done();
                    System.out.println("monitor done!!!");
                }
            });
        } catch (Exception e) {
        	e.printStackTrace();
            ErrorHandler.showError(getShell(), "Unexpected error while obtaining UI hierarchy", e);
        }
    }
    
    /**
     * 
     * @author ZYC
     * @param node 操作的XML节点，
     * @param type 操作类型
     * @param arg  操作参数
     * @return  Action  根据传入信息，构造出的Action对象
     * 2016年11月28日
     */
    public Action updateAction(BasicTreeNode node, String type, String arg){
    	Action action = new Action(type);
    	if(Operate.CLICK.equals(type)){
    		action.setUiNodeInfo(node);
    	}else if(Operate.INPUT.equals(type)){
    		action.setUiNodeInfo(node);
    		action.setArgument(arg);
    	}else if(Operate.SWIPE.equals(type)){
    		action.setArgument(arg);
    	}else if(Operate.SENDKC.equals(type)){
    		action.setArgument(arg);
    	}else if(Operate.SLEEP.equals(type)){
    		action.setArgument(arg);
    	}
    	actions.add(action);
    	System.out.println(type + action.getItemName());
    	mListViewer.refresh(false);
    	return action;
    }
    
    public Menu createEditPopup(Shell parentShell){
    	Menu popMenu = new Menu(parentShell, SWT.POP_UP);
    	MenuItem inputItem = new MenuItem(popMenu, SWT.CASCADE);
    	MenuItem swipeItem = new MenuItem(popMenu, SWT.CASCADE);
    	MenuItem sendKCItem = new MenuItem(popMenu, SWT.CASCADE);
    	inputItem.setText("&input");
    	inputItem.setImage(new Image(null, "images/input.png"));
    	swipeItem.setText("&swipe");
    	swipeItem.setImage(new Image(null, "images/swipe.png"));
    	sendKCItem.setText("&send key");
    	sendKCItem.setImage(new Image(null, "images/sendKC.png"));
    	inputItem.addSelectionListener(new SelectionListener(){

			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				BasicTreeNode node = mModel.getSelectedNode();
				if(!((UiNode)node).getAttribute("class").contains("EditText")){
					ErrorHandler.showError(getShell(),"Error,not a inputable widget", new Exception("unsupport input widget"));
					return;
				}
				InputDialog idg = new InputDialog(parentShell);
				if(idg.open() != InputDialog.OK){
					return;
				}
				if(InputDialog.getInput() == null || InputDialog.getInput().trim().equals("")){
					return;
				}
				performAction(updateAction(mModel.getSelectedNode(), Operate.INPUT, InputDialog.getInput()));
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
    		
    	});
    	class SwipeSelectionListener implements SelectionListener{

    		private String dire;
			public SwipeSelectionListener(String string) {
				// TODO Auto-generated constructor stub
				dire = string;
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				performAction(updateAction(mModel.getSelectedNode(), Operate.SWIPE, dire));
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
    		
    	}
    	Menu swipeDirectionMenu = new Menu(parentShell, SWT.DROP_DOWN);
    	swipeItem.setMenu(swipeDirectionMenu);
    	MenuItem swipeUpItem = new MenuItem(swipeDirectionMenu, SWT.PUSH);
    	swipeUpItem.setText("&Up");
    	MenuItem swipeRightItem = new MenuItem(swipeDirectionMenu, SWT.PUSH);
    	swipeRightItem.setText("&Right");
    	MenuItem swipeLeftItem = new MenuItem(swipeDirectionMenu, SWT.PUSH);
    	swipeLeftItem.setText("&Left");
    	MenuItem swipeDownItem = new MenuItem(swipeDirectionMenu, SWT.PUSH);
    	swipeDownItem.setText("&Down");
    	swipeUpItem.addSelectionListener(new SwipeSelectionListener("up"));
    	swipeRightItem.addSelectionListener(new SwipeSelectionListener("right"));
    	swipeLeftItem.addSelectionListener(new SwipeSelectionListener("left"));
    	swipeDownItem.addSelectionListener(new SwipeSelectionListener("down"));
    	Menu sendKCOptionMenu = new Menu(parentShell, SWT.DROP_DOWN);
    	sendKCItem.setMenu(sendKCOptionMenu);
    	MenuItem sendBACKItem = new MenuItem(sendKCOptionMenu, SWT.PUSH);
    	sendBACKItem.setText("&BACK");
    	sendBACKItem.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				performAction(updateAction(mModel.getSelectedNode(), Operate.SENDKC, "back"));
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
    	return popMenu;
    	
    }
    
    public void setModel(UiAutomatorModel model, String pageSource, Image screenshot) {
        mModel = model;
//        mModelFile = pageSource;

        if (mScreenshot != null) {
            mScreenshot.dispose();
        }
        mScreenshot = screenshot;
        if (pageSource != null) {
            pageSources.add(pageSource);	
        }
        redrawScreenshot();
        mListViewer.refresh(false);
    }

    public boolean shouldShowNafNodes() {
        return mModel != null ? mModel.shouldShowNafNodes() : false;
    }

    public void toggleShowNaf() {
        if (mModel != null) {
            mModel.toggleShowNaf();
        }
    }
    
	/**
	 * @return the mRIListViewer
	 */
	public ListViewer getmRIListViewer() {
		return mRIListViewer;
	}

	/**
	 * @return the runningInfos
	 */
	public ArrayList<RunningInfo> getRunningInfos() {
		return runningInfos;
	}

	/**
	 * @param mRIListViewer the mRIListViewer to set
	 */
	public void setmRIListViewer(ListViewer mRIListViewer) {
		this.mRIListViewer = mRIListViewer;
	}

	/**
	 * @param runningInfos the runningInfos to set
	 */
	public void setRunningInfos(ArrayList<RunningInfo> runningInfos) {
		this.runningInfos = runningInfos;
	}

	/**
	 * @param pageSources the pageSources to set
	 */
	public void setPageSources(ArrayList<String> pageSources) {
		this.pageSources = pageSources;
	}

	/**
	 * @return the pageSources
	 */
	public ArrayList<String> getPageSources() {
		return pageSources;
	}

	/**
	 * @return the appiumConfig
	 */
	public AppiumConfig getAppiumConfig() {
		return appiumConfig;
	}

	/**
	 * @param appiumConfig the appiumConfig to set
	 */
	public void setAppiumConfig(AppiumConfig appiumConfig) {
		this.appiumConfig = appiumConfig;
	}

	/**
	 * @return the mListViewer
	 */
	public ListViewer getmListViewer() {
		return mListViewer;
	}
	/**
	 * @return the actions
	 */
	public ArrayList<Action> getActions() {
		return actions;
	}
	public void addRunningInfos(RunningInfo info){
		synchronized(this){
			runningInfos.add(info);
			if (Display.getDefault().getThread() != Thread.currentThread()) {
	            Display.getDefault().syncExec(new Runnable() {
	                @Override
	                public void run() {
	                    mRIListViewer.refresh();
	                }
	            });
	        } else {
                mRIListViewer.refresh();
	        }
		}
	}

	public void dropLastPageSource() {
		pageSources.remove(pageSources.size() - 1);
	}

	/**
	 * called when a script was finished.
	 * 释放当前页面资源，准备下次录制操作
	 * @author ZYC
	 * 2016年11月28日
	 */
	public void empty() {
		pageSources.clear();
		actions.clear();
		appiumConfig.getDriver().quit();
		appiumConfig.close();
		setAppiumConfig(null);
		count = 0;
		setModel(null, null, null);
	}
}