package com.example.smart_stick_android;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Image;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Timer;
import java.util.TimerTask;

public class LandscapeActivity extends AppCompatActivity implements SensorEventListener
{
    static final float UMBRAL_ACELEROMETRO_MENOR_DESDE = -2;
    static final float UMBRAL_ACELEROMETRO_MENOR_HASTA = 2;

    static final float UMBRAL_ACELEROMETRO_MAYOR_DESDE = 3;
    static final float UMBRAL_ACELEROMETRO_MAYOR_HASTA = 11;

    BluetoothService bluetoothService = new BluetoothService();
    boolean isConnected = false;
    boolean isPortrait = false;

    float distancia;

    private SensorManager sensorManager;
    private Sensor sensor;

    private Thread actualizarDistanciaThread ;

    EditText txtDistancia;
    private Timer timer;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landscape);
        txtDistancia = (EditText) findViewById(R.id.txtDistancia);

        Intent intentService = new Intent(this, BluetoothService.class);
        bindService(intentService, serviceConnection, Context.BIND_AUTO_CREATE);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(LandscapeActivity.this, sensor, sensorManager.SENSOR_DELAY_UI);

        actualizarDistanciaThread = new Thread(new ActualizarDistanciaThread());
        actualizarDistanciaThread.start();

    }
    @Override
    public void onSensorChanged(SensorEvent sensorEvent)
    {
        float x = sensorEvent.values[0];
        float y = sensorEvent.values[1];
        float z = sensorEvent.values[2];

        if(!isPortrait && (x >= UMBRAL_ACELEROMETRO_MENOR_DESDE && x <= UMBRAL_ACELEROMETRO_MENOR_HASTA)
                && (y >= UMBRAL_ACELEROMETRO_MAYOR_DESDE && y <= UMBRAL_ACELEROMETRO_MAYOR_HASTA))
        {
            isPortrait = true;
            Intent intent = new Intent(LandscapeActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i)
    {

    }

    private ServiceConnection serviceConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder)
        {
            BluetoothService.MyLocalBinder binder = (BluetoothService.MyLocalBinder) iBinder;
            bluetoothService = binder.getBoundService();
            isConnected = true;
            bluetoothService.polling();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName)
        {
            isConnected = false;
        }
    };

    public class ActualizarDistanciaThread implements Runnable
    {
        String distancia;
        public ActualizarDistanciaThread()
        {

        }
        @Override
        public void run()
        {
            while(true)
            {
                distancia = bluetoothService.getDistancia() + " cm";
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        txtDistancia.setText(distancia);
                    }
                });
                try
                {
                    Thread.sleep(2000);
                }
                catch (InterruptedException e)
                {
                    throw new RuntimeException(e);
                }
            }

        }
    }
}
