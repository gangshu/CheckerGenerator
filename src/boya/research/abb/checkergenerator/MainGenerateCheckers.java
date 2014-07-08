package boya.research.abb.checkergenerator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import boya.research.abb.checkergenerator.checkerbuilder.CallConstraintCheckerBuilder;
import boya.research.abb.checkergenerator.checkerbuilder.CheckerBuilder;
import boya.research.abb.checkergenerator.checkerbuilder.CheckerXMLBuilder;
import boya.research.abb.checkergenerator.checkerbuilder.ConditionalCheckConstraintCheckerBuilder;
import boya.research.abb.checkergenerator.checkerbuilder.HelperXMLBuilder;
import boya.research.abb.checkergenerator.checkerbuilder.ValueConstraintCheckerBuilder;
import boya.research.abb.checkergenerator.constraints.CallConstraint;
import boya.research.abb.checkergenerator.constraints.ConditionalCheckConstraint;
import boya.research.abb.checkergenerator.constraints.ValueConstraint;
import boya.research.abb.checkergenerator.data.CheckerInfo;
import boya.research.abb.checkergenerator.testcasebuilder.CallConstraintTestcaseBuilder;
import boya.research.abb.checkergenerator.testcasebuilder.ConditionalCheckTestcaseBuilder;
import boya.research.abb.checkergenerator.testcasebuilder.TestCaseBuilder;
import boya.research.abb.checkergenerator.testcasebuilder.ValueConstraintTestcaseBuilder;
import boya.research.abb.checkergenerator.utils.ExecWrapper;
import boya.research.abb.checkergenerator.utils.Utils;;

public class MainGenerateCheckers {
    
    static Map<String, Integer> map = new HashMap<String, Integer>();
    
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: pathToSpec pathToCheckerFolder useXMLName (true/false) [prefix]");
            return;
        }
        String pathToSpec = args[0];
        String pathToCheckerFolder = args[1];
        boolean useXMLName = Boolean.parseBoolean(args[2]);
        String prefix = "";
        if(args.length > 3)
            prefix = args[3];

        String cmd = "cmd.exe /C cd " + pathToCheckerFolder;
        cmd += "&& kwcreatechecker --language cxx --type common --code ";
        RuleSpecificationParser parser = new RuleSpecificationParser(pathToSpec);
		try{
			parser.parse();
		}catch(Exception e){
			e.printStackTrace();
		}
        
        /**
         * Value Checkers
         */
        List<CheckerInfo> valInfoList = parser.getValCheckerInfo();
        List<ValueConstraint> valList = parser.getValList();
        if(valList != null){
        for (int i = 0; i < valInfoList.size(); i++) {
            CheckerInfo curCheckerInfo = valInfoList.get(i);
            ValueConstraint valConstraint = valList.get(i);
            //If a checker already exists, need to automatically generate a new checker name
            String checkerName = Utils.getUniqueNameInFolder(pathToSpec.split("\\\\")[pathToSpec.split("\\\\").length - 1], pathToCheckerFolder);
            checkerName = checkerName.substring(0, checkerName.length() - 4);
            curCheckerInfo.setCheckerName(checkerName);
            curCheckerInfo.setCheckerName(checkerName);
            cmd += curCheckerInfo.getCheckerName();
            ExecWrapper exe = new ExecWrapper(cmd);
            System.out.println(cmd);
            exe.run();

            String path = pathToCheckerFolder + "\\" + curCheckerInfo.getCheckerName();
            
            System.out.println("Building checker: " + curCheckerInfo.getCheckerName());
            ValueConstraintCheckerBuilder builder = new ValueConstraintCheckerBuilder(true /*Value checkers are interprocedural*/, curCheckerInfo,
                valConstraint);
            ValueConstraintTestcaseBuilder tcbuilder = new ValueConstraintTestcaseBuilder(valConstraint);
            outputChecker(builder, tcbuilder, curCheckerInfo, path);
        }
        }
        /**
         * Conditional Checkers
         */
        List<CheckerInfo> conInfoList = parser.getConCheckerInfo();
        List<ConditionalCheckConstraint> conList = parser.getConList();
        if(conList != null){
        for (int i = 0; i < conInfoList.size(); i++) {
            CheckerInfo curCheckerInfo = conInfoList.get(i);
            ConditionalCheckConstraint conConstraint = conList.get(i);
            String checkerName = curCheckerInfo.getCheckerName();
            if(useXMLName){
                checkerName = pathToSpec.split("\\\\")[pathToSpec.split("\\\\").length - 1];
                checkerName = checkerName.substring(0, checkerName.length() - 4);
            }
            checkerName = prefix + checkerName;
            //If a checker already exists, need to automatically generate a new checker name
            checkerName = Utils.getUniqueNameInFolder(checkerName, pathToCheckerFolder);
            curCheckerInfo.setCheckerName(checkerName);
            cmd += curCheckerInfo.getCheckerName();
            ExecWrapper exe = new ExecWrapper(cmd);
            System.out.println(cmd);
            exe.run();
            
            String path = pathToCheckerFolder + "\\" + curCheckerInfo.getCheckerName();

            System.out.println("Building checker: " + curCheckerInfo.getCheckerName());
            
            ConditionalCheckConstraintCheckerBuilder builder = new ConditionalCheckConstraintCheckerBuilder(false, curCheckerInfo,
                conConstraint);
            ConditionalCheckTestcaseBuilder tcbuilder = new ConditionalCheckTestcaseBuilder(conConstraint);
            outputChecker(builder, tcbuilder, curCheckerInfo, path);
        }
        }
        
        /**
         * Call Checkers
         */
        List<CheckerInfo> calInfoList = parser.getCalCheckerInfo();
        List<CallConstraint> calList = parser.getCalList();
        if(calList != null){
        for (int i = 0; i < calInfoList.size(); i++) {
            CheckerInfo curCheckerInfo = calInfoList.get(i);
            CallConstraint calConstraint = calList.get(i);
            //If a checker already exists, need to automatically generate a new checker name
            String checkerName = Utils.getUniqueNameInFolder(pathToSpec.split("\\\\")[pathToSpec.split("\\\\").length - 1], pathToCheckerFolder);
            checkerName = checkerName.substring(0, checkerName.length() - 4);
            curCheckerInfo.setCheckerName(checkerName);
            cmd += curCheckerInfo.getCheckerName();
            ExecWrapper exe = new ExecWrapper(cmd);
            System.out.println(cmd);
            exe.run();

            String path = pathToCheckerFolder + "\\" + curCheckerInfo.getCheckerName();
            System.out.println("Building checker: " + curCheckerInfo.getCheckerName());
            CallConstraintCheckerBuilder builder = new CallConstraintCheckerBuilder(false, curCheckerInfo,
                calConstraint);
            CallConstraintTestcaseBuilder tcbuilder = new CallConstraintTestcaseBuilder(calConstraint);
            outputChecker(builder, tcbuilder, curCheckerInfo, path);
        }
        }
    }
    
//    /**
//     * Check if a checker with checkerName is under pathToCheckerFolder
//     * @param checkerName
//     * @param pathToCheckerFolder
//     * @return
//     */
//    public static String checkerName(String checkerName, String pathToCheckerFolder){
//        String tmp = checkerName;
//        File file = new File(pathToCheckerFolder + "\\" + checkerName);
//        int i = 1;
//        while(file.exists()){
//            checkerName = tmp + String.valueOf(i++);
//            file = new File(pathToCheckerFolder + "\\" + checkerName);
//        }
//        return checkerName;
//    }
    
    public static void outputChecker(CheckerBuilder builder, TestCaseBuilder tcbuilder, CheckerInfo curCheckerInfo, String path){
        
        String checkerCode = builder.generateChecker();
        String testcaseCode = tcbuilder.getTestCase();
        String checkerXML = CheckerXMLBuilder.getCheckerXML(curCheckerInfo);
        String helpXML = HelperXMLBuilder.getHelperXML(curCheckerInfo);

        try {
            Writer writer = new FileWriter(new File(path + "\\checker.cpp"));
            writer.write(checkerCode);
            writer.close();

            writer = new FileWriter(new File(path + "\\testcase.cc"));
            writer.write(testcaseCode);
            writer.close();

            writer = new FileWriter(new File(path + "\\checkers.xml"));
            writer.write(checkerXML);
            writer.close();

            writer = new FileWriter(new File(path + "\\help.xml"));
            writer.write(helpXML);
            writer.close();
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

}
