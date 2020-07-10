package org.dpppt.backend.sdk.ws.gateway;

import org.dpppt.backend.sdk.model.Exposee;
import org.dpppt.backend.sdk.data.DPPPTDataService;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.*;

public class Gateway {

    private final DPPPTDataService dataService;
    private final String appSource;
    private Map<String, String> lastBatchTagMap;
    private Map<String, Set<String>> toBeDownloaded;
    private final ResponseParser responseParser;

    public Gateway(DPPPTDataService dataService, String appSource) {
        this.dataService = dataService;
        this.appSource = appSource;
        this.lastBatchTagMap = new HashMap<>();
        this.toBeDownloaded = new HashMap<>();
        this.responseParser = new ResponseParser();
    }

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
            dataService.upsertExposees(exposees, appSource);
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
                .setHeader("token", "24beac0f63eaf1183893f19ba2f7d3b008fb889e");

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
            //TODO: query better to get only Cypriots
            List<Exposee> exposees = dataService.getSortedExposedForBatchReleaseTimeAndCountry(Instant.now().toEpochMilli(), interval, "CY");
            String json = responseParser.getJson(exposees);
            HttpResponse<String> response = upload(json,"anything","a");
            //Console output :
            System.out.println(responseParser.prettifyJson(json));
            System.out.println(response.statusCode() == 200 ? "Upload complete!" : "Upload Error!\n" + response.body());
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
                .setHeader("token", "24beac0f63eaf1183893f19ba2f7d3b008fb889e")
                .setHeader("content-type", "application/json")
                .setHeader("batchTag", batchTag)
                .setHeader("batchSignature", batchSignature)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        return response;
    }

}
