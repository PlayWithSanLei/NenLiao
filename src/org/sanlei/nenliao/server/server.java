package org.sanlei.nenliao.server;

import org.sanlei.nenliao.utils.Configuration;
import org.sanlei.nenliao.utils.Util;

import java.net.ServerSocket;
import java.net.Socket;

public class server {
    public static void main(String[] args) throws Exception {
        // create serverSocket
        @SuppressWarnings("resource")
        ServerSocket serverSocket = new ServerSocket(Configuration.PORT);
        // success info
        System.out.println("Server is running" + Util.getLocalHostAddress() + ":" + Configuration.PORT);
        // listen port and create new handleClientRunnable thread
        while(true) {
            // receive client socket
            Socket socket = serverSocket.accept();
            // client IP
            String ip = socket.getInetAddress().getHostAddress();
            // client port
            int port = socket.getPort();
            // create new thread
            Runnable runnable = new HandleMessageRunnable(socket, ip, port);
            Thread handleClientThread = new Thread(runnable);
            handleClientThread.start();
        }
}
}