package tfsapps.sockapp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class MyClient {

    public String str_status = "";
    public String recv_mess = "";
    public String send_mess = "";
    public String now_status = "";
    private Socket cSocket = null;
    private OutputStream writer = null; //書込み
    private InputStream reader = null;  //読込み
    private String ipaddress = "192.168.1.19";
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
    public boolean SendMessage(String cmd) {

        if (cSocket.isConnected() == false){
            return false;
        }

        str_status = "[3] テキスト送信中です・・・\n";

        // テキスト送信
        try {
            writer = cSocket.getOutputStream();

            if (cmd.indexOf("LA6") != -1) {
                send_mess = SendCmd_LA6();
                writer.write(send_mess.getBytes("US-ASCII"));
            }
            else if (cmd.indexOf("iA0") != -1) {
                send_mess = SendCmd_iA0();
                writer.write(send_mess.getBytes("US-ASCII"));
            }

            str_status += "送信>>>："+send_mess+"\n";

        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /*********************************
        メッセージ受信
     *********************************/
    public Boolean RecvMessage() {

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
                recv_mess = new String(w, 0, size, "UTF-8");
            }
            str_status += "受信<<<："+recv_mess+"\n";

        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /*********************************
        LA6 機械番号通知
     *********************************/
    public String SendCmd_LA6(){
        String tmp_cmd = "";
        byte w[] = new byte[128];
        w[0] = 0x0a;
        w[1] = 'L';
        w[2] = 'A';
        w[3] = '6';
        w[4] = 'L';
        w[5] = '0';
        w[6] = '0';
        w[7] = '1';
        w[8] = 'S'; //チェックデジットは適当？器物側は見ていない
        w[9] = 'S'; //チェックデジットは適当？器物側は見ていない
        w[10] = 0x0d;
        try {
            tmp_cmd = new String(w, 0, 11, "US-ASCII");
        }
        catch (IOException e){

        }
        return tmp_cmd;
    }
    /*********************************
        nA1 状態通知
        2:  非生産中
        6:  生産中
        8:  エラー発生中
     *********************************/
    public boolean RecvCmd_nA1(){
        now_status = "";

        try {
            byte tmp[] = recv_mess.getBytes("US-ASCII");
            if (tmp[24] == 0x32){
                now_status = "非生産中";
            }
            else if(tmp[24] == 0x36){
                now_status = "生産中";
            }
            else if(tmp[24] == 0x38){
                now_status = "エラー発生中";
            }
            else{
                now_status = "";
            }
            return true;
        }
        catch (IOException e){
            return false;
        }
    }
    /*********************************
        iA0 エラー通知応答
     *********************************/
    public String SendCmd_iA0(){
        String tmp_cmd = "";
        byte w[] = new byte[128];
        w[0] = 0x0a;
        w[1] = 'i';
        w[2] = 'A';
        w[3] = '0';
        w[4] = 'L';
        w[5] = '0'; //エラー受信分
        w[6] = '0';
        w[7] = '0'; //エラー受信秒
        w[8] = '0';
        w[9] = '0'; //シリアル番号
        w[10] = '0';
        w[11] = '0';
        w[12] = '0';
        w[13] = 'S'; //チェックデジットは適当？器物側は見ていない
        w[14] = 'S'; //チェックデジットは適当？器物側は見ていない
        w[15] = 0x0d;
        try {
            tmp_cmd = new String(w, 0, 16, "US-ASCII");
        }
        catch (IOException e){

        }
        return tmp_cmd;
    }
}
