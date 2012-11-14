package deobfuscator;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

public class Deobfuscator {
	
	private static List<ClassNode> getClassesFromJar(String jarfilePath) throws IOException {
		List<ClassNode> classes = new LinkedList<ClassNode>();
		@SuppressWarnings("resource")
		JarFile jarfile = new JarFile(jarfilePath);
		for (Enumeration<JarEntry> entries = jarfile.entries(); entries.hasMoreElements();) {
			JarEntry entry = entries.nextElement();

			String entryName = entry.getName();
			if (entryName.endsWith(".class")) {
				ClassNode classNode = new ClassNode();

				InputStream classFileInputStream = jarfile.getInputStream(entry);
				try {
					ClassReader reader = new ClassReader(classFileInputStream);
					reader.accept(classNode, 0);
					classes.add(classNode);
				} finally {
					classFileInputStream.close();
				}
			}
		}
		return classes;
	}

	public static ClassMapping deobfuscate(
			String annotatedJarFile,
			String obfuscatedJarFile,
			ClassComparator similarityMetric
		) throws IOException {
		
		ArrayList<ClassNode> annotated = new ArrayList<ClassNode>(getClassesFromJar(annotatedJarFile));
		ArrayList<ClassNode> obfuscated = new ArrayList<ClassNode>(getClassesFromJar(obfuscatedJarFile));

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

		// Find the best matching obfuscated class for each annotated class
		// Store the mapping in cMap
		Map<String, String> cMap = new HashMap<String, String>();
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
			// System.out.printf("%s -> %s:\n", aClass.name, bestOClass.name);
			cMap.put(aClass.name, bestOClass.name);
		}
		return new ClassMapping(cMap);
	}

}
