package swagyyydevelopment.jswag180.com.obdmonitor.CustomCommands;

//

import com.github.pires.obd.commands.ObdCommand;

public class ShortTermFuleTrimBank1Command extends ObdCommand {

    private String data = "";

    public ShortTermFuleTrimBank1Command() {

        super("01 06");

    }

    public ShortTermFuleTrimBank1Command(ShortTermFuleTrimBank1Command other) {
        super(other);
    }

    @Override
    protected void performCalculations() {
        data = String.valueOf(buffer.get(2) / 1.28 - 100);// - 40
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
        return "ShortTermFuleTrimBank1";
    }
}


