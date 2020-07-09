package org.dpppt.backend.sdk.ws.gateway;

import com.google.gson.Gson;
import org.dpppt.backend.sdk.model.Exposee;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ResponseParser {

    private final String jsonString;

    private final Gson gson;

    public class Data {
        public String ExposeeKey;
        public Date KeyDate;
        public ArrayList<String> ExposeeCountries;
    }

    public ResponseParser(String json) {
        this.jsonString = json;
        this.gson = new Gson();
    }

    public List<Exposee> getExposeeList() {
        List<Exposee> list = new ArrayList<>();
        Data[] dataArray = gson.fromJson(jsonString, Data[].class);
        for(Data data : dataArray) {
            Exposee exposee = new Exposee();
            exposee.setKey(data.ExposeeKey);
            exposee.setKeyDate(data.KeyDate.getTime());
            exposee.setCountryCodeList(data.ExposeeCountries);
            list.add(exposee);
        }
        return list;
    }

}
