package de.kompf.tzdata.rest;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

/**
 * JUnit test for {@link TzDataService}
 * 
 */
public class TzDataTest {

  private static WebResource service;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    service = Client.create().resource("http://localhost:28100/tz/bylonlat");
  }

  @Test
  public void testRegular() throws IOException {
    assertEquals("Europe/Berlin", service.path("9").path("50").get(String.class));
  }

  @Test
  public void testInner() throws IOException {
    assertEquals("Europe/Vatican", service.path("12.45332").path("41.90236").get(String.class));
  }

  @Test
  public void testNearShore() throws IOException {
    assertEquals("Europe/Zagreb", service.path("16.83655").path("43.21218").get(String.class));
  }

  @Test
  public void testOffShore() throws IOException {
    assertEquals("Etc/GMT+2", service.path("-23.73047").path("47.93107").get(String.class));
    assertEquals("Etc/GMT", service.path("7.45").path("0").get(String.class));
    assertEquals("Etc/GMT-1", service.path("7.55").path("0").get(String.class));
  }

}
