import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * ����һ����Ϣ�࣬��Ϣ�Ĵ���ͨ����Ϣ�����
 * @author DearAhri520
 */
public class Message implements Serializable {//����Ϣ��ɱ����л�

    /**��Ҫ���͵���Ϣ*/
    private String message;
    /**��Ҫ���������Ϣ*/
    boolean isWaitingToDo=false;
    boolean isGroup=false;                  //�Ƿ�Ϊ��Ⱥ��Ϣ
    boolean isFriend=false;                 //�Ƿ�Ϊ�Ӻ�����Ϣ
    boolean isLoginOrRegister=false;        //�Ƿ�Ϊע��ʱ���¼ʱ���͵���Ϣ
    Channel sendFrom= null;                 //��Ϣ������
    ChatGroup theChatGroup=null;            //��Ҫ�����Ⱥ��
    Date theMessageSendTime;

    public Message(String message){
        this.message=message;
    }

    //Ҫ�������Ϣ
    //��Ⱥ��Ϣ
    public Message(String message,boolean isSystem,boolean isGroup,ChatGroup theChatGroup,Channel sendFrom){
        this.message=message;
        this.isWaitingToDo=isSystem;
        this.isGroup=isGroup;
        this.theChatGroup=theChatGroup;
        this.sendFrom=sendFrom;
    }

    //�Ӻ�����Ϣ
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
