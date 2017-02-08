package swagyyydevelopment.jswag180.com.obdmonitor;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
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


public class FuleInfo extends AppCompatActivity {

    TextView tvFeulLvl;
    InputStream mmInStream = null;
    OutputStream mmOutStream = null;
    BluetoothSocket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fuel_info);

        tvFeulLvl = (TextView) findViewById(R.id.tvFeulLvl);

        socket = swagyyydevelopment.jswag180.com.obdmonitor.Socket.getSocket();
        try {

            mmInStream = socket.getInputStream();
            mmOutStream = socket.getOutputStream();

        } catch (Exception e) {
            e.printStackTrace();
        }
        new getInfo().execute("");
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
                        //j.setRotation((msg.getData().getFloat("ROT")));
                        tvFeulLvl.setText(msg.getData().getString("FUEL"));//msg.getData().getInt("RPM")
                    }
                    break;

            }
        }
    };

    public class getInfo extends AsyncTask<String, Integer, Integer> {


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
                    FuelLevelCommand i;
                    i = new FuelLevelCommand();
                    i.run(mmInStream, mmOutStream);
                    float d = i.getPercentage();
                    String unitToString = Float.toString(d);
                    Message msg = mHandler.obtainMessage(3);
                    Bundle bundle = new Bundle();
                    bundle.putString("FUEL", unitToString);
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

            return null;
        }


    }
}
