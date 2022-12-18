package tfsapps.sockapp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class MyClient {

    public String str_status = "";
    private Socket cSocket = null;
    private OutputStream writer = null; //書込み
    private InputStream reader = null;  //読込み
    private String ipaddress = "192.168.1.17";
    private int counter = 0;

    public MyClient(String ipaddr) {
        if(ipaddr.isEmpty() == false) {
            ipaddress = ipaddr;
        }
    }

    /*********************************
        ソケット接続
     *********************************/
    public boolean Connect() {
        str_status += "[1] 接続中\n";
        InetSocketAddress _socket = new InetSocketAddress(ipaddress, 2002);
        try {
            cSocket = new Socket();
            cSocket.connect(_socket, 1000);
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        str_status += "[2] 接続完了\n";
        return true;
    }
    /*********************************
         ソケット切断
     *********************************/
    public boolean DisConnect() {
        try {
            cSocket.close();
            cSocket = null;
            writer.close();
            reader.close();
        }
        catch (Exception e) {
        }
        return true;
    }

    /*********************************
        メッセージ送信
     *********************************/
    public boolean SendMessage(String temp) {

        if (cSocket.isConnected() == false){
            return false;
        }

        str_status = "[3] テキスト送信中です・・・\n";

        // テキスト送信
        try {
            writer = cSocket.getOutputStream();
            counter++;
            String str = (temp + counter);
            writer.write(str.getBytes("UTF-8"));
            str_status += "送信>>>："+str+"\n";

        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /*********************************
     メッセージ受信
     *********************************/
    public Boolean RecvMessage() {

        String temp = "";
        byte w[] = new byte[4048];
        int size;
        if (cSocket.isConnected() == false){
            return false;
        }
        str_status += "[4] テキスト受信中です・・・\n";

        // テキスト受信
        try {
            reader = cSocket.getInputStream();
            size = reader.read(w);
            if (size <= 0)  {
                return false;
            }
            else{
                temp = new String(w, 0, size, "UTF-8");
            }
            str_status += "受信<<<："+temp+"\n";

        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
