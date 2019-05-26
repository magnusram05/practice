package javaweekly.org.practice.java.multithreading;

import java.util.List;

public interface Grepper<P> {
	List<String> grep(P path, String searchString);
} 