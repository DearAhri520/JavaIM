import java.io.IOException;
import java.net.Socket;

/**
 * �ͻ���
 *
 * @author DearAhri520
 */
public class Client {
    public static void main(String[] args) throws IOException {
        /*��������ַ*/
        String serverIpAddress ="10.151.42.44";
        Socket socket;
        /*�ͻ�����ߴ���һ��socket��,�˿���9999*/
        socket = new Socket(serverIpAddress ,9999);
        /*����������Ϣ���߳�*/
        new Thread(new Send(socket)).start();
        /*����������Ϣ���߳�*/
        new Thread(new Receive(socket)).start();
    }
}