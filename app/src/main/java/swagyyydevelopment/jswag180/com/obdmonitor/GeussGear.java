package swagyyydevelopment.jswag180.com.obdmonitor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.enums.ObdProtocols;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class GeussGear extends Activity {

    TextView txtbah;
    Handler customHandler = new Handler();
    BluetoothSocket socket;
    InputStream mmInStream = null;
    OutputStream mmOutStream = null;
    int rpm, lastRpm;
    double speed = 0.00;
    RPMCommand i = new RPMCommand();
    SpeedCommand s = new SpeedCommand();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guess_gear);

        txtbah = (TextView) findViewById(R.id.txtbah);

        socket = Socket.getSocket();
        try {
            mmInStream = socket.getInputStream();
            mmOutStream = socket.getOutputStream();
            new EchoOffCommand().run(mmInStream, mmOutStream);
            new LineFeedOffCommand().run(mmInStream, mmOutStream);
            new TimeoutCommand(125).run(mmInStream, mmOutStream);
            new SelectProtocolCommand(ObdProtocols.AUTO).run(mmInStream, mmOutStream);
        } catch (Exception ignored) {

        }
        customHandler.postDelayed(getRpm, 0);

    }

    @SuppressLint("HandlerLeak")
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
                        if (socket.isConnected()) {
                            finish();
                        }

                    }
                    break;
                case 2:
                    if (null != activity) {

                        rpm = msg.getData().getInt("RPM");
                        speed = msg.getData().getFloat("SPEED");

                        //1st(0.9728) 2nd(0.6918) 3rd(0.4652) 4th(0.3021) 5th(0.2145)

                        double gearRatio = ((speed / (60 * 2.17046302)) / (rpm / 1000));// speed(km/h) / (60 * wheel diameter) / rpm in thousands

                        if (gearRatio == 0.97) { //1st 0.972809667673716

                            txtbah.setText("Gear: " + "1st");

                        } else if (gearRatio == 0.69) { //2nd  0.6918429003021148

                            txtbah.setText("Gear: " + "2nd");

                        } else if (gearRatio == 0.46) { //3rd 0.4652567975830816

                            txtbah.setText("Gear: " + "3rd");

                        } else if (gearRatio == 0.30) { //4th 0.3021148036253776

                            txtbah.setText("Gear: " + "4th");

                        } else if (gearRatio == 0.21) { //5th  0.2145015105740181

                            txtbah.setText("Gear: " + "5th");

                        } else if (gearRatio == 0.92) { //Reverse 0.9274924471299094

                            txtbah.setText("Gear: " + "Reverse");

                        } else {
                            txtbah.setText("Gear: " + "Unknown: " + gearRatio);
                        }


                    }
                    break;

            }
        }
    };

    Runnable getRpm = new Runnable() {
        @Override
        public void run() {

            try {

                i.run(mmInStream, mmOutStream);
                s.run(mmInStream, mmOutStream);
                float d = i.getRPM();
                float b = s.getMetricSpeed();
                Message msg = mHandler.obtainMessage(2);
                Bundle bundle = new Bundle();
                bundle.putFloat("RPM", d);
                bundle.putFloat("SPEED", b);
                msg.setData(bundle);
                mHandler.sendMessage(msg);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();

            }

            customHandler.postDelayed(this, 0);
        }
    };

}
