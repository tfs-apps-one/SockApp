package tfsapps.sockapp;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private MyServer mySvr;
    private MyClient myClt;

    private TextView message;
    private RadioButton rbtn_Svr;
    private RadioButton rbtn_Clt;
    private EditText inp_IpAdress;
    private Button btn_Start;

    private int mode;
    private Context context;

    private boolean taskrun = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        message = (TextView) findViewById(R.id.message);
        rbtn_Svr = (RadioButton) findViewById(R.id.radio_server);
        rbtn_Clt = (RadioButton) findViewById(R.id.radio_client);
        inp_IpAdress = (EditText) findViewById(R.id.input_ipAdress);
    }

    /* ラジオボタン：ソケットサーバー　モード[ON] */
    public void onRbtn_Server(View view)
    {
        mode = 0;
        rbtn_Svr.setChecked(true);
        rbtn_Clt.setChecked(false);
    }
    /* ラジオボタン：ソケットクライアント　モード[ON] */
    public void onRbtn_Client(View view)
    {
        mode = 1;
        rbtn_Svr.setChecked(false);
        rbtn_Clt.setChecked(true);
    }
    /* ボタン：モード強制終了 */
    public void onStop(View view) {
        taskrun = false;
        message.setText("初期状態 .. .. .. ");
    }
    /* ボタン：スタート */
    public void onStart(View view) {

        if (mode == 0){
            ServerStart();
        }else{
            ClientStart();
        }
    }
    /* ボタン：PING */
    public void onPing(View v){
        String ipadrr = inp_IpAdress.getText().toString();
        String tempcmd = "ping -c 5 " + ipadrr;
        Thread thread = new Thread(){
            public void run() {
                message.setText("---PING START---");
                Runtime runtime = Runtime.getRuntime();
                Process proc = null;
                try{
                    if (ipadrr.isEmpty()) {
                        proc = runtime.exec("ping -c 5 192.168.1.17");
                    }
                    else {
                        proc = runtime.exec(tempcmd);
                    }
                    proc.waitFor();
                }catch(Exception e){}
                int exitVal = proc.exitValue();
                if(exitVal == 0){
                    message.setText("-->PING OK ");
                }
                else{
                    message.setText("-->PING NG ");
                }
            }
        };
        thread.start();
    }

    /*
        ソケットクライアント　モード処理
     */
    public void ClientStart(){
        String tmpAddress = inp_IpAdress.getText().toString();
        taskrun = true;

        if (myClt == null){
            myClt = new MyClient(tmpAddress);
        }

        /* 専用スレッド起動 */
        Thread thread = new Thread(){
            public void run() {
                int step = 0;
                int retry = 0;

                try {
                    while (taskrun) {
                        switch (step) {
                            case 0: //接続待ち
                                if (myClt.Connect()) {
                                    step = 1;
                                }
                                break;
                            case 1: //送信
                                if (retry > 5) {
                                    myClt.DisConnect();
                                    retry = 0;
                                    step = 0;
                                }
                                else {
                                    if (myClt.SendMessage("テストメッセージ")) {
                                        step = 2;
                                    } else {
                                        retry++;
                                    }
                                }
                                break;
                            case 2: //受信
                                if (retry > 5) {
                                    myClt.DisConnect();
                                    retry = 0;
                                    step = 0;
                                }
                                else {
                                    if (myClt.RecvMessage().isEmpty() == false) {
                                        step = 1;
                                        retry = 0;
                                    } else {
                                        retry++;
                                    }
                                }
                                break;
                        }
                        //画面更新
                        message.setText(myClt.str_status);
                        sleep(300);
                    }

                    // loop 抜ける
                    myClt.DisConnect();
                    myClt = null;

                }catch(Exception e){
                    return;
                }
            }
        };
        thread.start();
    }

    /*
        ソケットサーバー　モード処理
     */
    public void ServerStart() {

        taskrun = true;

        if (mySvr == null){
            mySvr = new MyServer();
        }

        /* 専用スレッド起動 */
        Thread thread = new Thread(){
            public void run() {
                int step = 0;
                int retry = 0;

                try {
                    while (taskrun) {
                        switch (step) {
                            case 0: //接続待ち
                                if (mySvr.Accept(2002) ){
                                    step = 1;
                                }
                                break;
                            case 1: //受信
                                if (mySvr.RecvMessage()) {
                                    step = 2;
                                }
                                break;
                            case 2: //送信
                                if (mySvr.SendMessage()) {
                                    step = 1;
                                }
                                break;
                        }
                        message.setText(mySvr.recv_mess);
                        sleep(100);
                    }

                    // loop 抜ける
                    mySvr.DisConnect();
                    mySvr = null;

                }catch(Exception e){
                    return;
                }
            }
        };
        thread.start();
    }
}