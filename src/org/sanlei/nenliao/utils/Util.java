package org.sanlei.nenliao.utils;
import java.awt.Toolkit;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Util {
    public static String getCurrentTime() {
        String time=null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        time=sdf.format(new Date());
        return time;
    }

    public static int getScreenWidth() {
        int screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
        return screenWidth;
    }

    public static int getScreenHeight() {
        int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
        return screenHeight;
    }

    public static String getLocalHostAddress() {
        String localHostAddress=null;
        try {
            localHostAddress=InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return localHostAddress;
    }

}

