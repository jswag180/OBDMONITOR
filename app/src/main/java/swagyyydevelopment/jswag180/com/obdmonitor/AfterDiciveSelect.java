package swagyyydevelopment.jswag180.com.obdmonitor;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.github.pires.obd.commands.fuel.FuelLevelCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.enums.ObdProtocols;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


public class AfterDiciveSelect extends AppCompatActivity {

    Button BUTTONDISCON, BGAGE, btnFule, btnTemps, btnshiftLight, btnZToS, btnGessGear, btnDigSet, btnDataLogger, btnFueling;
    String address = null;
    Context context = this;
    InputStream mmInStream = null;
    OutputStream mmOutStream = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    boolean disCon = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    Handler diagnosticHandler = new Handler();
    SharedPreferences sharedPref = null;
    NotificationCompat.Builder mBuilder = null;
    NotificationManager mNotificationManager = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_select);

        BUTTONDISCON = (Button) findViewById(R.id.BUTTONDISCON);
        btnshiftLight = (Button) findViewById(R.id.btnSiftLight);
        BGAGE = (Button) findViewById(R.id.BGAGE);
        btnFule = (Button) findViewById(R.id.btnFule);
        btnTemps = (Button) findViewById(R.id.btnTemp);
        btnZToS = (Button) findViewById(R.id.btnZToS);
        btnGessGear = (Button) findViewById(R.id.btnGessGear);
        btnDigSet = (Button) findViewById(R.id.btnDigSet);
        btnDataLogger = (Button) findViewById(R.id.btnDataLogger);
        btnFueling = (Button) findViewById(R.id.btnFueling);


        address = getIntent().getExtras().getString("EXTRA_ADDRESS");
        //Toast.makeText(getApplicationContext(), "Attempting to connect to " + address, Toast.LENGTH_SHORT).show();
        //new ConBluetooth().execute(getIntent().getExtras().getString("EXTRA_ADDRESS"));
        Intent intentConServise = new Intent(context, BtCon.class);
        //intentConServise.putExtra("EXTRA_ADDRESS",address);
        InfoWrapper.setAdress(address);
        startService(intentConServise);

        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        mBuilder = new NotificationCompat.Builder(this);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder.setDefaults(Notification.DEFAULT_ALL);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher);

        btSocket = Socket.getSocket();
        diagnosticHandler.postDelayed(diagnostic, 0);


        BUTTONDISCON.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Disconnect();
            }
        });

        btnFule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent x = new Intent(getApplicationContext(), FuleInfo.class);
                //Socket.setSocket(btSocket);
                startActivity(x);
            }
        });

        BGAGE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent z = new Intent(getApplicationContext(), gage.class);
                //Socket.setSocket(btSocket);
                z.putExtra("dev", "false");
                startActivity(z);
            }
        });

        btnTemps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent y = new Intent(getApplicationContext(), Temps.class);
                //Socket.setSocket(btSocket);
                //z.putExtra("dev","false");
                startActivity(y);
            }
        });

        btnshiftLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent a = new Intent(getApplicationContext(), ShiftLights.class);
                //Socket.setSocket(btSocket);
                startActivity(a);
            }
        });

        btnZToS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Socket.setSocket(btSocket);
                startActivity(new Intent(getApplicationContext(), zeroToSixty.class));
            }
        });

        btnGessGear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), GeussGear.class));
            }
        });

        btnDigSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), diagnosticSettings.class));
            }
        });

        btnDataLogger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), DataLogger.class));
            }
        });

        btnFueling.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Fueling.class));
            }
        });

    }

    public final Handler mHandler = new Handler() {

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
                case 2:
                    if (null != activity) {

                    }
                    break;

            }
        }
    };

    private void Disconnect() {
        if (btSocket != null) //If the btSocket is busy
        {
            try {
                btSocket.close(); //close connection
            } catch (IOException e) {
                Message msg = mHandler.obtainMessage(1);
                Bundle bundle = new Bundle();
                bundle.putString("toast", e.toString());
                msg.setData(bundle);
                mHandler.sendMessage(msg);
            }
        }
        disCon = true;
        finish(); //return to the first layout

    }

    public class ConBluetooth extends AsyncTask<String, String, String> {

        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = ProgressDialog.show(AfterDiciveSelect.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected String doInBackground(String... params) {

            try {
                if (btSocket == null || !isBtConnected) {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();
                }
            } catch (IOException e) {
                ConnectSuccess = false;
            }

            return null;

        }

        private void msg(String s) {
            Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);


            if (!ConnectSuccess) {
                msg("Connection Failed. Try again.");
                finish();
            } else {
                msg("Connected.");
                isBtConnected = true;

                try {
                    mmInStream = btSocket.getInputStream();
                    mmOutStream = btSocket.getOutputStream();
                } catch (IOException e) {

                }


            }
            //new ConnectedThread().execute("a");
            progress.dismiss();
        }
    }

    Runnable diagnostic = new Runnable() {
        @Override
        public void run() {

            btSocket = swagyyydevelopment.jswag180.com.obdmonitor.Socket.getSocket();

            try {
                mmInStream = btSocket.getInputStream();
                mmOutStream = btSocket.getOutputStream();
                new EchoOffCommand().run(mmInStream, mmOutStream);
                new LineFeedOffCommand().run(mmInStream, mmOutStream);
                new TimeoutCommand(125).run(mmInStream, mmOutStream);
                new SelectProtocolCommand(ObdProtocols.AUTO).run(mmInStream, mmOutStream);

                //if (sharedPref.getBoolean("FuleLVL",false)){

                FuelLevelCommand fuel;
                fuel = new FuelLevelCommand();
                fuel.run(mmInStream, mmOutStream);
                float d = fuel.getPercentage();
                //if (d <= sharedPref.getFloat("FuelAlertLVL",0)){
                mBuilder.setContentTitle("Warning fuel level low");
                mBuilder.setContentText("Fuel level is : " + (int) d);
                mNotificationManager.notify(1334, mBuilder.build());

                //}
                // }

                /*if(sharedPref.getBoolean("OilChangeAlert",false)){
                    BatteryVoltzCommand dis;
                    dis = new BatteryVoltzCommand();
                    dis.run(mmInStream,mmOutStream);
                    Message msg = mHandler.obtainMessage(1);
                    Bundle bundle = new Bundle();
                    bundle.putString("toast",  String.valueOf( dis.getCalculatedResult()));
                    msg.setData(bundle);
                    mHandler.sendMessage(msg);
                }*/

            } catch (Exception e) {
                Message msg = mHandler.obtainMessage(1);
                Bundle bundle = new Bundle();
                bundle.putString("toast", e.toString());
                msg.setData(bundle);
                mHandler.sendMessage(msg);
            }

        }
    };

}
