package org.dpppt.backend.sdk.ws.gateway;

import java.util.*;

public class DownloadBatchTags {
    private Map<String, String> lastBatchTagMap;

    private Map<String, Set<String> > toBeDownloaded;

    public DownloadBatchTags() {

        this.lastBatchTagMap = new HashMap<>();
        this.toBeDownloaded = new HashMap<>();
    }

    public void setLastBatchTag(String date, String batchTag) {
        lastBatchTagMap.put(date,batchTag);
    }

    public String getLastBatchTag(String date) {
        return lastBatchTagMap.get(date);
    }

    public void addToBeDownloadedBatchTag(String date, String batchTag) {
        Set<String> set = toBeDownloaded.containsKey(date) ? toBeDownloaded.get(date) : new HashSet<>();
        set.add(batchTag);
        toBeDownloaded.put(date,set);
    }

}
