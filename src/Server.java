import java.io.*;
import java.net.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**服务端*/
public class Server {

    /**所有的注册过的用户*/
    private static Set<Channel> allChannel=new HashSet<>();
    /**所有被创建的群聊*/
    private static Set<ChatGroup> allGroup=new HashSet<>();

    public static void main(String[] args) throws IOException {
        allChannel=Collections.synchronizedSet(allChannel);
        allGroup=Collections.synchronizedSet(allGroup);

        ServerSocket serverSocket=new ServerSocket(9999);
        System.out.println("-----服务端启动-----");

        /*不停地等待客户端的连接*/
        while(true) {
            /*调用accept()方法，返回一个Socket的对象*/
            Socket socket = serverSocket.accept();
            /*建立连接*/
            System.out.println("一个客户端建立了连接");
            Channel c = new Channel(socket);
            /*创建一个新用户*/
            System.out.println("Yes_1");
            /*开启一个新用户的线程*/
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
