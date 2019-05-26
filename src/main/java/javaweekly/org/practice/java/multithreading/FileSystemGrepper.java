package javaweekly.org.practice.java.multithreading;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class FileSystemGrepper implements Grepper<String> {
	private FileSystemGrepper(){};

	private static Logger logger = LogManager.getLogger(FileSystemGrepper.class.getName());

	private static Grepper<String> INSTANCE = new FileSystemGrepper();

    public static Grepper<String> getInstance(){
    	return INSTANCE;
    }

    private static BiFunction<String, String, String> linesPrinter = (file, unfiltered) -> {
		logger.info("Searching in: {}, Line: {}", file, unfiltered);
		return unfiltered;
	};

	private static BiFunction<String, String, String> filteredLinePrinter = (file, filtered) -> {
		logger.info("Found in: {} in lines {}", file, filtered);
		return filtered;
	};

	public List<String> grep(String file, final String searchString){

		try (BufferedReader reader = new BufferedReader(new FileReader(
				file))) {
			return reader.lines()
					.filter((current) -> current.contains(searchString))
					.map((filtered)->filteredLinePrinter.apply(file, filtered))
					.collect(Collectors.toList());
		}catch (IOException e) {
			logger.error("Unable to grep: ", e);
		}
    	return Collections.EMPTY_LIST;
    }
}
