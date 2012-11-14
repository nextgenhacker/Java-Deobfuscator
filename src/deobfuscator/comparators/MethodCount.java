package deobfuscator.comparators;

import org.objectweb.asm.tree.ClassNode;

import deobfuscator.ClassComparator;

/**
 * Compares classes by the number of methods
 */
public class MethodCount implements ClassComparator {
	@Override
	public float similarity(ClassNode original, ClassNode transformed) {
		int o = original.methods.size();
		int t = transformed.methods.size();
		int diff = o - t;
		float diff2 = diff * diff;
		return diff2;
	}
}
