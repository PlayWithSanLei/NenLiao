package org.sanlei.nenliao.client;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.OutputStream;
import java.net.Socket;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;
import org.sanlei.nenliao.utils.Configuration;
import org.sanlei.nenliao.utils.Util;
public class FrontEnd extends JFrame {
    private StringBuilder receiverStringBuilder;
    private Socket socket;
    private JButton sendButton;
    private JButton cleanButton;
    private JButton exitButton;
    private JLabel tipLabel;
    // 聊天消息框
    public JTextArea messageJTextArea;
    // 聊天消息框的滚动窗
    private JScrollPane messageJScrollPane;
    // 聊天文本输入框
    private JTextArea sendJTextArea;
    // 在线列表
    public JTable onlineJTable;
    // 在线列表的滚动窗
    private JScrollPane onlineJTableJScrollPane;

    public FrontEnd() {
        initFrame();
        initComponents();
    }

    private void initFrame() {
        // 标题
        setTitle("嫩聊");
        // 大小
        setSize(Configuration.CLIENT_FRAME_WIDTH, Configuration.CLIENT_FRAME_HEIGHT);
        // 设置布局:不使用默认布局，完全自定义
        setLayout(null);
    }

    private void initComponents() {
        initSendButton();
        initCleanButton();
        initExitButton();
        initTipLabel();
        initSendJTextArea();
        initMessageJTextArea();
        initOnlineJTable();
    }

    //发送消息
    private void initSendButton() {
        sendButton = new JButton("发送");
        sendButton.setBounds(20, 600, 100, 60);
        sendButton.setFont(new Font("宋体", Font.BOLD, 18));
        // 添加发送按钮的响应事件
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                // 将JTextArea的滚动条拉至其底部
                messageJTextArea.setCaretPosition(messageJTextArea.getDocument().getLength());
                try {
                    String receivers = receiverStringBuilder.toString();
                    String message = sendJTextArea.getText();
                    if (receivers != null && receivers.length() > 0) {
                        // 在聊天窗显示与发送相关的信息
                        messageJTextArea.append(Util.getCurrentTime() + Configuration.NEWLINE + "发往 " + receivers+ Configuration.NEWLINE);
                        // 在聊天窗显示发送消息
                        messageJTextArea.append(message + Configuration.NEWLINE);
                        // 向服务器发送聊天信息
                        OutputStream out = socket.getOutputStream();
                        out.write((Configuration.TYPE_CHAT + Configuration.SEPARATOR + receivers + Configuration.SEPARATOR+ message).getBytes());
                    }
                } catch (Exception e) {

                } finally {
                    // 清空文本输入框
                    sendJTextArea.setText("");
                }
            }
        });
        this.add(sendButton);
    }


    private void initCleanButton() {
        cleanButton = new JButton("清屏");
        cleanButton.setBounds(140, 600, 100, 60);
        cleanButton.setFont(new Font("宋体", Font.BOLD, 18));
        // 添加清屏按钮的响应事件
        cleanButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                messageJTextArea.setText("");
            }
        });
        this.add(cleanButton);
    }

    private void initExitButton() {
        exitButton = new JButton("退出");
        exitButton.setBounds(260, 600, 100, 60);
        exitButton.setFont(new Font("宋体", Font.BOLD, 18));
        // 添加退出按钮的响应事件
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                try {
                    // 向服务器发送退出信息
                    OutputStream out = socket.getOutputStream();
                    out.write((Configuration.TYPE_EXIT + Configuration.SEPARATOR).getBytes());
                    System.exit(0);
                } catch (Exception e) {
                }
            }
        });
        this.add(exitButton);
    }

    private void initTipLabel() {
        tipLabel = new JLabel("亲，您想和谁聊天呢？");
        tipLabel.setBounds(20, 420, 300, 30);
        this.add(tipLabel);
    }

    private void initSendJTextArea() {
        sendJTextArea = new JTextArea();
        sendJTextArea.setBounds(20, 460, 360, 120);
        sendJTextArea.setFont(new Font("宋体", Font.BOLD, 16));
        this.add(sendJTextArea);
    }

    private void initMessageJTextArea() {
        messageJTextArea = new JTextArea();
        // 聊天消息框自动换行
        messageJTextArea.setLineWrap(true);
        // 聊天框不可编辑，只用来显示
        messageJTextArea.setEditable(false);
        // 设置聊天框字体
        messageJTextArea.setFont(new Font("楷体", Font.BOLD, 16));
        messageJScrollPane = new JScrollPane(messageJTextArea);
        // 设置滚动窗的水平滚动条属性:不出现
        messageJScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        // 设置滚动窗的垂直滚动条属性:需要时自动出现
        messageJScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        // 设置滚动窗大小和位置
        messageJScrollPane.setBounds(20, 20, 360, 400);
        // 添加聊天窗口的滚动窗
        this.add(messageJScrollPane);
    }

    private void initOnlineJTable() {
        // 当前在线用户列表的列标题
        String[] colTitles = { "IP", "端口" };
        onlineJTable = new JTable(new DefaultTableModel(null, colTitles) {
            // 设置表格不可编辑
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        // 添加在线列表项被鼠标选中的相应事件
        onlineJTable.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent event) {
                // 取得在线列表的数据模型
                DefaultTableModel tableModel = (DefaultTableModel) onlineJTable.getModel();
                // 提取鼠标选中的行作为消息目标(最少一个人，最多为全体在线者)
                int[] selectedIndex = onlineJTable.getSelectedRows();
                // 将所有消息目标的uid拼接成一个字符串,以逗号分隔
                receiverStringBuilder = new StringBuilder("");
                for (int i = 0; i < selectedIndex.length; i++) {
                    String ip = (String) tableModel.getValueAt(selectedIndex[i], 0);
                    String port = (String) tableModel.getValueAt(selectedIndex[i], 1);
                    receiverStringBuilder.append(ip);
                    receiverStringBuilder.append(":");
                    receiverStringBuilder.append(port);
                    if (i != selectedIndex.length - 1) {
                        receiverStringBuilder.append(",");
                    }
                }
                tipLabel.setText("消息发送至：" + receiverStringBuilder.toString());
            }

            @Override
            public void mousePressed(MouseEvent event) {
            };

            @Override
            public void mouseReleased(MouseEvent event) {
            };

            @Override
            public void mouseEntered(MouseEvent event) {
            };

            @Override
            public void mouseExited(MouseEvent event) {
            };
        });
        onlineJTableJScrollPane = new JScrollPane(onlineJTable);
        // 设置滚动窗的水平滚动条属性:不出现
        onlineJTableJScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        // 设置滚动窗的垂直滚动条属性:需要时自动出现
        onlineJTableJScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        // 设置当前在线列表滚动窗大小和位置
        onlineJTableJScrollPane.setBounds(420, 20, 250, 400);
        this.add(onlineJTableJScrollPane);
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }
}
