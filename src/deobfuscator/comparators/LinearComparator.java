package deobfuscator.comparators;

import java.util.HashMap;

import org.objectweb.asm.tree.ClassNode;

import deobfuscator.ClassComparator;

/**
 * A class comparator which uses a linear combination of other metrics to
 * compare two classes.
 */
public class LinearComparator implements ClassComparator {
	// A mapping from each metric to it's weight
	private HashMap<ClassComparator, Integer> metrics = new HashMap<ClassComparator, Integer>();
	private int totalWeight = 0;

	public LinearComparator() {
	}

	public void add(ClassComparator metric, int weight) {
		metrics.put(metric, weight);
		totalWeight += weight;
	}

	@Override
	public float similarity(ClassNode original, ClassNode transformed) {
		float total = 0;
		for (ClassComparator metric : metrics.keySet()) {
			total += metric.similarity(original, transformed);
		}
		return total / totalWeight;
	}

}
