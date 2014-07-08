package boya.research.abb.checkergenerator.checkerbuilder;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import boya.research.abb.checkergenerator.helperfunctions.HelperFunction;

public class GetFile {
    public static String SourceTrigger_simple = "SourceTrigger_simple.txt";
    public static String SourceTrigger_simple2 = "SourceTrigger_simple_2.txt";
    public static String SourceTrigger_ValPost = "SourceTrigger_ValPost.txt";
    public static String SinkTrigger_ValPre = "SinkTrigger_ValPre.txt";
    public static String SinkTrigger_ValPost = "SinkTrigger_ValPost.txt";
    public static String SinkTrigger_Cal = "SinkTrigger_Cal.txt";
    public static String SinkTrigger_ConPost = "SinkTrigger_ConPost.txt";
    public static String SinkTrigger_ConPre = "SinkTrigger_ConPre.txt";
    
    public static File getFile(String fileName){
        File file = null;
        try {
            file = new File(new URI(CheckerBuilder.class.getResource("").toString()+fileName));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return file;
    }

}
