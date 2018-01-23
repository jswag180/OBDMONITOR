package swagyyydevelopment.jswag180.com.obdmonitor.CustomCommands;

import com.github.pires.obd.commands.ObdCommand;

public class BatteryVoltzCommand extends ObdCommand {

    private String data = "";

    public BatteryVoltzCommand() {

        super("AT RV");

    }

    public BatteryVoltzCommand(BatteryVoltzCommand other) {
        super(other);
    }

    @Override
    protected void performCalculations() {
        data = String.valueOf(buffer.get(2));// - 40
    }

    @Override
    public String getFormattedResult() {
        return data;
    }

    @Override
    public String getCalculatedResult() {
        return data;
    }

    @Override
    public String getName() {
        return "GETBETTVOLTZ";
    }
}
