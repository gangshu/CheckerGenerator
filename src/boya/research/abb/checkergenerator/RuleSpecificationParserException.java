package boya.research.abb.checkergenerator;

public class RuleSpecificationParserException extends Exception {

    String msg;
    public RuleSpecificationParserException(String strMessage){
      super(strMessage);
      msg = strMessage;
     }

     public String toString(){
      return "RuleSpecificationParserException ["+msg+"]";
     }  

}
