package com.jack.utils;

/**
 * Created by quqing on 15/9/29.
 */
public interface CmdConfig {

    String UUID_ANDROID = "adb devices|grep -v List|awk '{print $1}'";

    String APP_INFO = "aapt dump badging #apk#";

    String APP_CURRENT_ACTIVITY = "adb -s #udid# shell dumpsys activity";

    String APP_PROCESS_EXISTS = "adb -s #udid# shell ps #package#";

    // api level
    String API_LEVEL = "adb -s #udid# shell getprop ro.build.version.sdk";

    // release version 平台版本号
    String RELEASE_VERSION = "adb -s #udid# shell getprop ro.build.version.release";

    // system version 系统版本号
    String SYS_VERSION = "adb -s #udid# shell getprop ro.build.version.emui";

    // version 版本号
    String VERSION = "adb -s #udid# shell getprop ro.build.display.id";

    // engine version 内核版本号
    String CORE_VERSION = "adb -s #udid# shell cat /proc/version";

    // product 手机型号
    String PRODUCT = "adb -s #udid# shell getprop ro.build.product";

    // resolution 屏幕尺寸
    String RESOLUTION = "adb -s #udid# shell getprop persist.dash.max.rep.resolution";

    // language
    String LANGUAGE = "adb -s #udid# shell getprop persist.sys.language";

    // kill uiautomator 杀死uiautomator进程
    String KILL_APP_PROCESS = "adb -s #udid# shell kill -9 #pid#";

    String KILL_SYS_PROCESS = "kill -9 #pid#";

    // find uiautomator process 查找uiautomator进程
    String FIND_UIAUTOMATOR_PROCESS = "adb -s #udid# shell ps uiautomator";

    // create dir
    String CREATE_DIR = "mkdir #dir#";

    // screen capture
    String SCREEN_CAP = "adb -s #udid# shell \"/system/bin/rm /data/local/tmp/screenshot.png;/system/bin/screencap -p /data/local/tmp/screenshot.png\"";

    String SCREEN_IOS_CAP = "idevicescreenshot -u #udid#;mv -f screenshot*.tiff #path2png#";

    // pull screenshot
    String PULL_SCREENSHOT = "adb -s #udid# pull /data/local/tmp/screenshot.png #path2png#";
    
    //capture screen in limit time
    String SCREEN_RECORD = "adb -s #udid# shell \"/system/bin/rm /data/local/tmp/record.mp4;/system/bin/screenrecord --time-limit #time# --size #width#x#height# /data/local/tmp/record.mp4\"";

    //pull screen record
    String PULL_SCREENRECORD = "adb -s #udid# pull /data/local/tmp/record.mp4 #path2png#";
    
    // logcat pid
    String LOG_CAT_PID = "adb -s #udid# shell ps logcat|grep logcat";

    // ios syslog pid
    String LOG_SYS_PID = "ps -A|grep idevicesyslog|grep -v grep";

    // logcat command for android
    String LOG_ANDROID_CMD = "adb -s #udid# logcat -v time -b events *:I";
    
    // syslog command for ios
    String LOG_IOS_CMD = "idevicesyslog -u #udid#";

    // linux date
    String LINUX_DATE = "echo `date +\"%Y%m%d\"`";

    // linux time
    String LINUX_TIME = "echo `date +\"%H%M\"`";

    // windows date
    String WIN_DATE = "echo %DATE:~0,4%%DATE:~5,2%%DATE:~8,2%";

    // windows time
    String WIN_TIME = "echo %TIME:~0,2%%TIME:~3,2%%TIME:~6,2%";

    // get process info
    String QUERY_PROCESS_INFO_WIN = "tasklist|findstr #pid#";
    
    // kill appium server
//    String LINUX_KILL_APPIUM_SERVER = "ps -A|grep node|grep -v grep|awk 'NR=1 {print $1}'|xargs kill -9";
    String KILL_APPIUM_SERVER_WIN = "tskill #pid#";
    String KILL_APPIUM_SERVER_LINUX = "kill -9 #pid#";
    
    // check appium server
    String CHECK_APPIUM_SERVER_LINUX = "netstat -anop|grep #port#|awk '{print $7}'";
    String CHECK_APPIUM_SERVER_WIN = "netstat -ano|findstr #port#|findstr LISTENING";
    
    // start appium server
    String START_APPIUM_SERVER = "appium -p #port# --session-override --command-timeout 7200";
}