package de.kompf.tzdata.rest;

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

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

/**
 * JUnit test for {@link TzDataService}.
 * 
 * It tests if concurrent calls of
 * {@link TzDataService#bylonlat(String, String)} are working.
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
          result = service.path(String.valueOf(x)).path(String.valueOf(y)).get(String.class);
          if (!expectedTz.equals(result)) {
            System.out.printf("Worker failed with result '%s' at (%.5f, %.5f)%n", result, x, y);
            return result;
          }
        }
      }
      System.out.println("Worker success with result " + result);
      return result;
    }
  }

  private static WebResource service;;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    service = Client.create().resource("http://localhost:28100/tz/bylonlat");
  }

  @Test
  public void testParallel() throws IOException, InterruptedException {
    ExecutorService executorService = Executors.newCachedThreadPool();
    
    Worker[] workers = new Worker[] {
        new Worker(8, 52, 12, 53, "Europe/Berlin"),
        new Worker(9, 50, 11, 52, "Europe/Berlin"),
        new Worker(0, 46, 3, 48, "Europe/Paris")
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
