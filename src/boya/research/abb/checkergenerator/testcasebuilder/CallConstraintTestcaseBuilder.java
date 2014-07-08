package boya.research.abb.checkergenerator.testcasebuilder;

import java.util.List;

import boya.research.abb.checkergenerator.RuleSpecificationParser;
import boya.research.abb.checkergenerator.constraints.CallConstraint;
import boya.research.abb.checkergenerator.constraints.ConditionalCheckConstraint;
import boya.research.abb.checkergenerator.data.CheckerInfo;
import boya.research.abb.checkergenerator.data.Function;
import boya.research.abb.checkergenerator.data.Parameter;

public class CallConstraintTestcaseBuilder extends TestCaseBuilder {

    CallConstraint constraint;
    public CallConstraintTestcaseBuilder(CallConstraint constraint){
        super();
        this.constraint = constraint;
    }
    
    @Override
    public String getTypeDef() {
        return getTypeDef(constraint.getFunction_1(), constraint.getFunction_2());
    }
    
    /**
     * 
     */
    @Override
    public String getFunctionDef() {
        return getFunctionDef(constraint.getFunction_1()) + "\n" + getFunctionDef(constraint.getFunction_2());
    }

    @Override
    public String getPositiveTest() {
        String ret = "";
        ret += getTestFunctionDecl(true, constraint.getFunction_1());
        
        String funcRet = "";
        if(!constraint.getFunction_1().getParam(0).getParameterType().equals("void"))
        	funcRet += constraint.getFunction_1().getParam(0).getParameterType() + getPointers(constraint.getFunction_1().getParam(0).getPointerLevel()) + " __x = ";
        
        ret += "{\n";
        ret += "\t" + funcRet + getCall(constraint.getFunction_1()) + ";\n";
        ret += "\treturn;\n";
        ret += "}\n";        
        return ret;
    }

    /**
     * Need to handle the following cases:
     * (1) parameter pair is input-input
     * (2) parameter pair is output-input
     */
    @Override
    public String getNegativeTest() {
        Function f1 = constraint.getFunction_1();
        Function f2 = constraint.getFunction_2();
        int i1 = constraint.getParamPair().i_1;
        int i2 = constraint.getParamPair().i_2;
        if(i1 != 0 && i2 !=0){
            if(constraint.getLocation().equals(CallConstraint.pre)){
                Function tmp_f = f1;
                f1 = f2;
                f2 = tmp_f;
                int tmp_i = i1;
                i1 = i2;
                i2 = tmp_i;
                return getNegativeTest1(f1, f2, i1, i2);
            }else
                return getNegativeTest1(f1, f2, i1, i2);
        }else{
            if(constraint.getLocation().equals(CallConstraint.pre)){
                Function tmp_f = f1;
                f1 = f2;
                f2 = tmp_f;
                int tmp_i = i1;
                i1 = i2;
                i2 = tmp_i;
                return getNegativeTest2(f1, f2, i1, i2);
            }else
                return getNegativeTest2(f1, f2, i1, i2);
        }
    }
    
    /**
     * Handle the case where parameter pair is input-input:
     * f1(x);
     * f2(x);
     * If input pair has different pointer levels:
     * (1) Choose the one with higher pointer level as the common input
     * (2) When calling the other one, need to deference the pointer till it reaches the appropriate memory block
     * 
     * For example, the input-pair is:
     * int **x;
     * int *x;
     * Then:
     * (1) Declaration: void netative_test(....., int ** param11);
     * (2) Call-pair:
     * f1(..., param11, ...);
     * f2(..., *param11, ...);
     * 
     * @param f1
     * @param f2
     * @return
     */
    private String getNegativeTest1(Function f1, Function f2, int i1, int i2){
        String ret = "";
        /**
         * 1. Get function declaration
         */
        ret += "void negative_test (";
        /**
         * 1.1 Get all parameters other than the common parameter for f1
         */
        for(int i = 1; i < f1.getNumParams(); i++){
            if(i == i1)
                continue;
            Parameter param = f1.getParam(i);
			if (param.getParameterType().equals("void") && param.getPointerLevel() == 0)
				continue;
            ret += param.getParameterType() + getPointers(param.getPointerLevel()) + " " + "paramX" + i + ",";
        }
        /**
         * 1.2 Get all parameters other than the common parameter for f2
         */
        for(int i = 1; i < f2.getNumParams(); i++){
            if(i == i2)
                continue;
            Parameter param = f2.getParam(i);
			if (param.getParameterType().equals("void") && param.getPointerLevel() == 0)
				continue;
            ret += param.getParameterType() + getPointers(param.getPointerLevel()) + " " + "paramY" + i + ",";            
        }
        /**
         * 1.3 Get the common parameter of f1 and f2
         * Note: if they have different pointer levels, choose the one with larger pointer level
         */
        String param_co = "param" + i1 + i2;
        Parameter param1 = f1.getParam(i1), param2 = f2.getParam(i2);
        if(param1.getPointerLevel() >= param2.getPointerLevel()){
            ret += param1.getParameterType() + getPointers(param1.getPointerLevel()) + " " + param_co;         
        }else {
            ret += param2.getParameterType() + getPointers(param2.getPointerLevel()) + " " + param_co; 
        }
        if(ret.endsWith(","))
            ret = ret.substring(0, ret.length());
        ret += ") {\n";
        /**
         * 2. Get call pair
         */
        String call1 = getCall(f1);
        String call2 = getCall(f2);
        call1 = call1.replaceAll("param", "paramX");
        call2 = call2.replaceAll("param", "paramY");
        if(param1.getPointerLevel() >= param2.getPointerLevel()){
            call1 = call1.replaceAll("paramX" + i1, param_co);
            call2 = call2.replaceAll("paramY" + i2, getPointers(param1.getPointerLevel() - param2.getPointerLevel()) + param_co);
        }
        else{
            call1 = call1.replaceAll("paramX" + i1, getPointers(param2.getPointerLevel() - param1.getPointerLevel()) + param_co);
            call2 = call2.replaceAll("paramY" + i2, param_co);
        }
        /**
         * 3. Assemble all and return
         */
        ret += "\t" + call1 + ";\n";
        ret += "\t" + call2 + ";\n";
        ret += "\treturn;\n";
        ret += "}";
        return ret;
    }
    
    /**
     * Handle the case where parameter pair is output-input:
     * y = f1();
     * f2(y);
     * @param f1
     * @param f2
     * @return
     */
    private String getNegativeTest2(Function f1, Function f2, int i1, int i2){
        String ret = "";
        /**
         * 1. Get function declaration
         */
        ret += "void negative_test (";
        /**
         * 1.1 Get all parameters other than the common parameter for f1
         */
        for(int i = 1; i < f1.getNumParams(); i++){
            Parameter param = f1.getParam(i);
			if (param.getParameterType().equals("void") && param.getPointerLevel() == 0)
				continue;
            ret += param.getParameterType() + getPointers(param.getPointerLevel()) + " " + "paramX" + i + ",";
        }
        /**
         * 1.2 Get all parameters other than the common parameter for f2, except for param_i2
         */
        for(int i = 1; i < f2.getNumParams(); i++){
            if(i == i2)
                continue;
            Parameter param = f2.getParam(i);
			if (param.getParameterType().equals("void") && param.getPointerLevel() == 0)
				continue;
            ret += param.getParameterType() + getPointers(param.getPointerLevel()) + " " + "paramY" + i;
            if(i != f2.getNumParams() - 1)
                ret += ",";            
        }
        if(ret.endsWith(","))
            ret = ret.substring(0, ret.length() - 1);
        ret += ") {\n";
        /**
         * 2. Get call pair
         */
        Parameter param1 = f1.getParam(i1), param2 = f2.getParam(i2);
        String call1 = param1.getParameterType() + getPointers(param1.getPointerLevel()) + " __x = " + getCall(f1);
        String call2 = getCall(f2);
        call1 = call1.replaceAll("param", "paramX");
        call2 = call2.replaceAll("param", "paramY");
        if(param1.getPointerLevel() >= param2.getPointerLevel()){
            call2 = call2.replaceAll("paramY"+i2, getPointers(param1.getPointerLevel() - param2.getPointerLevel()) + "__x");
        }else
            call2 = call2.replaceAll("paramY"+i2, getAddrs(param2.getPointerLevel() - param1.getPointerLevel()) + "__x");
        
        /**
         * 3. Assemble all and return
         */
        ret += "\t" + call1 + ";\n";
        ret += "\t" + call2 + ";\n";
        ret += "\treturn;\n";
        ret += "}";
        return ret;       
    }
    
    public static void main(String[] args) {
        String pathToSpec = "data\\tests\\test_1\\Specification\\PxxFunPreCall1.xml";
        RuleSpecificationParser parser = new RuleSpecificationParser(pathToSpec);
		try{
			parser.parse();
		}catch(Exception e){
			e.printStackTrace();
		}
        List<CheckerInfo> checkerinfoList = parser.getValCheckerInfo();
        List<CallConstraint> constList = parser.getCalList();
        for (int i = 0; i < constList.size(); i++) {
            CallConstraint constraint = constList.get(i);
            TestCaseBuilder tcbuilder = new CallConstraintTestcaseBuilder(constraint);
            System.out.println(tcbuilder.getTypeDef());
            System.out.println(tcbuilder.getFunctionDef());
            System.out.println(tcbuilder.getPositiveTest());
            System.out.println(tcbuilder.getNegativeTest());
        }
    }

}
