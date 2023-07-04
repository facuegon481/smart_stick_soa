package com.example.smart_stick_android;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class DispositivosVinculadosActivity extends AppCompatActivity
{
    private static final String TAG = "DispositivosVinculados";
    ListView devicesList;
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter mPairedDevicesArrayAdapter;

    private DispositivosVinculadosActivity _this;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dispositivos_vinculados);
        _this = this;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        VerificarEstadoBT();
        mPairedDevicesArrayAdapter = new ArrayAdapter(this, R.layout.dispositivos_encontrados);
        devicesList = (ListView) findViewById(R.id.devicesList);
        devicesList.setAdapter(mPairedDevicesArrayAdapter);
        devicesList.setOnItemClickListener(mDeviceClickListener);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        try
        {
            Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

            if (pairedDevices.size() > 0)
            {
                for (BluetoothDevice device : pairedDevices)
                {
                    mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            }
        }
        catch(SecurityException e)
        {
            Log.e("err2", e.getMessage());
        }

    }


    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener()
    {
        public void onItemClick(AdapterView av, View v, int arg2, long arg3)
        {
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);
            finishAffinity();

            //
            Intent intentService = new Intent(_this, BluetoothService.class);
            intentService.putExtra(EXTRA_DEVICE_ADDRESS, address);
            startService(intentService);

            // Realiza un intent para iniciar la siguiente actividad
            Intent intent = new Intent(DispositivosVinculadosActivity.this, MainActivity.class);
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
            startActivity(intent);
        }
    };

    private void VerificarEstadoBT()
    {
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null)
        {
            Toast.makeText(getBaseContext(), "El dispositivo no soporta Bluetooth", Toast.LENGTH_SHORT).show();
        } else
        {
            if (mBtAdapter.isEnabled())
            {
                Log.d(TAG, "...Bluetooth Activado...");
            } else
            {
                //Solicita al usuario que active Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                try
                {
                    startActivityForResult(enableBtIntent, 1);
                }
                catch(SecurityException ex)
                {
                    Log.e("err2", ex.getMessage());
                }
            }
        }
    }
}