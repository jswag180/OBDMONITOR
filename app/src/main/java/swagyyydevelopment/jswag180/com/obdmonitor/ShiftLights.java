package swagyyydevelopment.jswag180.com.obdmonitor;


import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
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

public class ShiftLights extends AppCompatActivity {

    TextView tvRPM;
    ImageView ivLGreen, ivRGreen, ivLOrnge, ivROrnge, ivRed;
    BluetoothSocket socket;
    InputStream mmInStream = null;
    OutputStream mmOutStream = null;
    Context context = this;
    MediaPlayer mp;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shit_lights);

        tvRPM = (TextView) findViewById(R.id.tvRPM);
        ivLGreen = (ImageView) findViewById(R.id.ivLGreen);
        ivRGreen = (ImageView) findViewById(R.id.ivRGreen);
        ivLOrnge = (ImageView) findViewById(R.id.ivLOrnge);
        ivROrnge = (ImageView) findViewById(R.id.ivROrnge);
        ivRed = (ImageView) findViewById(R.id.ivRed);

        mp = MediaPlayer.create(context, R.raw.baloney);
        socket = swagyyydevelopment.jswag180.com.obdmonitor.Socket.getSocket();
        try {
            mmInStream = socket.getInputStream();
            mmOutStream = socket.getOutputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
        new infoGrab().execute("");

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
                        String v = msg.getData().getString("RPM");
                        tvRPM.setText(v);

                        if (isBetween(Integer.parseInt(v), 3666, 4331)) {//666.7 3666 4332 4998

                            //green
                            ivLGreen.setVisibility(View.VISIBLE);
                            ivRGreen.setVisibility(View.VISIBLE);
                            ivLOrnge.setVisibility(View.INVISIBLE);
                            ivROrnge.setVisibility(View.INVISIBLE);
                            ivRed.setVisibility(View.INVISIBLE);

                        } else if (isBetween(Integer.parseInt(v), 4332, 4997)) {

                            //yellow
                            ivLGreen.setVisibility(View.VISIBLE);
                            ivRGreen.setVisibility(View.VISIBLE);
                            ivLOrnge.setVisibility(View.VISIBLE);
                            ivROrnge.setVisibility(View.VISIBLE);
                            ivRed.setVisibility(View.INVISIBLE);

                        } else if (isBetween(Integer.parseInt(v), 4998, 6000)) {

                            //red

                            ivLGreen.setVisibility(View.VISIBLE);
                            ivRGreen.setVisibility(View.VISIBLE);
                            ivLOrnge.setVisibility(View.VISIBLE);
                            ivROrnge.setVisibility(View.VISIBLE);
                            ivRed.setVisibility(View.VISIBLE);

                            try {
                                if (mp.isPlaying()) {
                                    mp.stop();
                                    mp.release();
                                    mp = MediaPlayer.create(context, R.raw.baloney);
                                }
                                mp.start();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }


                    }
                    break;

            }
        }
    };

    public static boolean isBetween(int x, int lower, int upper) {
        return lower <= x && x <= upper;
    }

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


    }

}
