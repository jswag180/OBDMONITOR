package swagyyydevelopment.jswag180.com.obdmonitor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.commands.temperature.AirIntakeTemperatureCommand;
import com.github.pires.obd.commands.temperature.AmbientAirTemperatureCommand;
import com.github.pires.obd.commands.temperature.EngineCoolantTemperatureCommand;
import com.github.pires.obd.enums.ObdProtocols;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import swagyyydevelopment.jswag180.com.obdmonitor.CustomCommands.BatteryVoltzCommand;
import swagyyydevelopment.jswag180.com.obdmonitor.CustomCommands.ShortTermFuleTrimBank1Command;
import swagyyydevelopment.jswag180.com.obdmonitor.CustomCommands.ShortTermFuleTrimBank2Command;


public class DataLogger extends Activity {


    Button btnStart, btnStop;
    CheckBox ckIntakeTMP, ckCoolentTMP, ckExTMP, ckBatVoltz, ckRPM, ckFuelTrim;
    ProgressBar pbRuning;
    protected PowerManager.WakeLock mWakeLock;
    Context context = this;
    BluetoothSocket socket;
    InputStream mmInStream = null;
    OutputStream mmOutStream = null;
    int place = 0;
    boolean isRunning = false;
    static boolean IntakeTMP, CoolantTMP, ExTMP, BatVoltz, RPM, FuelTrim;
    static List<Object[]> dat = new ArrayList<Object[]>();

    private static Workbook wb;
    private static File file;
    private static Sheet mySheet;
    private static FileOutputStream os = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_logging);

        btnStart = (Button) findViewById(R.id.btnStart);
        btnStop = (Button) findViewById(R.id.btnStop);
        pbRuning = (ProgressBar) findViewById(R.id.pbRuning);
        ckIntakeTMP = (CheckBox) findViewById(R.id.ckIntakeTMP);
        ckCoolentTMP = (CheckBox) findViewById(R.id.ckCoolentTMP);
        ckExTMP = (CheckBox) findViewById(R.id.ckExTMP);
        ckBatVoltz = (CheckBox) findViewById(R.id.ckBatVoltz);
        ckRPM = (CheckBox) findViewById(R.id.ckRPM);
        ckFuelTrim = (CheckBox) findViewById(R.id.ckFuelTrim);

        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
        this.mWakeLock.acquire();

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntakeTMP = ckIntakeTMP.isChecked();
                CoolantTMP = ckCoolentTMP.isChecked();
                ExTMP = ckExTMP.isChecked();
                BatVoltz = ckBatVoltz.isChecked();
                RPM = ckRPM.isChecked();
                FuelTrim = ckFuelTrim.isChecked();
                try {

                    socket = swagyyydevelopment.jswag180.com.obdmonitor.Socket.getSocket();
                    mmInStream = socket.getInputStream();
                    mmOutStream = socket.getOutputStream();
                    new EchoOffCommand().run(mmInStream, mmOutStream);
                    new LineFeedOffCommand().run(mmInStream, mmOutStream);
                    new TimeoutCommand(125).run(mmInStream, mmOutStream);
                    new SelectProtocolCommand(ObdProtocols.AUTO).run(mmInStream, mmOutStream);


                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                }

                isRunning = true;
                synchronized (this) {
                    new log().execute();
                }

            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRunning = false;
                try {
                    //Toast.makeText(getApplicationContext(), dat.size(), Toast.LENGTH_LONG).show();
                    DataLogging.closeExcelFile(dat);
                    Message msg1 = mHandler.obtainMessage(2);
                    Bundle bundle1 = new Bundle();
                    bundle1.putBoolean("state", true);
                    msg1.setData(bundle1);
                    mHandler.sendMessage(msg1);
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Stop btn" + e.toString(), Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        DataLogging.closeExcelFile(dat);
        this.mWakeLock.release();
        Message msg1 = mHandler.obtainMessage(2);
        Bundle bundle1 = new Bundle();
        bundle1.putBoolean("state", true);
        msg1.setData(bundle1);
        mHandler.sendMessage(msg1);
        super.onDestroy();
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


                    }
                    break;
                case 2:
                    if (null != activity) {

                        if (msg.getData().getBoolean("state")) {
                            pbRuning.setVisibility(View.VISIBLE);
                        } else {
                            pbRuning.setVisibility(View.INVISIBLE);
                        }

                    }
                    break;

            }
        }
    };

    public class log extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            DataLogging.getExcelFile(context, "DataLog.xls");

            AirIntakeTemperatureCommand intake = new AirIntakeTemperatureCommand();
            EngineCoolantTemperatureCommand coolant = new EngineCoolantTemperatureCommand();
            AmbientAirTemperatureCommand external = new AmbientAirTemperatureCommand();
            BatteryVoltzCommand battery = new BatteryVoltzCommand();
            RPMCommand rpmCommand = new RPMCommand();
            ShortTermFuleTrimBank1Command fuelTrim1 = new ShortTermFuleTrimBank1Command();
            ShortTermFuleTrimBank2Command fuelTrim2 = new ShortTermFuleTrimBank2Command();

            Message msg1 = mHandler.obtainMessage(2);
            Bundle bundle1 = new Bundle();
            bundle1.putBoolean("state", true);
            msg1.setData(bundle1);
            mHandler.sendMessage(msg1);


            while (isRunning) {
                synchronized (this) {
                    try {

                        wait(1000);//pullInterval

                        if (!isRunning) {
                            break;
                        }

                        place++;

                        String coolantTMP = "Null", intakeTMP = "Null", externalTMP = "Null", batteryVolts = "Null", enginRPM = "Null", fuleTrim = "Null";

                        if (IntakeTMP) {
                            intake.run(mmInStream, mmOutStream);
                            intakeTMP = String.valueOf(Math.round(intake.getImperialUnit()));
                        }
                        if (CoolantTMP) {
                            coolant.run(mmInStream, mmOutStream);
                            coolantTMP = String.valueOf(Math.round(coolant.getImperialUnit()));
                        }
                        if (ExTMP) {
                            external.run(mmInStream, mmOutStream);
                            externalTMP = String.valueOf(Math.round(external.getImperialUnit()));
                        }
                        if (BatVoltz) {
                            battery.run(mmInStream, mmOutStream);
                            batteryVolts = battery.getFormattedResult();
                        }
                        if (RPM) {
                            rpmCommand.run(mmInStream, mmOutStream);
                            enginRPM = String.valueOf(rpmCommand.getRPM());
                        }
                        if (FuelTrim) {
                            fuelTrim1.run(mmInStream, mmOutStream);
                            fuelTrim2.run(mmInStream, mmOutStream);
                            String ft1 = String.valueOf(fuelTrim1.getCalculatedResult());
                            String ft2 = String.valueOf(fuelTrim2.getCalculatedResult());
                            float avrage = (Float.parseFloat(ft1) + Float.parseFloat(ft2)) / 2;
                            fuleTrim = String.valueOf(Math.round(avrage));
                            //fuleTrim = fuelTrim1.getCalculatedResult();
                        }

                        long timeStamp = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());//Unix Timestamp
                        java.util.Date time = new java.util.Date(timeStamp * 1000);//Unix to normal time

                        DataLogging.writeExcelFile(String.valueOf(time), coolantTMP, intakeTMP, externalTMP, batteryVolts, enginRPM, fuleTrim);
/*
                        Message msg = mHandler.obtainMessage(1);
                        Bundle bundle = new Bundle();
                        bundle.putString("toast", "added entry.");
                        msg.setData(bundle);
                        mHandler.sendMessage(msg);*/
                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
                    } catch (InterruptedException e) {
                        Message msg = mHandler.obtainMessage(1);
                        Bundle bundle = new Bundle();
                        bundle.putString("toast", e.toString());
                        msg.setData(bundle);
                        mHandler.sendMessage(msg);
                        break;
                    } catch (Exception e) {
                        Message msg = mHandler.obtainMessage(1);
                        Bundle bundle = new Bundle();
                        bundle.putString("toast", e.toString());
                        msg.setData(bundle);
                        mHandler.sendMessage(msg);
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            DataLogging.closeExcelFile(dat);
            Message msg1 = mHandler.obtainMessage(2);
            Bundle bundle1 = new Bundle();
            bundle1.putBoolean("state", false);
            msg1.setData(bundle1);
            mHandler.sendMessage(msg1);
            super.onPostExecute(aVoid);
        }
    }

    public static class DataLogging {


        public static boolean getExcelFile(Context context, String fileName) {

            boolean success = false;

            file = new File(context.getExternalFilesDir(null), fileName);

            //New Workbook
            wb = new HSSFWorkbook();

            //New Sheet
            mySheet = null;
            mySheet = wb.createSheet("logSheet1");

            dat.add(new Object[]{"timeStamp", "coolantTMP", "intakeTMP", "externalTMP", "batteryVolts", "engineRPM", "fuelTrim"});

            return success;

        }

        public static void writeExcelFile(String timeStamp, String coolentTMP, String intakeTMP, String extrnalTMP, String batteryVolts, String enginRpm, String fuleTrim) {

            dat.add(new Object[]{timeStamp, coolentTMP, intakeTMP, extrnalTMP, batteryVolts, enginRpm, fuleTrim});

        }

        public static void closeExcelFile(List<Object[]> data) {

            int rownum = 0;
            for (int i = 0; i < data.size(); i++) { //for (String key : keyset) {
                Row row = mySheet.createRow(rownum++);
                Object[] objArr = data.get(i);
                int cellnum = 0;
                for (Object obj : objArr) {
                    Cell cell = row.createCell(cellnum++);
                    if (obj instanceof Date)
                        cell.setCellValue((Date) obj);
                    else if (obj instanceof Boolean)
                        cell.setCellValue((Boolean) obj);
                    else if (obj instanceof String)
                        cell.setCellValue((String) obj);
                    else if (obj instanceof Double)
                        cell.setCellValue((Double) obj);
                }
            }

            try {
                os = new FileOutputStream(file);
                wb.write(os);
                Log.w("FileUtils", "Writing file" + file);
            } catch (IOException e) {
                Log.w("FileUtils", "Error writing " + file, e);
            } catch (Exception e) {
                Log.w("FileUtils", "Failed to save file", e);
            } finally {
                try {
                    if (null != os)
                        os.close();
                } catch (Exception ex) {

                }
            }

        }


    }


}