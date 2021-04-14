package p.moskovets.exchange;

import com.beust.jcommander.Parameter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.io.Serializable;
import java.util.Map;

public class Command implements Serializable {

    private final String TYPE = "type";
    private final String KEY = "key";
    private final String VALUE = "value";

    @Parameter
    private List<String> parameters = new ArrayList<>();

    @Parameter(names = "-t", description = "Type of the request", converter = CommandType.class)
    private CommandType type = CommandType.exit;

    @Parameter(names = "-k", description = "Index of the cell")
    private String key = "";

    @Parameter(names = "-v", description = "Value to save in the database")
    private String value = "";

    @Parameter(names = "-in", description = "Read a request from that file")
    private String fileName = "";

    public Map<String, String> getMap() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put(TYPE, type.toString());
        switch (type) {
            case set:
                map.put(KEY, key);
                map.put(VALUE, value);
                break;
            case get:
            case delete:
                map.put(KEY, key);
                break;
        }
        return map;
    }

    @Override
    public String toString() {

        switch (type) {
            case set:
                return type + " " + key + " " + value;
            case get:
            case delete:
                return type + " " + key;
            case exit:
                return type.toString();
            default:
                return "";
        }
    }

    public String getFileName() {
        return fileName;
    }
}
