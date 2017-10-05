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

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
    static int IntakeRow, CoolantRow, ExternalRow, BattaryRow, TimeRow;
    static boolean IntakeTMP, CoolantTMP, ExTMP, BatVoltz;

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
                    DataLogging.closeExcelFile();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG);
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        DataLogging.closeExcelFile();
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


                        wait(5000);

                        place++;

                        if (IntakeTMP) {
                            intake.run(mmInStream, mmOutStream);
                            DataLogging.writeExcelFile(String.valueOf((int) intake.getImperialUnit()), IntakeRow, true, place, false);
                        } else if (CoolantTMP) {
                            coolant.run(mmInStream, mmOutStream);
                            DataLogging.writeExcelFile(String.valueOf((int) coolant.getImperialUnit()), CoolantRow, true, place, false);
                        } else if (ExTMP) {
                            external.run(mmInStream, mmOutStream);
                            DataLogging.writeExcelFile(String.valueOf((int) external.getImperialUnit()), ExternalRow, true, place, false);
                        } else if (BatVoltz) {
                            battery.run(mmInStream, mmOutStream);
                            DataLogging.writeExcelFile(String.valueOf(battery.getFormattedResult()), BattaryRow, true, place, false);
                        }
                        long timeStamp = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());//Unix Timestamp
                        java.util.Date time = new java.util.Date(timeStamp * 1000);//Unix to normal time
                        DataLogging.writeExcelFile(time.toString(), TimeRow, true, place, false);

                        Message msg = mHandler.obtainMessage(1);
                        Bundle bundle = new Bundle();
                        bundle.putString("toast", "added entry");
                        msg.setData(bundle);
                        mHandler.sendMessage(msg);
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
            DataLogging.closeExcelFile();
            super.onPostExecute(aVoid);
        }
    }

    public static class DataLogging {


        public static boolean getExcelFile(Context context, String fileName) {

            boolean success = false;

            file = new File(context.getExternalFilesDir(null), fileName);

            //if (!file.exists()) {

            //New Workbook
            wb = new HSSFWorkbook();

            //Cell c = null;

            //Cell style for header row
            //CellStyle cs = wb.createCellStyle();
            //cs.setFillForegroundColor(HSSFColor.WHITE.index);
            //cs.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

            //New Sheet
            mySheet = null;
            mySheet = wb.createSheet("logSheet1");

            // Generate column headings
            Cell c = null;

            CellStyle cs = wb.createCellStyle();
            cs.setFillForegroundColor(HSSFColor.WHITE.index);
            cs.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

            Row row1 = null;

            int nextRow = 0;

            if (IntakeTMP) {
                if (nextRow == 0) {
                    row1 = mySheet.createRow(nextRow);
                } else {
                    nextRow++;
                    row1 = mySheet.createRow(nextRow);
                }

                IntakeRow = nextRow;

                c = row1.createCell(0);
                c.setCellValue("IntakeTMP");
                c.setCellStyle(cs);

            }


            if (CoolantTMP) {
                if (nextRow == 0) {
                    row1 = mySheet.createRow(nextRow);
                } else {
                    nextRow++;
                    row1 = mySheet.createRow(nextRow);
                }

                CoolantRow = nextRow;

                c = row1.createCell(0);//colum
                c.setCellValue("CoolantTMP");
                c.setCellStyle(cs);
            }


            if (ExTMP) {
                if (nextRow == 0) {
                    row1 = mySheet.createRow(nextRow);
                } else {
                    nextRow++;
                    row1 = mySheet.createRow(nextRow);
                }

                ExternalRow = nextRow;

                c = row1.createCell(0);//colum
                c.setCellValue("ExternalTMP");
                c.setCellStyle(cs);
            }

            if (nextRow == 0) {
                row1 = mySheet.createRow(nextRow);
            } else {
                nextRow++;
                row1 = mySheet.createRow(nextRow);
            }

            TimeRow = nextRow;

            c = row1.createCell(0);//colum
            c.setCellValue("Time");
            c.setCellStyle(cs);


            //}
                /*
            else {

                try {
                    FileInputStream myInput = new FileInputStream(file);
                    POIFSFileSystem myFileSystem = new POIFSFileSystem(myInput);
                    HSSFWorkbook myWorkBook = new HSSFWorkbook(myFileSystem);
                    mySheet = myWorkBook.getSheet("logSheet1");
                    //Toast.makeText(context, "Found sheet", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
            */
            /*
            try {
                os = new FileOutputStream(file);
                wb.write(os);
                success = true;
            } catch (IOException e) {
                Toast.makeText(context, "Error writing " + e, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(context, "Failed to save file" + e, Toast.LENGTH_SHORT).show();
            } finally {
                try {
                    //if (null != os)
                    //os.close();
                } catch (Exception ex) {
                    //Toast.makeText(context, "Found sheet =" + ex, Toast.LENGTH_SHORT).show();
                }
            }
            */
            return success;

        }

        public static void writeExcelFile(String string, int row, boolean Rowexists, int colum, boolean Colexists) {

            Cell c = null;

            CellStyle cs = wb.createCellStyle();
            cs.setFillForegroundColor(HSSFColor.WHITE.index);
            cs.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

            Row row1 = null;

            if (Rowexists) {
                row1 = mySheet.getRow(row);
            } else {
                row1 = mySheet.createRow(row);
            }
            if (!Colexists) {
                c = row1.createCell(colum);
            } else {
                c = row1.getCell(colum);
            }
            c.setCellValue(string);
            c.setCellStyle(cs);

        /*
            try {

                wb.write(os);
                Log.w("FileUtils", "Writing file" + file);
            } catch (IOException e) {
                Log.w("FileUtils", "Error writing " + file, e);
            } catch (Exception e) {
                Log.w("FileUtils", "Failed to save file", e);
            }
            */
        }

        public static void closeExcelFile() {

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
