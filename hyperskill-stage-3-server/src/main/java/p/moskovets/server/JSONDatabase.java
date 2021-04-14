package p.moskovets.server;

import com.google.gson.*;
import p.moskovets.exchange.ClientRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@FunctionalInterface
interface Func<R> {
    R execute();
}

class JSONDatabase {

    private final JsonElement ERROR_MSG = new JsonPrimitive("ERROR");
    private final JsonElement OK_MSG = new JsonPrimitive("OK");
    private final JsonElement NO_SUCH_KEY = new JsonPrimitive("No such key");

    private final String RESPONSE = "response";
    private final String VAL = "value";
    private final String REASON = "reason";


    private final Path filePath = Path.of("src\\server\\data\\db.json");

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();
    private final Gson gson;

    private JsonObject database;

    public JSONDatabase(Gson gson) {
        this.gson = gson;
        readFile();
    }

    private Map<String, JsonElement> getNoSuchKeyResponse() {
        LinkedHashMap<String, JsonElement> map = new LinkedHashMap<>();
        map.put(RESPONSE, ERROR_MSG);
        map.put(REASON, NO_SUCH_KEY);
        return map;
    }

    private Map<String, JsonElement> getOKResponse() {
        LinkedHashMap<String, JsonElement> map = new LinkedHashMap<>();
        map.put(RESPONSE, OK_MSG);
        return map;
    }

    private Map<String, JsonElement> getValueResponse(JsonElement value) {
        Map<String, JsonElement> map = getOKResponse();
        map.put(VAL, value);
        return map;
    }

    private void readFile() {
        try {
            database = gson.fromJson(new String(Files.readAllBytes(filePath)), JsonObject.class);
        } catch (IOException e) {
            try {
                Files.createDirectories(filePath.getParent());

            } catch (Exception ex) {

            }
            database = new JsonObject();
        }
        System.out.println(filePath.toAbsolutePath());
    }

    private void updateFile() {
        try {
            Files.writeString(filePath,
                    gson.toJson(database),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            return;
        }
    }

    private synchronized Map<String, JsonElement> modify(Func<Map<String, JsonElement>> func) {
        writeLock.lock();
        Map<String, JsonElement> retval = func.execute();
        updateFile();
        writeLock.unlock();
        return retval;
    }


    private synchronized Map<String, JsonElement> read(Func<Map<String, JsonElement>> func) {
        readLock.lock();
        Map<String, JsonElement> retval = func.execute();
        readLock.unlock();
        return retval;
    }

    private Map<String, JsonElement> getFromJson(Iterator<JsonElement> keys, JsonElement dbase) {
        if (keys.hasNext()) {
            String key = keys.next().getAsString();
            if (dbase.isJsonObject()) {
                JsonObject jsonObject = (JsonObject) dbase;
                if (jsonObject.has(key)) {
                    if (keys.hasNext()) {
                        return getFromJson(keys, jsonObject.get(key));
                    } else {
                        return getValueResponse(jsonObject.get(key));
                    }
                } else {
                    return getNoSuchKeyResponse();
                }
            } else {
                return getNoSuchKeyResponse();
            }
        } else {
            return getNoSuchKeyResponse();
        }
    }

    private void writeToJson(Iterator<JsonElement> keys, JsonElement value, JsonElement dbase, JsonElement parent, String currentKey) {
        JsonObject jsonObject = null;
        if ((keys != null && keys.hasNext()) || currentKey != null) {
            String key = currentKey != null ? currentKey : keys.next().getAsString();
            if (dbase.isJsonObject()) {
                jsonObject = (JsonObject) dbase;
                if (keys != null && keys.hasNext()) {
                    if (!jsonObject.has(key)) {
                        jsonObject.add(key, new JsonObject());
                    }
                    writeToJson(keys, value, jsonObject.get(key), dbase, null);
                } else {
                    if (jsonObject.has(key)) {
                        jsonObject.remove(key);
                    }
                    jsonObject.add(key, value);
                }
            } else if ((dbase.isJsonPrimitive() || dbase.isJsonArray() || dbase.isJsonNull()) && parent != null) {
                jsonObject = parent.getAsJsonObject();
                jsonObject.remove(key);
                jsonObject.add(key, new JsonObject());
                writeToJson(keys, value, dbase, null, key);
            }
        }
    }

    private Map<String, JsonElement> deleteInJson(Iterator<JsonElement> keys, JsonElement dbase) {
        JsonObject jsonObject = null;
        if (keys.hasNext()) {
            String key = keys.next().getAsString();
            if (dbase.isJsonObject()) {
                jsonObject = (JsonObject) dbase;
                if (keys.hasNext()) {
                    if (jsonObject.has(key)) {
                        return deleteInJson(keys, jsonObject.get(key));
                    } else {
                        return getNoSuchKeyResponse();
                    }
                } else {
                    if (jsonObject.has(key)) {
                        jsonObject.remove(key);
                        return getOKResponse();
                    } else {
                        return getNoSuchKeyResponse();
                    }
                }
            } else if ((dbase.isJsonPrimitive() || dbase.isJsonArray() || dbase.isJsonNull()) ) {
                return getNoSuchKeyResponse();
            }
        }
        return getNoSuchKeyResponse();
    }

    private Map<String, JsonElement> get(JsonElement key) {
        if (key.isJsonArray()) {
            return getFromJson(((JsonArray) key).iterator(), database);
        } else if (key.isJsonPrimitive()) {
            JsonArray array = new JsonArray();
            array.add(key);
            return getFromJson(array.iterator(), database);
        } else {
            return getNoSuchKeyResponse();
        }
    }

    private Map<String, JsonElement> set(JsonElement key, JsonElement value) {
        if (key.isJsonArray()) {
            writeToJson(((JsonArray) key).iterator(), value, database, null, null);
        } else if (key.isJsonPrimitive()) {
            writeToJson(null, value, database, null, key.getAsString());
        }
        return getOKResponse();
    }

    private Map<String, JsonElement> delete(JsonElement key) {
        if (key.isJsonArray()) {
            return deleteInJson(((JsonArray) key).iterator(), database);
        } else if (key.isJsonPrimitive()) {
            JsonArray array = new JsonArray();
            array.add(key);
            return deleteInJson(array.iterator(), database);
        } else {
            return getNoSuchKeyResponse();
        }
    }

    private Map<String, JsonElement> defaultResponse() {
        return getOKResponse();
    }

    private Map<String, JsonElement> getWrapper(JsonElement key) {
        return read(() -> get(key));
    }

    private Map<String, JsonElement> setWrapper(JsonElement key, JsonElement value) {
        return modify(() -> set(key, value));
    }

    private Map<String, JsonElement> deleteWrapper(JsonElement key) {
        return modify(() -> delete(key));
    }

    Map<String, JsonElement> process(String requestString, Procedure shutDown) {

        ClientRequest request = gson.fromJson(requestString, ClientRequest.class);

        switch (request.getCommandType()) {
            case get:
                return getWrapper(request.getKey());
            case set:
                return setWrapper(request.getKey(), request.getValue());
            case delete:
                return deleteWrapper(request.getKey());
            case exit:
                shutDown.execute();
            default:
                return defaultResponse();
        }
    }

}