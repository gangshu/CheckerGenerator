package boya.research.abb.checkergenerator.auxiliaryfunctions;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import boya.research.abb.checkergenerator.data.CheckerInfo;
import boya.research.abb.checkergenerator.utils.Utils;

public class AuxiliaryFunctions {
    /**
     * Preceeding Code
     */
    public static String PrecedingCode = "PrecedingCode.txt";
    /**
     * Code for intraprocedural checkers
     */
    public static String processFunction_1 = "processFunction_1.txt";
    /**
     * Thie process function puts the input parameter of a function also into the source trigger
     */
    public static String processFunction_2 = "processFunction_2.txt";
    public static String TrailingCodeintra = "TrailingCodeIntra.txt";
    
    /**
     * Code for interprocedural checkers
     */    
    public static String checker = "checker.txt";
    public static String getSinkFBKB = "getSinkFBKB.txt";
    public static String getSourceFBKB = "getSourceFBKB.txt";
    public static String TrailingCodeInter = "TrailingCodeInter.txt";
    
    
    CheckerInfo checkerInfo;
    public AuxiliaryFunctions(CheckerInfo checkerInfo){
        this.checkerInfo = checkerInfo;
    }
    
    public String getPreceedingCode(){
        String str = Utils.readFile(getFile(this.PrecedingCode));
        return str.replaceAll("<CheckerName>", checkerInfo.getCheckerName());
    }
    
    public String getProcessFunction(int i){
        if(i == 1)
            return getProcessFunction_1();
        else
            return getProcessFunction_2();       
    }
    
    public String getProcessFunction_1(){
        String str = Utils.readFile(getFile(this.processFunction_1));
        str = str.replaceAll("<CheckerName>", checkerInfo.getCheckerName());
        str = str.replaceAll("<SourceMessage>", checkerInfo.getSourceTriggerMessage());
        str = str.replaceAll("<SinkMessage>", checkerInfo.getSinkTriggerMessage());
        return str;
    }
    
    public String getProcessFunction_2(){
        String str = Utils.readFile(getFile(this.processFunction_2));
        str = str.replaceAll("<CheckerName>", checkerInfo.getCheckerName());
        str = str.replaceAll("<SourceMessage>", checkerInfo.getSourceTriggerMessage());
        str = str.replaceAll("<SinkMessage>", checkerInfo.getSinkTriggerMessage());
        return str;
    }
    
    public String getTrailingCodeIntra(){
        return Utils.readFile(getFile(this.TrailingCodeintra));
    }
    
    public String getChecker(){
        String str = Utils.readFile(getFile(this.checker));
        str = str.replaceAll("<CheckerName>", checkerInfo.getCheckerName());
        str = str.replaceAll("<SourceMessage>", checkerInfo.getSourceTriggerMessage());
        str = str.replaceAll("<SinkMessage>", checkerInfo.getSinkTriggerMessage());
        return str;
    }
    
    public String getSourceFBKB(){
        String str = Utils.readFile(getFile(this.getSourceFBKB));
        str = str.replaceAll("<CheckerName>", checkerInfo.getCheckerName());
        str = str.replaceAll("<SourceMessage>", checkerInfo.getSourceTriggerMessage());
        return str;
    }
    
    public String getSinkFBKB(){
        String str = Utils.readFile(getFile(this.getSinkFBKB));
        str = str.replaceAll("<CheckerName>", checkerInfo.getCheckerName());
        str = str.replaceAll("<SinkMessage>", checkerInfo.getSinkTriggerMessage());
        return str;
    }
    
    public String getTrailingCodeInter(){
        String str = Utils.readFile(getFile(this.TrailingCodeInter));
        str = str.replaceAll("<CheckerName>", checkerInfo.getCheckerName());
        return str;
    }
    
    private File getFile(String fileName){
        File file = null;
        try {
        	System.out.println(AuxiliaryFunctions.class.getResource("").toString());
            file = new File(new URI(AuxiliaryFunctions.class.getResource("").toString()+fileName));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return file;
    }
    
    public static void main(String[] args){
        CheckerInfo checkerInfo = new CheckerInfo("TestChecker", "Message on source trigger", "Message on sink trigger","Checker Message", "Checker Title");
        AuxiliaryFunctions aux = new AuxiliaryFunctions(checkerInfo);
        System.out.println("-------------------------" + aux.PrecedingCode + "-------------------------" );
        System.out.println(aux.getPreceedingCode());
        System.out.println("-------------------------" + aux.processFunction_1 + "-------------------------" );
        System.out.println(aux.getProcessFunction_1());
        System.out.println("-------------------------" + aux.TrailingCodeintra + "-------------------------" );
        System.out.println(aux.getTrailingCodeIntra());
        System.out.println("-------------------------" + aux.checker + "-------------------------" );
        System.out.println(aux.getChecker());
        System.out.println("-------------------------" + aux.getSourceFBKB + "-------------------------" );
        System.out.println(aux.getSourceFBKB());
        System.out.println("-------------------------" + aux.getSinkFBKB + "-------------------------" );
        System.out.println(aux.getSinkFBKB());
        System.out.println("-------------------------" + aux.TrailingCodeInter + "-------------------------" );
        System.out.println(aux.getTrailingCodeInter());
        
    }
}
