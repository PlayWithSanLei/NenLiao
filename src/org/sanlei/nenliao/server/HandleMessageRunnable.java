package org.sanlei.nenliao.server;

import org.sanlei.nenliao.utils.Configuration;
import org.sanlei.nenliao.utils.Util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

public class HandleMessageRunnable implements Runnable {
    // get uid from merge ip and port of client
    private String clientUid = null;
    // save all uid
    static ArrayList<String> clientUidArrayList = new ArrayList<String>();
    // hashmap save the key and value by collection of uid and handleMessageRunnable
    static HashMap<String, HandleMessageRunnable> hashMap = new HashMap<String, HandleMessageRunnable>();

    // parameters
    private Socket socket;
    private String ip;
    private int port;
    public HandleMessageRunnable(Socket socket, String ip, int port) {
        this.socket = socket;
        this.ip = ip;
        this.port = port;
        this.clientUid = ip + ":" + port;
    }

    @Override
    public void run() {
        try {
            addClient();
            sendConnectedMessage();
            updateOnlineList();
            handleMessage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addClient() {
        clientUidArrayList.add(clientUid);
        hashMap.put(clientUid, this);
    }

    public void removeClient() {
        int index = clientUidArrayList.indexOf(clientUid);
        clientUidArrayList.remove(index);
        hashMap.remove(clientUid);
    }

    private InputStream socketInputStream;
    private OutputStream socketOutputStream;
    public void sendConnectedMessage() throws IOException {
        // get InputStream
        socketInputStream = socket.getInputStream();
        // get OutputStream
        socketOutputStream = socket.getOutputStream();
        // send the message of connect successfully to client
        String successMessage = "server: " + Configuration.NEWLINE + Util.getCurrentTime() + Configuration.NEWLINE + "Connect successfully!" + Configuration.NEWLINE
                + "server IP: " + Util.getLocalHostAddress() + ", port: " + Configuration.PORT + Configuration.NEWLINE
                + "Client IP: " + ip + ", port: " + port + Configuration.NEWLINE;
        socketOutputStream.write(successMessage.getBytes());
    }

    public void handleMessage() throws IOException {
        byte[] buf = new byte[1024];
        int len = 0;
        // listen and forward message from client
        while(true) {
            len = socketInputStream.read(buf);
            String message = new String(buf, 0, len);
            System.out.println("server receive message: " + message);
            int separatorIndex = message.indexOf(Configuration.SEPARATOR);
            // message type: quit or chat
            String messageType = message.substring(0, separatorIndex);
            // message: null or message
            String messageContent = message.substring(separatorIndex + 1);
            // Process message according to message type
            if(messageType.equals(Configuration.TYPE_EXIT)) {
                // update ArrayList and HashMap, remove quited uid and thread
                removeClient();
                // update onlinelist
                updateOnlineList();
                // end
                break;
            }
            // chat
            if(messageType.equals(Configuration.TYPE_CHAT)) {
                int messageContentSeparatorIndex = messageContent.indexOf(Configuration.SEPARATOR);
                // get receiver's address
                String[] receiveClientUidArray = messageContent.substring(0, messageContentSeparatorIndex).split(",");
                // get chat content
                String word = messageContent.substring(messageContentSeparatorIndex + 1);
                // send message to receiver
                sendChatMessage(clientUid, receiveClientUidArray, word);
            }
        }
    }

    // update online list to all client
    public void updateOnlineList() throws IOException {
        StringBuilder stringBuilder = new StringBuilder(Configuration.TYPE_UPDATE_ONLINE_LIST + Configuration.SEPARATOR);
        for(String clientUid: clientUidArrayList) {
            stringBuilder.append(clientUid);
            int index = clientUidArrayList.indexOf(clientUid);
            int size = clientUidArrayList.size();
            if (index != size -1) {
                stringBuilder.append(",");
            }
        }
        String onlineClients = stringBuilder.toString();
        for(String clientUid:clientUidArrayList) {
            OutputStream out = hashMap.get(clientUid).socket.getOutputStream();
            out.write(onlineClients.getBytes());
        }
    }

    // send message to client
    public void sendChatMessage(String sendClientUid, String[] receiveClientUidArray, String word) throws IOException {
        for(String clientUid : receiveClientUidArray) {
            OutputStream out = hashMap.get(clientUid).socket.getOutputStream();
            String message = Configuration.TYPE_CHAT + Configuration.SEPARATOR + sendClientUid + Configuration.SEPARATOR + word;
            out.write(message.getBytes());
        }
    }
}
