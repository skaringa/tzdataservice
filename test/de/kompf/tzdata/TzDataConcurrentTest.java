package de.kompf.tzdata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * JUnit test for {@link TzDataShpFileReadAndLocate}.
 * 
 * It tests if concurrent calls of
 * {@link TzDataShpFileReadAndLocate#process(double, double)} are working.
 * 
 */
public class TzDataConcurrentTest {

  class Worker implements Callable<String> {

    private double xstart, ystart, xstop, ystop;
    private String expectedTz;

    public Worker(double xstart, double ystart, double xstop, double ystop, String expectedTz) {
      this.xstart = xstart;
      this.ystart = ystart;
      this.xstop = xstop;
      this.ystop = ystop;
      this.expectedTz = expectedTz;
    }

    @Override
    public String call() throws Exception {
      String result = "";
      System.out.println("Starting worker");
      for (double x = xstart; x < xstop; x += 0.1) {
        for (double y = ystart; y < ystop; y += 0.1) {
          try {
            result = tzdata.process(x, y);
            if (! expectedTz.equals(result)) {
              System.out.printf("Worker failed with result '%s' at (%.5f, %.5f)%n", result, x, y);
              return result;
            }
          } catch (IOException e) {
            result = e.toString();
          }
        }
      }
      System.out.println("Worker success with result " + result);
      return result;
    }
  }

  private static TzDataShpFileReadAndLocate tzdata;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    final String shpfile = System.getProperty("user.home") + "/projects/tz/dist/combined_shapefile.shp";
    tzdata = new TzDataShpFileReadAndLocate();
    tzdata.openInputShapefile(shpfile);
  }

  @Test
  public void testParallel() throws IOException, InterruptedException {
    ExecutorService executorService = Executors.newCachedThreadPool();
    
    Worker[] workers = new Worker[] {
        new Worker(7.366211, 51.19312, 13.38135, 53.47497, "Europe/Berlin"),
        new Worker(8.327637, 47.85003, 11.90918, 53.60554, "Europe/Berlin"),
        new Worker(-0.4394531, 43.70759, 5.756836, 48.98022, "Europe/Paris")
    };

    List<Future<String>> jobs = new LinkedList<>();
    for (Worker worker : workers) {
      jobs.add(executorService.submit(worker));
    }
    
    int i = 0;
    for (Future<String> job : jobs) {
      try {
        assertEquals(workers[i++].expectedTz, job.get());
      } catch (ExecutionException e) {
        fail(e.toString());
      }
    }
  }
}
