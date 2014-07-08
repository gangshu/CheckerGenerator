package boya.research.abb.checkergenerator.constraints;

public class VConstraint{
    /**
     * Currently supported type:
     * int, double, null
     * 
     * These are all infered from the XML
     */
    String type;
    String value;
    
    public VConstraint(String type, String value){
        this.type = type;
        this.value = value;
    }
    
    public String getType(){
        return type;
    }
    
    public String getValue(){
        return value;
    }
}