package deobfuscator;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * Represents a mapping from one set of java classes, methods, and fields to
 * another
 * 
 * TODO handle more than just classes
 */
public class ClassMapping {
	private Map<String, String> cmap;

	public ClassMapping(Map<String, String> classes) {
		this.cmap = classes;
	}

	public String toString() {
		return cmap.toString();
	}

	public static ClassMapping loadFromProguardMapping(InputStream file) {
		Scanner s = new Scanner(file);
		HashMap<String, String> map = new HashMap<String, String>();
		while (s.hasNextLine()) {
			String ln = s.nextLine();
			// if this line indicates an entire class mapping
			if (ln.endsWith(":") && ln.contains("->")) {
				String[] classes = ln.split(":")[0].split("->");
				String aName = classes[0].trim();
				String oName = classes[1].trim();
				String aClsPath = aName.replace('.', '/');
				String oClsPath = oName.replace('.', '/');
				map.put(aClsPath, oClsPath);
			}
			// TODO handle methods and fields
		}
		s.close();
		return new ClassMapping(map);
	}

	/**
	 * @param deobfuscation
	 *            The other mapping to compare to
	 * @return The percentage of correctly matched items out of all items
	 *         attempted to be matched
	 */
	public float getMatchPercent(ClassMapping other) {
		Set<String> attemptedMatch = other.cmap.keySet();
		int correct = 0;
		int total = 0;
		for (String cName : attemptedMatch) {
			if (cmap.containsKey(cName) && other.cmap.containsKey(cName)) {
				total++;
				if (cmap.get(cName).equals(other.cmap.get(cName))) {
					System.out.println("Correctly matched: " + cName);
					correct++;
				}
			}
		}
		return (float) correct / (float) total;
	}
}
