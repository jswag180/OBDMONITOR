package swagyyydevelopment.jswag180.com.obdmonitor;

import android.app.ProgressDialog;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.HandlerThread;
import android.os.IBinder;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

public class BtCon extends Service {

    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public BtCon() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        address = InfoWrapper.getAdress();
        Toast.makeText(getApplicationContext(), "Attempting to connect to " + address, Toast.LENGTH_SHORT).show();

        boolean ConnectSuccess = true;
        //progress = ProgressDialog.show(BtCon.this, "Connecting...", "Please wait!!!");
        try {
            if (btSocket == null || !isBtConnected) {
                btSocket = null;
                myBluetooth = null;
                myBluetooth = BluetoothAdapter.getDefaultAdapter();
                BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);
                btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);
                BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                btSocket.connect();
            }
        } catch (IOException e) {
            ConnectSuccess = false;
        }

        if (!ConnectSuccess) {
            msg("Connection Failed.");
            stopSelf();
        } else {
            msg("Connected.");
            isBtConnected = true;
            Socket.setSocket(btSocket);
            stopSelf();

        }

        //progress.dismiss();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {

        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Thread.NORM_PRIORITY);
        thread.start();

    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

}
