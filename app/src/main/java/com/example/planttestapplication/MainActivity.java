package com.example.planttestapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import java.util.Arrays;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.serialportlibrary.service.impl.SerialPortBuilder;
import com.serialportlibrary.service.impl.SerialPortService;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static boolean sendFlag;
    SerialPort myPort = new SerialPort();
    Button open, send, close;
    TextView receivetext;
    EditText sendtext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        open = (Button) findViewById(R.id.open);
        send = (Button) findViewById(R.id.send);
        close = (Button) findViewById(R.id.close);
        receivetext = (TextView) findViewById(R.id.receivetext);
        sendtext = (EditText) findViewById(R.id.sendtext);

        send.setOnClickListener(this);
        open.setOnClickListener(this);
        close.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        if (v == open) {
            myPort.startPort();
            myPort.receiveMesage();
        }
        if (v == send) {
            sendFlag = true;
            String str = sendtext.getText().toString();
            myPort.sendMessage(str);
        }
        if (v == close) {
            sendFlag = false;
            myPort.closePort();
        }
    }

    class SerialPort {
        private byte[] mBuffer;
        private Handler handler;
        SerialPortService serialPortService;

        //开启串口
        public void startPort() {
            serialPortService = new SerialPortBuilder()
                    .setTimeOut(100L)
                    .setBaudrate(115200)
                    .setDevicePath("dev/ttyMT3")
                    .createService();

            serialPortService.isOutputLog(true);
        }

        public void sendMessage(final String receiveString) {
            //发数据的线程
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (sendFlag) {
                        byte[] sendData = receiveString.getBytes();
                        try {
                            serialPortService.sendData(sendData);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }

        public void receiveMesage() {

            handler = new Handler(); //创建主线程的handler  用于接收数据时更新UI
            serialPortService.setOnDataReceiveListener(new SerialPortService.OnDataReceiveListener() {
                @Override
                public void onDataReceive(byte[] buffer, int size) {
                    Log.d("tag", "进入数据监听事件 " + new String(buffer));
                    mBuffer = buffer;
                    handler.post(runnable);//利用handler将数据传递到主线程

                }

                //开线程更新UI
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        //String mString=ByteStringUtil.byteArrayToHexStr(mBuffer);
                        String myString = new String(mBuffer);
                        //String myStrings=ByteStringUtil.convertbyteToASCII(mBuffer);
                        byte[] myByte = new byte[]{0x6, '2'};
                        //如果byte[]里面是十六进制数，则会转化成十进制，如果是字符则会转成十进制的ASCII码的值，其实都是二进制保存的
                        Log.e("tag", Arrays.toString(myByte) + myByte.length);
                        Log.e("tag", Arrays.toString(mBuffer) + mBuffer.length);//遍历打印：[35, 52, 69]，打印的是10进制数据
                        //也就是如果按照这个值转成字符串的话，会对应着转成这个值的ascii码对应的字符
                        receivetext.setText("size：" + (mBuffer.length - 1) + "\n" + "数据监听：" + myString);
                    }
                };
            });
        }

        public void closePort() {
            serialPortService.close();
            sendFlag = false;//关闭串口以后还要把发送数据标志位置false，不然它还会发数据，而这个时候串口已经关闭了。。。

        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        myPort.closePort();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


}

