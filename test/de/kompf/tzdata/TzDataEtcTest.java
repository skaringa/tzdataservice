package de.kompf.tzdata;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * JUnit test for {@link TzDataEtc}
 * 
 */
public class TzDataEtcTest {

  @Test
  public void testGMT() {
    assertEquals("Etc/GMT", TzDataEtc.tzNameFromLon(0.));
  }

  @Test
  public void testEast() {
    assertEquals("Etc/GMT-1", TzDataEtc.tzNameFromLon(15.));
  }

  @Test
  public void testWest() {
    assertEquals("Etc/GMT+4", TzDataEtc.tzNameFromLon(-60.));
  }

  @Test
  public void testTransition() {
    assertEquals("Etc/GMT", TzDataEtc.tzNameFromLon(7.45));
    assertEquals("Etc/GMT-1", TzDataEtc.tzNameFromLon(7.55));
  }

  @Test
  public void test180() {
    assertEquals("Etc/GMT-12", TzDataEtc.tzNameFromLon(179.5));
    assertEquals("Etc/GMT+12", TzDataEtc.tzNameFromLon(-179.5));
  }
}
