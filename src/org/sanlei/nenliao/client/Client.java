package org.sanlei.nenliao.client;

import org.sanlei.nenliao.utils.Configuration;
import org.sanlei.nenliao.utils.Util;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class Client {
    private Socket socket = null;
    private FrontEnd frontEnd;
    private InputStream socketInputStream;

    public static void main(String[] args) {
       Client client = new Client();
       client.initFrontEnd();
       client.connectServer();
       client.handleReceiveMessage();
    }

    private void initFrontEnd() {
        frontEnd = new FrontEnd();
        frontEnd.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        int screenWidth = Util.getScreenWidth();
        int screenHeight = Util.getScreenHeight();
        int x = (screenWidth - Configuration.CLIENT_FRAME_WIDTH) / 2;
        int y = (screenHeight - Configuration.CLIENT_FRAME_HEIGHT) / 2;
        frontEnd.setLocation(x, y);
        frontEnd.setVisible(true);
    }

    private void connectServer() {
        try {
            // connect server
            socket = new Socket(InetAddress.getLocalHost(), Configuration.PORT);
            frontEnd.setSocket(socket);
            // getInputStream
            socketInputStream = socket.getInputStream();
            // getOutputStream
            OutputStream socketOutputStream = socket.getOutputStream();
            // get welcome message from server
            byte[] buf = new byte[1024];
            int len = socketInputStream.read(buf);
            // put the welcome message to the message panel
            String welcomeMessage = new String(buf, 0, len);
            frontEnd.messageJTextArea.append(welcomeMessage);
            frontEnd.messageJTextArea.append(Configuration.NEWLINE);
        } catch (Exception e) {
           e.printStackTrace();
        }
    }

    private void handleReceiveMessage(){
        try {
            while(true) {
                byte[] buf = new byte[1024];
                socketInputStream = socket.getInputStream();
                int len = socketInputStream.read(buf);
                // handle messages from server
                String message = new String (buf, 0, len);
                System.out.println("client get message:" + message);
                int separatorIndex = message.indexOf(Configuration.SEPARATOR);
                // messageType: update the list of online or chat
                String messageType = message.substring(0, separatorIndex);
                // message: the newest list of online or chat
                String messageContent = message.substring(separatorIndex+1);
                // chat
                if(messageType.equals(Configuration.TYPE_CHAT)) {
                    int messageContentSeparatorIndex = messageContent.indexOf(Configuration.SEPARATOR);
                    String from = messageContent.substring(0, messageContentSeparatorIndex);
                    String word = messageContent.substring(messageContentSeparatorIndex + 1);
                    frontEnd.messageJTextArea.append(Util.getCurrentTime() + Configuration.NEWLINE + "from" + from + Configuration.NEWLINE + word + Configuration.NEWLINE);
                    frontEnd.messageJTextArea.setCaretPosition(frontEnd.messageJTextArea.getDocument().getLength());
                }
                // update online list
                if(messageType.equals(Configuration.TYPE_UPDATE_ONLINE_LIST)) {
                    // get data model of online list
                    DefaultTableModel tableModel = (DefaultTableModel) frontEnd.onlineJTable.getModel();
                    // flush online list
                    tableModel.setRowCount(0);
                    // update online list
                    String[] onlineArray = messageContent.split(",");
                    // add current online user
                    for(String online: onlineArray) {
                        String[] stringArray = new String[2];
//                        if(online.equals(Util.getLocalHostAddress() + ":" + socket.getLocalPort())) {
//                            continue;
//                        }
                        int colonIndex = online.indexOf(":");
                        stringArray[0] = online.substring(0, colonIndex);
                        stringArray[1] = online.substring(colonIndex + 1);
                        tableModel.addRow(stringArray);
                    }
                    // get model of online list
                    DefaultTableCellRenderer tableCellRenderer = new DefaultTableCellRenderer();
                    // table data display by centered
                    tableCellRenderer.setHorizontalAlignment(JLabel.CENTER);
                    frontEnd.onlineJTable.setDefaultRenderer(Object.class, tableCellRenderer);
                }


            }
        } catch (Exception e) {
            frontEnd.messageJTextArea.append("server error");
            e.printStackTrace();
        }
    }
}
