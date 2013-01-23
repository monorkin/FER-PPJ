import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

/**
 * Povezuje sva četiri labosa u jedan prevoditelj ppjC -> FRISC
 * 
 * @author Petar Šegina <psegina@ymail.com>
 * 
 */
public class Prevoditelj {

	public static void main(String[] args) {
		try {

			InputStream source = new ByteArrayInputStream(new Scanner(System.in).useDelimiter("").next().toString()
					.getBytes());

			/*
			 * Otkomentirati za ponovno pokretanje analizatora
			 */
//			System.err.println("***Pokrećem generiranje leksičkog analizatora");
//			GLA.run(new FileInputStream("res/ppjc_data/ppjC.lan"));
//			
//			System.err.println("***Pokrećem generiranje semantičkog analizatora");
//			GSA.run(new FileInputStream("res/ppjc_data/ppjC.san"));
//			
			System.err.println("***Pokrećem leksičku analizu");
			ByteArrayOutputStream uniformniZnakovi = new ByteArrayOutputStream();
			LA.run(source, uniformniZnakovi, new File("analizator/lexerStates.ser"));

			System.err.println("***Započinjem stvaranje generativnog stabla");
			ByteArrayOutputStream generativnoStabloOut = new ByteArrayOutputStream();
			SA.run(new ByteArrayInputStream(uniformniZnakovi.toByteArray()), generativnoStabloOut, new File(
					"analizator/lr1.ser"));

			InputStream generativnoStablo = new ByteArrayInputStream(generativnoStabloOut.toByteArray());

			System.err.println("***Započinjem semantičku analizu");
			SemantickiAnalizator.run(generativnoStablo, System.err);
			System.err.println("***Započinjem generiranje koda");
			GeneratorKoda.run(generativnoStablo, System.out);
			System.err.println("***Završio s radom");
		} catch (IOException e) {
			System.err.println("***Došlo je do greške");
			e.printStackTrace();
			System.exit(-1);
		}
	}

}
