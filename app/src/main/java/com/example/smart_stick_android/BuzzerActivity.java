package com.example.smart_stick_android;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class BuzzerActivity extends AppCompatActivity {

    private BluetoothService bluetoothService = new BluetoothService();
    private boolean isConnected = false; // checkear si sirve eso, que utilidad darle al flag
    private Button buzzerActionButton;
    private EditText estadoBuzzerTxt;
    private boolean buzzerActivado = false;

    protected Button btnVolver;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            BluetoothService.MyLocalBinder binder = (BluetoothService.MyLocalBinder) iBinder;
            bluetoothService = binder.getBoundService();
            isConnected = true;
            getBuzzerEstado();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isConnected = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.buzzer);

        Intent intentService = new Intent(this, BluetoothService.class);
        bindService(intentService, serviceConnection, Context.BIND_AUTO_CREATE);

        buzzerActionButton = findViewById(R.id.btnBuzzerAction);
        estadoBuzzerTxt = findViewById((R.id.txtEstadoBuzzer));

        btnVolver = findViewById(R.id.btnVolver);

        buzzerActionButton.setOnClickListener(btnBuzzerActionListener);
        btnVolver.setOnClickListener(btnVolverListener);

        //bluetoothService.write("E");
    }
    public void getBuzzerEstado(){
        bluetoothService.write("E"); // hacer el comando E en arduino para que responda con el estado del buzzer
        buzzerActivado = bluetoothService.getBuzzerActivado();
        String estado = buzzerActivado? "Sonando" : "Apagado";
        estadoBuzzerTxt.setText(estado);
        cambiarButtonText();
    }
    private void cambiarButtonText(){
        if(buzzerActivado){
            buzzerActionButton.setText("Apagar buzzer");
        }else {
            buzzerActionButton.setText("Encender buzzer");
        }
    }

    private View.OnClickListener btnVolverListener = new View.OnClickListener(){

        @Override
        public void onClick(View view) {
            finish();
        }
    };
    private View.OnClickListener btnBuzzerActionListener = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            try {
                if(buzzerActivado){
                    bluetoothService.write("F"); // en arduino hacer que con F apague el buzzer
                }else {
                    bluetoothService.write("G"); // en arduino hacer que con G prenda el buzzer
                }

                buzzerActivado = !buzzerActivado;
                String estado = buzzerActivado ? "Sonando" : "Apagado";
                estadoBuzzerTxt.setText(estado);

                cambiarButtonText();
            } catch (Exception e) {
                Log.i("err3", e.getMessage());
            }
        }
    };
}
