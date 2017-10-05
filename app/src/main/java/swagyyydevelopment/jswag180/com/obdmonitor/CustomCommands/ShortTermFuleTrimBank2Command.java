package swagyyydevelopment.jswag180.com.obdmonitor.CustomCommands;

//

import com.github.pires.obd.commands.ObdCommand;

public class ShortTermFuleTrimBank2Command extends ObdCommand {

    private String data = "";

    public ShortTermFuleTrimBank2Command() {

        super("01 08");

    }

    public ShortTermFuleTrimBank2Command(ShortTermFuleTrimBank2Command other) {
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
        return "ShortTermFuleTrimBank2";
    }

}
