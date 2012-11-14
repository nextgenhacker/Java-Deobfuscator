package deobfuscator.comparators;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

import deobfuscator.MethodComparator;

public class BasicMethodComparator extends MethodComparator {
	
	private static final float METHOD_PARAMETER_CONFIDENCE_PADDING = 0.f;

	@Override
	public float similarity(MethodNode a, MethodNode b) {
		// Get the return type and argument types and frequencies of the unobfuscated method
		HashMap<Type, Integer> A_MethodParameters = GetMethodSignatureStatistics(a);
		Type A_ReturnType = Type.getReturnType(a.desc);
		
		HashMap<Type, Integer> B_MethodParameters = GetMethodSignatureStatistics(b);
		Type B_ReturnType = Type.getReturnType(b.desc);
		
		// If the return types are different, then we know that this is not a potential match
		if( A_ReturnType.getSort() != B_ReturnType.getSort() ){
			return 0.f;
		}
		
		// Compute a "method confidence" that the two methods being compared are the same, starting at 1.0 and decreasing
		// as the number of dissimilarities increases
		float MethodConfidence = 1.f;
		
		// Build a list of all parameter types
		Set<Type> AllParameterTypes = new TreeSet<Type>(new Comparator<Type>(){
			@Override
			/*
			public int compare(Type o1, Type o2) {
				return o1.getDescriptor().compareTo(o2.getDescriptor());
			}
			*/
			public int compare(Type o1, Type o2) {
				return Integer.compare(o1.getSort(),o2.getSort());
			}
		});
		AllParameterTypes.addAll(A_MethodParameters.keySet());
		AllParameterTypes.addAll(B_MethodParameters.keySet());
		
		// Iterate over the parameters, multiplying through by the ratio of the number of same-type parameters
		// in A and B
		for( Type ParameterType : AllParameterTypes ){
			if( A_MethodParameters.containsKey(ParameterType) && B_MethodParameters.containsKey(ParameterType) ){
				float A_Ct = (float)A_MethodParameters.get(ParameterType)+METHOD_PARAMETER_CONFIDENCE_PADDING;
				float B_Ct = (float)B_MethodParameters.get(ParameterType)+METHOD_PARAMETER_CONFIDENCE_PADDING;
				MethodConfidence *= (A_Ct>=B_Ct)?(B_Ct/A_Ct):(A_Ct/B_Ct);
			} else if( A_MethodParameters.containsKey(ParameterType) ) {
				MethodConfidence *= (METHOD_PARAMETER_CONFIDENCE_PADDING/(A_MethodParameters.get(ParameterType)+METHOD_PARAMETER_CONFIDENCE_PADDING));
			} else {
				MethodConfidence *= (METHOD_PARAMETER_CONFIDENCE_PADDING/(B_MethodParameters.get(ParameterType)+METHOD_PARAMETER_CONFIDENCE_PADDING));
			}
		}
		
		return MethodConfidence;
			
	}

}
