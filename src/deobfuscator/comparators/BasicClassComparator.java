package deobfuscator.comparators;

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
		int result = 0;
		
		//
		// Loop over the methods and determine if the method signatures match
		//
		for( int i = 0; i < original.methods.size(); i++ ){
			
			BasicMethodComparator bcm = new BasicMethodComparator();
			bcm.similarity((MethodNode)original.methods.get(0), (MethodNode)original.methods.get(0));
			/*
			// No suitable matches for this method were found, this class likely does not match the other class
			if( PotentialMethodMatches.size() == 0 ){
				return Integer.MAX_VALUE;
			}
*/
		}
		
		return result;
	}

}