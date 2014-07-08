package boya.research.abb.checkergenerator.constraints;


import boya.research.abb.checkergenerator.data.Function;

/**
 * This function records the call constraint on function_1
 * Basically, when function_1 is called, function_2 should be called. 
 * And there corresponding parameters should be the same variable
 * @author Boya Sun
 *
 */
public class CallConstraint {
    public static String pre = "pre";
    public static String post = "post";
    Function function_1, function_2;
    CallPair paramPair;
    String location;
    
    public CallConstraint(Function function_1, Function function_2, CallPair paramPair, String location){
        this.function_1 = function_1;
        this.function_2 = function_2;
        this.paramPair = paramPair;
        if(paramPair.i_1 == 0 && paramPair.i_2 == 0)
            throw new IllegalArgumentException("parameter pair cannot be <0,0>, which indicates a pair of return values");
        if(!location.equals("pre") && !location.equals("post"))
            throw new IllegalArgumentException("location should be pre or post");
        if(location.equals("pre") && paramPair.i_1 == 0)
            throw new IllegalArgumentException("When specifying a pre call constraint, the function_1 cannot be involved with the return value");
        if(location.equals("post") && paramPair.i_2 == 0)
            throw new IllegalArgumentException("When specifying a post call constraint, the function_2 cannot be involved with the return value");
        this.location = location;
    }

    public Function getFunction_1() {
        return function_1;
    }

    public Function getFunction_2() {
        return function_2;
    }

    public CallPair getParamPair() {
        return paramPair;
    }

    public String getLocation() {
        return location;
    }
       
}


