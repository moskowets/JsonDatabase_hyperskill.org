package p.moskovets.exchange;
import com.beust.jcommander.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

public class Command implements Serializable {

    @Parameter
    private List<String> parameters = new ArrayList<>();

    @Parameter(names = "-t", description = "Type of the request", converter = CommandType.class)
    private CommandType type;

    @Parameter(names = "-i", description = "Index of the cell")
    private int index;

    @Parameter(names = "-m", description = "Value to save in the database")
    private String message;

    public CommandType getType() {
        return type;
    }

    public int getIndex() {
        return index;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {

        switch (type) {
            case set:
                return type + " " + index + " " + message;
            case get:
            case delete:
                return type + " " + index;
            case exit:
                return type.toString();
            default:
                return "";
        }
    }
}
