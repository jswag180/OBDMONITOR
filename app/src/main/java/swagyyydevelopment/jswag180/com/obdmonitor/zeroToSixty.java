package swagyyydevelopment.jswag180.com.obdmonitor;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.enums.ObdProtocols;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class zeroToSixty extends Activity {

    Button btnStart, btnReset;
    TextView txtTime, txtSpeed;
    int i = 0;
    int ms, ss, mm;
    boolean getSpeed = false;
    boolean isCounting = false;
    boolean ready = false;
    float speed;
    Handler customHandler = new Handler();
    BluetoothSocket socket;
    InputStream mmInStream = null;
    OutputStream mmOutStream = null;
    long startTime = 0L, timeInMilliseconds = 0L, timeSwapBuff = 0L, updateTime = 0L;

    Runnable updateTimerThread = new Runnable() {

        @Override

        public void run() {

            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;

            updateTime = timeSwapBuff + timeInMilliseconds;

            int secs = (int) (updateTime / 1000);

            int mins = secs / 60;

            secs %= 60;

            int milliseconds = (int) (updateTime % 1000);

            txtTime.setText("" + mins + ":" + String.format("%02d", secs) + ":"

                    + String.format("%03d", milliseconds));

            customHandler.postDelayed(this, 0);

        }

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zero_to_sizty);

        DialogFragment newFragment = new InfoDialogFragment();
        newFragment.show(getFragmentManager(), "missiles");
        btnStart = (Button) findViewById(R.id.btnStart);
        btnReset = (Button) findViewById(R.id.btnReset);
        txtTime = (TextView) findViewById(R.id.txtTime);
        txtSpeed = (TextView) findViewById(R.id.txtSpeed);

        if (devMode.getDevMode()) {

        } else {
            socket = swagyyydevelopment.jswag180.com.obdmonitor.Socket.getSocket();
            try {
                mmInStream = socket.getInputStream();
                mmOutStream = socket.getOutputStream();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                synchronized (this) {
                    getSpeed = true;
                    new VSpeed().execute();
                }

            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getSpeed = false;
                timeSwapBuff += timeInMilliseconds;
                customHandler.removeCallbacks(updateTimerThread);
                startTime = 0L;
                timeInMilliseconds = 0L;
                timeSwapBuff = 0L;
                updateTime = 0L;


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

                        speed = msg.getData().getFloat("SPEED");

                        txtSpeed.setText(Integer.toString((int) speed) + "mph");

                        if (speed == 0F) {
                            ready = true;
                        }
                        if (ready && !isCounting) {
                            if (speed > 0) {
                                isCounting = true;
                                startTime = SystemClock.uptimeMillis();
                                customHandler.postDelayed(updateTimerThread, 0);
                            }
                        }
                        if (isCounting) {
                            if (speed >= 60) {
                                getSpeed = false;
                                isCounting = false;
                                ready = false;
                                timeSwapBuff += timeInMilliseconds;
                                customHandler.removeCallbacks(updateTimerThread);
                            }
                        }

                    }
                    break;

            }
        }
    };

    public class VSpeed extends AsyncTask<String, Integer, Void> {

        @Override
        protected Void doInBackground(String... params) {
            synchronized (this) {

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
                while (getSpeed) {
                    try {
                        SpeedCommand i;
                        i = new SpeedCommand();
                        i.run(mmInStream, mmOutStream);
                        Float d = i.getImperialSpeed();
                        Message msg = mHandler.obtainMessage(2);
                        Bundle bundle = new Bundle();
                        bundle.putFloat("SPEED", d);
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

            }


            return null;
        }
    }

    public static class InfoDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Press start when ready. Time wont start until you start moving.")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // FIRE ZE MISSILES!
                        }
                    });
            /*
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
                    */
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }

}
