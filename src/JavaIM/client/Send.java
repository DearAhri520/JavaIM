package JavaIM.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.Socket;

import JavaIM.close.Close;
import JavaIM.message.Message;

/**
 * 客户端的发送消息，用多线程封装
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
            /*释放资源*/
            release();
        }
    }

    @Override
    public void run() {
        while(isRunning){
            /*从控制台获取用户输入的消息*/
            String message=getMessageFromConsole();
            if (message!=null){
                /*调用send方法,向服务器发送message*/
                this.send(new Message(message));
            }
        }
        System.out.println("send线程结束");
    }

    /**
     * 从控制台获得消息,返回一个String类
     */
    private String getMessageFromConsole() {
        String message;
        try {
            /*得到从控制台读到的消息*/
            message=bufferedReader.readLine();
            return message;
        } catch (IOException e) {
            System.out.println("Error:  JavaIM.Client Send(thread) getMessageFromConsole(method)");
            release();
        }
        return null;
    }

    /**
     * 发送消息
     *
     * @param message 需要发送的消息
     */
    private void send(Message message){
        try {
            /*向服务器发送消息*/
            objectOutputStream.writeObject(message);
        } catch (IOException e) {
            System.out.println("Error:  JavaIM.Client Send(thread) send(method)");
            release();
        }
    }

    /**
     * 释放资源
     */
    private void release(){
        this.isRunning=false;
        Close.close(objectOutputStream,socket);
        System.out.println("send线程的资源已关闭");
    }
}