package de.kompf.tzdata;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * JUnit test for {@link TzDataShpFileReadAndLocate}.
 * If tests time zones in international waters and works only with
 * combined-shapefile-with-oceans.shp.
 * 
 */
public class TzDataEtcWithShapefileTest {

  private static TzDataShpFileReadAndLocate tzdata;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    final String shpfile = System.getProperty("user.home") + "/projects/tz/dist/combined-shapefile-with-oceans.shp";
    tzdata = new TzDataShpFileReadAndLocate();
    tzdata.openInputShapefile(shpfile);
  }

  @Test
  public void testGMT() throws IOException {
    assertEquals("Etc/GMT", tzdata.process(0., 0.));
  }

  @Test
  public void testEast() throws IOException {
    assertEquals("Etc/GMT-1", tzdata.process(15., -50.));
  }

  @Test
  public void testWest() throws IOException {
    assertEquals("Etc/GMT+4", tzdata.process(-60., 25.));
  }

  @Test
  public void testTransition() throws IOException {
    assertEquals("Etc/GMT", tzdata.process(7.45, 0.));
    assertEquals("Etc/GMT-1", tzdata.process(7.55, 0.));
  }

  @Test
  public void test180() throws IOException {
    assertEquals("Etc/GMT-12", tzdata.process(179.5, 0.));
    assertEquals("Etc/GMT+12", tzdata.process(-179.5, 0));
  }
}
