package org.dpppt.backend.sdk.ws.gateway;

import org.dpppt.backend.sdk.data.gaen.GAENDataService;
import org.dpppt.backend.sdk.model.Exposee;
import org.dpppt.backend.sdk.data.DPPPTDataService;
import org.dpppt.backend.sdk.ws.security.secrets.Secrets;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.*;

public class Gateway {

    private DPPPTDataService dataService;
//    private GAENDataService dataService;
    private final String appSource;
    private Secrets secrets;
    private Map<String, String> lastBatchTagMap;
    private Map<String, Set<String>> toBeDownloaded;
    private final ResponseParser responseParser;

    public Gateway(DPPPTDataService dataService, String appSource) {
        this.dataService = dataService;
        this.appSource = appSource;
        this.secrets = new Secrets();
        this.lastBatchTagMap = new HashMap<>();
        this.toBeDownloaded = new HashMap<>();
        this.responseParser = new ResponseParser();
    }

//    public Gateway(GAENDataService dataService, String appSource) {
//        this.dataService = dataService;
//        this.appSource = appSource;
//        this.secrets = new Secrets();
//        this.lastBatchTagMap = new HashMap<>();
//        this.toBeDownloaded = new HashMap<>();
//        this.responseParser = new ResponseParser();
//    }

    //TODO: check if content with specific batchTag has been downloaded
    public void addToBeDownloaded(String date, String batchTag) {
        Set<String> set = toBeDownloaded.containsKey(date) ? toBeDownloaded.get(date) : new HashSet<>();
        set.add(batchTag);
        toBeDownloaded.put(date,set);
    }

    public boolean downloadNewKeys(String date)
    {
        try {
            HttpResponse<String> response = download(date, lastBatchTagMap.get(date));
            List<Exposee> exposees = responseParser.getExposeeList(response.body());
            dataService.upsertExposees(exposees, appSource, "global");
            lastBatchTagMap.put(date, response.headers().map().get("batchTag").get(0));
            //Console output
            System.out.println("Download Response:");
            System.out.println(responseParser.prettifyJson(response.body()));
            return true;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    private HttpResponse<String> download(String date, String batchTag)
            throws Exception
    {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create("http://iti-386.iti.gr:57000/api/diagnosiskeys/download/"+date))
                .setHeader("content-type", "application/json")
                .setHeader("token", secrets.getValue("token"));

        if(batchTag != null) {
            requestBuilder.setHeader("batchTag", batchTag);
        }
        HttpRequest request = requestBuilder.build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        return response;
    }

    public boolean uploadNewKeys(long interval)
    {
        try{
            List<Exposee> exposees = dataService.getLocalExposedForBatchReleaseTime(Instant.now().toEpochMilli(), interval);
            if(exposees.size() > 0) {
                String json = responseParser.getJson(exposees);
                HttpResponse<String> response = upload(json, "a", "b");
                //Console output :
                System.out.println(responseParser.prettifyJson(json));
                System.out.println(response.statusCode() == 200 ? "Upload complete!" : "Upload Error!\n" + response.body());
            }
            else {System.out.println("There are no new keys to upload.");}
            return true;
        }
        catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    private HttpResponse<String> upload(String requestBody, String batchTag, String batchSignature)
            throws Exception
    {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://iti-386.iti.gr:57000/api/diagnosiskeys/update/"))
                .setHeader("token", secrets.getValue("token"))
                .setHeader("content-type", "application/json")
                .setHeader("batchTag", batchTag)
                .setHeader("batchSignature", batchSignature)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        return response;
    }

}
