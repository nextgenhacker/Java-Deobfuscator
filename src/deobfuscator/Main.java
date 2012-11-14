package deobfuscator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import deobfuscator.comparators.FieldCount;
import deobfuscator.comparators.LinearComparator;
import deobfuscator.comparators.MethodCount;

public class Main {
	public static void printUsage(String[] args) {
		System.out
				.println("Usage: "
						+ " <Annotated Jar> <Obfuscated Jar> <Mapping File> [Unobfuscated Files to Consider]\n"
						+ "\n"
						+ "<Annotated Jar> - a path to a jar file containing unobfuscated Java *.class files\n"
						+ "<Obfuscated Jar> - a path to a jar file containing obfuscated Java *.class files to match against the unobfuscated *.class files\n"
						+ "<Mapping File> - the mapping file generated from proguard to test for correctness\n"
						+ "[Unobfuscated Files to Consider] - a list of files in the <Annotated Jar> to consider. If not specified, all files will be considered\n");
	}

	public static void main(String[] args) {
		// ------------------------------------------------------------------------------------------------
		// Show usage and exit if too few command line parameters are given
		// ------------------------------------------------------------------------------------------------
		if (args.length < 3) {
			printUsage(args);
			return;
		}

		String annotatedJarFile = args[0];

		String obfuscatedJarFile = args[1];
		
		String mappingFile = args[2];

		String[] libraryClasses = null;
		if (args.length >= 3) {
			libraryClasses = args[2].split(" ");
		}

		LinearComparator metric = new LinearComparator();
		metric.add(new FieldCount(), 1);
		metric.add(new MethodCount(), 1);
		try {
			Deobfuscator.deobfuscate(annotatedJarFile, obfuscatedJarFile,
					metric);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void oldMain(String[] args) {
		// ------------------------------------------------------------------------------------------------
		// Build a list of un-obfuscated *.class files to consider
		// ------------------------------------------------------------------------------------------------
		File[] unobfuscatedClassFiles;
		{ // Introduced scoping to keep the list of variables clean
			ArrayList<String> unobfuscatedFilePaths = new ArrayList<String>();
			File rootUnobfuscatedSourceDirectory = new File(args[1]);
			File[] unobfuscatedSourceFiles = rootUnobfuscatedSourceDirectory
					.listFiles(new FilenameFilter() {
						@Override
						public boolean accept(File f, String fname) {
							return fname.endsWith(".class");
						}
					});
			for (File unobfuscatedSourceFile : unobfuscatedSourceFiles) {
				if (args.length == 3) {
					unobfuscatedFilePaths.add(unobfuscatedSourceFile
							.getAbsolutePath());
				} else {
					for (int i = 3; i < args.length; i++) {
						if (args[i].equals(unobfuscatedSourceFile.getName())) {
							unobfuscatedFilePaths.add(unobfuscatedSourceFile
									.getAbsolutePath());
							break;
						}
					}
				}
			}
			unobfuscatedClassFiles = new File[unobfuscatedFilePaths.size()];
			for (int i = 0; i < unobfuscatedFilePaths.size(); i++) {
				unobfuscatedClassFiles[i] = new File(
						unobfuscatedFilePaths.get(i));
			}
		}

		// ------------------------------------------------------------------------------------------------
		// Build a list of obfuscated *.class files to consider
		// ------------------------------------------------------------------------------------------------
		File[] obfuscatedClassFiles;
		{ // Introduced scoping to keep the list of variables clean
			File rootObfuscatedSourceDirectory = new File(args[2]);
			obfuscatedClassFiles = rootObfuscatedSourceDirectory
					.listFiles(new FilenameFilter() {
						@Override
						public boolean accept(File f, String fname) {
							return fname.endsWith(".class");
						}
					});
		}

		// Allocate a 2D array to store the confidence results of each of the
		// MxN *.class comparisons
		int[][] confidence = new int[unobfuscatedClassFiles.length][unobfuscatedClassFiles.length];

		// ------------------------------------------------------------------------------------------------
		// Loop over each pair of input files and determine the confidence
		// ------------------------------------------------------------------------------------------------
		for (int i = 0; i < unobfuscatedClassFiles.length; i++) {
			try {

				// Open the current unobfuscated file
				FileInputStream unobfuscatedFIS = new FileInputStream(
						unobfuscatedClassFiles[i]);
				ClassReader unobfusctaedReader = new ClassReader(
						unobfuscatedFIS);
				ClassNode unobfuscatedClassNode = new ClassNode();
				unobfusctaedReader.accept(unobfuscatedClassNode, 0);

				for (int j = 0; j < obfuscatedClassFiles.length; i++) {

					// Open the current obfuscated file
					FileInputStream obfuscatedFIS = new FileInputStream(
							obfuscatedClassFiles[j]);
					ClassReader obfuscatedReader = new ClassReader(
							obfuscatedFIS);
					ClassNode obfuscatedClassNode = new ClassNode();
					obfuscatedReader.accept(obfuscatedClassNode, 0);

					// Compute the confidence (in this case, the very weak
					// example where the classes match if they have the same
					// number of fields,
					// interfaces, and methods)
					int currentConfidence = 0;
					currentConfidence += Math.abs(unobfuscatedClassNode.fields
							.size() - obfuscatedClassNode.fields.size());
					currentConfidence += Math.abs(unobfuscatedClassNode.methods
							.size() - obfuscatedClassNode.methods.size());
					currentConfidence += Math
							.abs(unobfuscatedClassNode.interfaces.size()
									- obfuscatedClassNode.interfaces.size());
					currentConfidence += Math
							.abs(unobfuscatedClassNode.innerClasses.size()
									- obfuscatedClassNode.innerClasses.size());

					// Store the current confidence in the confidence matrix
					confidence[i][j] = currentConfidence;

					obfuscatedFIS.close();

				}

				unobfuscatedFIS.close();

			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		// ------------------------------------------------------------------------------------------------
		// Loop over the confidence matrix and print out the mapping between
		// classes
		// ------------------------------------------------------------------------------------------------
		for (int i = 0; i < unobfuscatedClassFiles.length; i++) {
			int min_index = -1;
			int min_val = Integer.MAX_VALUE;
			for (int j = 0; j < obfuscatedClassFiles.length; j++) {
				if (confidence[i][j] < min_val) {
					min_val = confidence[i][j];
					min_index = j;
				}
			}

			// Open the current unobfuscated file
			try {
				FileInputStream unobfuscatedFIS = new FileInputStream(
						unobfuscatedClassFiles[i]);
				ClassReader unobfusctaedReader = new ClassReader(
						unobfuscatedFIS);
				ClassNode unobfuscatedClassNode = new ClassNode();
				unobfusctaedReader.accept(unobfuscatedClassNode, 0);
				System.out.print(unobfuscatedClassNode.name);
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}

			System.out.print(" maps to ");

			// Open the matching obfuscated file
			try {
				FileInputStream obfuscatedFIS = new FileInputStream(
						obfuscatedClassFiles[min_index]);
				ClassReader obfusctaedReader = new ClassReader(obfuscatedFIS);
				ClassNode obfuscatedClassNode = new ClassNode();
				obfusctaedReader.accept(obfuscatedClassNode, 0);
				System.out.println(obfuscatedClassNode.name);
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}

		}

	}
}
