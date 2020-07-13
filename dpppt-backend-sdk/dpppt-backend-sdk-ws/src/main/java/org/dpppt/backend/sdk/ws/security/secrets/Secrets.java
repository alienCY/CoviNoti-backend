package org.dpppt.backend.sdk.ws.security.secrets;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class Secrets {

    private final String secretAdr = "dpppt-backend-sdk/dpppt-backend-sdk-ws/src/main/java/org/dpppt/backend/sdk/ws/security/secrets/secrets.json";
    private Map<String, String> values;

    public Secrets() {
        try {
            Reader reader = Files.newBufferedReader(Paths.get(secretAdr));
            Type type = new TypeToken<Map<String, String>>(){}.getType();
            values = new Gson().fromJson(reader, type);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public String getValue(String key) {
        return values.get(key) != null ? values.get(key) : null;
    }
}
