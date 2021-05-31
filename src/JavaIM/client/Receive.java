package JavaIM.client;

import JavaIM.close.Close;
import JavaIM.message.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

/**
 * @author DearAhri520
 *
 * �ͻ��˵�receive�������ö��̷߳�װ
 */
/**�ͻ��˵�receive�������ö��̷߳�װ*/
public class Receive implements Runnable{
    private Socket socket;
    private ObjectInputStream objectInputStream=null;
    private boolean isRunning;

    public Receive(Socket socket){
        this.socket=socket;
        this.isRunning=true;
        try{
            objectInputStream=new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.out.println("Error:  Client Receive(thread) Receive(constructor)");
            this.release();
        }
    }

    @Override
    public void run() {
        while(isRunning){
            Message message=this.receive();
            if (message!=null){
                System.out.println(message.getMessage());
            }
        }
        System.out.println("receive�߳̽���");
    }

    private Message receive(){
        Message message=null;
        try{
            message=(Message) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error:  Client Receive(thread) receive(method)");
            release();
        }
        return message;
    }

    //�ͷ���Դ
    private void release(){
        this.isRunning=false;
        Close.close(objectInputStream,socket);
        System.out.println("receive�̵߳���Դ�ѹر�");
    }
}