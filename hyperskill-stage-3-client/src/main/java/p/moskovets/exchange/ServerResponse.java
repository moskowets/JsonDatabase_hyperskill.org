package p.moskovets.exchange;

public class ServerResponse {
    final String response;
    final String value;
    final String reason;

    public ServerResponse(String response, String value, String reason) {
        this.response = response;
        this.value = value;
        this.reason = reason;
    }

    public String getResponse() {
        return response;
    }

    public String getValue() {
        return value;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("ServerResponse{")
                .append("response: ").append(response)
                .append(", value: ").append(value)
                .append(", reason: ").append(reason)
                .append("}").toString();
    }
}
