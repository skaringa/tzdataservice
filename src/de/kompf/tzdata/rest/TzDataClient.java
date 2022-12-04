package de.kompf.tzdata.rest;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * REST client to determine timezone ids from lat/lon pairs by asking
 * TzDataService.
 * 
 */
public class TzDataClient {

  /**
   * @param args
   * @throws IOException
   */
  public static void main(String[] args) throws Exception {
    TzDataClient tzDataClient = new TzDataClient();

    BufferedReader reader;
    if (args.length == 1) {
      reader = new BufferedReader(new InputStreamReader(new FileInputStream(args[0])));
    } else {
      reader = new BufferedReader(new InputStreamReader(System.in));
    }

    String line;
    while ((line = reader.readLine()) != null) {
      try (Scanner scanner = new Scanner(line)) {
        String lat = scanner.next();
        String lon = scanner.next();
        String tzid = tzDataClient.process(lon, lat);
        System.out.printf("%s\t%s\n", line, tzid);
      } catch (NoSuchElementException e) {
        // ignore
      }
    }

    reader.close();
  }

  private HttpClient client;

  public TzDataClient() {
    client = HttpClient.newHttpClient();
  }

  public String process(String lon, String lat) throws URISyntaxException, IOException, InterruptedException {
    HttpRequest request = HttpRequest.newBuilder(
        new URI(String.format("http://localhost:28100/tz/bylonlat/%s/%s", lon, lat)))
        .GET()
        .build();
    HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
    return response.body();
  }
}
