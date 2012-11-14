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

		ClassMapping groundTruth;
		try {
			FileInputStream mappingFile = new FileInputStream(args[2]);
			groundTruth = ClassMapping.loadFromProguardMapping(mappingFile);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		String[] libraryClasses = null;
		if (args.length >= 3) {
			libraryClasses = args[2].split(" ");
		}

		LinearComparator metric = new LinearComparator();
		metric.add(new FieldCount(), 1);
		metric.add(new MethodCount(), 1);
		ClassMapping deobfuscation;
		try {
			deobfuscation = Deobfuscator.deobfuscate(annotatedJarFile,
					obfuscatedJarFile, metric);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		float matchPercentage = groundTruth.getMatchPercent(deobfuscation);
		System.out.println("Match percentage: " + matchPercentage);
	}
}
