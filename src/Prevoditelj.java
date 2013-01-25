import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Povezuje sva četiri labosa u jedan prevoditelj ppjC -> FRISC
 * 
 * @author Petar Šegina <psegina@ymail.com>
 * 
 */
public class Prevoditelj {

	public static void main(String[] args) {
		try {

			String line;
			StringBuilder input = new StringBuilder();
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			while ((line = br.readLine()) != null) {
				input.append(line);
				input.append("\n");
			}

			br.close();

			InputStream source = new ByteArrayInputStream(input.toString().getBytes());

			/*
			 * Otkomentirati za ponovno pokretanje generatora
			 */
//			System.err.println("***Pokrećem generiranje leksičkog analizatora");
//			GLA.run(new FileInputStream("res/ppjc_data/ppjC.lan"));
//
//			System.err.println("***Pokrećem generiranje semantičkog analizatora");
//			GSA.run(new FileInputStream("res/ppjc_data/ppjC.san"));

			System.err.println("***Pokrećem leksičku analizu");
			ByteArrayOutputStream uniformniZnakovi = new ByteArrayOutputStream();
			LA.run(source, uniformniZnakovi, new File(GLA.OUTPUT));

			System.err.println("***Započinjem stvaranje generativnog stabla");
			ByteArrayOutputStream generativnoStabloOut = new ByteArrayOutputStream();
			ByteArrayInputStream uniformniZnakoviIn = new ByteArrayInputStream(uniformniZnakovi.toByteArray());
			SA.run(uniformniZnakoviIn, generativnoStabloOut, new File(GSA.OUTPUT));

			InputStream generativnoStablo = new ByteArrayInputStream(generativnoStabloOut.toByteArray());

			System.err.println("***Započinjem semantičku analizu");
			SemantickiAnalizator.run(generativnoStablo, System.err);
			System.err.println("***Započinjem generiranje koda");
			generativnoStablo.reset();
			GeneratorKoda.run(generativnoStablo, System.out);
			System.err.println("***Završio s radom");
		} catch (IOException e) {
			System.err.println("***Došlo je do greške");
			e.printStackTrace();
			System.exit(-1);
		}
	}

}
