package boya.research.abb.checkergenerator.constraints;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import boya.research.abb.checkergenerator.RuleSpecificationParserException;
import boya.research.abb.checkergenerator.data.Function;

/**
 * This classs describes value constraint on a parameter of the function, or a
 * field of a parameter of a function. (1) parameterFieldId = -1, if none is
 * specified (2) There are the following kinds of constraints: Greater
 * GreaterEquals Smaller SmallerEquals NotEquals Equals The allowed combinations
 * are below: Greater(Equals) - Smaller(Equals) NotEquals Equals
 * 
 * @author Boya Sun
 */
public class ValueConstraint extends ConditionalCheckConstraint {
    public static String pre = "pre";

    public static String post = "post";

    public static String GE = "GreaterEquals";

    public static String G = "Greater";

    public static String S = "Smaller";

    public static String SE = "SmallerEquals";

    public static String NE = "NotEquals";

    public static String E = "Equals";

    Map<String, VConstraint> map;
    
    Double GEConstraint = null, GConstraint = null, SConstraint = null, SEConstraint = null, NEConstraint = null, EConstraint = null;

    public ValueConstraint(Function function, int parameterId, boolean constraintOnField, String location,
        Map<String, VConstraint> map) {
        super(function, parameterId, constraintOnField, location);
        this.map = map;
        /**
         * Sanity Check
         */
        Iterator<String> it = map.keySet().iterator();
        Double g = null, s = null;
        int countG = 0, countS = 0;
        try {
            while (it.hasNext()) {
                String str = it.next();
                if (str.equals(NE) || str.equals(E))
                    if (map.keySet().size() > 1)
                        throw new RuleSpecificationParserException(
                            "If the value constraint is NotEquals or Equals, it should be the only value constraint specified");
                if(str.equals(G)||str.equals(GE)){
                    VConstraint vConstraint = map.get(str);
                    g = Double.valueOf(vConstraint.getValue());
                    countG ++;
                }       
                if(str.equals(S)||str.equals(SE)){
                    VConstraint vConstraint = map.get(str);
                    s = Double.valueOf(vConstraint.getValue());
                    countS ++;
                }      
            }
            
            if((countG == 0 && countS == 0) || (countG == 1 && countS == 0) || (countG == 0 && countS == 1) || (countG == 1 && countS == 1)){
                if(g != null && s != null)
                    if(g > s)
                        throw new RuleSpecificationParserException("The Greater(Equals) and Smaller(Equals) specification is not compatible!");
            }else
                throw new RuleSpecificationParserException("The only allowed value constraint combinations are: <G(E), S(E)>, E or NE");
        } catch (Exception e) {
            e.printStackTrace();
        }
        /**
         * Assign constraint values
         */
        it = map.keySet().iterator();
        VConstraint vConstraint;
        while(it.hasNext()){
            String str = it.next();
            vConstraint = map.get(str);
            if(str.equals(NE)){
                if(vConstraint.value.equals("NULL"))
                    this.NEConstraint = new Double(0);
                else {
                    this.NEConstraint = new Double(vConstraint.value);
                }
            } else if(str.equals(E)){
                if(vConstraint.value.equals("NULL"))
                    this.EConstraint = new Double(0);
                else {
                    this.EConstraint = new Double(vConstraint.value);
                }
            } else if(str.equals(G)){
                this.GConstraint = new Double(vConstraint.value);
            } else if(str.equals(GE)){
                this.GEConstraint = new Double(vConstraint.value);
            } else if(str.equals(S)){
                this.SConstraint = new Double(vConstraint.value);
            } else if(str.equals(SE)){
                this.SEConstraint = new Double(vConstraint.value);
            } 
        }
    }

    public Double getGEConstraint() {
        return GEConstraint;
    }

    public Double getGConstraint() {
        return GConstraint;
    }

    public Double getSConstraint() {
        return SConstraint;
    }

    public Double getSEConstraint() {
        return SEConstraint;
    }

    public Double getNEConstraint() {
        return NEConstraint;
    }

    public Double getEConstraint() {
        return EConstraint;
    }

    public Map<String, VConstraint> getMap() {
        return map;
    }
}