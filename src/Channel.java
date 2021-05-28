import java.io.*;
import java.net.Socket;
import java.util.*;

/**
 * 用户类
 * @author DearAhri520
 */
public class Channel implements Runnable, Serializable {
    private static int theFirstAccountNumber=10000;             //初始账号 每一位用户注册都将该账号分配给用户 并使其自增;
    private ObjectOutputStream objectOutputStream=null;         //输出流
    private ObjectInputStream objectInputStream=null;           //输入流
    private Socket socket;                                      //socket
    private String name_String=null;                            //用户名
    private String accountNumber=null;                          //账号
    private String password=null;                               //密码
    private Boolean isRunning;                                  //客户端是否正在运行
    private Boolean isLogin=false;                              //客户端是否登录

    HashSet<Channel> myFriendList=new HashSet<>();              //所有的好友
    HashSet<ChatGroup> myGroupList=new HashSet<>();             //所有的群聊
    private HashSet<Message> waitingToDo=new HashSet<>();       //等待处理的消息
    private HashSet<Message> notLoginMessage =new HashSet<>();    //未上线时接收的消息

    String getName() {
        return name_String;
    }

    String getAccountNumber() {
        return accountNumber;
    }

    boolean getIsRunning(){
        return isRunning;
    }

    HashSet<ChatGroup> getMyGroupList(){
        return myGroupList;
    }

    Channel(Socket socket){
        /*获取socket*/
        this.socket=socket;
        /*将用户标志为正在运行*/
        isRunning=true;
        try {
            /*输出流*/
            objectInputStream=new ObjectInputStream(socket.getInputStream());
            /*输入流*/
            objectOutputStream=new ObjectOutputStream(socket.getOutputStream());
        } catch (Exception e) {
            System.out.println("Error:  Channel Channel(constructor)");
            release();
        }
    }

    @Override
    public void run(){
        loginOrRegister();                        //启动登录和注册界面
        System.out.println("Yes_4");
        notLoginMessage();                         //获取未登录时接收到的信息
        for(ChatGroup chatGroup:myGroupList){     //更新群聊信息
            updateGroupInformation(chatGroup);
        }
        menu();
    }

    /**
     * 服务端的receive方法，从对应客户端接收消息
     * @return 返回接收的消息，如果用户不运行，则返回一个消息内容为空的消息
     */
    Message receive(){
        if (isRunning){
            Message messageFromClient=new Message("");
            try {
                messageFromClient = (Message) objectInputStream.readObject();
            }catch (IOException | ClassNotFoundException e) {
                System.out.println("Error:  Channel(class) receive(method)");
                release();
            }
            return messageFromClient;
        }
        return new Message("");
    }

    /**
     * 服务端的send方法，向对应的客户端发送消息
     * @param message 向客户端发送的消息
     */
    void send(Message message){                //如果用户正在运行且用户已经登陆，此方法才被执行
        if(isLogin||message.isLoginOrRegister){//如果已经登陆或者该消息在登陆或注册时发送
            if (isRunning){
                try{
                    if(message.isWaitingToDo){//如果需要发送的消息需要被处理，则不发送而是存入waitingToDo HashSet中
                        this.waitingToDo.add(message);
                    }else{
                        objectOutputStream.writeObject(message);
                        objectOutputStream.flush();
                    }
                } catch (IOException e) {
                    System.out.println("Error:  Channel(class) send(method)");
                    release();
                }
            }
        }else{                      //如果用户未登录
            Server.getAllChannel().remove(this);
            if(message.isWaitingToDo){
                this.waitingToDo.add(message);
            }else{
                this.notLoginMessage.add(message);
            }
            Server.getAllChannel().add(this);
        }
    }

    /**
     * 启动登录|注册界面
     */
    private void loginOrRegister() {
        boolean isFind=false;
        System.out.println("Yes_3");
        this.send(new Message("请输入接下来的指令：",true));
        this.send(new Message("1：注册，2：登录",true));
        while (isRunning) {
            Message getMessageOnLoginOrRegister = this.receive();
            switch (getMessageOnLoginOrRegister.getMessage()){
                case "1":
                    this.Register();
                case "2":
                    isFind=true;
                    this.Login();
                    break;
                default:
                    this.send(new Message("输入的指令无效，请重新输入指令。",true));
            }
            if (isFind){
                break;
            }
        }
        this.send(new Message("登陆成功。",true));
    }

    /**
     * 用户登录
     */
    private void Register(){
        this.send(new Message("进入注册页面",true));   //提示用户进入注册界面
        /*第一次获取的密码*/
        Message firstPasswordFromClient;
        /*第二次获取的密码*/
        Message secondPasswordFromClient;

        while(isRunning) {
            this.send(new Message("请输入密码：",true));
            firstPasswordFromClient = this.receive();

            this.send(new Message("请再次输入密码：",true));
            secondPasswordFromClient = this.receive();

            if (secondPasswordFromClient.getMessage().equals(firstPasswordFromClient.getMessage())) {//输入的两次密码相等
                this.send(new Message("密码设置成功。",true));
                this.password=firstPasswordFromClient.getMessage();
                break;
            }
            this.send(new Message("两次输入的密码不正确，请重新输入。",true));
        }

        this.send(new Message("请输入您的用户名：",true));
        this.name_String=this.receive().getMessage();                    //获取用户的用户名
        this.send(new Message("您获得的账号为："+theFirstAccountNumber,true));    //发送用户的账号
        this.accountNumber=theFirstAccountNumber+"";
        this.send(new Message("您已成功注册，接下来将自动跳转至登录界面。",true));
        Server.getAllChannel().add(this);                           //该用户加入已注册的用户array中
        theFirstAccountNumber++;                                    //分配用户的账号自增，保证所有用户的账号不同
    }

    /**
     * 用户登录
     */
    private void Login(){
        this.send(new Message("进入登陆页面",true));//提示用户进入登陆界面
        boolean isHaveLogin=false;           //记录用户是否重复登陆
        boolean isChannelFind=false;         //记录用户输入的账号与密码是否正确
        String getAccountNumber;             //记录用户输入的账号
        String getPassword;                  //记录用户输入的密码
        while(isRunning) {
            this.send(new Message("请输入账号",true));
            getAccountNumber=this.receive().getMessage();                 //从客户端获取账号

            this.send(new Message("请输入密码",true));
            getPassword=this.receive().getMessage();                      //从客户端获取密码
            Iterator<Channel> iterator=Server.getAllChannel().iterator();
            while(iterator.hasNext()){
                Channel channel=iterator.next();
                if(channel.accountNumber.equals(getAccountNumber)){       //如果从服务端的数据中发现了相匹配的账号
                    if(channel.isLogin){
                        isHaveLogin=true;                                 //如果该用户已登录，则标记
                    }else{                                                //如果该用户未登录
                        if(channel.password.equals(getPassword)) {        //如果密码符合
                            this.myGroupList=channel.myGroupList;         //获取群聊列表
                            this.myFriendList=channel.myFriendList;       //获取好友列表
                            this.notLoginMessage =channel.notLoginMessage;   //获取未登录时得到的消息
                            this.accountNumber=channel.accountNumber;
                            this.name_String=channel.name_String;
                            iterator.remove();                            //从数据库中删除该元素
                            isChannelFind=true;                           //记录用户输入的账号与密码正确
                        }
                    }
                    break;
                }
            }

            if(isHaveLogin){                                              //如果用户重复登录，则令其再此输入账号和密码
                this.send(new Message("你已经登录，请勿重复登陆",true));
                continue;
            }

            if(isChannelFind){                                            //如果用户输入的账号与密码正确
                this.isLogin=true;                                        //将该用户的是否登录改为true
                Server.getAllChannel().add(this);                         //数据库中重新添加该元素
                break;                                                    //跳出登录程序
            }else{
                this.send(new Message("你输入的账号或密码不正确，请重新输入",true));
            }
        }
    }

    //输出未登录时接收到的消息
    private void notLoginMessage(){
        if (this.notLoginMessage.isEmpty()){
            this.send(new Message("在你离线期间没有接收到任何消息"));
            return;
        }
        for (Message message: notLoginMessage){
            this.send(message);
        }
    }

    //菜单界面，登录之后才能执行此界面的功能
    private void menu(){
        this.send(new Message("--------菜单--------"));
        this.send(new Message("1. 好友菜单"));
        this.send(new Message("2. 群聊菜单"));
        this.send(new Message("3. 待处理的信息"));
        this.send(new Message("4. 退出登录"));

        while(isRunning){
            this.send(new Message("请输入指令"));
            Message messageOfMenu=this.receive();
            switch (messageOfMenu.getMessage()){//选择指令
                case "1": this.friendMenu();break;
                case "2": this.groupMenu();break;
                case "3": this.seeWaitingToDo();break;
                case "4": this.release();break;
                default:
                    this.send(new Message("指令错误"));
            }
            if ("4".equals(messageOfMenu.getMessage())){
                return;
            }
        }
    }

    /*好友菜单*/
    private void friendMenu() {
        Message messageOfFriendMenu;
        this.send(new Message("请输入指令"));
        this.send(new Message("--------好友菜单--------"));
        this.send(new Message("1. 查看好友列表"));
        this.send(new Message("2. 添加好友"));
        this.send(new Message("3. 删除好友"));
        this.send(new Message("4. 好友聊天"));
        this.send(new Message("输入@Exit即可返回菜单"));
        while(isRunning){
            messageOfFriendMenu=this.receive();
            if ("@Exit".equals(messageOfFriendMenu.getMessage())){
                this.send(new Message("已返回菜单"));
                return;
            }
            switch (messageOfFriendMenu.getMessage()){//选择指令
                case "1": SeeFriendList();break;
                case "2": addFriend();break;
                case "3": deleteFriend();break;
                case "4": chatWithFriends();break;
                default:
                    this.send(new Message("指令错误,请重新输入"));
            }
        }
    }

    /*群聊菜单*/
    private void groupMenu(){
            Message messageOfGroupMenu;
            this.send(new Message("请输入指令"));
            this.send(new Message("--------群聊菜单--------"));
            this.send(new Message("1. 查看群聊列表"));
            this.send(new Message("2. 创建群聊"));
            this.send(new Message("3. 加入群聊"));
            this.send(new Message("4. 退出群聊"));
            this.send(new Message("5. 邀请群员"));
            this.send(new Message("6. 群聊聊天"));
            this.send(new Message("输入@Exit即可返回菜单"));
            while(isRunning){
                messageOfGroupMenu=this.receive();
                if ("@Exit".equals(messageOfGroupMenu.getMessage())){
                    this.send(new Message("已返回菜单"));
                    return;
                }
                switch (messageOfGroupMenu.getMessage()){//选择指令
                    case "1": SeeGroupList();break;
                    case "2": ChatGroup.createGroup(this);break;
                    case "3": ChatGroup.joinGroup(this);break;
                    case "4": ChatGroup.exitGroup(this);break;
                    case "5": ChatGroup.inviteGroupMember(this);break;
                    case "6": ChatGroup.chatWithGroup(this);break;
                default:
                    this.send(new Message("指令错误,请重新输入"));
            }
        }
    }

    /*发送好友列表*/
    void SeeFriendList(){
        if (myFriendList.size()==0){
            this.send(new Message("您还没有添加好友，快去添加好友吧"));
            return;
        }
        this.send(new Message("您的好友有："));
        for (Channel c:myFriendList){
            this.send(new Message(c.name_String+"  ("+c.accountNumber+")"));
        }
    }

    /*与好友聊天*/
    private void chatWithFriends(){
        boolean isFind=false;
        String chatWithFriend;
        String sendToFriend;
        Channel friend=null;
        SeeFriendList();

        while (isRunning) {
            this.send(new Message("请输入你想要聊天的好友账号"));
            chatWithFriend = this.receive().getMessage();
            if ("@Exit".equals(chatWithFriend)){
                this.send(new Message("已返回好友菜单"));
                return;
            }

            for (Channel c : myFriendList) {                            //在好友列表中找到了该好友
                if (c.getAccountNumber().equals(chatWithFriend)) {
                    isFind = true;
                    break;
                }
            }

            if (!isFind) {                                              //如果好友列表中未找到该好友
                this.send(new Message("未找到该好友，请重新输入好友账号或输入@Exit返回好友菜单"));
                continue;
            }else{                                                      //如果好友列表中找到该好友
                for(Channel channel:Server.getAllChannel()){            //从数据库中获取该好友的信息
                    if(channel.accountNumber.equals(chatWithFriend)){
                        friend=channel;                                 //将该好友的信息复制给friend变量
                    }
                }
            }

            this.send(new Message("请输入你要发送的内容或输入@Exit返回好友菜单"));
            while (isRunning){
                sendToFriend=this.receive().getMessage();
                if ("@Exit".equals(sendToFriend)){
                    this.send(new Message("已返回好友菜单"));
                    return;
                }
                if(friend!=null){
                    friend.send(new Message(this.name_String+"对你发送："+sendToFriend));
                }else{
                    System.out.println("class: Channel   method: send");
                }
            }
        }
    }

    //添加好友
    private void addFriend(){
        boolean isFind=false;
        Channel theFindFriend = null;

        this.send(new Message("请输入你想添加的好友账号或输入@Exit返回好友菜单"));
        String wantToAddFriend=this.receive().getMessage();
        if ("@Exit".equals(wantToAddFriend)){
            this.send(new Message("返回好友菜单"));
            return;
        }

        if (wantToAddFriend.equals(this.accountNumber)){
            this.send(new Message("你无法添加自己为好友"));
            this.send(new Message("已返回好友菜单"));
            return;
        }

        for (Channel c:this.myFriendList){
            if (c.getAccountNumber().equals(wantToAddFriend)){
                this.send(new Message("你已添加过该好友"));
                this.send(new Message("已返回好友菜单"));
                return;
            }
        }

        for (Channel c:Server.getAllChannel()){
            if (c.accountNumber.equals(wantToAddFriend)){
                this.send(new Message("该用户信息: 用户名"+c.name_String+" （"+c.accountNumber+"）"));
                isFind=true;
                theFindFriend=c;
                break;
            }
        }
        if(!isFind){
            this.send(new Message("未查询到该好友"));
            this.send(new Message("已返回好友菜单"));
            return;
        }

        this.send(new Message("是否发送申请添加该好友，输入1确认，输入2取消"));
        String getNumber=this.receive().getMessage();

        if ("1".equals(getNumber)){
            theFindFriend.send(new Message("系统消息： 用户"+this.name_String+"("+this.accountNumber+")"+" 申请添加你为好友 "));//向群主发送一条系统消息
            theFindFriend.send(new Message("系统消息： 用户"+this.name_String+"("+this.accountNumber+")"+"申请添加你为好友，输入1同意，输入2拒绝",true,true,this));
            //向群主发送一条isSystem为true的消息，如果表示为true，则会进入等待处理的list

            this.send(new Message("已发送申请，等待对方处理"));
        }else if ("2".equals(getNumber)){
            this.send(new Message("你已拒绝申请添加该好友"));
        }else {
            this.send(new Message("指令错误"));
        }
        this.send(new Message("已返回好友菜单"));
    }

    //删除好友
    private void deleteFriend(){
        SeeFriendList();
        boolean isFind=false;
        Channel deleteFriend=null;

        this.send(new Message("请输入你想删除的好友的账号或输入@Exit返回好友菜单"));
        String getDeleteFriend=this.receive().getMessage();
        if ("@Exit".equals(getDeleteFriend)){
            this.send(new Message("返回好友菜单"));
            return;
        }

        for (Channel c:myFriendList){
            if (c.accountNumber.equals(getDeleteFriend)){
                deleteFriend=c;
                isFind=true;
                break;
            }
        }

        if (!isFind){
            this.send(new Message("未找到该好友"));
        }else{
            this.send(new Message(("是否删除该好友"+"\n"+"1：删除好友，2：不删除好友")));
            String yesOrNo=this.receive().getMessage();
            if ("1".equals(yesOrNo)){
                this.myFriendList.remove(deleteFriend);
                this.send(new Message("你已删除该好友"));
                deleteFriend.send(new Message(this.name_String+"已将你删除"));
                deleteFriend.myFriendList.remove(this);
            }else if ("2".equals(yesOrNo)){
                this.send(new Message("删除好友操作已取消"));
            }else {
                this.send(new Message("指令错误"));
            }
        }
        this.send(new Message("已返回好友菜单"));
    }

    /*发送群聊列表*/
    void SeeGroupList(){
        if (myGroupList.size()==0){
            this.send(new Message("您还没有加入群聊，快去添加群聊吧"));
            return;
        }
        this.send(new Message("您加入的群聊有："));
        for (ChatGroup c:myGroupList){
            this.send(new Message(c.getName()+"  ("+c.getGroupNumber()+")"));
        }
    }

    /*更新单个群聊中该用户的信息*/
    private void updateGroupInformation(ChatGroup chatGroup){
        Server.getAllGroup().removeIf(chatGroup1 -> chatGroup1.equals(chatGroup));
        chatGroup.getGroupAllChannel().add(this);
        Server.getAllGroup().add(chatGroup);
    }

    /*查看等待处理的消息*/
    private void seeWaitingToDo(){
        int count=1;

        if (waitingToDo.size()==0){
            this.send(new Message("您没有等待处理的消息"));
            this.send(new Message("已返回菜单"));
            return;
        }

        Iterator<Message> iterator=waitingToDo.iterator();
        this.send(new Message("您共有 "+waitingToDo.size()+" 条消息需要处理"));
        while (iterator.hasNext()) {
            Message m=iterator.next();
            this.send(new Message("您收到的第 " + count++ + " 条消息：" + m.getMessage()));
            this.send(new Message("请处理该消息或输入@Exit已返回菜单"));
            String getMessage=this.receive().getMessage();
            if ("@Exit".equals(getMessage)){
                this.send(new Message("已返回菜单"));
                return;
            }

            if (( !"1".equals(getMessage) && !"2".equals(getMessage))){
                this.send(new Message("指令错误，已返回菜单"));
                return;
            }

            if("1".equals(getMessage)){
                if (m.isGroup){
                    for (ChatGroup chatGroup:Server.getAllGroup()){
                        if (chatGroup.getName().equals(m.theChatGroup.getName())){
                            chatGroup.getGroupAllChannel().add(m.sendFrom);
                        }
                    }

                    m.sendFrom.myGroupList.add(m.theChatGroup);
                    this.send(new Message("你已同意"+m.sendFrom.name_String+"加入群聊"));
                    m.sendFrom.send(new Message(m.theChatGroup.getName()+"的群主同意了你的申请，一起来愉快的聊天吧"));
                }

                if(m.isFriend){
                    Server.getAllChannel().remove(this);
                    this.myFriendList.add(m.sendFrom);
                    Server.getAllChannel().add(this);
                    Server.getAllChannel().remove(m.sendFrom);
                    m.sendFrom.myFriendList.add(this);
                    Server.getAllChannel().add(m.sendFrom);
                    this.send(new Message("你已同意"+m.sendFrom.name_String+"添加你为好友"));
                    m.sendFrom.send(new Message(this.name_String+"同意了你的申请，一起来愉快的聊天吧"));
                }

            }else {
                if (m.isGroup){
                    this.send(new Message("你已拒绝"+m.sendFrom.name_String+"加入群聊"));
                    m.sendFrom.send(new Message(m.theChatGroup+"的群主拒绝了你的申请"));
                }

                if(m.isFriend){
                    this.send(new Message("你已拒绝"+m.sendFrom.name_String+"添加你为好友"));
                    m.sendFrom.send(new Message(this.name_String+"拒绝了你的申请"));
                }
            }
            iterator.remove();
        }
        this.send(new Message("已返回菜单"));
    }
    
    //释放资源
    private void release(){
        Server.getAllChannel().remove(this);
        this.isLogin=false;
        this.isRunning=false;                                           //停止运行
        Server.getAllChannel().add(this);
        System.out.println(this.socket.getInetAddress()+"下线了");      //输出下线的用户
        Close.close(objectInputStream,objectOutputStream,socket);      //关闭资源
        System.out.println("该用户的资源已经释放");
    }

    @Override
    public boolean equals(Object o) {//比较账号
        if (this == o) {return true;}
        if (o == null || getClass() != o.getClass()) {return false;}
        Channel channel = (Channel) o;
        return Objects.equals(accountNumber, channel.accountNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountNumber);
    }
}