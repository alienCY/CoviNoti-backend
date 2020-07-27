/*
Model for documents stored in Mongo's collection "exposed"
*/

package org.dpppt.backend.sdk.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;

@Document
public class ExposeeDoc {

    @Id
    @JsonIgnore
    private String Id;

    @NotNull
    private String key;

    @NotNull
    private Date received_at;

    @NotNull
    private Date keyDate;

    @NotNull
    private ArrayList<String> countryCodeList;

    @NotNull
    private String appSource;

    public void setReceivedAt(Date time) {
        this.received_at = time;
    }

    public Date getReceivedAt() {
        return received_at;
    }

    public String getAppSource() {
        return appSource;
    }

    public void setAppSource(String appSource) {
        this.appSource = appSource;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @JsonIgnore
    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public Date getKeyDate() {
        return keyDate;
    }

    public void setKeyDate(Date keyDate) {
        this.keyDate = keyDate;
    }

    public ArrayList<String> getCountryCodeList() {
        return countryCodeList;
    }

    public void setCountryCodeList(ArrayList<String> countryCodeList) {
        this.countryCodeList = countryCodeList;
    }
}