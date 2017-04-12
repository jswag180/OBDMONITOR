package swagyyydevelopment.jswag180.com.obdmonitor;


import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pires.obd.commands.pressure.IntakeManifoldPressureCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.commands.temperature.EngineCoolantTemperatureCommand;
import com.github.pires.obd.enums.ObdProtocols;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Temps extends AppCompatActivity {

    BluetoothSocket socket;
    InputStream mmInStream = null;
    OutputStream mmOutStream = null;
    protected PowerManager.WakeLock mWakeLock;
    TextView txtENG, txtOIL;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temps);

        txtENG = (TextView) findViewById(R.id.txtENG);
        txtOIL = (TextView) findViewById(R.id.txtOIL);

        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
        this.mWakeLock.acquire();

        socket = swagyyydevelopment.jswag180.com.obdmonitor.Socket.getSocket();
        try {

            mmInStream = socket.getInputStream();
            mmOutStream = socket.getOutputStream();

        } catch (Exception e) {
            e.printStackTrace();
        }
        synchronized (this) {
            new infoGrab().execute("");
        }
    }

    @Override
    protected void onDestroy() {
        this.mWakeLock.release();
        super.onDestroy();
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

                        txtOIL.setText("Intake PSI" + "\n" + msg.getData().getString("OIL"));//"OIL" + System.getProperty("line.separator") +
                        txtENG.setText("Coolant" + "\n" + msg.getData().getString("ENG"));//"ENG"  + System.getProperty("line.separator")

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
            synchronized (this) {
                while (true) {
                    try {
                        wait(100);
                        EngineCoolantTemperatureCommand i;
                        i = new EngineCoolantTemperatureCommand();
                        i.run(mmInStream, mmOutStream); // getting the Engine Coolant temp
                        float d = i.getImperialUnit();
                        String EngCoolToString = Float.toString(d);

                        IntakeManifoldPressureCommand k;
                        k = new IntakeManifoldPressureCommand();
                        k.run(mmInStream, mmOutStream);
                        //float e = k.;//getImperialUnit()
                        String OilCoolToString = k.getFormattedResult();//k.getFormattedResult() Float.toString(e)


                        Message msg = mHandler.obtainMessage(2);// start the bundle of data to the handler
                        Bundle bundle = new Bundle();
                        bundle.putString("ENG", EngCoolToString);
                        bundle.putString("OIL", OilCoolToString);
                        msg.setData(bundle);
                        mHandler.sendMessage(msg);

                    } catch (InterruptedException e) {
                        Message msg = mHandler.obtainMessage(1);
                        Bundle bundle = new Bundle();
                        bundle.putString("toast", e.toString());
                        msg.setData(bundle);
                        mHandler.sendMessage(msg);
                        break;
                    } catch (IOException e) {
                        Message msg = mHandler.obtainMessage(1);
                        Bundle bundle = new Bundle();
                        bundle.putString("toast", e.toString());
                        msg.setData(bundle);
                        mHandler.sendMessage(msg);
                        break;
                    }
                }
            }
            return 0;
        }

    }

}
