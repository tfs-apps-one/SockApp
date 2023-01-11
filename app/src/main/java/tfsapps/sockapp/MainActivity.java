package tfsapps.sockapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private MyServer mySvr;
    private MyClient myClt;
    private TextView status;
    private TextView message;
    private TextView history;
    private RadioButton rbtn_Svr;
    private RadioButton rbtn_Clt;
    private EditText inp_IpAdress;

    private int mode;
    private boolean taskrun = false;

    private boolean sv_recv_wait = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        status = (TextView) findViewById(R.id.status);
        message = (TextView) findViewById(R.id.message);
        history = (TextView) findViewById(R.id.history);

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
        LA6　機械番号通知（PC→AL、AL→PC）　※起動局がPC、サーバーとの通信開始コマンド
        nA1　状態通知（AL→PC）　　　　　　　※応答なし
        iA0　エラー通知（AL→PC、PC→AL）　　
        vA0　実績通知（AL→PC、PC→AL）　
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
                sv_recv_wait = false;
                try {
                    while (taskrun) {
                        switch (step) {
                            case 0: //接続待ち
                                if (myClt.Connect()) {
                                    step = 2;
                                }
                                break;
                                /*
                            case 1: //器物確認（LA6）
                                if (retry > 5) {
                                    myClt.DisConnect();
                                    retry = 0;  step = 0;
                                }
                                else {
                                    if (sv_recv_wait == false) {
                                        if (myClt.SendMessage("LA6")) {
                                            sv_recv_wait = true;
                                            step = 1;
                                        } else {
                                            retry++;
                                        }
                                    }
                                    else{
                                        if(myClt.RecvMessage() == true){
                                            if (myClt.recv_mess.indexOf("LA6") != -1) {
                                                sv_recv_wait = true;
                                                step = 2;
                                            }
                                        }
                                        else{
                                            retry++;
                                        }
                                    }
                                }
                                break;

                                 */
                            case 2: //上記以外
                                if (retry > 5) {
                                    myClt.DisConnect();
                                    retry = 0;  step = 0;
                                }
                                else {
                                    if (myClt.RecvMessage() == true) {
                                        //  状態通知（ポーリング受信）
                                        if (myClt.recv_mess.indexOf("nA1")  != -1){
                                            myClt.RecvCmd_nA1();
                                            /* 画面更新だけ行う */
                                            status.setText(myClt.now_machine+"号機\n"+myClt.now_status);
                                        }
                                        //  エラー通知
                                        else if (myClt.recv_mess.indexOf("iA0")  != -1){
                                            /* エラー解析と表示処理 */
                                            if (myClt.RecvCmd_iA0()){
                                                message.setText(myClt.now_error_id+"\n"+myClt.now_error_mess);
                                            }
                                            if (myClt.SendMessage("iA0")) {
                                                step = 2;
                                            } else {
                                                retry++;
                                            }
                                        }
                                    }
                                }
                                break;
                        }
                        //画面更新
//                        message.setText(myClt.str_status);
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