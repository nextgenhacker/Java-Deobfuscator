package deobfuscator;

import org.objectweb.asm.tree.MethodNode;

public interface MethodComparator {
	/**
	 * @return A measure of similarity between methods a and b in the range [0,
	 *         1], with 0 indicating no similarity, and 1 indicating identical
	 *         methods.
	 */
	public float similarity(MethodNode a, MethodNode b);
}
