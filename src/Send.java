import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.Socket;

//客户端的发送消息，用多线程封装（完成）
public class Send implements Runnable{

    private BufferedReader bufferedReader;
    private Socket socket;
    private ObjectOutputStream objectOutputStream=null;
    private boolean isRunning;

    //构造器
    Send(Socket socket){
        this.isRunning=true;
        this.socket=socket;
        bufferedReader=new BufferedReader(new InputStreamReader(System.in));
        try {
            objectOutputStream=new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            System.out.println("Error:  Client Send(thread) Send(constructor)");
            release();//释放资源
        }
    }

    @Override
    //重写run方法
    public void run() {
        while(isRunning){
            Message message=getMessageFromConsole();//从控制台获取用户输入的消息
            if (message!=null){
                this.send(message);                 //调用send方法，向服务器发送message
            }
        }
        System.out.println("send线程结束");
    }

    //从控制台获得消息,返回Message一个类
    private Message getMessageFromConsole() {
        String message;
        try {
            message=bufferedReader.readLine();//得到从控制台读到的消息
            return new Message(message);
        } catch (IOException e) {
            System.out.println("Error:  Client Send(thread) getMessageFromConsole(method)");
            release();
        }
        return null;
    }

    //发送消息
    private void send( Message message){
        try {
            objectOutputStream.writeObject(message);//向服务器发送消息
        } catch (IOException e) {
            System.out.println("Error:  Client Send(thread) send(method)");
            release();
        }
    }

    //释放资源
    private void release(){
        this.isRunning=false;
        Close.close(objectOutputStream,socket);
        System.out.println("send线程的资源已关闭");
    }
}