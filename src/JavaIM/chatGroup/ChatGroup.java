package JavaIM.chatGroup;

import JavaIM.Server;
import JavaIM.message.Message;
import JavaIM.channel.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;

/**
 * 群聊类
 *
 * @author DearAhri520
 */
public class ChatGroup implements Serializable {
    /**分配的群聊账号*/
    private static int GroupNumber = 100000;
    /**所有的群成员列表*/
    private HashSet<Channel> groupAllChannel = new HashSet<>();
    /**群主*/
    private Channel groupMaster;
    /**群聊名称*/
    private String name;
    /**群聊账号*/
    private String groupNumber;

    /**
     *
     * @param groupMaster 群主
     * @param name 群聊名称
     */
    private ChatGroup(Channel groupMaster, String name) {
        this.groupAllChannel.add(groupMaster);
        /*群主*/
        this.groupMaster = groupMaster;
        /*群聊名称*/
        this.name = name;
        /*群聊账号*/
        this.groupNumber = GroupNumber + "";
        /*分配的群聊账号*/
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
     * @return 返回群聊信息
     */
    private String info() {
        return "群主： " + this.groupMaster.getName() + "  群账号： " + this.groupNumber + "  群聊名称： " + this.name;
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
     * 创建群聊
     * @param channel 群主
     */
    public static void createGroup(Channel channel) {
        channel.send(new Message("请输入群聊名称或输入@Exit返回群聊菜单"));
        //创建新群聊，并且设置群主和群聊名称
        Message getMessage = channel.receive();
        if ("@Exit".equals(getMessage.getMessage())) {
            channel.send(new Message("已返回群聊菜单"));
            return;
        }
        ChatGroup chatGroup = new ChatGroup(channel, getMessage.getMessage());
        /*将该群聊添加进myGroupList*/
        channel.myGroupList.add(chatGroup);
        /*将该群聊添加进allGroupList*/
        Server.getAllGroup().add(chatGroup);
        channel.send(new Message("新的群聊创建成功 群聊名称为" + chatGroup.getName() + ", 群聊账号为" + chatGroup.getGroupNumber()));
        channel.send(new Message("已返回群聊菜单"));
    }

    /**
     * 申请加入群聊
     * @param channel 想要加入群聊的用户
     */
    public static void joinGroup(Channel channel) {
        boolean isFind = false;
        ChatGroup theFindGroup = null;

        channel.send(new Message("请输入你想加入的群聊账号"));
        String getWantToJoin = channel.receive().getMessage();
        for (ChatGroup c : channel.myGroupList) {
            if (c.getGroupNumber().equals(getWantToJoin)) {
                channel.send(new Message("你已经加入该群聊"));
                channel.send(new Message("已返回群聊菜单"));
                return;
            }
        }
        for (ChatGroup c : Server.getAllGroup()) {
            if (c.getGroupNumber().equals(getWantToJoin)) {
                theFindGroup = c;
                channel.send(new Message("该群群聊信息: " + c.info()));
                isFind = true;
                break;
            }
        }

        if (!isFind) {
            channel.send(new Message("未查询到该群聊"));
            channel.send(new Message("已返回群聊菜单"));
            return;
        }

        channel.send(new Message("是否加入申请加入该群聊，输入1确认，输入2取消"));
        String getNumber = channel.receive().getMessage();
        if ("1".equals(getNumber)) {
            if (channel.myGroupList.contains(theFindGroup)) {
                channel.send(new Message("你已加入该群聊"));
            } else {
                /*向群主发送一条系统消息,表明有用户试图加入群聊*/
                theFindGroup.getGroupMaster().send(new Message("系统消息： 用户" + channel.getName() + "(" + channel.getAccountNumber() + ")" + " 申请加入 " + theFindGroup.getName() + " 群聊"));
                /*向群主发送一条isSystem为true的消息，如果表示为true，则会进入等待处理的list*/
                theFindGroup.getGroupMaster().send(new Message("系统消息： 用户" + channel.getName() + "(" + channel.getAccountNumber() + ")" + " 申请加入 " + theFindGroup.getName() + " 群聊" +
                        "同意请输入1，拒绝请输入2   ", true, true, theFindGroup, channel));
                channel.send(new Message("已发送申请，等待该群群主处理"));
            }
        } else if ("2".equals(getNumber)) {
            channel.send(new Message("你已取消申请加入群聊"));
        } else {
            channel.send(new Message("指令错误"));
        }
        channel.send(new Message("已返回群聊菜单"));
    }

    /**
     * 退出群聊
     * @param channel 退出群聊的用户
     */
    public static void exitGroup(Channel channel) {
        boolean isFind = false;

        channel.SeeGroupList();             //发送群聊列表
        if (channel.myGroupList.size() == 0) {
            channel.send(new Message("已返回群聊菜单"));
            return;
        }
        channel.send(new Message("请输入你想退出的群聊账号，或输入@Exit返回到菜单界面"));
        String getMessageExitGroup = channel.receive().getMessage();
        if ("@Exit".equals(getMessageExitGroup)) {
            channel.send(new Message("已返回群聊菜单"));
            return;
        }
        ChatGroup deleteChatGroup = null;
        for (ChatGroup c : channel.myGroupList) {
            if (getMessageExitGroup.equals(c.getGroupNumber())) {
                isFind = true;
                /*如果你是群主*/
                if (c.getGroupMaster().getAccountNumber().equals(channel.getAccountNumber())) {
                    channel.send(new Message("你是该群群主，无法退出"));
                    channel.send(new Message("已返回群聊菜单"));
                    return;
                }
                /*向群主发送退出群聊的消息*/
                c.getGroupMaster().send(new Message("系统消息： " + channel.getName() + "已退出群聊：" + c.getName()));
                deleteChatGroup = c;
                break;
            }
        }
        if (isFind) {
            /*从自己的群聊列表中删除该群聊*/
            channel.send(new Message("你已成功退出该群聊"));
            channel.myGroupList.remove(deleteChatGroup);
            /*从Server端的群聊中删除自己*/
            ChatGroup finalDeleteChatGroup = deleteChatGroup;
            Server.getAllGroup().removeIf(chatGroup1 -> chatGroup1.equals(finalDeleteChatGroup));
            finalDeleteChatGroup.getGroupAllChannel().remove(channel);
            Server.getAllGroup().add(finalDeleteChatGroup);
        } else {
            channel.send(new Message("输入的指令错误或未找到该群聊"));
        }
        channel.send(new Message("已返回群聊菜单"));
    }

    /**
     * 群聊聊天
     * @param channel 发送群聊信息的用户
     */
    public static void chatWithGroup(Channel channel) {
        String chatWithGroup;
        String sendToGroup;
        boolean isFind = false;
        channel.SeeGroupList();

        while (channel.getIsRunning()) {
            channel.send(new Message("请输入你想要聊天的群聊账号"));
            chatWithGroup = channel.receive().getMessage();
            if ("@Exit".equals(chatWithGroup)) {
                channel.send(new Message("已返回群聊菜单"));
                return;
            }

            for (ChatGroup c : channel.myGroupList) {
                if (c.getGroupNumber().equals(chatWithGroup)) {
                    isFind = true;
                    break;
                }
            }

            if (!isFind) {
                channel.send(new Message("未找到该群聊，请重新输入或输入@Exit返回群聊菜单"));
                continue;
            }

            channel.send(new Message("请输入你要发送的内容或输入@Exit返回群聊菜单"));
            while (channel.getIsRunning()) {
                sendToGroup = channel.receive().getMessage();
                if ("@Exit".equals(sendToGroup)) {
                    channel.send(new Message("已返回群聊菜单"));
                    return;
                }
                for (ChatGroup chatGroup : Server.getAllGroup()) {
                    if (chatGroup.getGroupNumber().equals(chatWithGroup)) {
                        for (Channel channel1 : chatGroup.groupAllChannel) {
                            if (channel1 != channel) {
                                channel1.send(new Message(channel.getName() + "对" + chatGroup.getName() + "群聊的所有人说" + sendToGroup));
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

    /**
     * 邀请群成员(只能邀请自己的好友,只有当你是群主时才能邀请好友)
     */
    public static void inviteGroupMember(Channel channel) {
        boolean isFindFriend = false;
        boolean isFindGroup = false;
        String inviteGroupMemberNumber;
        String inviteGroupNumber;
        Channel wantToInvite = null;

        channel.SeeFriendList();
        channel.send(new Message("请输入你想要邀请的好友的账号（你只能邀请自己的好友）"));
        inviteGroupMemberNumber = channel.receive().getMessage();
        for (Channel channel1 : channel.myFriendList) {
            if (channel1.getAccountNumber().equals(inviteGroupMemberNumber)) {
                wantToInvite = channel1;
                isFindFriend = true;
                break;
            }
        }

        if (!isFindFriend) {
            channel.send(new Message("没有找到该好友"));
            channel.send(new Message("已返回群聊菜单"));
            return;
        }

        channel.send(new Message("请输入你想要邀请的群聊账号"));
        inviteGroupNumber = channel.receive().getMessage();
        for (ChatGroup chatGroup : channel.myGroupList) {
            if (chatGroup.getGroupNumber().equals(inviteGroupNumber) && chatGroup.groupMaster.equals(channel)) {
                isFindGroup = true;
                break;
            }
        }

        if (!isFindGroup) {
            channel.send(new Message("未查询到该群聊或者你不是该群聊群主"));
            channel.send(new Message("已返回群聊菜单"));
            return;
        }

        channel.send(new Message("是否邀请该好友，输入1确认，输入2取消"));
        inviteGroupMemberNumber = channel.receive().getMessage();
        if ("1".equals(inviteGroupMemberNumber)) {
            for (ChatGroup chatGroup : Server.getAllGroup()) {
                if (chatGroup.getGroupNumber().equals(inviteGroupNumber)) {
                    wantToInvite.send(new Message(channel.getName() + "已邀请你加入群聊：" + chatGroup.getName()));
                    chatGroup.groupAllChannel.add(wantToInvite);
                    wantToInvite.myGroupList.add(chatGroup);
                    break;
                }
            }
            channel.send(new Message("邀请成功"));
            channel.send(new Message("已返回群聊菜单"));
        } else if ("2".equals(inviteGroupMemberNumber)) {
            channel.send(new Message("已取消邀请好友"));
            channel.send(new Message("已返回群聊菜单"));
        } else {
            channel.send(new Message("指令错误，已返回群聊菜单"));
        }
    }
}