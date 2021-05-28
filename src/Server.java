import java.io.*;
import java.net.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**�����*/
public class Server {

    /**���е�ע������û�*/
    private static Set<Channel> allChannel=new HashSet<>();
    /**���б�������Ⱥ��*/
    private static Set<ChatGroup> allGroup=new HashSet<>();

    public static void main(String[] args) throws IOException {
        allChannel=Collections.synchronizedSet(allChannel);
        allGroup=Collections.synchronizedSet(allGroup);

        ServerSocket serverSocket=new ServerSocket(9999);
        System.out.println("-----���������-----");

        /*��ͣ�صȴ��ͻ��˵�����*/
        while(true) {
            /*����accept()����������һ��Socket�Ķ���*/
            Socket socket = serverSocket.accept();
            /*��������*/
            System.out.println("һ���ͻ��˽���������");
            Channel c = new Channel(socket);
            /*����һ�����û�*/
            System.out.println("Yes_1");
            /*����һ�����û����߳�*/
            new Thread(c).start();
            System.out.println("Yes_2");
        }
    }

    static Set<Channel> getAllChannel() {
        return allChannel;
    }

    static Set<ChatGroup> getAllGroup() {
        return allGroup;
    }
}
