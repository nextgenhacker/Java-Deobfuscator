package deobfuscator.comparators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import deobfuscator.ClassComparator;

public class BasicClassComparator implements ClassComparator {

	@Override
	public float similarity(ClassNode original, ClassNode transformed) {
		
		//
		// Early rejection step
		//
		// It should be a somewhat safe assumption that ProGuard will not:
		// 1) Add additional fields to classes (provided classes are not being folded together)
		// 2) Add additional methods to classes (provided classes are not being folded together)
		// 3) Remove any interfaces from an obfuscated class
		// 4) Create additional inner classes
		//
		if( original.fields.size() < transformed.fields.size()				||
			original.methods.size() < transformed.methods.size()			||
			original.interfaces.size() != transformed.interfaces.size() 	||
			original.innerClasses.size() < transformed.innerClasses.size()
		){
			return Integer.MAX_VALUE;
		}
		
		// The result starts at 0 and strictly increases as confidence decreases
		float result = 0;
		
		// Make a copy of the set of methods for use in mapping methods
		List<MethodNode> unassignedMethods = new ArrayList<MethodNode>();
		for( int i = 0; i < original.methods.size(); i++ ){
			MethodNode currentNode = (MethodNode)original.methods.get(i);
			List<String> currentExceptions = new ArrayList<String>();
			for( int j = 0; j < currentNode.exceptions.size(); j++ ){
				currentExceptions.add((String)currentNode.exceptions.get(j));
			}
			String[] exceptions_s = new String[currentExceptions.size()];
			currentExceptions.toArray(exceptions_s);
			unassignedMethods.add(new MethodNode(currentNode.access,currentNode.name,currentNode.desc,currentNode.signature,exceptions_s));
		}
		
		// Allocate a BasicMethodComparator
		BasicMethodComparator bmc = new BasicMethodComparator();
		
		HashMap<String,String> MethodMappings = new HashMap<String,String>();
		
		//
		// Loop over the methods and try to match them if possible
		//
		for( int j = 0; j < transformed.methods.size(); j++ ){
			
			float bestMethodConfidence = -1.f;
			int bestMethodIndex = -1;
			for( int i = 0; i < unassignedMethods.size(); i++ ){
				
				float confidence = bmc.similarity(unassignedMethods.get(i), (MethodNode)transformed.methods.get(j));
				if( confidence > bestMethodConfidence ){
					bestMethodIndex = i;
					bestMethodConfidence = confidence;
				}
				
			}
			
			if( bestMethodIndex > -1 ){
				MethodMappings.put(unassignedMethods.get(bestMethodIndex).name+unassignedMethods.get(bestMethodIndex).desc, ((MethodNode)transformed.methods.get(j)).name+((MethodNode)transformed.methods.get(j)).desc);
				unassignedMethods.remove(bestMethodIndex);
				result += bestMethodConfidence;
			}
			
		}
		
		result /= ((transformed.methods.size()>0)?transformed.methods.size():1);
		
		if( original.fields.size() >= transformed.fields.size() ){
			if( original.fields.size() > 0 ){
				result *= (transformed.fields.size()/original.fields.size());
			}
		} else {
			if( transformed.fields.size() > 0 ){
				result *= (original.fields.size()/transformed.fields.size());
			}
		}
		
		System.out.println("Compared classes '" + original.name + "' and '" + transformed.name + "':\nResulting Method Mappings:\n");
		for( String input : MethodMappings.keySet()){
			System.out.println(input + " -> " + MethodMappings.get(input));
		}
		System.out.println("Resulting Confidence:");
		System.out.printf("%f\n\n",result/((transformed.methods.size()>0)?transformed.methods.size():1));
		
		return result/((transformed.methods.size()>0)?transformed.methods.size():1);
	}

}