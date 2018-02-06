package swagyyydevelopment.jswag180.com.obdmonitor;


import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.enums.ObdProtocols;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class gage extends AppCompatActivity{

    ImageView j;
    TextView tvRpm;
    BluetoothSocket socket;
    InputStream mmInStream = null;
    OutputStream mmOutStream = null;
    Handler graberHandler = new Handler();
    Context context = this;
    RPMCommand i = new RPMCommand();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gage);
        ImageView i = (ImageView) findViewById(R.id.IMVTEST);
        j = (ImageView) findViewById(R.id.IMVNED);
        tvRpm = (TextView) findViewById(R.id.tvRpm);
        //if(!(getIntent().getExtras().get("dev") == "true")) {
        socket = swagyyydevelopment.jswag180.com.obdmonitor.Socket.getSocket();
        try {

            mmInStream = socket.getInputStream();
            mmOutStream = socket.getOutputStream();

        } catch (Exception e) {
            e.printStackTrace();
        }
        //new infoGrab().execute("");
        try {
            new EchoOffCommand().run(mmInStream, mmOutStream);
            new LineFeedOffCommand().run(mmInStream, mmOutStream);
            new TimeoutCommand(125).run(mmInStream, mmOutStream);
            new SelectProtocolCommand(ObdProtocols.AUTO).run(mmInStream, mmOutStream);
            graberHandler.postDelayed(obdCom, 0);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //}
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
                case 3:
                    if (null != activity) {
                        j.setRotation((msg.getData().getFloat("ROT")));
                        tvRpm.setText(msg.getData().getString("RPM"));//msg.getData().getInt("RPM")
                    }
                    break;

            }
        }
    };

    Runnable obdCom = new Runnable() {
        @Override
        public void run() {
            synchronized (this) {
                try {

                    i.run(mmInStream, mmOutStream);
                    int d = i.getRPM();
                    String unitToString = Integer.toString(d);
                    Message msg = mHandler.obtainMessage(3);
                    Bundle bundle = new Bundle();
                    bundle.putFloat("ROT", scale(d, 0, 7000, -90, 90));
                    bundle.putString("RPM", unitToString);
                    msg.setData(bundle);
                    mHandler.sendMessage(msg);

                } catch (IOException | InterruptedException e) {
                    //sendToast(e.toString());
                    graberHandler.removeCallbacks(obdCom);
                    Intent intentConServise = new Intent(context, BtCon.class);
                    startService(intentConServise);
                    socket = swagyyydevelopment.jswag180.com.obdmonitor.Socket.getSocket();
                    graberHandler.postDelayed(obdCom, 0);
                }
                graberHandler.postDelayed(this, 0);
            }
        }
    };

    public float scale(final float valueIn, final float baseMin, final float baseMax, final float limitMin, final float limitMax) {
        return ((limitMax - limitMin) * (valueIn - baseMin) / (baseMax - baseMin)) + limitMin;
    }
/*
    public class infoGrab extends AsyncTask<String, Integer, Integer> {

        @Override
        protected Integer doInBackground(String... params) {

            try {
                new EchoOffCommand().run(mmInStream, mmOutStream);
                new LineFeedOffCommand().run(mmInStream, mmOutStream);
                new TimeoutCommand(125).run(mmInStream, mmOutStream);
                new SelectProtocolCommand(ObdProtocols.AUTO).run(mmInStream, mmOutStream);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            while (true) {
                try {
                    RPMCommand i;
                    i = new RPMCommand();
                    i.run(mmInStream, mmOutStream);
                    int d = i.getRPM();
                    String unitToString = Integer.toString(d);
                    Message msg = mHandler.obtainMessage(3);
                    Bundle bundle = new Bundle();
                    bundle.putFloat("ROT", scale(d, 0, 7000, -90, 90));
                    bundle.putString("RPM", unitToString);
                    msg.setData(bundle);
                    mHandler.sendMessage(msg);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }

            return 0;
        }

        public float scale(final float valueIn, final float baseMin, final float baseMax, final float limitMin, final float limitMax) {
            return ((limitMax - limitMin) * (valueIn - baseMin) / (baseMax - baseMin)) + limitMin;
        }
    }
    */

}
