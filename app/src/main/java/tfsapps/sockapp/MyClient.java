package tfsapps.sockapp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.TimerTask;

import android.content.Context;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class MyClient {
    //public class MyClient extends TimerTask {

    public String str_status = "";
    private Socket cSocket = null;
    private OutputStream writer = null; //書込み
    private InputStream reader = null;  //読込み

    private Handler mHandler = new Handler();   //UI Threadへのpost用ハンドラ
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
    public String SendRecvMessage(String send) {

        String temp = "";
        byte [] w = new byte[4048];
        int size;

        if (cSocket.isConnected() == false){
            return temp;
        }

        // テキスト送信
        try {
            writer = cSocket.getOutputStream();
            counter++;
            String str = (send + counter);
            writer.write(str.getBytes("UTF-8"));
            str_status += "[3] テキスト送信\n";

            reader = cSocket.getInputStream();
            size = reader.read(w);
            temp = new String(w, 0, size, "UTF-8");


            str_status += ">>>" + temp;

        } catch (IOException e) {
            return temp;
        }
        return temp;
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
            /*
            counter++;
            String str = (temp + counter);
            writer.write(str.getBytes("UTF-8"));*/

            String str = SendCmd_LA6();
            writer.write(str.getBytes("US-ASCII"));
            str_status += "送信>>>："+str+"\n";

        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /*********************************
     メッセージ受信
     *********************************/
    public String RecvMessage() {

        String temp = "";
        byte [] w = new byte[4048];
        int size;
        if (cSocket.isConnected() == false){
            return temp;
        }
        str_status += "[4] テキスト受信中です・・・\n";

        // テキスト受信
        try {
            reader = cSocket.getInputStream();
            size = reader.read(w);
            if (size <= 0)  {
                return temp;
            }
            else{
                temp = new String(w, 0, size, "UTF-8");
            }
            str_status += "受信<<<："+temp+"\n";

        } catch (IOException e) {
            return temp;
        }
        return temp;
    }

    /*
        LA6
     */
    public String SendCmd_LA6(){
        String tmp_cmd = "";
        byte [] w = new byte[128];
        w[0] = 0x0a;
        w[1] = 'L';
        w[2] = 'A';
        w[3] = '6';
        w[4] = 'L';
        w[5] = '0';
        w[6] = '0';
        w[7] = '1';
        w[8] = 'S';
        w[9] = 'S';
        w[10] = 0x0d;
        try {
            tmp_cmd = new String(w, 0, 11, "US-ASCII");
        }
        catch (IOException e){

        }
        return tmp_cmd;
    }


}
