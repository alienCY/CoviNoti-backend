package org.dpppt.backend.sdk.model.gaen;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Date;

@Document
public class GaenKeyDoc {

    @Id
    @JsonIgnore
    private String Id;

    @NotNull
    @Size(min = 24, max = 24)
    String key;

    @NotNull
    Integer rollingStartNumber;

    @NotNull
    Integer rollingPeriod;

    @NotNull
    Integer transmissionRiskLevel;

    @NotNull
    String appSource;

    @NotNull
    private ArrayList<String> countryCodeList;

    @NotNull
    private Date receivedAt;

    Integer fake = 0;

    public String getKey() {
        return this.key;
    }

    public void setKey(String keyData) {
        this.key = keyData;
    }

    public Integer getRollingStartNumber() {
        return this.rollingStartNumber;
    }

    public void setRollingStartNumber(Integer rollingStartNumber) {
        this.rollingStartNumber = rollingStartNumber;
    }

    public Integer getRollingPeriod() {
        return this.rollingPeriod;
    }

    public void setRollingPeriod(Integer rollingPeriod) {
        this.rollingPeriod = rollingPeriod;
    }

    public Integer getTransmissionRiskLevel() {
        return this.transmissionRiskLevel;
    }

    public void setTransmissionRiskLevel(Integer transmissionRiskLevel) {
        this.transmissionRiskLevel = transmissionRiskLevel;
    }

    public String getAppSource() { return appSource; }

    public void setAppSource(String appSource) {
        this.appSource = appSource;
    }

    public ArrayList<String> getCountryCodeList() {
        return countryCodeList;
    }

    public void setCountryCodeList(ArrayList<String> countryCodeList) {
        this.countryCodeList = countryCodeList;
    }

    public Date getReceivedAt() { return this.receivedAt; }

    public void setReceivedAt(Date date) { this.receivedAt = date; }
}
