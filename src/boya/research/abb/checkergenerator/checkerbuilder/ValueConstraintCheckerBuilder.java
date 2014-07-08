package boya.research.abb.checkergenerator.checkerbuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import boya.research.abb.checkergenerator.RuleSpecificationParser;
import boya.research.abb.checkergenerator.auxiliaryfunctions.AuxiliaryFunctions;
import boya.research.abb.checkergenerator.constraints.ValueConstraint;
import boya.research.abb.checkergenerator.data.CheckerInfo;
import boya.research.abb.checkergenerator.helperfunctions.ExtractVarsInExpr;
import boya.research.abb.checkergenerator.helperfunctions.IsType;
import boya.research.abb.checkergenerator.helperfunctions.IsValueLegal;
import boya.research.abb.checkergenerator.helperfunctions.traverseAST;
import boya.research.abb.checkergenerator.utils.ExecWrapper;
import boya.research.abb.checkergenerator.utils.Utils;

import boya.research.abb.checkergenerator.RuleSpecificationParser;
public class ValueConstraintCheckerBuilder extends CheckerBuilder {

    ValueConstraint valueConstraint;
    public ValueConstraintCheckerBuilder(boolean isInter, CheckerInfo checkerInfo, ValueConstraint valueConstraint) {
        super(isInter, checkerInfo);
        this.valueConstraint = valueConstraint;
    }

    @Override
    public String generateHelperFunctions() {
        String decl = "", body= "";
        /**
         * 1. Get isValueLegal function
         */
        IsValueLegal isValueLegal = new IsValueLegal(valueConstraint);
        decl += isValueLegal.generateDecl();
        body += isValueLegal.generateCode();
        decl += "\n";
        body += "\n";
        
        /**
         * 2. Get extractVarsInExpr function
         */
        ExtractVarsInExpr extractVarsInExpr = new ExtractVarsInExpr();
        decl += extractVarsInExpr.generateDecl();
        body += extractVarsInExpr.generateCode();
        decl += "\n";
        body += "\n";
        
        /**
         * 3. Get isType function and traverseAST function for postcondition value constraints
         */
        if(valueConstraint.getParameterId() == 0){
        	IsType isType = new IsType();
        	decl += isType.generateDecl();
        	body += isType.generateCode();
        	
        	traverseAST ast = new traverseAST();
        	decl += ast.generateDecl();
        	body += ast.generateCode();
        }
        return decl + "\n" + body;
    }


    @Override
    public String generateSourceTrigger() {
        String code;
        /**
         * 1. Handling pre condition value constraint
         */
        if(valueConstraint.getParameterId() != 0)
            code = Utils.readFile(GetFile.getFile(GetFile.SourceTrigger_simple));
        /**
         * 2. Handling post condition value constraint
         */
        else{
            code = Utils.readFile(GetFile.getFile(GetFile.SourceTrigger_ValPost));
            code = code.replaceAll("<FunctionName>", valueConstraint.getFunction().getName());
            /**
             * If the value constraint is on a pointer, set <LevelOfPOinters> to be 0, which means that we directly checks whether the pointer is 0;
             * Otherwise, need to set <LevelOfPointers> as it is and check the value in the memory block that the pointer points to.
             */
            if(valueConstraint.getMap().containsKey(ValueConstraint.NE) && valueConstraint.getMap().get(ValueConstraint.NE).getValue().equals("NULL"))
            	code = code.replaceAll("<LevelOfPointers>", String.valueOf(0));
            else
            	code = code.replaceAll("<LevelOfPointers>", String.valueOf(valueConstraint.getFunction().getParam(valueConstraint.getParameterId()).getPointerLevel()));           
        }
        return code;
    }

    @Override
    public String generateSinkTrigger() {
        String code;
        /**
         * 1. Handling post condition value constraint
         */
        if(valueConstraint.getParameterId() == 0){
            code = Utils.readFile(GetFile.getFile(GetFile.SinkTrigger_ValPost));
            code = code.replaceAll("<TypeOfReturnValue>", valueConstraint.getFunction().getParam(0).getParameterType());
        }
        
        /**
         * 2. Handling pre condition value constraint
         */
        else{
            code = Utils.readFile(GetFile.getFile(GetFile.SinkTrigger_ValPre));
            code = code.replaceAll("<FunctionName>", valueConstraint.getFunction().getName());
            code = code.replaceAll("<ID_Cons>", String.valueOf(valueConstraint.getParameterId()));
            /**
             * If the value constraint is on a pointer, set <LevelOfPOinters> to be 0, which means that we directly checks whether the pointer is 0;
             * Otherwise, need to set <LevelOfPointers> as it is and check the value in the memory block that the pointer points to.
             */
            if(valueConstraint.getMap().containsKey(ValueConstraint.NE) && valueConstraint.getMap().get(ValueConstraint.NE).getValue().equals("NULL"))
            	code = code.replaceAll("<LevelOfPointers>", String.valueOf(0));
            else
            	code = code.replaceAll("<LevelOfPointers>", String.valueOf(valueConstraint.getFunction().getParam(valueConstraint.getParameterId()).getPointerLevel())); 
        }
        return code;
    }

    /**
     * Value checkers use process_1
     */
    @Override
    public String generateTrailingCode() {
        String ret = "";
        if(!isInter){
            if(valueConstraint.getParameterId() == 0)
                ret += aux.getProcessFunction_1();
            else
                ret += aux.getProcessFunction_2();
            ret += "\n";
            ret += aux.getTrailingCodeIntra();           
        }
        else{
            ret += aux.getChecker();
            ret += "\n";
            ret += aux.getSourceFBKB();
            ret += "\n";
            ret += aux.getSinkFBKB();
            ret += "\n";
            ret += aux.getTrailingCodeInter();
        }
        return ret;
    } 
    
    public static void main(String[] args) throws IOException{
        String pathToSpec = "E:\\Boya-research\\EclipseWorkSpace\\ABBKlocwork\\data\\tests\\test_1\\Specification\\P181FunPreCond.xml";
        String pathToChecker = "E:\\Boya-research\\EclipseWorkSpace\\ABBKlocwork\\data\\tests\\test_1\\AutoCheckers";
        String cmd = "cmd.exe /C cd " + pathToChecker;
        cmd += "&& kwcreatechecker --language cxx --type common --code ";       
        RuleSpecificationParser parser = new RuleSpecificationParser(pathToSpec);
		try{
			parser.parse();
		}catch(Exception e){
			e.printStackTrace();
		}
        List<CheckerInfo> checkerinfoList = parser.getValCheckerInfo();
        List<ValueConstraint> constList = parser.getValList();
        for(int i = 0; i < checkerinfoList.size(); i++){
            CheckerInfo curCheckerInfo = checkerinfoList.get(i);
            ValueConstraint valueConstraint = constList.get(i);
            curCheckerInfo.setCheckerName("P181" + curCheckerInfo.getCheckerName());
            cmd += curCheckerInfo.getCheckerName();
            ExecWrapper exe = new ExecWrapper(cmd);
            System.out.println(cmd);
            exe.run();
            
            String path = pathToChecker + "\\" + curCheckerInfo.getCheckerName();
            
            ValueConstraintCheckerBuilder builder = new ValueConstraintCheckerBuilder(false, curCheckerInfo, valueConstraint);
            String checkerCode = builder.generateChecker();
            String checkerXML = CheckerXMLBuilder.getCheckerXML(curCheckerInfo);
            String helpXML = HelperXMLBuilder.getHelperXML(curCheckerInfo);
            
            try{
            	Writer writer = new FileWriter(new File(path + "\\checker.cpp"));
            	writer.write(checkerCode);
            	writer.close();
            	
            	writer = new FileWriter(new File(path + "\\checkers.xml"));
            	writer.write(checkerXML);
            	writer.close();
            	
            	writer = new FileWriter(new File(path + "\\help.xml"));
            	writer.write(helpXML);
            	writer.close();
            }catch(IOException io){
            	io.printStackTrace();
            }
        }
    }
}
