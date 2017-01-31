package swagyyydevelopment.jswag180.com.obdmonitor;


import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
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
    BluetoothSocket socket;
    InputStream mmInStream = null;
    OutputStream mmOutStream = null;
    int RPM = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gage);
        ImageView i = (ImageView) findViewById(R.id.IMVTEST);
        j = (ImageView) findViewById(R.id.IMVNED);


        try {
            socket = swagyyydevelopment.jswag180.com.obdmonitor.Socket.getSocket();
            mmInStream = socket.getInputStream();
            mmOutStream = socket.getOutputStream();
            new infoGrab().execute("");
            new DailSetting().execute("");
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                        RPM = msg.getData().getInt("RPM");
                    }
                    break;
                case 3:
                    if (null != activity) {
                        j.setRotation(msg.getData().getInt("ROT"));
                    }
                    break;

            }
        }
    };

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
                    Message msg = mHandler.obtainMessage(2);
                    Bundle bundle = new Bundle();
                    bundle.putInt("RPM", d);
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
    }

    public class DailSetting extends AsyncTask<String, Integer, Integer> {


        @Override
        protected Integer doInBackground(String... params) {

            while (true) {

                try {

                    Message msg = mHandler.obtainMessage(3);
                    Bundle bundle = new Bundle();
                    bundle.putInt("ROT", (int) scale(RPM, 0, 6000, -90, 90));
                    msg.setData(bundle);
                    mHandler.sendMessage(msg);

                } catch (Exception e) {
                    break;
                }

            }

            return null;
        }

        public double scale(final double valueIn, final double baseMin, final double baseMax, final double limitMin, final double limitMax) {
            return ((limitMax - limitMin) * (valueIn - baseMin) / (baseMax - baseMin)) + limitMin;
        }
    }


}
