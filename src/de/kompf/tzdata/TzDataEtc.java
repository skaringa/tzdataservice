package de.kompf.tzdata;

/**
 * Helper to get the Etc timezone id for offshore regions (international
 * waters).
 * 
 */
public class TzDataEtc {

  /**
   * Compute the TZ offset from the longitude (valid in international waters).
   */
  public static int tzOffsetFromLon(double lon) {
    /*
     * In international waters, time zone boundaries are meridians 15 deg apart,
     * except that UTC-12 and UTC+12 are each 7.5 deg wide and are separated by
     * the 180 deg meridian
     * (not by the International Date Line, which is for land and territorial
     * waters only).
     * A captain can change ship's clocks any time after entering a new time
     * zone; midnight changes are common.
     */
    return (int) Math.floor((lon - 7.500000001) / 15) + 1;
  }

  /**
   * Compute the TZ name from the longitude (valid in international waters).
   */
  public static String tzNameFromLon(double lon) {
    String tzname;
    int tzOffset = tzOffsetFromLon(lon);
    if (tzOffset == 0) {
      tzname = "Etc/GMT";
    } else {
      tzname = String.format("Etc/GMT%+d", -tzOffset);
    }
    return tzname;
  }
}
