package JavaIM.chatGroup;

import JavaIM.Server;
import JavaIM.message.Message;
import JavaIM.channel.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;

/**
 * Ⱥ����
 *
 * @author DearAhri520
 */
public class ChatGroup implements Serializable {
    /**�����Ⱥ���˺�*/
    private static int GroupNumber = 100000;
    /**���е�Ⱥ��Ա�б�*/
    private HashSet<Channel> groupAllChannel = new HashSet<>();
    /**Ⱥ��*/
    private Channel groupMaster;
    /**Ⱥ������*/
    private String name;
    /**Ⱥ���˺�*/
    private String groupNumber;

    /**
     *
     * @param groupMaster Ⱥ��
     * @param name Ⱥ������
     */
    private ChatGroup(Channel groupMaster, String name) {
        this.groupAllChannel.add(groupMaster);
        /*Ⱥ��*/
        this.groupMaster = groupMaster;
        /*Ⱥ������*/
        this.name = name;
        /*Ⱥ���˺�*/
        this.groupNumber = GroupNumber + "";
        /*�����Ⱥ���˺�*/
        GroupNumber++;
    }

    public String getName() {
        return this.name;
    }

    public String getGroupNumber() {
        return this.groupNumber;
    }

    private Channel getGroupMaster() {
        return this.groupMaster;
    }

    public HashSet<Channel> getGroupAllChannel() {
        return this.groupAllChannel;
    }

    /**
     *
     * @return ����Ⱥ����Ϣ
     */
    private String info() {
        return "Ⱥ���� " + this.groupMaster.getName() + "  Ⱥ�˺ţ� " + this.groupNumber + "  Ⱥ�����ƣ� " + this.name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChatGroup chatGroup = (ChatGroup) o;
        return Objects.equals(groupMaster, chatGroup.groupMaster) &&
                Objects.equals(name, chatGroup.name) &&
                Objects.equals(groupNumber, chatGroup.groupNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupMaster, name, groupNumber);
    }

    /**
     * ����Ⱥ��
     * @param channel Ⱥ��
     */
    public static void createGroup(Channel channel) {
        channel.send(new Message("������Ⱥ�����ƻ�����@Exit����Ⱥ�Ĳ˵�"));
        //������Ⱥ�ģ���������Ⱥ����Ⱥ������
        Message getMessage = channel.receive();
        if ("@Exit".equals(getMessage.getMessage())) {
            channel.send(new Message("�ѷ���Ⱥ�Ĳ˵�"));
            return;
        }
        ChatGroup chatGroup = new ChatGroup(channel, getMessage.getMessage());
        /*����Ⱥ����ӽ�myGroupList*/
        channel.myGroupList.add(chatGroup);
        /*����Ⱥ����ӽ�allGroupList*/
        Server.getAllGroup().add(chatGroup);
        channel.send(new Message("�µ�Ⱥ�Ĵ����ɹ� Ⱥ������Ϊ" + chatGroup.getName() + ", Ⱥ���˺�Ϊ" + chatGroup.getGroupNumber()));
        channel.send(new Message("�ѷ���Ⱥ�Ĳ˵�"));
    }

    /**
     * �������Ⱥ��
     * @param channel ��Ҫ����Ⱥ�ĵ��û�
     */
    public static void joinGroup(Channel channel) {
        boolean isFind = false;
        ChatGroup theFindGroup = null;

        channel.send(new Message("��������������Ⱥ���˺�"));
        String getWantToJoin = channel.receive().getMessage();
        for (ChatGroup c : channel.myGroupList) {
            if (c.getGroupNumber().equals(getWantToJoin)) {
                channel.send(new Message("���Ѿ������Ⱥ��"));
                channel.send(new Message("�ѷ���Ⱥ�Ĳ˵�"));
                return;
            }
        }
        for (ChatGroup c : Server.getAllGroup()) {
            if (c.getGroupNumber().equals(getWantToJoin)) {
                theFindGroup = c;
                channel.send(new Message("��ȺȺ����Ϣ: " + c.info()));
                isFind = true;
                break;
            }
        }

        if (!isFind) {
            channel.send(new Message("δ��ѯ����Ⱥ��"));
            channel.send(new Message("�ѷ���Ⱥ�Ĳ˵�"));
            return;
        }

        channel.send(new Message("�Ƿ������������Ⱥ�ģ�����1ȷ�ϣ�����2ȡ��"));
        String getNumber = channel.receive().getMessage();
        if ("1".equals(getNumber)) {
            if (channel.myGroupList.contains(theFindGroup)) {
                channel.send(new Message("���Ѽ����Ⱥ��"));
            } else {
                /*��Ⱥ������һ��ϵͳ��Ϣ,�������û���ͼ����Ⱥ��*/
                theFindGroup.getGroupMaster().send(new Message("ϵͳ��Ϣ�� �û�" + channel.getName() + "(" + channel.getAccountNumber() + ")" + " ������� " + theFindGroup.getName() + " Ⱥ��"));
                /*��Ⱥ������һ��isSystemΪtrue����Ϣ�������ʾΪtrue��������ȴ������list*/
                theFindGroup.getGroupMaster().send(new Message("ϵͳ��Ϣ�� �û�" + channel.getName() + "(" + channel.getAccountNumber() + ")" + " ������� " + theFindGroup.getName() + " Ⱥ��" +
                        "ͬ��������1���ܾ�������2   ", true, true, theFindGroup, channel));
                channel.send(new Message("�ѷ������룬�ȴ���ȺȺ������"));
            }
        } else if ("2".equals(getNumber)) {
            channel.send(new Message("����ȡ���������Ⱥ��"));
        } else {
            channel.send(new Message("ָ�����"));
        }
        channel.send(new Message("�ѷ���Ⱥ�Ĳ˵�"));
    }

    /**
     * �˳�Ⱥ��
     * @param channel �˳�Ⱥ�ĵ��û�
     */
    public static void exitGroup(Channel channel) {
        boolean isFind = false;

        channel.SeeGroupList();             //����Ⱥ���б�
        if (channel.myGroupList.size() == 0) {
            channel.send(new Message("�ѷ���Ⱥ�Ĳ˵�"));
            return;
        }
        channel.send(new Message("�����������˳���Ⱥ���˺ţ�������@Exit���ص��˵�����"));
        String getMessageExitGroup = channel.receive().getMessage();
        if ("@Exit".equals(getMessageExitGroup)) {
            channel.send(new Message("�ѷ���Ⱥ�Ĳ˵�"));
            return;
        }
        ChatGroup deleteChatGroup = null;
        for (ChatGroup c : channel.myGroupList) {
            if (getMessageExitGroup.equals(c.getGroupNumber())) {
                isFind = true;
                /*�������Ⱥ��*/
                if (c.getGroupMaster().getAccountNumber().equals(channel.getAccountNumber())) {
                    channel.send(new Message("���Ǹ�ȺȺ�����޷��˳�"));
                    channel.send(new Message("�ѷ���Ⱥ�Ĳ˵�"));
                    return;
                }
                /*��Ⱥ�������˳�Ⱥ�ĵ���Ϣ*/
                c.getGroupMaster().send(new Message("ϵͳ��Ϣ�� " + channel.getName() + "���˳�Ⱥ�ģ�" + c.getName()));
                deleteChatGroup = c;
                break;
            }
        }
        if (isFind) {
            /*���Լ���Ⱥ���б���ɾ����Ⱥ��*/
            channel.send(new Message("���ѳɹ��˳���Ⱥ��"));
            channel.myGroupList.remove(deleteChatGroup);
            /*��Server�˵�Ⱥ����ɾ���Լ�*/
            ChatGroup finalDeleteChatGroup = deleteChatGroup;
            Server.getAllGroup().removeIf(chatGroup1 -> chatGroup1.equals(finalDeleteChatGroup));
            finalDeleteChatGroup.getGroupAllChannel().remove(channel);
            Server.getAllGroup().add(finalDeleteChatGroup);
        } else {
            channel.send(new Message("�����ָ������δ�ҵ���Ⱥ��"));
        }
        channel.send(new Message("�ѷ���Ⱥ�Ĳ˵�"));
    }

    /**
     * Ⱥ������
     * @param channel ����Ⱥ����Ϣ���û�
     */
    public static void chatWithGroup(Channel channel) {
        String chatWithGroup;
        String sendToGroup;
        boolean isFind = false;
        channel.SeeGroupList();

        while (channel.getIsRunning()) {
            channel.send(new Message("����������Ҫ�����Ⱥ���˺�"));
            chatWithGroup = channel.receive().getMessage();
            if ("@Exit".equals(chatWithGroup)) {
                channel.send(new Message("�ѷ���Ⱥ�Ĳ˵�"));
                return;
            }

            for (ChatGroup c : channel.myGroupList) {
                if (c.getGroupNumber().equals(chatWithGroup)) {
                    isFind = true;
                    break;
                }
            }

            if (!isFind) {
                channel.send(new Message("δ�ҵ���Ⱥ�ģ����������������@Exit����Ⱥ�Ĳ˵�"));
                continue;
            }

            channel.send(new Message("��������Ҫ���͵����ݻ�����@Exit����Ⱥ�Ĳ˵�"));
            while (channel.getIsRunning()) {
                sendToGroup = channel.receive().getMessage();
                if ("@Exit".equals(sendToGroup)) {
                    channel.send(new Message("�ѷ���Ⱥ�Ĳ˵�"));
                    return;
                }
                for (ChatGroup chatGroup : Server.getAllGroup()) {
                    if (chatGroup.getGroupNumber().equals(chatWithGroup)) {
                        for (Channel channel1 : chatGroup.groupAllChannel) {
                            if (channel1 != channel) {
                                channel1.send(new Message(channel.getName() + "��" + chatGroup.getName() + "Ⱥ�ĵ�������˵" + sendToGroup));
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

    /**
     * ����Ⱥ��Ա(ֻ�������Լ��ĺ���,ֻ�е�����Ⱥ��ʱ�����������)
     */
    public static void inviteGroupMember(Channel channel) {
        boolean isFindFriend = false;
        boolean isFindGroup = false;
        String inviteGroupMemberNumber;
        String inviteGroupNumber;
        Channel wantToInvite = null;

        channel.SeeFriendList();
        channel.send(new Message("����������Ҫ����ĺ��ѵ��˺ţ���ֻ�������Լ��ĺ��ѣ�"));
        inviteGroupMemberNumber = channel.receive().getMessage();
        for (Channel channel1 : channel.myFriendList) {
            if (channel1.getAccountNumber().equals(inviteGroupMemberNumber)) {
                wantToInvite = channel1;
                isFindFriend = true;
                break;
            }
        }

        if (!isFindFriend) {
            channel.send(new Message("û���ҵ��ú���"));
            channel.send(new Message("�ѷ���Ⱥ�Ĳ˵�"));
            return;
        }

        channel.send(new Message("����������Ҫ�����Ⱥ���˺�"));
        inviteGroupNumber = channel.receive().getMessage();
        for (ChatGroup chatGroup : channel.myGroupList) {
            if (chatGroup.getGroupNumber().equals(inviteGroupNumber) && chatGroup.groupMaster.equals(channel)) {
                isFindGroup = true;
                break;
            }
        }

        if (!isFindGroup) {
            channel.send(new Message("δ��ѯ����Ⱥ�Ļ����㲻�Ǹ�Ⱥ��Ⱥ��"));
            channel.send(new Message("�ѷ���Ⱥ�Ĳ˵�"));
            return;
        }

        channel.send(new Message("�Ƿ�����ú��ѣ�����1ȷ�ϣ�����2ȡ��"));
        inviteGroupMemberNumber = channel.receive().getMessage();
        if ("1".equals(inviteGroupMemberNumber)) {
            for (ChatGroup chatGroup : Server.getAllGroup()) {
                if (chatGroup.getGroupNumber().equals(inviteGroupNumber)) {
                    wantToInvite.send(new Message(channel.getName() + "�����������Ⱥ�ģ�" + chatGroup.getName()));
                    chatGroup.groupAllChannel.add(wantToInvite);
                    wantToInvite.myGroupList.add(chatGroup);
                    break;
                }
            }
            channel.send(new Message("����ɹ�"));
            channel.send(new Message("�ѷ���Ⱥ�Ĳ˵�"));
        } else if ("2".equals(inviteGroupMemberNumber)) {
            channel.send(new Message("��ȡ���������"));
            channel.send(new Message("�ѷ���Ⱥ�Ĳ˵�"));
        } else {
            channel.send(new Message("ָ������ѷ���Ⱥ�Ĳ˵�"));
        }
    }
}