package org.dpppt.backend.sdk.data;

import org.dpppt.backend.sdk.model.Exposee;
import org.dpppt.backend.sdk.model.ExposeeDoc;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ExposeeDocMapper {

    public List<Exposee> unDoc(List<ExposeeDoc> docList) {
        List<Exposee> list = new ArrayList<>();
        for(var e : docList) {
            Exposee exposee = new Exposee();
            exposee.setKey(e.getKey());
            exposee.setKeyDate(e.getKeyDate().getTime());
            exposee.setCountryCodeList(e.getCountryCodeList());
            list.add(exposee);
        }
        return list;
    }

    public ExposeeDoc toDoc(Exposee exposee, String appSource) {
        ExposeeDoc exposeeDoc = new ExposeeDoc();
        exposeeDoc.setKey(exposee.getKey());
        exposeeDoc.setAppSource(appSource);
        exposeeDoc.setKeyDate(new Date(exposee.getKeyDate()));
        exposeeDoc.setReceivedAt(new Date(System.currentTimeMillis()));
        exposeeDoc.setCountryCodeList(exposee.getCountryCodeList());
        return exposeeDoc;
    }

}
