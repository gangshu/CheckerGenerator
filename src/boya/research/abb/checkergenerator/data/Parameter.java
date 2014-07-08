package boya.research.abb.checkergenerator.data;

import java.util.ArrayList;
import java.util.List;

public class Parameter {

    int parameterId;
    String parameterType;
    String declName;
    int pointerLevel;
    /**
     * The inferedParamType is infered from the value constraint of a parameter.
     * It could be a primitive type int or double, or a pointer
     * This is used to create a type alias of the parameterType, which is used in the test case
     */
    String inferedParamType = null;
    /**
     * Specify the field that has the constraint
     */
    Parameter field = null;
    
    public Parameter getField() {
        return field;
    }

    public Parameter(int id, String parameterType, String parameterName, int pointerLevel){
        this.parameterId = id;
        this.parameterType = parameterType;
        this.declName = parameterName;
        this.pointerLevel = pointerLevel;
    }
    
    public void setField(Parameter field){
        this.field = field;
    }
    
    public void setInferedParamType(String inferedParamType){
        this.inferedParamType = inferedParamType;
    }
    public int getParameterId() {
        return parameterId;
    }

    public String getParameterType() {
        return parameterType;
    }

    public String getDeclName() {
        return declName;
    }

    public int getPointerLevel() {
        return pointerLevel;
    }

    public String getInferedParamType() {
        return inferedParamType;
    }
}
