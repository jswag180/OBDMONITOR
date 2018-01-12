package swagyyydevelopment.jswag180.com.obdmonitor;

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
import android.widget.Toast;

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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class DataLogger extends Activity {


    Button btnStart, btnStop;
    CheckBox ckIntakeTMP, ckCoolentTMP, ckExTMP, ckBatVoltz;
    //Handler tempsHandler = new Handler();
    protected PowerManager.WakeLock mWakeLock;
    Context context = this;
    BluetoothSocket socket;
    InputStream mmInStream = null;
    OutputStream mmOutStream = null;
    int place = 0;
    boolean isRunning = false;
    static boolean IntakeTMP, CoolantTMP, ExTMP, BatVoltz;
    static Map<Integer, Object[]> dat = new HashMap<Integer, Object[]>();

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
        ckIntakeTMP = (CheckBox) findViewById(R.id.ckIntakeTMP);
        ckCoolentTMP = (CheckBox) findViewById(R.id.ckCoolentTMP);
        ckExTMP = (CheckBox) findViewById(R.id.ckExTMP);
        ckBatVoltz = (CheckBox) findViewById(R.id.ckBatVoltz);

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
                try {

                    socket = swagyyydevelopment.jswag180.com.obdmonitor.Socket.getSocket();
                    mmInStream = socket.getInputStream();
                    mmOutStream = socket.getOutputStream();
                    new EchoOffCommand().run(mmInStream, mmOutStream);
                    new LineFeedOffCommand().run(mmInStream, mmOutStream);
                    new TimeoutCommand(125).run(mmInStream, mmOutStream);
                    new SelectProtocolCommand(ObdProtocols.AUTO).run(mmInStream, mmOutStream);


                } catch (Exception e) {

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
                    Toast.makeText(getApplicationContext(), dat.size(), Toast.LENGTH_LONG).show();
                    DataLogging.closeExcelFile(dat);
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        DataLogging.closeExcelFile(dat);
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

            while (isRunning) {
                synchronized (this) {
                    try {


                        wait(1000);

                        if (!isRunning) {
                            break;
                        }

                        place++;

                        String coolantTMP = "Null", intakeTMP = "Null", externalTMP = "Null", batteryVolts = "Null";

                        if (IntakeTMP) {
                            intake.run(mmInStream, mmOutStream);
                            intakeTMP = String.valueOf(intake.getImperialUnit());
                        } else if (CoolantTMP) {
                            coolant.run(mmInStream, mmOutStream);
                            coolantTMP = String.valueOf(coolant.getImperialUnit());
                        } else if (ExTMP) {
                            external.run(mmInStream, mmOutStream);
                            externalTMP = String.valueOf(external.getImperialUnit());
                        } else if (BatVoltz) {
                            battery.run(mmInStream, mmOutStream);
                            batteryVolts = battery.getFormattedResult();
                        }

                        long timeStamp = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());//Unix Timestamp
                        java.util.Date time = new java.util.Date(timeStamp * 1000);//Unix to normal time

                        DataLogging.writeExcelFile(String.valueOf(time), coolantTMP, intakeTMP, externalTMP, batteryVolts, place);
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
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            DataLogging.closeExcelFile(dat);
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

            //dat.put("rowNum", new Object[]{"timeStamp", "coolantTMP", "intakeTMP", "externalTMP", "batteryVolts"});

            return success;

        }

        public static void writeExcelFile(String timeStamp, String coolentTMP, String intakeTMP, String extrnalTMP, String batteryVolts, int rowNum) {

            dat.put(rowNum, new Object[]{timeStamp, coolentTMP, intakeTMP, extrnalTMP, batteryVolts});

        }

        public static void closeExcelFile(Map<Integer, Object[]> data) {


            //Set<Integer> keyset = data.keySet();
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