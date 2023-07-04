package com.example.smart_stick_android;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class BluetoothService extends Service
{
    static final UUID mUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    final int handlerState = 0;
    private ConnectedThread mConnectedThread;
    private String distancia = "";
    private boolean buzzerActivado = false;
    private Timer timer;

    Handler bluetoothIn;
    BluetoothSocket btSocket;

    MyLocalBinder localBinder = new MyLocalBinder();

    public void polling()
    {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
             write("D");
            }
        },0,5000);
    }

    public void stop_polling()
    {
        timer.cancel();
    }

    public void write(String data)
    {
        this.mConnectedThread.write(data);
    }
    public String getDistancia()
    {
        return this.distancia;
    }

    public boolean getBuzzerActivado()
    {
        return this.buzzerActivado;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startid)
    {
        bluetoothIn = Handler_Msg_Hilo_Principal();
        Bundle addressExtra = intent.getExtras();
        String address = (String) addressExtra.get(DispositivosVinculadosActivity.EXTRA_DEVICE_ADDRESS);

        try
        {
            BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

            BluetoothDevice device = btAdapter.getRemoteDevice(address);
            btSocket = device.createRfcommSocketToServiceRecord(mUUID);
            btSocket.connect();

            mConnectedThread = new ConnectedThread(btSocket);
            mConnectedThread.start();

        }
        catch (SecurityException ex)
        {
            Log.i("err", ex.getMessage());
        }
        catch (IOException e)
        {
            Log.i("err2", e.getMessage());
        }

        return super.onStartCommand(intent, flags, startid);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return localBinder;
    }

    private Handler Handler_Msg_Hilo_Principal ()
    {
        return new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState)
                {
                    String readMessage = (String) msg.obj;
                    String[] comando_completo = readMessage.split("_");

                    if(comando_completo.length == 2)
                    {
                        String evento = comando_completo[0];
                        String valor = comando_completo[1];

                        switch (evento){
                            case "D":
                                distancia = valor;
                                break;
                            case "E":
                                buzzerActivado = Boolean.parseBoolean(valor);
                                break;
                            default:
                                Log.i("BluetoothService","Evento inválido");
                                break;
                        }

                    }
                    else
                    {
                        Log.i("BluetoothService","invalid command");
                    }
                }
            }
        };
    }

    private class ConnectedThread extends Thread
    {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket)
        {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try
            {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            }
            catch (IOException e)
            {

            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run()
        {
            byte[] buffer = new byte[256];
            int bytes;

            while (true)
            {
                try
                {
                    bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);

                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e)
                {
                    break;
                }
            }
        }


        //write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();
            try {
                mmOutStream.write(msgBuffer);
            } catch (IOException e) {
                //if you cannot write, close the application
                Log.e("error tread", "conexion falló");
            }
        }
    }

    public class MyLocalBinder extends Binder{

        BluetoothService getBoundService()
        {
            return BluetoothService.this;
        }
    }
}


