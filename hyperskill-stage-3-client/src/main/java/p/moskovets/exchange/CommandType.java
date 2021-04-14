package p.moskovets.exchange;

import com.beust.jcommander.IStringConverter;

public enum CommandType implements IStringConverter<CommandType> {
    set("set"),
    get("get"),
    delete("delete"),
    exit("exit");

    private final String commandText;

    CommandType(String commandText) {
        this.commandText = commandText;
    }

    @Override
    public String toString() {
        return commandText;
    }

    @Override
    public CommandType convert(String value) {
        return CommandType.valueOf(value);
    }
}
