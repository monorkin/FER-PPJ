package hr.unizg.fer.zemris.ppj.maheri.semantics.type;

import hr.unizg.fer.zemris.ppj.maheri.Logger;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class Types {
	private Type[] typesToTest = {

	CharType.INSTANCE, new ConstType(CharType.INSTANCE), IntType.INSTANCE, new ConstType(IntType.INSTANCE),
			new ArrayType(IntType.INSTANCE), new ArrayType(new ConstType(IntType.INSTANCE)),
			new ArrayType(CharType.INSTANCE), new ArrayType(new ConstType(CharType.INSTANCE))

	};

	List<String> names = Arrays.asList("char", "const char", "int", "const int", "int[]", "const int[]", "char[]",
			"const char[]");

	@Test
	public void testImplicitConversions() {

		System.out.print(String.format("%12s\t", "from \\ to"));
		for (int i = 0; i < typesToTest.length; ++i) {
			System.out.print(String.format("%12s\t", names.get(i)));
		}
		Logger.log("");

		for (int i = 0; i < typesToTest.length; ++i) {
			System.out.print(String.format("%12s\t", names.get(i)));
			Type from = typesToTest[i];
			for (int j = 0; j < typesToTest.length; ++j) {
				boolean can = from.canConvertImplicit(typesToTest[j]);
				System.out.print(String.format("%12s\t", can ? "~" : ""));
			}
			Logger.log("");
		}

		for (int i = 0; i < typesToTest.length; ++i) {
			Assert.assertTrue("reflexive relation", typesToTest[i].canConvertImplicit(typesToTest[i]));
			for (int j = 0; j < typesToTest.length; ++j) {
				boolean ij = typesToTest[i].canConvertImplicit(typesToTest[j]);
				for (int k = 0; k < typesToTest.length; ++k) {
					boolean jk = typesToTest[i].canConvertImplicit(typesToTest[k]);
					boolean ik = typesToTest[i].canConvertImplicit(typesToTest[k]);
					if (ij && jk)
						Assert.assertTrue("transitive relation", ik);
				}
			}
		}
	}
	
	@Test
	public void testExplicitConversions() {
		for (int i = 0; i < typesToTest.length; ++i) {
			for (int j = 0; j < typesToTest.length; ++j) {
				boolean canImplicit = typesToTest[i].canConvertImplicit(typesToTest[j]);
				boolean canExplicit = typesToTest[i].canConvertExplicit(typesToTest[j]);
				
				String castString = names.get(i) + " -> " + names.get(j);
				if (canImplicit) {
					Assert.assertTrue(castString, canExplicit);
				}
				if (canExplicit && !canImplicit)
					Logger.log("Explicit-only casting: " + castString);
			}
		}
	}
}