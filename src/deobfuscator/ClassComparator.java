package deobfuscator;

import org.objectweb.asm.tree.ClassNode;

public interface ClassComparator {
	/**
	 * @return A measure of similarity between an "original" class and a
	 *         "transformed" class in the range [0.0, 1.0], with 0 indicating no
	 *         similarity, and 1 indicating identical classes.
	 */
	public float similarity(ClassNode original, ClassNode transformed);
}
