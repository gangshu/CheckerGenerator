package boya.research.abb.checkergenerator.testcasebuilder;

import java.util.List;
import java.util.Map;

import boya.research.abb.checkergenerator.RuleSpecificationParser;
import boya.research.abb.checkergenerator.constraints.VConstraint;
import boya.research.abb.checkergenerator.constraints.ValueConstraint;
import boya.research.abb.checkergenerator.data.CheckerInfo;
import boya.research.abb.checkergenerator.data.Function;
import boya.research.abb.checkergenerator.data.Parameter;

public class ValueConstraintTestcaseBuilder extends TestCaseBuilder {

    ValueConstraint constraint;
    String G_S = "if((<param> > <G>) && (<param> < <S>))";
    String GE_S = "if((<param> >= <G>) && (<param> < <S>))";
    String G_SE = "if((<param> > <G>) && (<param> <= <S>))";
    String GE_SE = "if((<param> >= <G>) && (<param> <= <S>))";
    String G = "if(<param> > <G>)";
    String GE = "if(<param> >= <G>)";
    String S = "if(<param> < <S>)";
    String SE = "if(<param> <= <S>)";
    String NE = "if(<param> != <N>)";
    String E = "if(<param> == <E>)";
    public ValueConstraintTestcaseBuilder(ValueConstraint constraint) {
        super();
        this.constraint = constraint;

    }

    @Override
    public String getTypeDef() {
        return getTypeDef(constraint.getFunction(), null);
    }

    /**
     * if the constraint is NE null, directly assign the input a null pointer
     * otherwise, assign an illegal value to the variable or the pointed-to
     * block of a variable
     */
    @Override
    public String getFunctionDef() {
        String ret = "";
        Parameter param = constraint.getFunction().getParam(constraint.getParameterId());
        Map<String, VConstraint> map = constraint.getMap();
        if (constraint.getParameterId() != 0)
            return getFunctionDef(constraint.getFunction());
        else {
            ret += getFunctionDecl(constraint.getFunction());
            ret += "{\n";
            ret += "\t";
            ret += param.getParameterType();
            ret += getPointers(param.getPointerLevel());
            ret += " __x;";
            if (map.containsKey(ValueConstraint.NE) && map.get(ValueConstraint.NE).getValue().equals("NULL")) {
                ret += "\n\t";
                ret += "__x = ";
                ret += "(";
                ret += param.getParameterType();
                ret += getPointers(param.getPointerLevel());
                ret += ")";
                ret += "0;";

            } else if (map.containsKey(ValueConstraint.E) && map.get(ValueConstraint.E).getValue().equals("NULL")) {
                ret += "\n";
            } else {
                ret += "\n\t";
                ret += getPointers(param.getPointerLevel());
                ret += "__x = ";
                /**
                 * Bug Fix: 1 and 1.0 are treated differently when retrieving constraint_value
                 */
                ret += "(" + param.getParameterType() +")" + getIllegalValue(constraint) + ";";
            }
            ret += "\n\treturn __x;\n";
            ret += "}\n";
            return ret;
        }
    }

    /**
     * Need to handle the following cases: (1) Preconstraint: if the constraint
     * is NE null, directly assign the input a null pointer otherwise, assign an
     * illegal value to the variable or the pointed-to block of a variable (2)
     * Postconstraint: assign the output of the function to a variable, and pass
     * it to another variable, then return
     */
    @Override
    public String getPositiveTest() {
        if (constraint.getParameterId() == 0)
            return getPositiveTestForRet();
        else
            return getPositiveTestForParam();
    }

    /**
     * Preconstraint: if the constraint is NE null, directly assign the input a
     * null pointer otherwise, assign an illegal value to the variable or the
     * pointed-to block of a variable
     * 
     * @return
     */
    public String getPositiveTestForParam() {
        String ret = "";
        ret += getTestFunctionDecl(true, constraint.getFunction());
        ret += "{\n";

        Parameter param = constraint.getFunction().getParam(constraint.getParameterId());
        Map<String, VConstraint> map = constraint.getMap();
        if (map.containsKey(ValueConstraint.NE) && map.get(ValueConstraint.NE).getValue().equals("NULL")) {
            ret += "\t";
            ret += "param" + param.getParameterId() + " = ";
            ret += "(";
            ret += param.getParameterType();
            ret += getPointers(param.getPointerLevel());
            ret += ")";
            ret += "0;";

        } else if (map.containsKey(ValueConstraint.E) && map.get(ValueConstraint.E).getValue().equals("NULL")) {
            ret += "\n";
        } else {
            ret += "\t";
            ret += getPointers(param.getPointerLevel());
            ret += "param" + param.getParameterId() + " = ";
            ret += "(" + param.getParameterType() +")" + getIllegalValue(constraint) + ";";
        }
        ret += "\n\t";
        ret += getCall(constraint.getFunction()) + ";";
        ret += "\n";
        ret += "}";
        return ret;
    }

    /**
     * assign the output of the function to a variable, and pass it to another
     * variable, then return
     * 
     * @return
     */
    public String getPositiveTestForRet() {
        String ret = "";
        ret += getTestFunctionDecl(true, constraint.getFunction());
        ret += "{\n";
        ret += "\t";
        Parameter param = constraint.getFunction().getParam(constraint.getParameterId());
        ret += param.getParameterType() + getPointers(param.getPointerLevel()) + " __x = "
            + getCall(constraint.getFunction()) + ";";
        ret += "\n";
        ret += "\t" + param.getParameterType() + getPointers(param.getPointerLevel()) + " __y = __x;";
        ret += "\n";
        ret += "}";
        return ret;

    }

    /**
     * (1) Constraint on parameter
     * (2) Constraint on return
     */
    @Override
    public String getNegativeTest() {
        if (constraint.getParameterId() == 0)
            return getNegativeTestForRet();
        else
            return getNegativeTestForParam();
    }
    /**
     * Assign a legal value for parameter, then call the function
     * @return
     */
    public String getNegativeTestForParam(){
        String ret = "";
        ret += getTestFunctionDecl(false, constraint.getFunction());
        ret += "{\n";

        Parameter param = constraint.getFunction().getParam(constraint.getParameterId());
        Map<String, VConstraint> map = constraint.getMap();
        
         //NE = null
        /**
         * Null pointers should be treated differently:
         * 
         */
        if (map.containsKey(ValueConstraint.NE) && map.get(ValueConstraint.NE).getValue().equals("NULL")) {        	
            ret += "\t" + param.getParameterType() + getPointers(param.getPointerLevel()) + " __x = " + "param" + param.getParameterId() + ";\n";
            ret += "\tif(__x != 0)\n";
            ret += "\t\t" + getCall(constraint.getFunction()).replace("param" + param.getParameterId(), "__x") + ";";
            ret += "\n";
            ret += "}";
            return ret;
        } 
        //E = null
        else if (map.containsKey(ValueConstraint.E) && map.get(ValueConstraint.E).getValue().equals("NULL")) {
            ret += "\t";
            ret += "param" + param.getParameterId() + " = ";
            ret += "(";
            ret += param.getParameterType();
            ret += getPointers(param.getPointerLevel());
            ret += ")";
            ret += "0;";
        } 
        else {
            ret += "\t";
            ret += getPointers(param.getPointerLevel()) + "param" + param.getParameterId() + " = " + "(" + param.getParameterType() +")" + getLegalValue(constraint) + ";";
        }
        ret += "\n\t";
        ret += getCall(constraint.getFunction()) + ";";
        ret += "\n";
        ret += "}";
        return ret;
    }
    
    /**
     * Add a conditional check on the parameter after calling the function
     * @return
     */
    public String getNegativeTestForRet(){
        String ret = "";
        ret += getTestFunctionDecl(false, constraint.getFunction());
        Parameter param = constraint.getFunction().getParam(constraint.getParameterId());
        Map<String, VConstraint> map = constraint.getMap();

        ret += "{\n";
        ret += "\t" + param.getParameterType() + getPointers(param.getPointerLevel()) + " __x = ";
        ret += getCall(constraint.getFunction()) + ";";
        ret += "\n";
        
        
        //NE NULL
        if (map.containsKey(ValueConstraint.NE) && map.get(ValueConstraint.NE).getValue().equals("NULL")) {
            ret += "\tif(__x != " + "(" + param.getParameterType() + getPointers(param.getPointerLevel()) + ")0" + ")\n";
            ret += "\t\treturn;";
        } 
        //E NULL
        else if (map.containsKey(ValueConstraint.E) && map.get(ValueConstraint.E).getValue().equals("NULL")) {
            ret += "\tif(__x == " + "(" + param.getParameterType() + getPointers(param.getPointerLevel()) + ")0" + ")\n";
            ret += "\t\treturn;";
        } else {
//            String curParam = getPointers(param.getPointerLevel()) + param.getDeclName();
        	String curParam = "__x";
        	curParam = getPointers(constraint.getFunction().getParam(constraint.getParameterId()).getPointerLevel()) + curParam;
            String cond = "";
            double G, S, N, E;
            //E
            if(constraint.getEConstraint() != null){
                E = constraint.getEConstraint();                
                cond = this.E.replace("<param>", curParam);
                cond = cond.replaceAll("<E>", String.valueOf(E));
            }
            //NE
            else if(constraint.getNEConstraint() != null){
                N = constraint.getNEConstraint();
                cond = this.NE.replace("<param>", curParam);
                cond = cond.replaceAll("<N>", String.valueOf(N));
            }
            //G & S
            else if(constraint.getGConstraint() != null && constraint.getSConstraint() != null){
                G = constraint.getGConstraint();
                S = constraint.getSConstraint();
                cond = this.G_S.replace("<param>", curParam);
                cond = cond.replaceAll("<G>", String.valueOf(G));
                cond = cond.replace("<S>", String.valueOf(S));
            }
            //GE & S
            else if(constraint.getGEConstraint() != null && constraint.getSConstraint() != null){
                G = constraint.getGEConstraint();
                S = constraint.getSConstraint();
                cond = this.GE_S.replace("<param>", curParam);
                cond = cond.replaceAll("<G>", String.valueOf(G));
                cond = cond.replace("<S>", String.valueOf(S));
            }
            //G & SE
            else if(constraint.getGConstraint() != null && constraint.getSEConstraint() != null){
                G = constraint.getGConstraint();
                S = constraint.getSEConstraint();
                cond = this.G_SE.replace("<param>", curParam);
                cond = cond.replaceAll("<G>", String.valueOf(G));
                cond = cond.replace("<S>", String.valueOf(S));
            }
            //GE & SE
            else if(constraint.getGEConstraint() != null && constraint.getSEConstraint() != null){
                G = constraint.getGEConstraint();
                S = constraint.getSEConstraint();
                cond = this.GE_SE.replace("<param>", curParam);
                cond = cond.replaceAll("<G>", String.valueOf(G));
                cond = cond.replace("<S>", String.valueOf(S));
            }
            //G
            else if(constraint.getGConstraint() != null){
                G = constraint.getGConstraint();
                cond = this.G.replace("<param>", curParam);
                cond = cond.replaceAll("<G>", String.valueOf(G));
            }
            //GE
            else if(constraint.getGEConstraint() != null){
                G = constraint.getGEConstraint();
                cond = this.GE.replace("<param>", curParam);
                cond = cond.replaceAll("<G>", String.valueOf(G));
            }
            //S
            else if(constraint.getSConstraint() != null){
                S = constraint.getSConstraint();
                cond = this.S.replace("<param>", curParam);
                cond = cond.replaceAll("<S>", String.valueOf(S));
            }
            //SE
            else if(constraint.getSEConstraint() != null){
                S = constraint.getSEConstraint();
                cond = this.SE.replace("<param>", curParam);
                cond = cond.replaceAll("<S>", String.valueOf(S));
            }
            ret += "\t" + cond;
            ret += "\n";
            ret += "\t\treturn;";
        }
        ret += "\n\treturn;\n";
        ret += "}";
        return ret;
    }

    public static void main(String[] args) {
        String pathToSpec = "data\\tests\\test_1\\Specification\\P14FunPostCond.xml";
        RuleSpecificationParser parser = new RuleSpecificationParser(pathToSpec);
		try{
			parser.parse();
		}catch(Exception e){
			e.printStackTrace();
		}
        List<CheckerInfo> checkerinfoList = parser.getValCheckerInfo();
        List<ValueConstraint> constList = parser.getValList();
//        TestCaseBuilder tcbuilder = new ValueConstraintTestcaseBuilder(null);
//        System.out.println(tcbuilder.getTypeDef(parser.getFunction(), null));
//        System.out.println(tcbuilder.getFunctionDef(parser.getFunction()));
//        System.out.println(tcbuilder.getTestFunctionDecl(true, parser.getFunction()));
//        System.out.println(tcbuilder.getCall(parser.getFunction()));
        for (int i = 0; i < constList.size(); i++) {
            ValueConstraint valueConstraint = constList.get(i);
            TestCaseBuilder tcbuilder = new ValueConstraintTestcaseBuilder(valueConstraint);
            System.out.println(tcbuilder.getTypeDef());
            System.out.println(tcbuilder.getFunctionDef());
            System.out.println(tcbuilder.getPositiveTest());
            System.out.println(tcbuilder.getNegativeTest());
        }
    }
}
