package hr.unizg.fer.zemris.ppj.maheri.tests.frisc;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class FRISCtest {
	
	//Ovdje radi testa
    public static void main(String[] argv) throws Exception {
        System.out.println("R0: "+ simulate("  ADD R0,3,R0\\n  ADD R0,3,R0", 0)); // glupi primjer 0 + 3 = 3; 3 + 3 = 6; 
    }
    
    // Trazi kao ulaz source (pazi da si svaki \n pretvorio u \\n) 
    // i index registar, ciju vrijednost zelis da ti vrati
    // Ostatak je chrna madjija!
    public static Integer simulate(String input, int register) throws Exception
    {
        int memSize = 256;

        //Fool proof
        Double output = null;
        input += "\\n  HALT";
        input += "\\n";

        //System.out.println("Raw: "+input);

        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("JavaScript");

        engine.put("ppjResult", -99999);
        engine.eval(new java.io.FileReader("src/hr/unizg/fer/zemris/ppj/maheri/tests/frisc/friscjs.js"));
        engine.eval("var ppj;"+
                "var source = \""+input+"\";\n" +
                //"source = \"  ADD r0,3,r0\\n\";\n" +
                "var simulator = new FRISC();" +
                "simulator.CPU.reset();" +
                "var result = frisc_asm.parse(source);\n" +
                "simulator.CPU._frequency = 1000;\n" +
                "simulator.MEM._size = "+memSize+" * 1024;\n" +
                "simulator.MEM.loadBinaryString(result.mem);\n" +
                "simulator.CPU.run(); ppjResult = simulator.CPU._r.r"+register%8+";");

        output = (Double) engine.get("ppjResult");

        return output.intValue(); 
    }
}

