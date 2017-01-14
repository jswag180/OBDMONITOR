package swagyyydevelopment.jswag180.com.obdmonitor;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.temperature.AmbientAirTemperatureCommand;
import com.github.pires.obd.commands.temperature.EngineCoolantTemperatureCommand;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.UUID;


public class AfterDiciveSelect extends AppCompatActivity{

    Button BUTTONSENDMSG,BUTTONDISCON,button2;
    EditText EDITTEXTINPUT;
    TextView TEXTVEIWRESULT;
    String address = null;
    String response = "test";
    InputStream mmInStream = null;
    OutputStream mmOutStream= null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    boolean disCon = false;
    boolean suc = true;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");//UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")  UUID.randomUUID()

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_select);

        BUTTONSENDMSG = (Button) findViewById(R.id.BUTTONSENDMSG);
        BUTTONDISCON = (Button) findViewById(R.id.BUTTONDISCON);
        EDITTEXTINPUT = (EditText) findViewById(R.id.EDITTEXTINPUT);
        TEXTVEIWRESULT = (TextView)findViewById(R.id.TEXTVIEWRESULT);
        button2 = (Button) findViewById(R.id.button2);


        address = getIntent().getExtras().getString("EXTRA_ADDRESS");
        Toast.makeText(getApplicationContext(),"Attempting to connect to " + address,Toast.LENGTH_SHORT).show();
        new ConBluetooth().execute(getIntent().getExtras().getString("EXTRA_ADDRESS"));



        BUTTONSENDMSG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //sendMsg("01 0C /r",true);

                sendMsg(EDITTEXTINPUT.getText().toString(),false);

            }
        });

        BUTTONDISCON.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Disconnect();
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EngineCoolantTemperatureCommand i;
                try {
                    i = new EngineCoolantTemperatureCommand();
                    i.run(mmInStream,mmOutStream);
                    TEXTVEIWRESULT.setText((int) i.getImperialUnit());//TODO play around with no using this command and how to get data from it

                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.toString(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public final Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Context activity = getApplicationContext();
            switch (msg.what) {

                case 1:
                    if (null != activity) {
                        Toast.makeText(activity, msg.getData().getString("toast"),
                                Toast.LENGTH_SHORT).show();

                    }
                    break;

            }
        }
    };

    public void sendMsg (String st , Boolean hasResponse){
        try {
            mmOutStream.write(st.getBytes());
            if(hasResponse){waitForResponse();}
        }catch (Exception e){

        }
    }

    public void waitForResponse (){

        try {
            byte[] b = new byte[1024];
            mmInStream.read(b);
            String s1 = new String(b);
            Toast.makeText(getApplicationContext(), s1,Toast.LENGTH_LONG).show();

        }catch (Exception e){

        }

    }

    private void Disconnect()
    {
        if (btSocket!=null) //If the btSocket is busy
        {
            try
            {
                btSocket.close(); //close connection
            }
            catch (IOException e)
            {  }
        }
        disCon = true;
        finish(); //return to the first layout

    }


    public class ConBluetooth extends AsyncTask<String,String,String> {

        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = ProgressDialog.show(AfterDiciveSelect.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected String doInBackground(String... params) {

            try
            {
                if (btSocket == null || !isBtConnected)
                {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;//if the try failed, you can check the exception here
                //Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_LONG).show();
            }

            return null;

        }

        private void msg(String s)
        {
            Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
        }



        @Override
        protected void onPostExecute(String s) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(s);


            if (!ConnectSuccess)
            {
                msg("Connection Failed. Try again.");
                finish();
            }
            else
            {
                msg("Connected.");
                isBtConnected = true;

                try {
                    mmInStream = btSocket.getInputStream();
                    mmOutStream = btSocket.getOutputStream();
                }catch (IOException e){

                }



            }
            new ConnectedThread().execute("a");
            progress.dismiss();
        }
    }

    private class ConnectedThread extends AsyncTask<String,String,String> {



        @Override
        protected String doInBackground(String... params) {

            //mmBuffer = new byte[1024];
            //int numBytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            //Toast.makeText(getApplicationContext(), "RUNNING RUNNING", Toast.LENGTH_SHORT).show();
            //TEXTVEIWRESULT.setText("RUNNING RUNNING");
            String s1 = "1";
            while (true) {

                try {
                    // Read from the InputStream.
                    byte[] b = new byte[1024];
                    mmInStream.read(b);
                    s1 = new String(b);
                    response = s1;
                    Message msg = mHandler.obtainMessage(1);
                    Bundle bundle = new Bundle();
                    bundle.putString("toast", s1);
                    msg.setData(bundle);
                    mHandler.sendMessage(msg);
                    //Toast.makeText(getApplicationContext(), s1, Toast.LENGTH_SHORT).show();
                    //Toasty(s1);
                } catch (IOException e) {
                    //Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                    //Toasty(e.toString());
                    break;
                }


            }

            return s1;
        }
    }

    }




