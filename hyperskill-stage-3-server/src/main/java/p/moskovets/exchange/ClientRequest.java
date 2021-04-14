package p.moskovets.exchange;

import com.google.gson.JsonElement;
import p.moskovets.server.Action;

import java.util.List;

public class ClientRequest {

    private final String type;
    private final JsonElement key;
    private final JsonElement value;

    public ClientRequest(String type, JsonElement key, JsonElement value) {
        this.type = type;
        this.key = key;
        this.value = value;
    }


    public String getType() {
        return type;
    }

    public JsonElement getKey() {
        return key;
    }

    public JsonElement getValue() {
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
