package javaweekly.org.practice.java.multithreading.util;

import javaweekly.org.practice.java.multithreading.FileSystemGrepper;
import javaweekly.org.practice.java.multithreading.Grepper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GrepperUtil {

    private static Grepper<String> grepper = FileSystemGrepper.getInstance();

    public static List<Callable<List<String>>>  getCallableSearchTasks(Stream<Path> paths, String searchString){
        return paths
                .filter(Files::isRegularFile)
                .map(Path::toAbsolutePath)
                .map(Path::toString)
                .map((filePathString)->
                        callableSearchTasks.apply(filePathString, searchString))
                .collect(Collectors.toList());
    }

   public static BiFunction<String, String, Callable<List<String>>> callableSearchTasks = (filePathString, searchString)-> {
        final Callable<List<String>> listCallable = () -> grepper.grep(filePathString, searchString);
        return listCallable;
    };
}
