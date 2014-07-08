package boya.research.abb.checkergenerator.checkerbuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import boya.research.abb.checkergenerator.RuleSpecificationParser;
import boya.research.abb.checkergenerator.constraints.CallConstraint;
import boya.research.abb.checkergenerator.constraints.ValueConstraint;
import boya.research.abb.checkergenerator.data.CheckerInfo;
import boya.research.abb.checkergenerator.data.Parameter;
import boya.research.abb.checkergenerator.helperfunctions.ExtractVarsInExpr;
import boya.research.abb.checkergenerator.helperfunctions.IsInteresting;
import boya.research.abb.checkergenerator.helperfunctions.Traverse;
import boya.research.abb.checkergenerator.testcasebuilder.CallConstraintTestcaseBuilder;
import boya.research.abb.checkergenerator.utils.ExecWrapper;
import boya.research.abb.checkergenerator.utils.Utils;

public class CallConstraintCheckerBuilder extends CheckerBuilder{

    CallConstraint constraint;
    public String codeToGetMem  = "\t\tfor(int i = 0; i < <PoiterLevel> && mem != 0; i++)\n" +    
                                  "\t\t\tmem = memitem_getPointed(mem);\n";
    public CallConstraintCheckerBuilder(boolean isInter, CheckerInfo checkerInfo, CallConstraint constraint) {
        super(isInter, checkerInfo);
        this.constraint = constraint;
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
        IsInteresting isInteresting = new IsInteresting(false, constraint);
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
        ret = Utils.readFile(GetFile.getFile(GetFile.SinkTrigger_Cal));
        ret = ret.replace("<FunctionName>", constraint.getFunction_1().getName());
        /**
         * 1. Post call constraint
         */
        if(constraint.getLocation().equals("post")){
            ret = ret.replace("<isForward>", "true");
        }
        /**
         * 2. Pre call constraint
         */
        else 
            ret = ret.replace("<isForward>", "false");                    
        
        /**
         * 3. Code to extract argument according to the constraint specification
         */
        if(constraint.getParamPair().i_1 == 0)
            ret = ret.replace("<CodeToGetArgInConstraint>", "          expr_t arg = node_getWrittenExpression(node);");
        else{
            ret = ret.replace("<CodeToGetArgInConstraint>", "          expr_t arg = expr_getCallArgument(expr, <ID>);");
            ret = ret.replace("<ID>", String.valueOf(constraint.getParamPair().i_1));
        }
        /**
         * 4. Code to get mem item if the involved parameters have different pointer levels
         */
        Parameter param1 = constraint.getFunction_1().getParam(constraint.getParamPair().i_1);
        Parameter param2 = constraint.getFunction_2().getParam(constraint.getParamPair().i_2);
        int pointerLevel = param2.getPointerLevel() < param1.getPointerLevel() ? param1.getPointerLevel() - param2.getPointerLevel():0;
        if(pointerLevel > 0)
            ret = ret.replace("<CodeToGetMem>", this.codeToGetMem.replace("<PoiterLevel>", String.valueOf(pointerLevel)));
        else
            ret = ret.replace("<CodeToGetMem>", "");        
        return ret;
    }
    
    public static void main(String[] args) throws IOException{
        String pathToSpec = "E:\\Boya-research\\EclipseWorkSpace\\ABBKlocwork\\data\\tests\\test_1\\Specification\\PxxFunPreCall1.xml";
        //P353FunPreCall2.xml, P458FunPostCall1.xml
        String pathToChecker = "E:\\Boya-research\\EclipseWorkSpace\\ABBKlocwork\\data\\tests\\test_1\\AutoCheckers";
        String prefix = "Pxx";
        String cmd = "cmd.exe /C cd " + pathToChecker;
        cmd += "&& kwcreatechecker --language cxx --type common --code ";       
        RuleSpecificationParser parser = new RuleSpecificationParser(pathToSpec);
		try{
			parser.parse();
		}catch(Exception e){
			e.printStackTrace();
		}
        List<CheckerInfo> checkerinfoList = parser.getCalCheckerInfo();
        List<CallConstraint> constList = parser.getCalList();
        for(int i = 0; i < checkerinfoList.size(); i++){
            CheckerInfo curCheckerInfo = checkerinfoList.get(i);
            CallConstraint calConstraint = constList.get(i);
            curCheckerInfo.setCheckerName(prefix + curCheckerInfo.getCheckerName());
            cmd += curCheckerInfo.getCheckerName();
            ExecWrapper exe = new ExecWrapper(cmd);
            System.out.println(cmd);
            exe.run();
            
            String path = pathToChecker + "\\" + curCheckerInfo.getCheckerName();
            
            CallConstraintCheckerBuilder builder = new CallConstraintCheckerBuilder(false, curCheckerInfo, calConstraint);
            CallConstraintTestcaseBuilder tcbuilder = new CallConstraintTestcaseBuilder(calConstraint);
            String checkerCode = builder.generateChecker();          
            String testcaseCode = tcbuilder.getTestCase();
            String checkerXML = CheckerXMLBuilder.getCheckerXML(curCheckerInfo);
            String helpXML = HelperXMLBuilder.getHelperXML(curCheckerInfo);
            
            try{
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
            }catch(IOException io){
                io.printStackTrace();
            }
        }
    }

}
