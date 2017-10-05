package swagyyydevelopment.jswag180.com.obdmonitor;


import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pires.obd.commands.ObdMultiCommand;
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
    boolean deBugMode = true;
    boolean isRunning = false;
    Context context = this;
    String EngCoolToString, OilCoolToString;
    ObdMultiCommand mult = new ObdMultiCommand();
    Handler tempsHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temps);

        txtENG = (TextView) findViewById(R.id.txtENG);
        txtOIL = (TextView) findViewById(R.id.txtOIL);

        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
        this.mWakeLock.acquire();


        synchronized (this) {
            try {
                socket = swagyyydevelopment.jswag180.com.obdmonitor.Socket.getSocket();
                mmInStream = socket.getInputStream();
                mmOutStream = socket.getOutputStream();
                new EchoOffCommand().run(mmInStream, mmOutStream);
                new LineFeedOffCommand().run(mmInStream, mmOutStream);
                new TimeoutCommand(125).run(mmInStream, mmOutStream);
                new SelectProtocolCommand(ObdProtocols.AUTO).run(mmInStream, mmOutStream);
                mult.add(new EngineCoolantTemperatureCommand());
                mult.add(new IntakeManifoldPressureCommand());
                tempsHandler.postDelayed(obdCom, 0);
            } catch (Exception e) {

            }
            isRunning = true;

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
                        if (socket.isConnected()) {
                            finish();
                        }

                    }
                    break;
                case 2:
                    if (null != activity) {

                        String[] parts = msg.getData().getString("OIL").split(",");
                        //Toast.makeText(activity, msg.getData().getString("OIL"), Toast.LENGTH_SHORT).show();
                        txtOIL.setText("Intake" + "\n" + "PSI" + "\n" + parts[1]);//"OIL" + System.getProperty("line.separator") +
                        txtENG.setText("\n" + "Coolant" + "\n" + parts[0]);//"ENG"  + System.getProperty("line.separator")

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
                    mult.sendCommands(mmInStream, mmOutStream);
                    Message msg = mHandler.obtainMessage(2);
                    Bundle bundle = new Bundle();
                    bundle.putString("OIL", mult.getFormattedResult());
                    msg.setData(bundle);
                    mHandler.sendMessage(msg);
                } catch (IOException | InterruptedException e) {
                    sendToast(e.toString());
                    tempsHandler.removeCallbacks(obdCom);
                    Intent intentConServise = new Intent(context, BtCon.class);
                    startService(intentConServise);
                    try {
                        wait(500);
                    } catch (InterruptedException e1) {
                        sendToast(e1.toString());
                    }
                    socket = swagyyydevelopment.jswag180.com.obdmonitor.Socket.getSocket();
                    tempsHandler.postDelayed(obdCom, 0);
                }
                tempsHandler.postDelayed(this, 0);
            }
        }
    };

    public void sendToast(String message) {
        Message msg = mHandler.obtainMessage(1);
        Bundle bundle = new Bundle();
        bundle.putString("toast", message);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

}
