package swagyyydevelopment.jswag180.com.obdmonitor;


import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.enums.ObdProtocols;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import swagyyydevelopment.jswag180.com.obdmonitor.CustomCommands.ShortTermFuleTrimBank1Command;
import swagyyydevelopment.jswag180.com.obdmonitor.CustomCommands.ShortTermFuleTrimBank2Command;

public class Fueling extends Activity {

    TextView tvSTB1, tvSTB2, tvSTB1Max, tvSTB1Min, tvSTB2Min, tvSTB2Max;
    Button btnReset;
    BluetoothSocket socket;
    InputStream mmInStream = null;
    OutputStream mmOutStream = null;
    ShortTermFuleTrimBank1Command STB1;
    ShortTermFuleTrimBank2Command STB2;
    Handler infoPullerHandler = new Handler();
    double STB1Max = 0.00;
    double STB1Min = 0.00;
    double STB2Max = 0.00;
    double STB2Min = 0.00;
    boolean run = false;
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fueling);

        tvSTB1 = (TextView) findViewById(R.id.tvSTB1);
        tvSTB2 = (TextView) findViewById(R.id.tvSTB2);
        tvSTB1Max = (TextView) findViewById(R.id.tvSTB1Max);
        tvSTB1Min = (TextView) findViewById(R.id.tvSTB1Min);
        tvSTB2Max = (TextView) findViewById(R.id.tvSTB2Max);
        tvSTB2Min = (TextView) findViewById(R.id.tvSTB2Min);
        btnReset = (Button) findViewById(R.id.btnReset);


        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                STB1Max = 0.00;
                tvSTB1Max.setText("Max" + "\n" + STB1Max);
                STB1Min = 0.00;
                tvSTB1Min.setText("Min" + "\n" + STB1Min);
                STB2Max = 0.00;
                tvSTB2Max.setText("Max" + "\n" + STB2Max);
                STB2Min = 0.00;
                tvSTB2Min.setText("Min" + "\n" + STB2Min);

                run = true;

                try {

                    socket = swagyyydevelopment.jswag180.com.obdmonitor.Socket.getSocket();
                    mmInStream = socket.getInputStream();
                    mmOutStream = socket.getOutputStream();
                    new EchoOffCommand().run(mmInStream, mmOutStream);
                    new LineFeedOffCommand().run(mmInStream, mmOutStream);
                    new TimeoutCommand(125).run(mmInStream, mmOutStream);
                    new SelectProtocolCommand(ObdProtocols.AUTO).run(mmInStream, mmOutStream);
                    STB1 = new ShortTermFuleTrimBank1Command();
                    STB2 = new ShortTermFuleTrimBank2Command();

                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                }

                infoPullerHandler.postDelayed(infoPullerThread, 0);

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

                        String STB1Val = msg.getData().getString("STB1");

                        String STB2Val = msg.getData().getString("STB2");


                        tvSTB1.setText("Bank 1" + "\n" + STB1Val);
                        tvSTB2.setText("Bank 2" + "\n" + STB2Val);

                        if (Double.parseDouble(STB1Val) >= STB1Max) {

                            STB1Max = Double.parseDouble(STB1Val);
                            tvSTB1Max.setText("Max" + "\n" + STB1Max);

                        }
                        if (Double.parseDouble(STB1Val) <= STB1Min) {

                            STB1Min = Double.parseDouble(STB1Val);
                            tvSTB1Min.setText("Min" + "\n" + STB1Min);

                        }
                        if (Double.parseDouble(STB2Val) >= STB2Max) {

                            STB2Max = Double.parseDouble(STB2Val);
                            tvSTB2Max.setText("Max" + "\n" + STB2Max);

                        }
                        if (Double.parseDouble(STB2Val) <= STB2Min) {

                            STB2Min = Double.parseDouble(STB2Val);
                            tvSTB2Min.setText("Min" + "\n" + STB2Min);

                        }


                    }
                    break;

            }
        }
    };

    Runnable infoPullerThread = new Runnable() {

        @Override
        public void run() {

            synchronized (this) {
                try {
                    STB1.run(mmInStream, mmOutStream);
                    STB2.run(mmInStream, mmOutStream);
                    Message msg = mHandler.obtainMessage(2);
                    Bundle bundle = new Bundle();
                    bundle.putString("STB1", STB1.getCalculatedResult());
                    bundle.putString("STB2", STB2.getCalculatedResult());
                    msg.setData(bundle);
                    mHandler.sendMessage(msg);
                } catch (IOException | InterruptedException e) {
                    sendToast(e.toString());
                    socket = swagyyydevelopment.jswag180.com.obdmonitor.Socket.getSocket();
                    infoPullerHandler.postDelayed(infoPullerThread, 0);
                }
                infoPullerHandler.postDelayed(infoPullerThread, 0);
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
