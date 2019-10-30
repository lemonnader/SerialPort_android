package com.serialportlibrary.service.impl;

import android.os.SystemClock;

import android.serialport.SerialPort;
import android.util.Log;

import com.serialportlibrary.service.ISerialPortService;
import com.serialportlibrary.util.ByteStringUtil;
import com.serialportlibrary.util.LogUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
public class SerialPortService  {

    public  boolean portStatus;//串口占用状态
    public boolean threadStatus;//线程状态
    private OutputStream outputStream;
    private InputStream inputStream;
    /**
     * 尝试读取数据间隔时间
     */
    private static int RE_READ_WAITE_TIME = 10;

    /**
     * 读取返回结果超时时间
     */
    private Long mTimeOut = 100L;
    /**
     * 串口地址
     */
    private String mDevicePath;

    /**
     * 波特率
     */
    private int mBaudrate;

    SerialPort mSerialPort;

    /**
     * 初始化串口
     *
     * @param devicePath 串口地址
     * @param baudrate   波特率
     * @param timeOut    数据返回超时时间
     * @throws IOException 打开串口出错
     */

    //创建一个SerialPortServic同时打开对应的串口
    public SerialPortService(String devicePath, int baudrate, Long timeOut) throws IOException {
        mTimeOut = timeOut;
        mDevicePath = devicePath;
        mBaudrate = baudrate;
        mSerialPort = new SerialPort(new File(mDevicePath), mBaudrate);//此处就是打开串口
        this.portStatus=true;//串口打开
        threadStatus=true;//接收数据的线程标志打开
        new ReadThread().start();//接收数据的线程打开

}

//发送数据
    public byte[] sendData(byte[] data) {
        synchronized (SerialPortService.this) {
            try {
                outputStream = mSerialPort.getOutputStream();
                outputStream.write(data);
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    /**
     * 单开一线程，来读数据
     */
    private class ReadThread extends Thread{
        @Override
        public void run() {
            super.run();
            //判断进程是否打开
            while (threadStatus){
                Log.d("tag", "进入线程run");
                if (Thread.currentThread().isInterrupted()) {
                    break;
                }//检查是否有线程冲突
                byte[] buffer ;
                int size; //读取数据的大小
                try {
                    inputStream = mSerialPort.getInputStream();
                    if (inputStream == null) {
                        return;
                    }
                    int available = inputStream.available();//得到接收数据的实际字节数
                    buffer=new byte[available];//数组长度由实际接收到的长度确定
                    size = inputStream.read(buffer);
                    if (size > 0){
                        Log.d("tag", "run: 接收到了数据：" + ByteStringUtil.byteArrayToHexStr(buffer));
                        onDataReceiveListener.onDataReceive(buffer,size);

                    }
                    SystemClock.sleep(RE_READ_WAITE_TIME);//让线程睡一会，不用一直读
                } catch (IOException e) {
                    Log.e("tag", "run: 数据读取异常：" +e.toString());
                }
            }

        }
    }

    //这是写了一监听器来监听接收数据
    public OnDataReceiveListener onDataReceiveListener = null;
    public  interface OnDataReceiveListener {
         void onDataReceive(byte[] buffer, int size);
    }//在MainActivity中进行实现
    public void setOnDataReceiveListener(OnDataReceiveListener dataReceiveListener) {
        onDataReceiveListener = dataReceiveListener;
    }

    public byte[] sendData(String date) {
        try {
            return sendData(ByteStringUtil.hexStrToByteArray(date));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void close() {
        if (mSerialPort != null) {
            mSerialPort.closePort();
            this.outputStream=null;
            this.inputStream=null;
            this.portStatus=false;//关闭串口
            threadStatus=false;//关闭线程
        }
    }


    public void isOutputLog(boolean debug) {
        LogUtil.isDebug = debug;
    }//？？应该没啥用


}
