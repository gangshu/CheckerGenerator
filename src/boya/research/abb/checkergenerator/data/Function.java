package boya.research.abb.checkergenerator.data;

import java.util.ArrayList;
import java.util.List;

/**
 * This class describes function specification of a method.
 * 
 * In the parameter list, actual-out parameter is always the first one, with ID=0
 * @author Boya Sun
 *
 */
public class Function {
    String name;
    /**
     * Note that the return value is always the first parameter with ID = 0
     */
    List<Parameter> paramList = new ArrayList<Parameter>();
    
    public Function(String name, List<Parameter> paramList){
        this.name = name;
        this.paramList = paramList;        
        
    }
    
    public void propagateInferedParamType(){
        /**
         * Bug Fix: Need to propagate the inferred types to all the parameters
         * ExampleBug:
         * Param1: Element
         * Param2: Element, NULL
         * Since parameters are traversed in sequence, Element will be considered as a non-pointer struct
         */
        for(Parameter param:paramList){
        	if(param.getInferedParamType() != null){
        		for(Parameter param2:paramList){
        			if(param2.getParameterType().equals(param.getParameterType()))
        				param2.setInferedParamType(param.getInferedParamType());
        		}
        	}
        }
    }
    
    public String getName(){
        return name;
    }
    
    public Parameter getParam(int i){
        if(i >= paramList.size())
            throw new IllegalArgumentException("boya.research.abb.checkergenerator.function.Function.getParam: input index out of bound");
        return paramList.get(i);
    }
    
    public int getNumParams(){
        return paramList.size();
    }
    
}
