package swagyyydevelopment.jswag180.com.obdmonitor;


public class devMode {

    private static boolean devModeEnabled = false;

    public static void setDevMode(boolean isEnabled) {
        devMode.devModeEnabled = isEnabled;
    }

    public static boolean getDevMode() {
        return devMode.devModeEnabled;
    }

}
