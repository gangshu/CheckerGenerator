package boya.research.abb.checkergenerator.checkerbuilder;

import boya.research.abb.checkergenerator.auxiliaryfunctions.AuxiliaryFunctions;
import boya.research.abb.checkergenerator.data.CheckerInfo;

public abstract class CheckerBuilder {
    public abstract String generateHelperFunctions();
    public abstract String generateSourceTrigger();
    public abstract String generateSinkTrigger();
    
    boolean isInter;
    CheckerInfo checkerInfo;
    AuxiliaryFunctions aux;
    public CheckerBuilder(boolean isInter, CheckerInfo checkerInfo){
        this.isInter = isInter;
        this.checkerInfo = checkerInfo;
        aux = new AuxiliaryFunctions(checkerInfo);
    }
    
    public String generateChecker(){
        String ret = "";
        ret += generatePrecedingCode();
        ret += "\n\n";
        ret += generateHelperFunctions();
        ret += "/**************The Source Trigger**********************/";
        ret += "\n";
        ret += generateSourceTrigger();
        ret += "/**************The Sink Trigger**********************/";
        ret += "\n";
        ret += generateSinkTrigger();
        ret += "\n";
        ret += generateTrailingCode();
        return ret;
    }
    
    public String generatePrecedingCode() {
        return aux.getPreceedingCode();
    }
    
    /**
     * All the following intraprocedural checkers use process_function_2:
     * Checkers based on ConditionalCheckConstraint
     * Checkers based on CallConstraint
     * Checkers based on ValueConstraint on parameter
     * 
     * The following intraprocedural checker use process_function_1:
     * Checkers based on ValueConstraint on return value
     * @return
     */
    public String generateTrailingCode() {
        String ret = "";
        if(!isInter){
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
    
}