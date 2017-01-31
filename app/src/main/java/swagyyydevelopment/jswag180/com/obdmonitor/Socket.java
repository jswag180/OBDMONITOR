package swagyyydevelopment.jswag180.com.obdmonitor;


import android.bluetooth.BluetoothSocket;

public class Socket {

    private static BluetoothSocket socket;

    public static void setSocket(BluetoothSocket socketpass) {
        Socket.socket = socketpass;
    }

    public static BluetoothSocket getSocket() {
        return Socket.socket;
        //return socket;
    }

}
