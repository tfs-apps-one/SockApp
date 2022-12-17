package tfsapps.sockapp;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class MyServer {
    /**
     * ソケット通信(サーバー側)
     */
    private String ipaddress = "127.0.0.1";
    public String recv_mess = "";
    private ServerSocket sSocket = null;
    private Socket socket = null;
    private InputStream reader = null;  //読込み
    private OutputStream writer = null; //書込み

    public MyServer() {
        ;
    }
    
    /*********************************
     ソケット受付処理
     *********************************/
    public boolean Accept(int port) {
        int myport = 0;
        try {
            if (sSocket != null) {
                return true;
            }

            if (port == 0)      myport = 2002;
            else                myport = port;

            //IPアドレスとポート番号を指定してサーバー側のソケットを作成
            sSocket = new ServerSocket(myport);
            socket = sSocket.accept();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    /*********************************
     ソケット切断
     *********************************/
    public boolean DisConnect() {
        try {
            socket.close();
            socket = null;
            writer.close();
            reader.close();
        }
        catch (Exception e) {
        }
        return true;
    }

    /*********************************
     メッセージ受信
     *********************************/
    public boolean RecvMessage() {

        String temp = "";
        byte w[] = new byte[4048];
        int  size;

        if (socket.isConnected() == false){
            return false;
        }
        try {
            reader = socket.getInputStream();
            size = reader.read(w);
            if (size <= 0)  {
                return false;
            }
            else{
                temp = new String(w, 0, size, "UTF-8");
            }
            recv_mess += "受信<<<："+temp+"\n";
            return true;
        }
        catch (Exception e){
            return false;
        }
    }

    /*********************************
     メッセージ送信
     *********************************/
    public boolean SendMessage() {

        String str = "私はサーバーです";

        if (socket.isConnected() == false){
            return false;
        }
        try {
            writer = socket.getOutputStream();
            writer.write(str.getBytes("UTF-8"));
            return true;
        }
        catch (Exception e){
            return false;
        }
    }
}
