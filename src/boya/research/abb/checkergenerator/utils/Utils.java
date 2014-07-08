package boya.research.abb.checkergenerator.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import boya.research.abb.checkergenerator.auxiliaryfunctions.AuxiliaryFunctions;
import boya.research.abb.checkergenerator.constraints.ValueConstraint;

public class Utils {
    public static String getLegalFuncNameForChecker(String functionName) {
        String ret = functionName.replaceAll(":", "COLON");
        ret = ret.replaceAll("_", "USCORE");
        return ret;
    }

    public static String readFile(File file) {
        byte[] b = null;
        try {
            BufferedInputStream reader = new BufferedInputStream(new FileInputStream(file));
            b = new byte[(int) file.length()];
            reader.read(b);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(b);
    }
    /**
     * Check if a checker with checkerName is under pathToCheckerFolder
     * @param checkerName
     * @param pathToFolder
     * @return
     */
    public static String getUniqueNameInFolder(String checkerName, String pathToFolder){
        String tmp = checkerName;
        /**
         * Remove the suffix of checkerName
         */
        if(tmp.contains("."))
            tmp = tmp.substring(0,tmp.lastIndexOf("."));
        File file = new File(pathToFolder + "\\" + checkerName);
        int i = 1;
        while(file.exists()){
            checkerName = tmp + String.valueOf(i++);
            file = new File(pathToFolder + "\\" + checkerName);
        }
        return checkerName;
    }
    
    public static void main(String[] args){
        try {
            File checker = new File(new URI(AuxiliaryFunctions.class.getResource("").toString()+"TrailingCodeIntra.txt"));
            System.out.println(readFile(checker));
            
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
