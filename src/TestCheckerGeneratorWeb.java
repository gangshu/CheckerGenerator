import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import boya.research.abb.checkergenerator.web.Checker;
import boya.research.abb.checkergenerator.web.GetCheckerFromXML;


public class TestCheckerGeneratorWeb {
	public static void main(String[] args){
		String fileName = "tests/PxxCall.xml";
		String XMLString = null;
		try{
			File file = new File(fileName);
			byte[] buffer = new byte[(int)file.length()];
			FileInputStream f = new FileInputStream(file);
			f.read(buffer);
			XMLString = new String(buffer);
			f.close();
			System.out.println(XMLString);
		}catch(IOException e){
			e.printStackTrace();
		}
		System.out.println("1. Get checker from XMLFile");
		testCheckersFromXMLFile(fileName);
		System.out.println("2. Get checker from XMLString");
		testCheckersFromXMLString(XMLString);
	}
	
	private static void testCheckersFromXMLFile(String fileName){
		List<Checker> checkers = GetCheckerFromXML.getCheckerFromXMLFile(fileName);
		if(checkers == null)
			System.out.println(GetCheckerFromXML.getException());
		else
			printCheckers(checkers);
	}
	
	private static void testCheckersFromXMLString(String XMLString){
		List<Checker> checkers = GetCheckerFromXML.getCheckerFromXMLString(XMLString);
		if(checkers == null)
			System.out.println(GetCheckerFromXML.getException());
		else
			printCheckers(checkers);
	}
	
	private static void printCheckers(List<Checker> checkers){
		for(Checker checker : checkers){
			System.out.println("========Messagse:========");
			System.out.println(checker.getMessage());
			System.out.println("========Checker Code:========");
			System.out.println(checker.getCheckerCode());
			System.out.println("========Test Case Code:========");
			System.out.println(checker.getTestcaseCode());
		}
	}
}
