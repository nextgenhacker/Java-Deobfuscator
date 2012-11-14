package deobfuscator.comparators;

import org.objectweb.asm.tree.ClassNode;

import deobfuscator.ClassComparator;

/**
 * Compares classes by the number of fields
 */
public class FieldCount implements ClassComparator {
	@Override
	public float similarity(ClassNode original, ClassNode transformed) {
		int o = original.fields.size();
		int t = transformed.fields.size();
		int diff = o - t;
		float diff2 = diff * diff;
		return diff2;
	}
}
