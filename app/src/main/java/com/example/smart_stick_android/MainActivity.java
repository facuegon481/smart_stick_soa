package com.example.smart_stick_android;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.UUID;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    /* Umbrales para el acelerometro*/
    static final float UMBRAL_ACELEROMETRO_MENOR_DESDE = -2;
    static final float UMBRAL_ACELEROMETRO_MENOR_HASTA = 2;

    static final float UMBRAL_ACELEROMETRO_MAYOR_DESDE = 3;
    static final float UMBRAL_ACELEROMETRO_MAYOR_HASTA = 11;

    Button btnDistancia;
    Button btnBuzzer;
    EditText txtDistancia;

    BluetoothService bluetoothService = new BluetoothService();
    boolean isConnected = false;
    boolean isLandscape_init = false;

    /* Sensores */
    private SensorManager sensorManager;
    private Sensor sensor;
    /* Fin sensores */
    private String orientacion = "portrait";

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            BluetoothService.MyLocalBinder binder = (BluetoothService.MyLocalBinder) iBinder;
            bluetoothService = binder.getBoundService();
            isConnected = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isConnected = false;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Seteamos los valores para vista
        btnDistancia = (Button) findViewById(R.id.btnDistancia);
        btnBuzzer = (Button) findViewById(R.id.btnBuzzer);
        txtDistancia = (EditText) findViewById(R.id.txtDistancia);

        //defino los handlers para los botones Apagar y encender
        btnDistancia.setOnClickListener(btnDistanciaListener);
        btnBuzzer.setOnClickListener(btnBuzzerListener);

        Intent intentService = new Intent(this, BluetoothService.class);
        bindService(intentService, serviceConnection, Context.BIND_AUTO_CREATE);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(MainActivity.this, sensor, sensorManager.SENSOR_DELAY_UI);
    }

    private View.OnClickListener btnBuzzerListener = new View.OnClickListener(){

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(MainActivity.this, BuzzerActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    };
    private View.OnClickListener btnDistanciaListener = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            try {
                bluetoothService.write("D");
                txtDistancia.setText(bluetoothService.getDistancia() + " cm");

            } catch (Exception e) {
                Log.i("err3", e.getMessage());
            }
        }
    };

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        float x = sensorEvent.values[0];
        float y = sensorEvent.values[1];
        float z = sensorEvent.values[2];

        if(!isLandscape_init && (y >= UMBRAL_ACELEROMETRO_MENOR_DESDE && y <= UMBRAL_ACELEROMETRO_MENOR_HASTA)
                && (x >= UMBRAL_ACELEROMETRO_MAYOR_DESDE && x <= UMBRAL_ACELEROMETRO_MAYOR_HASTA) ){

            // Realiza un intent para iniciar la siguiente actividad
            isLandscape_init = true;

            Intent intent = new Intent(MainActivity.this, LandscapeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

}

