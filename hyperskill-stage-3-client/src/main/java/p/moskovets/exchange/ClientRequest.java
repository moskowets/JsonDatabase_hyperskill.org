package p.moskovets.exchange;

public class ClientRequest {

    private final String type;
    private final String key;
    private final String value;

    public ClientRequest(String type, String key, String value) {
        this.type = type;
        this.key = key;
        this.value = value;
    }


    public String getType() {
        return type;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public CommandType getCommandType() {
        return CommandType.valueOf(type);
    }

    @Override
    public String toString() {
        return new StringBuilder().append("ClientRequest{")
                .append("type: ").append(type)
                .append(", key: ").append(key)
                .append(", value: ").append(value)
                .append("}").toString();
    }
}
