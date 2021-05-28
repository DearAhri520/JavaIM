import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.Socket;

//�ͻ��˵ķ�����Ϣ���ö��̷߳�װ����ɣ�
public class Send implements Runnable{

    private BufferedReader bufferedReader;
    private Socket socket;
    private ObjectOutputStream objectOutputStream=null;
    private boolean isRunning;

    //������
    Send(Socket socket){
        this.isRunning=true;
        this.socket=socket;
        bufferedReader=new BufferedReader(new InputStreamReader(System.in));
        try {
            objectOutputStream=new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            System.out.println("Error:  Client Send(thread) Send(constructor)");
            release();//�ͷ���Դ
        }
    }

    @Override
    //��дrun����
    public void run() {
        while(isRunning){
            Message message=getMessageFromConsole();//�ӿ���̨��ȡ�û��������Ϣ
            if (message!=null){
                this.send(message);                 //����send�����������������message
            }
        }
        System.out.println("send�߳̽���");
    }

    //�ӿ���̨�����Ϣ,����Messageһ����
    private Message getMessageFromConsole() {
        String message;
        try {
            message=bufferedReader.readLine();//�õ��ӿ���̨��������Ϣ
            return new Message(message);
        } catch (IOException e) {
            System.out.println("Error:  Client Send(thread) getMessageFromConsole(method)");
            release();
        }
        return null;
    }

    //������Ϣ
    private void send( Message message){
        try {
            objectOutputStream.writeObject(message);//�������������Ϣ
        } catch (IOException e) {
            System.out.println("Error:  Client Send(thread) send(method)");
            release();
        }
    }

    //�ͷ���Դ
    private void release(){
        this.isRunning=false;
        Close.close(objectOutputStream,socket);
        System.out.println("send�̵߳���Դ�ѹر�");
    }
}