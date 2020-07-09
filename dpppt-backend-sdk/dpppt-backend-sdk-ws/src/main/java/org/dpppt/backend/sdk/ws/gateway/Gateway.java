package org.dpppt.backend.sdk.ws.gateway;

import org.dpppt.backend.sdk.model.Exposee;
import org.dpppt.backend.sdk.ws.gateway.ResponseParser;
import org.dpppt.backend.sdk.data.DPPPTDataService;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class Gateway extends DownloadBatchTags {

    private final DPPPTDataService dataService;
    private final String appSource;

    public Gateway(DPPPTDataService dataService, String appSource) {
        this.dataService = dataService;
        this.appSource = appSource;
    }

    public boolean downloadNewKeys(String date)
    {
        try {
            HttpResponse<String> response = download(date, getLastBatchTag(date));
            ResponseParser parser = new ResponseParser(response.body());
            List<Exposee> exposees = parser.getExposeeList();
            dataService.upsertExposees(exposees, appSource);
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

}
