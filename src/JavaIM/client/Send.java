package JavaIM.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.Socket;

import JavaIM.close.Close;
import JavaIM.message.Message;

/**
 * �ͻ��˵ķ�����Ϣ���ö��̷߳�װ
 *
 * @author DearAhri520
 */
public class Send implements Runnable{

    private BufferedReader bufferedReader;
    private Socket socket;
    private ObjectOutputStream objectOutputStream=null;
    private boolean isRunning;

    public Send(Socket socket){
        this.isRunning=true;
        this.socket=socket;
        bufferedReader=new BufferedReader(new InputStreamReader(System.in));
        try {
            objectOutputStream=new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            System.out.println("Error:  JavaIM.Client Send(thread) Send(constructor)");
            /*�ͷ���Դ*/
            release();
        }
    }

    @Override
    public void run() {
        while(isRunning){
            /*�ӿ���̨��ȡ�û��������Ϣ*/
            String message=getMessageFromConsole();
            if (message!=null){
                /*����send����,�����������message*/
                this.send(new Message(message));
            }
        }
        System.out.println("send�߳̽���");
    }

    /**
     * �ӿ���̨�����Ϣ,����һ��String��
     */
    private String getMessageFromConsole() {
        String message;
        try {
            /*�õ��ӿ���̨��������Ϣ*/
            message=bufferedReader.readLine();
            return message;
        } catch (IOException e) {
            System.out.println("Error:  JavaIM.Client Send(thread) getMessageFromConsole(method)");
            release();
        }
        return null;
    }

    /**
     * ������Ϣ
     *
     * @param message ��Ҫ���͵���Ϣ
     */
    private void send(Message message){
        try {
            /*�������������Ϣ*/
            objectOutputStream.writeObject(message);
        } catch (IOException e) {
            System.out.println("Error:  JavaIM.Client Send(thread) send(method)");
            release();
        }
    }

    /**
     * �ͷ���Դ
     */
    private void release(){
        this.isRunning=false;
        Close.close(objectOutputStream,socket);
        System.out.println("send�̵߳���Դ�ѹر�");
    }
}