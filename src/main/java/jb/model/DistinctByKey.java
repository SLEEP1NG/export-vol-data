package jb.model;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

/**
 * Copied from
 * http://stackoverflow.com/questions/27870136/java-lambda-stream-distinct-on-
 * arbitrary-key
 * 
 * @author Jeanne
 *
 * @param <T>
 * @param <K>
 */
public class DistinctByKey<T, K> {
	Map<K, Boolean> seen = new ConcurrentHashMap<>();
	Function<T, K> keyExtractor;

	public DistinctByKey(Function<T, K> ke) {
		this.keyExtractor = ke;
	}

	public boolean filter(T t) {
		return seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
	}
}