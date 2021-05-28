import java.io.*;
import java.net.Socket;
import java.util.*;

/**
 * �û���
 * @author DearAhri520
 */
public class Channel implements Runnable, Serializable {
    private static int theFirstAccountNumber=10000;             //��ʼ�˺� ÿһλ�û�ע�ᶼ�����˺ŷ�����û� ��ʹ������;
    private ObjectOutputStream objectOutputStream=null;         //�����
    private ObjectInputStream objectInputStream=null;           //������
    private Socket socket;                                      //socket
    private String name_String=null;                            //�û���
    private String accountNumber=null;                          //�˺�
    private String password=null;                               //����
    private Boolean isRunning;                                  //�ͻ����Ƿ���������
    private Boolean isLogin=false;                              //�ͻ����Ƿ��¼

    HashSet<Channel> myFriendList=new HashSet<>();              //���еĺ���
    HashSet<ChatGroup> myGroupList=new HashSet<>();             //���е�Ⱥ��
    private HashSet<Message> waitingToDo=new HashSet<>();       //�ȴ��������Ϣ
    private HashSet<Message> notLoginMessage =new HashSet<>();    //δ����ʱ���յ���Ϣ

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
        /*��ȡsocket*/
        this.socket=socket;
        /*���û���־Ϊ��������*/
        isRunning=true;
        try {
            /*�����*/
            objectInputStream=new ObjectInputStream(socket.getInputStream());
            /*������*/
            objectOutputStream=new ObjectOutputStream(socket.getOutputStream());
        } catch (Exception e) {
            System.out.println("Error:  Channel Channel(constructor)");
            release();
        }
    }

    @Override
    public void run(){
        loginOrRegister();                        //������¼��ע�����
        System.out.println("Yes_4");
        notLoginMessage();                         //��ȡδ��¼ʱ���յ�����Ϣ
        for(ChatGroup chatGroup:myGroupList){     //����Ⱥ����Ϣ
            updateGroupInformation(chatGroup);
        }
        menu();
    }

    /**
     * ����˵�receive�������Ӷ�Ӧ�ͻ��˽�����Ϣ
     * @return ���ؽ��յ���Ϣ������û������У��򷵻�һ����Ϣ����Ϊ�յ���Ϣ
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
     * ����˵�send���������Ӧ�Ŀͻ��˷�����Ϣ
     * @param message ��ͻ��˷��͵���Ϣ
     */
    void send(Message message){                //����û������������û��Ѿ���½���˷����ű�ִ��
        if(isLogin||message.isLoginOrRegister){//����Ѿ���½���߸���Ϣ�ڵ�½��ע��ʱ����
            if (isRunning){
                try{
                    if(message.isWaitingToDo){//�����Ҫ���͵���Ϣ��Ҫ�������򲻷��Ͷ��Ǵ���waitingToDo HashSet��
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
        }else{                      //����û�δ��¼
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
     * ������¼|ע�����
     */
    private void loginOrRegister() {
        boolean isFind=false;
        System.out.println("Yes_3");
        this.send(new Message("�������������ָ�",true));
        this.send(new Message("1��ע�ᣬ2����¼",true));
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
                    this.send(new Message("�����ָ����Ч������������ָ�",true));
            }
            if (isFind){
                break;
            }
        }
        this.send(new Message("��½�ɹ���",true));
    }

    /**
     * �û���¼
     */
    private void Register(){
        this.send(new Message("����ע��ҳ��",true));   //��ʾ�û�����ע�����
        /*��һ�λ�ȡ������*/
        Message firstPasswordFromClient;
        /*�ڶ��λ�ȡ������*/
        Message secondPasswordFromClient;

        while(isRunning) {
            this.send(new Message("���������룺",true));
            firstPasswordFromClient = this.receive();

            this.send(new Message("���ٴ��������룺",true));
            secondPasswordFromClient = this.receive();

            if (secondPasswordFromClient.getMessage().equals(firstPasswordFromClient.getMessage())) {//����������������
                this.send(new Message("�������óɹ���",true));
                this.password=firstPasswordFromClient.getMessage();
                break;
            }
            this.send(new Message("������������벻��ȷ�����������롣",true));
        }

        this.send(new Message("�����������û�����",true));
        this.name_String=this.receive().getMessage();                    //��ȡ�û����û���
        this.send(new Message("����õ��˺�Ϊ��"+theFirstAccountNumber,true));    //�����û����˺�
        this.accountNumber=theFirstAccountNumber+"";
        this.send(new Message("���ѳɹ�ע�ᣬ���������Զ���ת����¼���档",true));
        Server.getAllChannel().add(this);                           //���û�������ע����û�array��
        theFirstAccountNumber++;                                    //�����û����˺���������֤�����û����˺Ų�ͬ
    }

    /**
     * �û���¼
     */
    private void Login(){
        this.send(new Message("�����½ҳ��",true));//��ʾ�û������½����
        boolean isHaveLogin=false;           //��¼�û��Ƿ��ظ���½
        boolean isChannelFind=false;         //��¼�û�������˺��������Ƿ���ȷ
        String getAccountNumber;             //��¼�û�������˺�
        String getPassword;                  //��¼�û����������
        while(isRunning) {
            this.send(new Message("�������˺�",true));
            getAccountNumber=this.receive().getMessage();                 //�ӿͻ��˻�ȡ�˺�

            this.send(new Message("����������",true));
            getPassword=this.receive().getMessage();                      //�ӿͻ��˻�ȡ����
            Iterator<Channel> iterator=Server.getAllChannel().iterator();
            while(iterator.hasNext()){
                Channel channel=iterator.next();
                if(channel.accountNumber.equals(getAccountNumber)){       //����ӷ���˵������з�������ƥ����˺�
                    if(channel.isLogin){
                        isHaveLogin=true;                                 //������û��ѵ�¼������
                    }else{                                                //������û�δ��¼
                        if(channel.password.equals(getPassword)) {        //����������
                            this.myGroupList=channel.myGroupList;         //��ȡȺ���б�
                            this.myFriendList=channel.myFriendList;       //��ȡ�����б�
                            this.notLoginMessage =channel.notLoginMessage;   //��ȡδ��¼ʱ�õ�����Ϣ
                            this.accountNumber=channel.accountNumber;
                            this.name_String=channel.name_String;
                            iterator.remove();                            //�����ݿ���ɾ����Ԫ��
                            isChannelFind=true;                           //��¼�û�������˺���������ȷ
                        }
                    }
                    break;
                }
            }

            if(isHaveLogin){                                              //����û��ظ���¼���������ٴ������˺ź�����
                this.send(new Message("���Ѿ���¼�������ظ���½",true));
                continue;
            }

            if(isChannelFind){                                            //����û�������˺���������ȷ
                this.isLogin=true;                                        //�����û����Ƿ��¼��Ϊtrue
                Server.getAllChannel().add(this);                         //���ݿ���������Ӹ�Ԫ��
                break;                                                    //������¼����
            }else{
                this.send(new Message("��������˺Ż����벻��ȷ������������",true));
            }
        }
    }

    //���δ��¼ʱ���յ�����Ϣ
    private void notLoginMessage(){
        if (this.notLoginMessage.isEmpty()){
            this.send(new Message("���������ڼ�û�н��յ��κ���Ϣ"));
            return;
        }
        for (Message message: notLoginMessage){
            this.send(message);
        }
    }

    //�˵����棬��¼֮�����ִ�д˽���Ĺ���
    private void menu(){
        this.send(new Message("--------�˵�--------"));
        this.send(new Message("1. ���Ѳ˵�"));
        this.send(new Message("2. Ⱥ�Ĳ˵�"));
        this.send(new Message("3. ���������Ϣ"));
        this.send(new Message("4. �˳���¼"));

        while(isRunning){
            this.send(new Message("������ָ��"));
            Message messageOfMenu=this.receive();
            switch (messageOfMenu.getMessage()){//ѡ��ָ��
                case "1": this.friendMenu();break;
                case "2": this.groupMenu();break;
                case "3": this.seeWaitingToDo();break;
                case "4": this.release();break;
                default:
                    this.send(new Message("ָ�����"));
            }
            if ("4".equals(messageOfMenu.getMessage())){
                return;
            }
        }
    }

    /*���Ѳ˵�*/
    private void friendMenu() {
        Message messageOfFriendMenu;
        this.send(new Message("������ָ��"));
        this.send(new Message("--------���Ѳ˵�--------"));
        this.send(new Message("1. �鿴�����б�"));
        this.send(new Message("2. ��Ӻ���"));
        this.send(new Message("3. ɾ������"));
        this.send(new Message("4. ��������"));
        this.send(new Message("����@Exit���ɷ��ز˵�"));
        while(isRunning){
            messageOfFriendMenu=this.receive();
            if ("@Exit".equals(messageOfFriendMenu.getMessage())){
                this.send(new Message("�ѷ��ز˵�"));
                return;
            }
            switch (messageOfFriendMenu.getMessage()){//ѡ��ָ��
                case "1": SeeFriendList();break;
                case "2": addFriend();break;
                case "3": deleteFriend();break;
                case "4": chatWithFriends();break;
                default:
                    this.send(new Message("ָ�����,����������"));
            }
        }
    }

    /*Ⱥ�Ĳ˵�*/
    private void groupMenu(){
            Message messageOfGroupMenu;
            this.send(new Message("������ָ��"));
            this.send(new Message("--------Ⱥ�Ĳ˵�--------"));
            this.send(new Message("1. �鿴Ⱥ���б�"));
            this.send(new Message("2. ����Ⱥ��"));
            this.send(new Message("3. ����Ⱥ��"));
            this.send(new Message("4. �˳�Ⱥ��"));
            this.send(new Message("5. ����ȺԱ"));
            this.send(new Message("6. Ⱥ������"));
            this.send(new Message("����@Exit���ɷ��ز˵�"));
            while(isRunning){
                messageOfGroupMenu=this.receive();
                if ("@Exit".equals(messageOfGroupMenu.getMessage())){
                    this.send(new Message("�ѷ��ز˵�"));
                    return;
                }
                switch (messageOfGroupMenu.getMessage()){//ѡ��ָ��
                    case "1": SeeGroupList();break;
                    case "2": ChatGroup.createGroup(this);break;
                    case "3": ChatGroup.joinGroup(this);break;
                    case "4": ChatGroup.exitGroup(this);break;
                    case "5": ChatGroup.inviteGroupMember(this);break;
                    case "6": ChatGroup.chatWithGroup(this);break;
                default:
                    this.send(new Message("ָ�����,����������"));
            }
        }
    }

    /*���ͺ����б�*/
    void SeeFriendList(){
        if (myFriendList.size()==0){
            this.send(new Message("����û����Ӻ��ѣ���ȥ��Ӻ��Ѱ�"));
            return;
        }
        this.send(new Message("���ĺ����У�"));
        for (Channel c:myFriendList){
            this.send(new Message(c.name_String+"  ("+c.accountNumber+")"));
        }
    }

    /*���������*/
    private void chatWithFriends(){
        boolean isFind=false;
        String chatWithFriend;
        String sendToFriend;
        Channel friend=null;
        SeeFriendList();

        while (isRunning) {
            this.send(new Message("����������Ҫ����ĺ����˺�"));
            chatWithFriend = this.receive().getMessage();
            if ("@Exit".equals(chatWithFriend)){
                this.send(new Message("�ѷ��غ��Ѳ˵�"));
                return;
            }

            for (Channel c : myFriendList) {                            //�ں����б����ҵ��˸ú���
                if (c.getAccountNumber().equals(chatWithFriend)) {
                    isFind = true;
                    break;
                }
            }

            if (!isFind) {                                              //��������б���δ�ҵ��ú���
                this.send(new Message("δ�ҵ��ú��ѣ���������������˺Ż�����@Exit���غ��Ѳ˵�"));
                continue;
            }else{                                                      //��������б����ҵ��ú���
                for(Channel channel:Server.getAllChannel()){            //�����ݿ��л�ȡ�ú��ѵ���Ϣ
                    if(channel.accountNumber.equals(chatWithFriend)){
                        friend=channel;                                 //���ú��ѵ���Ϣ���Ƹ�friend����
                    }
                }
            }

            this.send(new Message("��������Ҫ���͵����ݻ�����@Exit���غ��Ѳ˵�"));
            while (isRunning){
                sendToFriend=this.receive().getMessage();
                if ("@Exit".equals(sendToFriend)){
                    this.send(new Message("�ѷ��غ��Ѳ˵�"));
                    return;
                }
                if(friend!=null){
                    friend.send(new Message(this.name_String+"���㷢�ͣ�"+sendToFriend));
                }else{
                    System.out.println("class: Channel   method: send");
                }
            }
        }
    }

    //��Ӻ���
    private void addFriend(){
        boolean isFind=false;
        Channel theFindFriend = null;

        this.send(new Message("������������ӵĺ����˺Ż�����@Exit���غ��Ѳ˵�"));
        String wantToAddFriend=this.receive().getMessage();
        if ("@Exit".equals(wantToAddFriend)){
            this.send(new Message("���غ��Ѳ˵�"));
            return;
        }

        if (wantToAddFriend.equals(this.accountNumber)){
            this.send(new Message("���޷�����Լ�Ϊ����"));
            this.send(new Message("�ѷ��غ��Ѳ˵�"));
            return;
        }

        for (Channel c:this.myFriendList){
            if (c.getAccountNumber().equals(wantToAddFriend)){
                this.send(new Message("������ӹ��ú���"));
                this.send(new Message("�ѷ��غ��Ѳ˵�"));
                return;
            }
        }

        for (Channel c:Server.getAllChannel()){
            if (c.accountNumber.equals(wantToAddFriend)){
                this.send(new Message("���û���Ϣ: �û���"+c.name_String+" ��"+c.accountNumber+"��"));
                isFind=true;
                theFindFriend=c;
                break;
            }
        }
        if(!isFind){
            this.send(new Message("δ��ѯ���ú���"));
            this.send(new Message("�ѷ��غ��Ѳ˵�"));
            return;
        }

        this.send(new Message("�Ƿ���������Ӹú��ѣ�����1ȷ�ϣ�����2ȡ��"));
        String getNumber=this.receive().getMessage();

        if ("1".equals(getNumber)){
            theFindFriend.send(new Message("ϵͳ��Ϣ�� �û�"+this.name_String+"("+this.accountNumber+")"+" ���������Ϊ���� "));//��Ⱥ������һ��ϵͳ��Ϣ
            theFindFriend.send(new Message("ϵͳ��Ϣ�� �û�"+this.name_String+"("+this.accountNumber+")"+"���������Ϊ���ѣ�����1ͬ�⣬����2�ܾ�",true,true,this));
            //��Ⱥ������һ��isSystemΪtrue����Ϣ�������ʾΪtrue��������ȴ������list

            this.send(new Message("�ѷ������룬�ȴ��Է�����"));
        }else if ("2".equals(getNumber)){
            this.send(new Message("���Ѿܾ�������Ӹú���"));
        }else {
            this.send(new Message("ָ�����"));
        }
        this.send(new Message("�ѷ��غ��Ѳ˵�"));
    }

    //ɾ������
    private void deleteFriend(){
        SeeFriendList();
        boolean isFind=false;
        Channel deleteFriend=null;

        this.send(new Message("����������ɾ���ĺ��ѵ��˺Ż�����@Exit���غ��Ѳ˵�"));
        String getDeleteFriend=this.receive().getMessage();
        if ("@Exit".equals(getDeleteFriend)){
            this.send(new Message("���غ��Ѳ˵�"));
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
            this.send(new Message("δ�ҵ��ú���"));
        }else{
            this.send(new Message(("�Ƿ�ɾ���ú���"+"\n"+"1��ɾ�����ѣ�2����ɾ������")));
            String yesOrNo=this.receive().getMessage();
            if ("1".equals(yesOrNo)){
                this.myFriendList.remove(deleteFriend);
                this.send(new Message("����ɾ���ú���"));
                deleteFriend.send(new Message(this.name_String+"�ѽ���ɾ��"));
                deleteFriend.myFriendList.remove(this);
            }else if ("2".equals(yesOrNo)){
                this.send(new Message("ɾ�����Ѳ�����ȡ��"));
            }else {
                this.send(new Message("ָ�����"));
            }
        }
        this.send(new Message("�ѷ��غ��Ѳ˵�"));
    }

    /*����Ⱥ���б�*/
    void SeeGroupList(){
        if (myGroupList.size()==0){
            this.send(new Message("����û�м���Ⱥ�ģ���ȥ���Ⱥ�İ�"));
            return;
        }
        this.send(new Message("�������Ⱥ���У�"));
        for (ChatGroup c:myGroupList){
            this.send(new Message(c.getName()+"  ("+c.getGroupNumber()+")"));
        }
    }

    /*���µ���Ⱥ���и��û�����Ϣ*/
    private void updateGroupInformation(ChatGroup chatGroup){
        Server.getAllGroup().removeIf(chatGroup1 -> chatGroup1.equals(chatGroup));
        chatGroup.getGroupAllChannel().add(this);
        Server.getAllGroup().add(chatGroup);
    }

    /*�鿴�ȴ��������Ϣ*/
    private void seeWaitingToDo(){
        int count=1;

        if (waitingToDo.size()==0){
            this.send(new Message("��û�еȴ��������Ϣ"));
            this.send(new Message("�ѷ��ز˵�"));
            return;
        }

        Iterator<Message> iterator=waitingToDo.iterator();
        this.send(new Message("������ "+waitingToDo.size()+" ����Ϣ��Ҫ����"));
        while (iterator.hasNext()) {
            Message m=iterator.next();
            this.send(new Message("���յ��ĵ� " + count++ + " ����Ϣ��" + m.getMessage()));
            this.send(new Message("�봦�����Ϣ������@Exit�ѷ��ز˵�"));
            String getMessage=this.receive().getMessage();
            if ("@Exit".equals(getMessage)){
                this.send(new Message("�ѷ��ز˵�"));
                return;
            }

            if (( !"1".equals(getMessage) && !"2".equals(getMessage))){
                this.send(new Message("ָ������ѷ��ز˵�"));
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
                    this.send(new Message("����ͬ��"+m.sendFrom.name_String+"����Ⱥ��"));
                    m.sendFrom.send(new Message(m.theChatGroup.getName()+"��Ⱥ��ͬ����������룬һ�������������"));
                }

                if(m.isFriend){
                    Server.getAllChannel().remove(this);
                    this.myFriendList.add(m.sendFrom);
                    Server.getAllChannel().add(this);
                    Server.getAllChannel().remove(m.sendFrom);
                    m.sendFrom.myFriendList.add(this);
                    Server.getAllChannel().add(m.sendFrom);
                    this.send(new Message("����ͬ��"+m.sendFrom.name_String+"�����Ϊ����"));
                    m.sendFrom.send(new Message(this.name_String+"ͬ����������룬һ�������������"));
                }

            }else {
                if (m.isGroup){
                    this.send(new Message("���Ѿܾ�"+m.sendFrom.name_String+"����Ⱥ��"));
                    m.sendFrom.send(new Message(m.theChatGroup+"��Ⱥ���ܾ����������"));
                }

                if(m.isFriend){
                    this.send(new Message("���Ѿܾ�"+m.sendFrom.name_String+"�����Ϊ����"));
                    m.sendFrom.send(new Message(this.name_String+"�ܾ����������"));
                }
            }
            iterator.remove();
        }
        this.send(new Message("�ѷ��ز˵�"));
    }
    
    //�ͷ���Դ
    private void release(){
        Server.getAllChannel().remove(this);
        this.isLogin=false;
        this.isRunning=false;                                           //ֹͣ����
        Server.getAllChannel().add(this);
        System.out.println(this.socket.getInetAddress()+"������");      //������ߵ��û�
        Close.close(objectInputStream,objectOutputStream,socket);      //�ر���Դ
        System.out.println("���û�����Դ�Ѿ��ͷ�");
    }

    @Override
    public boolean equals(Object o) {//�Ƚ��˺�
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