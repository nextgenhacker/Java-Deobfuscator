package deobfuscator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class Deobfuscator {
	private static List<ClassNode> getClassesFromJar(String jarfilePath)
			throws IOException {
		List<ClassNode> classes = new LinkedList<ClassNode>();
		JarFile jarfile = new JarFile(jarfilePath);
		for (Enumeration<JarEntry> entries = jarfile.entries(); entries
				.hasMoreElements();) {
			JarEntry entry = entries.nextElement();

			String entryName = entry.getName();
			if (entryName.endsWith(".class")) {
				ClassNode classNode = new ClassNode();

				InputStream classFileInputStream = jarfile
						.getInputStream(entry);
				try {
					ClassReader reader = new ClassReader(classFileInputStream);
					reader.accept(classNode, 0);
					classes.add(classNode);
				} finally {
					classFileInputStream.close();
				}

				System.out.println("Loaded class: " + classNode.name);
			}
		}
		return classes;
	}
	public static void deobfuscate(String annotatedJarFile,
			String obfuscatedJarFile, ClassComparator similarityMetric)
			throws IOException {
		ArrayList<ClassNode> annotated = new ArrayList<ClassNode>(
				getClassesFromJar(annotatedJarFile));
		ArrayList<ClassNode> obfuscated = new ArrayList<ClassNode>(
				getClassesFromJar(obfuscatedJarFile));

		// Allocate a 2D array to store the confidence results of each of the
		// MxN *.class comparisons
		float[][] confidence = new float[annotated.size()][obfuscated.size()];

		for (int aIndex = 0; aIndex < annotated.size(); aIndex++) {
			ClassNode aClass = annotated.get(aIndex);
			for (int oIndex = 0; oIndex < obfuscated.size(); oIndex++) {
				ClassNode oClass = obfuscated.get(oIndex);
				confidence[aIndex][oIndex] = similarityMetric.similarity(
						aClass, oClass);
			}
		}

		// ------------------------------------------------------------------------------------------------
		// Loop over the confidence matrix and print out the mapping between
		// classes
		// ------------------------------------------------------------------------------------------------
		for (int aIndex = 0; aIndex < annotated.size(); aIndex++) {
			ClassNode aClass = annotated.get(aIndex);
			ClassNode bestOClass = null;
			float bestConfidence = Float.MAX_VALUE;
			for (int oIndex = 0; oIndex < obfuscated.size(); oIndex++) {
				if (confidence[aIndex][oIndex] < bestConfidence) {
					bestConfidence = confidence[aIndex][oIndex];
					ClassNode oClass = obfuscated.get(oIndex);
					bestOClass = oClass;
				}
			}
			System.out.printf("%s -> %s:\n", aClass.name, bestOClass.name);
		}
	}
	
	
	public static void main(String[] args) {
		
		//------------------------------------------------------------------------------------------------
		// Show usage and exit if too few command line parameters are given
		//------------------------------------------------------------------------------------------------
		if( args.length < 3 ){
			System.out.println(
					"Usage: " + args[0] + " <Input Unobfuscated Class Path> <Input Obfuscated Class Path> [Unobfuscated Files to Consider]\n" +
					"\n" +
					"<Input Unobfuscated Class Path> - a path to a directory containing unobfuscated Java *.class files" +
					"<Input Obfuscated Class Path> - a path to a directory containing obfuscated Java *.class files to match against the unobfuscated *.class files" +
					"[Unobfuscated Files to Consider] - a list of files in the <Input Unobfuscated Class Path> to consider. If not specified, all files will be considered"
			);
			return;
		}
		
		//------------------------------------------------------------------------------------------------
		// Build a list of un-obfuscated *.class files to consider
		//------------------------------------------------------------------------------------------------
		File[] unobfuscatedClassFiles;
		{ // Introduced scoping to keep the list of variables clean
			ArrayList<String> unobfuscatedFilePaths = new ArrayList<String>();
			File rootUnobfuscatedSourceDirectory = new File(args[1]);
			File[] unobfuscatedSourceFiles = rootUnobfuscatedSourceDirectory.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File f, String fname) {
					return fname.endsWith(".class");
				}
			});
			for( File unobfuscatedSourceFile : unobfuscatedSourceFiles ){
				if( args.length == 3 ){
					unobfuscatedFilePaths.add(unobfuscatedSourceFile.getAbsolutePath());
				} else {
					for( int i = 3; i < args.length; i++ ){
						if( args[i].equals(unobfuscatedSourceFile.getName())){
							unobfuscatedFilePaths.add(unobfuscatedSourceFile.getAbsolutePath());
							break;
						}
					}
				}
			}
			unobfuscatedClassFiles = new File[unobfuscatedFilePaths.size()];
			for( int i = 0; i < unobfuscatedFilePaths.size(); i++ ){
				unobfuscatedClassFiles[i] = new File(unobfuscatedFilePaths.get(i));
			}
		}
		
		//------------------------------------------------------------------------------------------------
		// Build a list of obfuscated *.class files to consider
		//------------------------------------------------------------------------------------------------
		File[] obfuscatedClassFiles;
		{ // Introduced scoping to keep the list of variables clean
			File rootObfuscatedSourceDirectory = new File(args[2]);
			obfuscatedClassFiles = rootObfuscatedSourceDirectory.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File f, String fname) {
					return fname.endsWith(".class");
				}
			});
		}
		
		// Allocate a 2D array to store the confidence results of each of the MxN *.class comparisons
		int[][] confidence = new int[unobfuscatedClassFiles.length][unobfuscatedClassFiles.length];
		
		//------------------------------------------------------------------------------------------------
		// Loop over each pair of input files and determine the confidence
		//------------------------------------------------------------------------------------------------
		for( int i = 0; i < unobfuscatedClassFiles.length; i++ ){
			try {
				
				// Open the current unobfuscated file
				FileInputStream unobfuscatedFIS = new FileInputStream(unobfuscatedClassFiles[i]);
				ClassReader unobfusctaedReader = new ClassReader(unobfuscatedFIS);
				ClassNode unobfuscatedClassNode = new ClassNode();
				unobfusctaedReader.accept(unobfuscatedClassNode, 0);
				
				for( int j = 0; j < obfuscatedClassFiles.length; j++ ){
				
					// Open the current obfuscated file
					FileInputStream obfuscatedFIS = new FileInputStream(obfuscatedClassFiles[j]);
					ClassReader obfuscatedReader = new ClassReader(obfuscatedFIS);
					ClassNode obfuscatedClassNode = new ClassNode();
					obfuscatedReader.accept(obfuscatedClassNode, 0);
					
					confidence[i][j] = ComputeConfidence(unobfuscatedClassNode,obfuscatedClassNode);
					
					obfuscatedFIS.close();
					
				}
				
				unobfuscatedFIS.close();
				
			} catch (IOException e) {
				e.printStackTrace();
			}				
		}
	}
	/**
	 * 
	 * ComputeConfidence
	 * 
	 * @param unobfuscatedNode The ClassNode representation of the current unobfuscated source class that is being searched for.
	 * @param obfuscatedNode The ClassNode representation of the current obfuscated source class that is being matched against.
	 * 
	 * @return The confidence that the unobfuscated source code was transformed into the current obfuscated source code. The closer to 0 the value is, the higher the confidence.
	 * 
	 */
	public static int ComputeConfidence( ClassNode unobfuscatedNode, ClassNode obfuscatedNode ){
		
		//
		// Early rejection step
		//
		// It should be a somewhat safe assumption that ProGuard will not:
		// 1) Add additional fields to classes (provided classes are not being folded together)
		// 2) Add additional methods to classes (provided classes are not being folded together)
		// 3) Remove any interfaces from an obfuscated class
		// 4) Create additional inner classes
		//
		if( unobfuscatedNode.fields.size() < obfuscatedNode.fields.size()				||
			unobfuscatedNode.methods.size() < obfuscatedNode.methods.size()				||
			unobfuscatedNode.interfaces.size() != obfuscatedNode.interfaces.size() 		||
			unobfuscatedNode.innerClasses.size() < obfuscatedNode.innerClasses.size()
		){
			return Integer.MAX_VALUE;
		}
		
		// The result starts at 0 and strictly increases as confidence decreases
		int result = 0;
		
		//
		// Loop over the methods and determine if the method signatures match
		//
		for( int i = 0; i < unobfuscatedNode.methods.size(); i++ ){
			
			// Get the return type and argument types and frequencies of the unobfuscated method
			HashMap<Type, Integer> UnobfuscatedMethodSignatureStatistics = GetMethodSignatureStatistics((MethodNode)unobfuscatedNode.methods.get(i));
			Type UnobfuscatedReturnType = Type.getReturnType(((MethodNode)unobfuscatedNode.methods.get(i)).desc);
			
			// Set up a list to store potential matches
			ArrayList<Pair<MethodNode,Integer>> PotentialMethodMatches = new ArrayList<Pair<MethodNode,Integer>>();
			
			// Iterate over all methods in the obfuscated class to try and find matches
			for( int j = 0; j < obfuscatedNode.methods.size(); j++ ){
				HashMap<Type, Integer> ObfuscatedMethodSignatureStatistics = GetMethodSignatureStatistics((MethodNode)unobfuscatedNode.methods.get(i));
				Type ObfuscatedReturnType = Type.getReturnType(((MethodNode)unobfuscatedNode.methods.get(i)).desc);
				
				// If the return types are different, then we know that this is not a potential match
				if( ObfuscatedReturnType.getSort() != UnobfuscatedReturnType.getSort() ){
					continue;
				}
				
				// Compute a "method confidence" that the two methods being compared are the same
				int MethodConfidence = 0;
				
				// The confidence is essentially the sum of the differences in the number of
				// method arguments of each type
				for( Type ParameterType : UnobfuscatedMethodSignatureStatistics.keySet() ){
					if( ObfuscatedMethodSignatureStatistics.containsKey(ParameterType)){
						MethodConfidence += Math.abs(ObfuscatedMethodSignatureStatistics.get(ParameterType)-UnobfuscatedMethodSignatureStatistics.get(ParameterType));
					} else {
						MethodConfidence += UnobfuscatedMethodSignatureStatistics.get(ParameterType);
					}
				}
				
				// Add this method-confidence pair to the list of candidates
				PotentialMethodMatches.add(new Pair<MethodNode,Integer>((MethodNode)obfuscatedNode.methods.get(j),MethodConfidence));
			}
			
			// No suitable matches for this method were found, this class likely does not match the other class
			if( PotentialMethodMatches.size() == 0 ){
				return Integer.MAX_VALUE;
			}

		}
		
		return result;
	}
	
	/**
	 * 
	 * This method takes in a MethodNode and returns a HashMap mapping the argument types and frequency
	 * 
	 * @param m The method node to compute argument statistics from
	 * @return A HashMap mapping argument types to their frequency
	 */
	public static HashMap<Type, Integer> GetMethodSignatureStatistics(MethodNode m){
		HashMap<Type, Integer> result = new HashMap<Type, Integer>(8);
		Type[] argumentTypes = Type.getArgumentTypes(m.desc);
		for( Type argumentType : argumentTypes ){
			result.put(argumentType,(result.containsKey(argumentType)?result.get(argumentType)+1:1));
		}
		return result;
	}
}
