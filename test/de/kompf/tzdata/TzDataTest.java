package de.kompf.tzdata;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * JUnit test for {@link TzDataShpFileReadAndLocate}
 * 
 */
public class TzDataTest {

  private static TzDataShpFileReadAndLocate tzdata;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    final String shpfile = System.getProperty("user.home") + "/projects/tz/dist/combined_shapefile.shp";
    tzdata = new TzDataShpFileReadAndLocate();
    tzdata.openInputShapefile(shpfile);
  }

  @Test
  public void testRegular() throws IOException {
    assertEquals("Europe/Berlin", tzdata.process(9., 50.));
  }

  @Test
  public void testInner() throws IOException {
    assertEquals("Europe/Vatican", tzdata.process(12.45332, 41.90236));
  }

  @Test
  public void testNearShore() throws IOException {
    assertEquals("Europe/Zagreb", tzdata.process(16.83655, 43.21218));
  }

  @Test
  public void testNearest() throws IOException {
    assertEquals("Europe/Rome", tzdata.process(13.72467, 45.64861));
    assertEquals("Europe/Ljubljana", tzdata.process(13.70132, 45.55637));
  }

  @Test
  public void testOffShore() throws IOException {
    assertEquals("", tzdata.process(-23.73047, 47.93107));
  }

}
