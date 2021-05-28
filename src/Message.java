import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * 定义一个消息类，消息的传递通过消息类进行
 * @author DearAhri520
 */
public class Message implements Serializable {//该消息类可被序列化

    /**需要发送的消息*/
    private String message;
    /**需要被处理的消息*/
    boolean isWaitingToDo=false;
    boolean isGroup=false;                  //是否为加群消息
    boolean isFriend=false;                 //是否为加好友消息
    boolean isLoginOrRegister=false;        //是否为注册时或登录时发送的消息
    Channel sendFrom= null;                 //消息发送者
    ChatGroup theChatGroup=null;            //想要加入的群聊
    Date theMessageSendTime;

    public Message(String message){
        this.message=message;
    }

    //要处理的消息
    //加群消息
    public Message(String message,boolean isSystem,boolean isGroup,ChatGroup theChatGroup,Channel sendFrom){
        this.message=message;
        this.isWaitingToDo=isSystem;
        this.isGroup=isGroup;
        this.theChatGroup=theChatGroup;
        this.sendFrom=sendFrom;
    }

    //加好友消息
    public Message(String message,boolean isSystem,boolean isFriend,Channel sendFrom){
        this.sendFrom=sendFrom;
        this.message=message;
        this.isWaitingToDo=isSystem;
        this.isFriend=isFriend;
    }

    public Message(String message,boolean isLoginOrRegister){
        this.message=message;
        this.isLoginOrRegister=true;
    }

    String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Message message1 = (Message) o;
        return isWaitingToDo == message1.isWaitingToDo &&
                isGroup == message1.isGroup &&
                isFriend == message1.isFriend &&
                Objects.equals(sendFrom, message1.sendFrom) &&
                Objects.equals(theChatGroup, message1.theChatGroup) &&
                Objects.equals(message, message1.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isWaitingToDo, sendFrom, isGroup, theChatGroup, isFriend, message);
    }
}
