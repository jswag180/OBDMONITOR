package swagyyydevelopment.jswag180.com.obdmonitor;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class OBD_MONITORING_ACTIVITY extends AppCompatActivity {
    private BluetoothAdapter myBluetooth = null;
    private Set pairedDevices;

    Button BUTTONGETPAIRED,button;
    Spinner SPINNERBD;
    Button BUTTONSLECT;
    Intent i = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_obd__monitoring__activity);

        BUTTONGETPAIRED = (Button) findViewById(R.id.BUTTONGETPAIRED);
        SPINNERBD = (Spinner) findViewById(R.id.SPPINERBD);
        BUTTONSLECT = (Button) findViewById(R.id.BUTTONSLECT);
        button = (Button) findViewById(R.id.button);

        i = new Intent(getApplicationContext(), AfterDiciveSelect.class);

        myBluetooth = BluetoothAdapter.getDefaultAdapter();

        if(myBluetooth == null)
        {
            //Show a mensag. that thedevice has no bluetooth adapter
            Toast.makeText(getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();
            //finish apk
            finish();
        }
        else
        {
            if (myBluetooth.isEnabled())
            { }
            else
            {
                //Ask to the user turn the bluetooth on
                Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(turnBTon,1);
            }
        }

        BUTTONGETPAIRED.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pairedDevicesList();
            }
        });

        BUTTONSLECT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(i);
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),gage.class));
            }
        });

    }//16

    private void pairedDevicesList()
    {
        Set<BluetoothDevice> pairedDevices = myBluetooth.getBondedDevices();
        ArrayList list = new ArrayList();

        if (pairedDevices.size()>0)
        {
            for(BluetoothDevice bt : pairedDevices)// BluetoothDevice bt : pairedDevices  int i = 0; i < pairedDevices.size(); i++
            {
                //BluetoothDevice bt = pairedDevices[i];
                list.add(bt.getName() + "\n" + bt.getAddress()); //Get the device's name and the address
            }
        }
        else
        {
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
        }

        final ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, list);
        SPINNERBD.setAdapter(adapter);
        //SPINNERBD.setOnItemClickListener(myListClickListener); //Method called when the device from the list is clicked
        SPINNERBD.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get the device MAC address, the last 17 chars in the View
                String info = ((TextView) view).getText().toString();
                String address = info.substring(info.length() - 17);
                // Make an intent to start next activity.

                //Change the activity.
                i.putExtra("EXTRA_ADDRESS", address); //this will be received at ledControl (class) Activity
                BUTTONSLECT.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }
}
