package javaweekly.org.practice.java.multithreading;



/*
Requirement:
Given a directory path and a search string, find its occurence in all the files
*/

/*
Approach:
1) Get the list of files in the directory
2) Create a thread pool 
3) Loop through the list of files in the directory 
       Create and submit a callable for each file in the directory that returns the list of lines 
       that contains the search term
4) Store the Futurue<Callable<List<String>>> in a list
5) Loop through the future list and extract the result.
*/

/*
Pool configuration
1) Create 10 core threads
2) 20 max threads
3) Bound the blocking queue with 20 tasks, so at any point in time a max. of 20 tasks 
can be executed in parallel.  
4) If the task count is lesser than or equal to 10 (core threads count), 
then the idle threads must be reaped to free-up memory.  For this we will specify max. idle time 
after which thread must be reaped.  This is considering that the pool is shared across searches.
If the pool is created and shutdown for each request, then idle thread reaping may not come into 
picture.  Or will it?  Suppose a directory containing 30 files must be searched, then 20 tasks 
will be submitted initially which will be served by max. 20 threads.  As 1 task completes, 
21st task will be executed by the 20th thread and so on will 30 tasks get dequed.  Suppose that 
the initial batch of 20 tasks are done, 10 remaining tasks will leave 10 threads idle.  So they 
must be reaped.d

*/
import javaweekly.org.practice.java.multithreading.util.GrepperUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.*;
import java.util.concurrent.*;
import java.util.function.*;

public class LogScrapper {
    private static ThreadPoolExecutor service = new CustomThreadPoolExecutor();
    private static Logger logger = LogManager.getLogger(LogScrapper.class.getName());

    public static void main(String [] args){
        search(args[0], args[1]);
    }

    public static void search(String path, String searchString){
    	try (Stream<Path> paths = Files.walk(Paths.get(path))) {
            try {
				List<Future<List<String>>> futureResults = service.invokeAll(GrepperUtil.getCallableSearchTasks(paths, searchString));
				while(true){
					if(service.getCompletedTaskCount() != futureResults.size()){
						continue;
					} else {
						break;
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			logger.error("IOException occurred: ", e);
		} finally {
			service.shutdown();
		}
	}

    static class CustomThreadPoolExecutor extends ThreadPoolExecutor{
        CustomThreadPoolExecutor(int corePoolSize, 
        	int maxPoolSize,
        	long keepAliveTime,
        	TimeUnit unit,
        	BlockingQueue blockingQueue) {
        	super(corePoolSize, maxPoolSize, keepAliveTime, unit, blockingQueue, 
        		SearchThreadFactory.newThreadFactory(),
        		new CallerRunsPolicy());
        }
        CustomThreadPoolExecutor(){
        	this(10, 20, 1, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(20));
        }

        private final ThreadLocal elapsed_time = new ThreadLocal();

        @Override
		public void beforeExecute(Thread t, Runnable r){
			elapsed_time.set(System.nanoTime());
        }

		@Override
		public void afterExecute(Runnable r, Throwable t){
        	long startTime = (Long) elapsed_time.get();
        	long elapsedTime = System.nanoTime() - startTime;
            logger.info("Completed in {}ms", elapsedTime/Math.pow(10,6));
		}
    }

    static class SearchThreadFactory implements ThreadFactory {
       int count;
	   public Thread newThread(Runnable r) {
	   	 count++;
	     return new Thread(r, "SearchThread_"+count);
	   }
	   public static ThreadFactory newThreadFactory(){
	   	 return new SearchThreadFactory();
	   }
	}
 
}

