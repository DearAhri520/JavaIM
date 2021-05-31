package JavaIM;

import JavaIM.client.Receive;
import JavaIM.client.Send;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * �ͻ���
 *
 * @author DearAhri520
 */
public class Client {
    public static void main(String[] args) throws IOException {
        /*��������ַ*/
        String serverIpAddress ="10.151.99.126";
        /*�ͻ�����ߴ���һ��socket��,�˿���9999*/
        Socket socket = new Socket(serverIpAddress ,9999);

        ThreadPoolExecutor threadPoolExecutor=new ThreadPoolExecutor(2, 2, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(),
                r -> {
                    Thread thread = new Thread(r);
                    thread.setUncaughtExceptionHandler((thread1, throwable) -> System.out.println("�����쳣����: "+throwable));
                    return thread;
                }
        );

        threadPoolExecutor.submit(new Send(socket));
        threadPoolExecutor.submit(new Receive(socket));
    }
}