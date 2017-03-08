package swagyyydevelopment.jswag180.com.obdmonitor;

import com.github.pires.obd.commands.ObdCommand;

public class ENGGETCURENTGEAR extends ObdCommand {

    private int gear = 0;

    public ENGGETCURENTGEAR() {

        super("11 B3");

    }

    public ENGGETCURENTGEAR(ENGGETCURENTGEAR other) {
        super(other);
    }

    @Override
    protected void performCalculations() {
        gear = buffer.get(2);// - 40
    }

    @Override
    public String getFormattedResult() {
        return Integer.toString(gear);
    }

    @Override
    public String getCalculatedResult() {
        return null;
    }

    @Override
    public String getName() {
        return "GETGEAR";
    }
}
