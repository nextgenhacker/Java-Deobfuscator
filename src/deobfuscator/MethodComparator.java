package deobfuscator;

import java.util.HashMap;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

public abstract class MethodComparator {
	/**
	 * @return A measure of similarity between methods a and b in the range [0,
	 *         1], with 0 indicating no similarity, and 1 indicating identical
	 *         methods.
	 */
	public abstract float similarity(MethodNode a, MethodNode b);
	
	/**
	 * 
	 * This method takes in a MethodNode and returns a HashMap mapping the argument types and frequency
	 * 
	 * @param m The method node to compute argument statistics from
	 * @return A HashMap mapping argument types to their frequency
	 */
	public HashMap<Type, Integer> GetMethodSignatureStatistics(MethodNode m){
		HashMap<Type, Integer> result = new HashMap<Type, Integer>(8);
		Type[] argumentTypes = Type.getArgumentTypes(m.desc);
		for( Type argumentType : argumentTypes ){
			result.put(argumentType,(result.containsKey(argumentType)?result.get(argumentType)+1:1));
		}
		return result;
	}
}