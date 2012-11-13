package deobfuscator;

import java.io.FileInputStream;
import java.io.IOException;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

public class Deobfuscator {
	private static final String TEST_FILE_NAME = "/home/puneet/Desktop/imagej_src/bytecode/ij/ij/Prefs.class";

	public static void main(String[] args) {
		try {
			FileInputStream fis = new FileInputStream(TEST_FILE_NAME);
			ClassReader reader = new ClassReader(fis);
			ClassNode cn = new ClassNode();
			reader.accept(cn, 0);
			for (int i = 0; i < cn.interfaces.size(); i++) {
				System.out.println("Implements: "
						+ (String) cn.interfaces.get(i));
			}
			for (int i = 0; i < cn.methods.size(); i++) {
				MethodNode m = (MethodNode) cn.methods.get(i);
				System.out.println(String.format("Method: %s - %s", m.name,
						m.desc));
			}
			for (int i = 0; i < cn.fields.size(); i++) {
				FieldNode f = (FieldNode) cn.fields.get(i);
				System.out.println(String.format("Field: %s - %s - %s", f.name,
						f.signature,
						(f.access == Opcodes.ACC_PUBLIC) ? "public"
								: "not public"));
			}
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
