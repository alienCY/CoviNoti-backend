package org.dpppt.backend.sdk.data.gaen;

import org.dpppt.backend.sdk.model.gaen.GaenKey;
import org.dpppt.backend.sdk.model.gaen.GaenKeyDoc;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GaenKeyDocMapper {
    public List<GaenKey> unDoc(List<GaenKeyDoc> docList) {
        List<GaenKey> list = new ArrayList<>();
        for(var e : docList) {
            GaenKey exposee = new GaenKey();
            exposee.setKeyData(e.getKey());
            exposee.setRollingStartNumber(e.getRollingStartNumber());
            exposee.setRollingPeriod(e.getRollingPeriod());
            exposee.setTransmissionRiskLevel(e.getTransmissionRiskLevel());
            exposee.setCountryCodeList(e.getCountryCodeList());
            list.add(exposee);
        }
        return list;
    }

    public GaenKeyDoc toDoc(GaenKey exposee, String appSource) {
        GaenKeyDoc exposeeDoc = new GaenKeyDoc();
        exposeeDoc.setKey(exposee.getKeyData());
        exposeeDoc.setRollingStartNumber(exposee.getRollingPeriod());
        exposeeDoc.setRollingPeriod(exposee.getRollingPeriod());
        exposeeDoc.setTransmissionRiskLevel(exposee.getTransmissionRiskLevel());
        exposeeDoc.setAppSource(appSource);
        exposeeDoc.setCountryCodeList(exposee.getCountryCodeList());
        exposeeDoc.setReceivedAt(new Date(System.currentTimeMillis()));
        return exposeeDoc;
    }
}
