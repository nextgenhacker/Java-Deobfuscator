package deobfuscator.comparators;

import java.util.HashMap;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

import deobfuscator.MethodComparator;

public class BasicMethodComparator extends MethodComparator {

	@Override
	public float similarity(MethodNode a, MethodNode b) {
		// Get the return type and argument types and frequencies of the unobfuscated method
		HashMap<Type, Integer> A_MethodSignatureStatistics = GetMethodSignatureStatistics(a);
		Type A_ReturnType = Type.getReturnType(a.desc);
		
		HashMap<Type, Integer> B_MethodSignatureStatistics = GetMethodSignatureStatistics(b);
		Type B_ReturnType = Type.getReturnType(b.desc);
		
		// If the return types are different, then we know that this is not a potential match
		if( A_ReturnType.getSort() != B_ReturnType.getSort() ){
			return 0.f;
		}
		
		// Compute a "method confidence" that the two methods being compared are the same, starting at 1.0 and decreasing
		// as the number of dissimilarities increases
		float MethodConfidence = 1.f;
		
		// The confidence is essentially the sum of the differences in the number of
		// method arguments of each type
		/*Set<Type> AllParameters = new TreeSet<Type>(new Comparator<Type>() {
			@Override
			public int compare(Type o1, Type o2) {
				return (String.valueOf(o1.getSort())+"_"+o1.);
			}
		});
		AllParameters.addAll(A_MethodSignatureStatistics.keySet());
		AllParameters.addAll(B_MethodSignatureStatistics.keySet());
		for( Type ParameterType : A_MethodSignatureStatistics.keySet() ){
			if( B_MethodSignatureStatistics.containsKey(ParameterType) ){
				MethodConfidence += Math.abs(B_MethodSignatureStatistics.get(ParameterType)-A_MethodSignatureStatistics.get(ParameterType));
			} else {
				MethodConfidence += A_MethodSignatureStatistics.get(ParameterType);
			}
		}*/
		
		for( int i = 0; i < A_MethodSignatureStatistics.keySet().size(); i++ ){
			System.out.println(((Type)A_MethodSignatureStatistics.keySet().toArray()[i]).getDescriptor());
		}
		
		return MethodConfidence;
			
	}

}
