package boya.research.abb.checkergenerator.checkerbuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import boya.research.abb.checkergenerator.RuleSpecificationParser;
import boya.research.abb.checkergenerator.constraints.ConditionalCheckConstraint;
import boya.research.abb.checkergenerator.constraints.ValueConstraint;
import boya.research.abb.checkergenerator.data.CheckerInfo;
import boya.research.abb.checkergenerator.helperfunctions.ExtractVarsInExpr;
import boya.research.abb.checkergenerator.helperfunctions.IsInteresting;
import boya.research.abb.checkergenerator.helperfunctions.Traverse;
import boya.research.abb.checkergenerator.utils.ExecWrapper;
import boya.research.abb.checkergenerator.utils.Utils;

public class ConditionalCheckConstraintCheckerBuilder extends CheckerBuilder {

    ConditionalCheckConstraint constraint;
    int ID_src;
    public ConditionalCheckConstraintCheckerBuilder(boolean isInter, CheckerInfo checkerInfo, ConditionalCheckConstraint constraint) {
        super(isInter, checkerInfo);
        this.constraint = constraint;
    }    
    
    public void setID_src(int id){
        this.ID_src = id;
    }
    
    @Override
    public String generateHelperFunctions() {
        String decl = "", body= "";
        /**
         * 1. Get traverse
         */
        Traverse traverse = new Traverse();
        decl += traverse.generateDecl();
        body += traverse.generateCode();
        decl += "\n";
        body += "\n";
        
        /**
         * 2. Get isInteresting
         */
        IsInteresting isInteresting = new IsInteresting(true, null);
        decl += isInteresting.generateDecl();
        body += isInteresting.generateCode();
        decl += "\n";
        body += "\n";
        
        /**
         * 3. Get extractVarsInExpr
         */
//        ExtractVarsInExpr extractVarsInExpr = new ExtractVarsInExpr();
//        decl += extractVarsInExpr.generateDecl();
//        body += extractVarsInExpr.generateCode();
//        decl += "\n";
//        body += "\n";
        
        return decl + "\n" + body;
        
    }
    
    @Override
    public String generateSourceTrigger() {
        return Utils.readFile(GetFile.getFile(GetFile.SourceTrigger_simple2));
    }

    @Override
    public String generateSinkTrigger() {
        String ret = "";
        /**
         * 1. Generate source trigger for postcondition rule
         */
        if(constraint.getParameterId() == 0){
            ret = Utils.readFile(GetFile.getFile(GetFile.SinkTrigger_ConPost));
        }
        /**
         * 2. Generate source trigger for precondition rule
         */
        else {
            ret = Utils.readFile(GetFile.getFile(GetFile.SinkTrigger_ConPre));
            ret = ret.replaceAll("<ID>", String.valueOf(constraint.getParameterId()));
        }
        ret = ret.replaceAll("<FunctionName>", constraint.getFunction().getName());
        return ret;
    }
    
    public static void main(String[] args) throws IOException{
        String pathToSpec = "E:\\Boya-research\\EclipseWorkSpace\\ABBKlocwork\\data\\tests\\test_1\\Specification\\PxxFunPreCond2.xml";
        String pathToChecker = "E:\\Boya-research\\EclipseWorkSpace\\ABBKlocwork\\data\\tests\\test_1\\AutoCheckers";
        String cmd = "cmd.exe /C cd " + pathToChecker;
        cmd += "&& kwcreatechecker --language cxx --type common --code ";       
        RuleSpecificationParser parser = new RuleSpecificationParser(pathToSpec);
		try{
			parser.parse();
		}catch(Exception e){
			e.printStackTrace();
		}
        List<CheckerInfo> checkerinfoList = parser.getConCheckerInfo();
        List<ConditionalCheckConstraint> constList = parser.getConList();
        for(int i = 0; i < checkerinfoList.size(); i++){
            CheckerInfo curCheckerInfo = checkerinfoList.get(i);
            ConditionalCheckConstraint constraint = constList.get(i);
            curCheckerInfo.setCheckerName("Pxx" + curCheckerInfo.getCheckerName());
            cmd += curCheckerInfo.getCheckerName();
            ExecWrapper exe = new ExecWrapper(cmd);
            System.out.println(cmd);
            exe.run();
            
            String path = pathToChecker + "\\" + curCheckerInfo.getCheckerName();
            
            ConditionalCheckConstraintCheckerBuilder builder = new ConditionalCheckConstraintCheckerBuilder(false, curCheckerInfo, constraint);
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
