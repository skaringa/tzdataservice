package de.kompf.tzdata.rest;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * JUnit test for {@link TzDataService}
 * 
 */
public class TzDataTest {

  private static HttpClient client;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    client = HttpClient.newHttpClient();
  }

  @Test
  public void testRegular() throws Exception {
    assertEquals("Europe/Berlin", process("9","50"));
  }

  @Test
  public void testInner() throws Exception {
    assertEquals("Europe/Vatican", process("12.45332", "41.90236"));
  }

  @Test
  public void testNearShore() throws Exception {
    assertEquals("Europe/Zagreb", process("16.83655", "43.21218"));
  }

  @Test
  public void testOffShore() throws Exception {
    assertEquals("Etc/GMT+2", process("-23.73047", "47.93107"));
    assertEquals("Etc/GMT", process("7.45", "0"));
    assertEquals("Etc/GMT-1", process("7.55", "0"));
  }

  private String process(String lon, String lat) throws URISyntaxException, IOException, InterruptedException {
    HttpRequest request = HttpRequest.newBuilder(
        new URI(String.format("http://localhost:28100/tz/bylonlat/%s/%s", lon, lat)))
        .GET()
        .build();
    HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
    return response.body();
  }
}
