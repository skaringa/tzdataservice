package de.kompf.tzdata.rest;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.NoSuchElementException;
import java.util.Scanner;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

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
  public static void main(String[] args) throws IOException {
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

  private WebResource service;

  public TzDataClient() {
    service = Client.create().resource("http://localhost:28100/tz/bylonlat");
  }

  public String process(String lon, String lat) {
    return service.path(lon).path(lat).get(String.class);
  }
}
