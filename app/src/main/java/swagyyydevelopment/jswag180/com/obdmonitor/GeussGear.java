package swagyyydevelopment.jswag180.com.obdmonitor;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

public class GeussGear extends Activity {

    TextView txtbah;
    Handler customHandler = new Handler();
    BluetoothSocket socket;
    InputStream mmInStream = null;
    OutputStream mmOutStream = null;
    int rpm, lastRpm;
    double delta = 0;

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
                        delta = rpm - lastRpm;
                        lastRpm = rpm;
                        txtbah.setText(rpm + "\nwith delta change of: " + delta);

                    }
                    break;

            }
        }
    };

    Runnable getRpm = new Runnable() {
        @Override
        public void run() {

            try {
                RPMCommand i;
                i = new RPMCommand();
                i.run(mmInStream, mmOutStream);
                int d = i.getRPM();
                Message msg = mHandler.obtainMessage(2);
                Bundle bundle = new Bundle();
                bundle.putInt("RPM", d);
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
