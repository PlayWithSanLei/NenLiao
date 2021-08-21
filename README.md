# NenLiao嫩聊
**项目使用Java编写,JDK版本为16**

[TOC]

## socket通信原理

首先从TCP/IP说起

TCP/IP协议是**互联网协议（簇）**的统称，对网络通信制定了一系列相应的规则，是通信的基础，它**提供点对点的链接机制，将数据应该如何封装、定址、传输、路由以及在目的地如何接收，都加以标准化**。

socket是在应用层和传输层之间的一个抽象层,socket本质上就是一个文件,建立连接的两端对socket文件进行读写操作,建立连接就是打开socket文件,断开连接就是关闭文件,原理比较简单

在实现上,服务端会监听一个端口,然后进入accept阻塞状态等待客户端连接

客户端会向服务端监听的端口发起请求,这时候建立连接,双方获取socket输入输出流,对socket文件进行读写操作,以此实现socket通信.

> 1. 服务器端先初始化Socket
> 2. 绑定端口
> 3. 对端口监听
> 4. 调用accept阻塞，等待客户端来连接
> 5. 客户端连接服务器(connect),服务器的accept 相当于和客户端的connect 一起完成了TCP的三次握手
> 6. 客户端发送发送数据请求
> 7. 服务器接收请求并处理，然后回应数据给客户端
> 8. 客户端读取到的数据，最后关闭连接
>
> 具体的流程就是这样

## 工具类
### Configuration
> + PORT 通信端口, 即服务端监听的端口号
> + CLIENT_FRAME_WIDTH 客户端窗口的宽
> + CLIENT_FRAME_HEIGHT 客户端窗口的高
> + SEPARATOR 分隔符 /  协议规定
> + TYPE_UPDATE_ONLINE_LIST 在线用户列表
> + TYPE_CHAT 消息类型 聊天
> + TYPE_EXIT 消息类型 退出
> + NEWLINE 新的一行

### Util
> + getCurrentTime()
> + 获取当前时间
> + getScreenWidth()
> + 获取屏幕宽度
> + getScreenHeight()
> + 获取屏幕高度
> + getLocalHostAddress()
> + 获取本机的IP地址
>  + `InetAddress.getLocalHost()`获取当前hostname以及ip地址,比如笔者的是`SanLei-Computer/192.168.3.7`这种形式, 我们只需要知道ip地址即可,因此在结果中调用`getHostAddress()`方法,最终得到结果`192.168.3.7`.

## 服务端

#### HandleMessageRunnable类

该类实现了接口Runnable, 并且重写run()方法
> In most cases, the Runnable interface should be used if you are only planning to override the run() method and no other Thread methods. This is important because classes should not be subclassed unless the programmer intends on modifying or enhancing the fundamental behavior of the class.

**成员**
+ clientUid: 客户端的UID,由ip以及端口组成
+ clientUidArrayList: 存储UID的ArrayList
+ hashMap: 存储以uid为key, handleMessageRunnable为value的键值对
+ socket: 在网络编程中，网络上的两个程序通过一个双向的通信连接实现数据的交换，这个连接的一端称为一个socket. Socket套接字是通信的基石，是支持TCP/IP协议的网络通信的基本操作单元。它是网络通信过程中端点的抽象表示，包含进行网络通信必须的五种信息：连接使用的协议，本地主机的IP地址，本地进程的协议端口，远地主机的IP地址，远地进程的协议端口。
  + > 对于一个功能齐全的Socket，都要包含以下基本结构，其工作过程包含以下四个基本的步骤:
    > + 创建Socket
    > + 打开连接到Socket的输入/出流
    > + 按照一定的协议对Socket进行读/写操作
    > + 关闭Socket。
+ ip: 客户端IP
+ port: 客户端端口号

##### 有参构造器HandleMessageRunnable
除了直接赋值socket, IP, port之外, 还组合了一个clientUID
##### 重写run()
> 包含了:
> + addClient(): 添加客户端
> + sendConnectedMessage(): 发送连接消息
> + updateOnlineList(): 更新在线列表
> + handleMessage(): 处理消息
> + socketInputStream: socket输入流
> + socketOutputStream: socket输出流
这四个方法会在下文详解

##### addClient() 添加客户端
直接给clientUidArrayList中添加客户端的UID即可,同时将uid和socket组成的键值对存入map中
##### removeClient() 移除客户端
将clientUidArrayList和hashMap中的clientUid移除即可,即视为客户端被移除
##### sendConnectedMessage() 发送连接成功消息
通过socketOutputStream输出流在socket写入成功消息, 注意写入的是字节,不是直接写入字符
##### handleMessage()
这个函数的主要作用是监听并且给客户端转发消息,持续进行

服务端一直在监听端口,直到客户端给发送了消息,然后通过分割符来判断消息类型,这里只有两种情况,如果是退出信息,直接移除客户端;如果是聊天信息,则获取客户端的UID,因为群聊,所以可能会有一个客户端发送给多个客户端的情况,使用`,`分割,将其获取存入数组,这个数组就是要接收消息的客户端的uid,其实就是ip+port,用于在网络上定位一个主机

剩下的就是消息内容了,将消息内容交给`sendChatMessage()`函数进行处理,下文会有解释

##### updateOnlineList()

**更新用户列表**

构建一个StringBuilder,加入刚才获取的客户端uid,给这些客户端更新用户列表,做法是:

> 1. 收集客户端uid,并且加入数组
> 2. 遍历数组,从map中获取这些uid对应的socket
> 3. 在这些socket中写入刚才构建好的用户列表

##### sendChatMessage()

**给客户端发送消息**

遍历前面获取的客户端UID数组,并且从map中通过UID获取客户端的socket写入消息,消息由这些部分组成:

> TYPE_CHAT + SEPARATOR + UID + SEPARATOR + content

#### server类

监听端口,打印提示信息,服务端已启动,建通端口XXX

然后进入持续监听状态,使用`accept()`方法接受客户端连接,当三次握手连接成功后,获取客户端的ip和端口,然后交给HandleMessageRunnable处理,创建一个新的线程,开始处理这个客户端的请求,继续等待其他客户端连接

### 客户端

#### GUI

界面最终效果是这样的,根据这个来进行设计

![image-20210821190648713](C:\Users\10052\AppData\Roaming\Typora\typora-user-images\image-20210821190648713.png)

GUI的重点在于监听器事件

##### initMessageJTextArea() 初始化信息框

除了设置基本的宽高位置之外,还需要加一个scrollbar,实现消息过多以后回看之前的消息的需求

##### initOnlineJTable() 初始化在线列表

在线列表首先是不可编辑的列表,其次需要给table中的row添加监听器事件,从而可以选择性的发送消息

> 添加鼠标事件,点击选择的对象获取其中的内容,这个内容就是UID
>
> 依次遍历被选中的对象,将其存入全局StringBuilder中,等待后续处理

最后在用户在线列表中加入scrollbar

##### initTipLabel() 初始化提示行

这个提示行用来展示选中的用户,在线列表中选中的用户存入的StringBuilder变量,然后显示在这里即可实现这个需求

##### initSendJTextArea() 初始化发送消息区域

设置大小,位置,没有别的难点

##### initSendButton() 初始化发送按钮

初始化一个发送按钮不仅意味着绘制一个按钮,而且还要加上监听事件

点击发送按钮之后,向socket写入相关信息

> TYPE_CHAT/接收方/当前时间 + 换行 + 发往 + 接收方(可以是一个列表) + 换行

##### initCleanButton() 初始化清屏按钮

监听器事件为将messageJTextArea置为空

##### initExitButton() 初始化退出按钮

监听器事件是向服务端发送一条退出消息

> TYPE_EXIT/

#### Client

##### main()

主函数中初始化一个GUI界面,然后客户端连接服务端,之后就可以正常进行接收消息

##### initFrontEnd() 初始化GUI界面

获取当前屏幕宽高,以此来设置界面居中位置

##### connectServer() 连接服务

+ 客户端实例化新的socket,向服务器发起请求

+ 同时将GUI中socket初始化为当前socket
+ 获取输入流输出流,完成连接

##### handleReceiveMessage() 处理接收到的信息

+ 在和服务端建立连接以后,读socket中的信息

+ 当客户端收到消息后,对消息进行解析

  + > TYPE_CHAT/UID/123
    >
    > UPDATE_ONLINELIST/UID

+ 按照这种规则进行解析以后,提取出message,添加到JTextArea中

+ 如果是更新用户列表,则直接提取出UID,添加到GUI的onlineTable中即可.

