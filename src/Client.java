import java.io.IOException;
import java.net.Socket;

/**
 * 客户端
 *
 * @author DearAhri520
 */
public class Client {
    public static void main(String[] args) throws IOException {
        /*服务器地址*/
        String serverIpAddress ="10.151.42.44";
        Socket socket;
        /*客户端这边创建一个socket类,端口是9999*/
        socket = new Socket(serverIpAddress ,9999);
        /*启动发送信息的线程*/
        new Thread(new Send(socket)).start();
        /*启动接收消息的线程*/
        new Thread(new Receive(socket)).start();
    }
}