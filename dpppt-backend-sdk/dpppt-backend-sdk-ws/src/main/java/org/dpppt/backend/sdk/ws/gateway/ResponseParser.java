package org.dpppt.backend.sdk.ws.gateway;

import com.google.gson.*;
import org.dpppt.backend.sdk.model.Exposee;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ResponseParser {

    private final Gson gson;

    private class DataGet {
        public String ExposeeKey;
        public Date KeyDate;
        public ArrayList<String> ExposeeCountries;
    }

    private class Data {
        public String key;
        public ArrayList<String> countries;
        public Date date;
    }

    private class DataPost {
        public List<Data> keys;

        public DataPost() {
            this.keys = new ArrayList<>();
        }
    }

    public ResponseParser() {
        this.gson = new Gson();
    }

    public List<Exposee> getExposeeList(String json) {
        List<Exposee> list = new ArrayList<>();
        DataGet[] dataArray = gson.fromJson(json, DataGet[].class);
        for(DataGet data : dataArray) {
            Exposee exposee = new Exposee();
            exposee.setKey(data.ExposeeKey);
            exposee.setKeyDate(data.KeyDate.getTime());
            exposee.setCountryCodeList(data.ExposeeCountries);
            list.add(exposee);
        }
        return list;
    }

    public String getJson(List<Exposee> list) {
        DataPost data = new DataPost();
        for(Exposee e : list) {
            Data inData = new Data();
            inData.key = e.getKey();
            inData.countries = e.getCountryCodeList();
            inData.date = new Date(e.getKeyDate());
            data.keys.add(inData);
        }
        return gson.toJson(data);
    }

    public String prettifyJson(String uglyJson) {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create();

        return gson.toJson(JsonParser.parseString(uglyJson));
    }

}
