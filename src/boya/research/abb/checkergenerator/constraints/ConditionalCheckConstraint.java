package boya.research.abb.checkergenerator.constraints;

import java.util.HashMap;
import java.util.Map;

import boya.research.abb.checkergenerator.data.Function;

public class ConditionalCheckConstraint {

    Function function;
    int parameterId;
    boolean constraintOnField;
    String location;
    
    public ConditionalCheckConstraint(Function function, int parameterId, boolean constraintOnField,  String location){
        this.function = function;
        this.parameterId = parameterId;
        this.constraintOnField = constraintOnField;    
        this.location = location;
    }

    public Function getFunction() {
        return function;
    }

    public int getParameterId() {
        return parameterId;
    }

    
    public boolean isConstraintOnField() {
        return constraintOnField;
    }

    public String getLocation(){
        return location;
    }
       
}