package boya.research.abb.checkergenerator.helperfunctions;

import boya.research.abb.checkergenerator.constraints.CallConstraint;
import boya.research.abb.checkergenerator.data.Parameter;
import boya.research.abb.checkergenerator.utils.Utils;

public class IsInteresting extends HelperFunction{
    boolean isControl;
    String isInteresting_control = "isInteresting_control.txt";
    String isInteresting_function = "isInteresting_function.txt";
    CallConstraint callConstraint;
    /**
     * For call-pair checkers:
     * In case that parameter involved in function_2 has a higher pointer level,
     * need to extract the memory so that the memory block it pointed to is at the same
     * level as the parameter involved in function_1.
     */
    public String codeToGetMem1 = "\t\tfor(int i = 0; i < <PoiterLevel> && mem1 != 0; i++)\n" +    
                                         "\t\t\tmem1 = memitem_getPointed(mem1);\n";
    public IsInteresting(boolean isControl, CallConstraint callConstraint){
        this.isControl = isControl;
        this.callConstraint = callConstraint;
    }
    @Override
    public String generateCode() {
        if(isControl)
            return Utils.readFile(getFile(this.isInteresting_control));
        else {
            String ret = Utils.readFile(getFile(this.isInteresting_function));
            ret = ret.replace("<FunctionName>", callConstraint.getFunction_2().getName());
            if(callConstraint.getParamPair().i_2 == 0)
                ret = ret.replace("<CodeToGetArgInConstraint>", "		expr_t arg = node_getWrittenExpression(node);");
            else{
                ret = ret.replace("<CodeToGetArgInConstraint>", "		expr_t arg = expr_getCallArgument(expr, <ID>);");
                ret = ret.replace("<ID>", String.valueOf(callConstraint.getParamPair().i_2));
            }
            /**
             * Extract memory
             */
            Parameter param1 = callConstraint.getFunction_1().getParam(callConstraint.getParamPair().i_1);
            Parameter param2 = callConstraint.getFunction_2().getParam(callConstraint.getParamPair().i_2);
            int pointerLevel = param2.getPointerLevel() > param1.getPointerLevel() ? param2.getPointerLevel() - param1.getPointerLevel():0;
            if(pointerLevel > 0)
                ret = ret.replace("<CodeToGetMem>", this.codeToGetMem1.replace("<PoiterLevel>", String.valueOf(pointerLevel)));
            else
                ret = ret.replace("<CodeToGetMem>", "");
            return ret;
        }
    }

    @Override
    public String generateDecl() {
    	if(!isControl)
        return "bool isInteresting(node_t, memitem_t);";
    	else
    		return "bool traverseAST(expr_t, memitem_t);\n" + "bool isInteresting(node_t, memitem_t);";
    }

}
