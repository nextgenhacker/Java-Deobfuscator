package deobfuscator;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Represents a mapping from one set of java classes, methods, and fields to
 * another
 * 
 * TODO handle more than just classes
 */
public class ClassMapping {
	public ClassMapping(Map<String, String> classes) {
	}

	public static ClassMapping loadFromProguardMapping(InputStream file) {
		Scanner s = new Scanner(file);
		HashMap<String, String> map = new HashMap<String, String>();
		while (s.hasNextLine()) {
			String ln = s.nextLine();
			// if this line indicates an entire class mapping
			if(ln.endsWith(":") && ln.contains("->")) {
				String[] classes = ln.split("->");
				String aName = classes[0];
				String oName = classes[1];
				map.put(aName, oName);
			}
		}
		s.close();
		return new ClassMapping(map);
	}
}
