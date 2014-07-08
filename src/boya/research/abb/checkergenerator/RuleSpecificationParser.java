package boya.research.abb.checkergenerator;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import boya.research.abb.checkergenerator.constraints.*;
import boya.research.abb.checkergenerator.data.*;
import boya.research.abb.checkergenerator.utils.Utils;

/**
 * This is used to parse the Rule Spec in the XML format. We need to get the
 * following information from the parser: List of different Constraints
 * CheckerInfo
 * 
 * @author Boya Sun
 */
public class RuleSpecificationParser {
    String pathToXML = null;

    String XMLString = null;
    
    public String getPathToXML() {
		return pathToXML;
	}

	public void setPathToXML(String pathToXML) {
		this.pathToXML = pathToXML;
	}

	public String getXMLString() {
		return XMLString;
	}

	public void setXMLString(String string) {
		XMLString = string;
	}

	List<ValueConstraint> valList = null;

    List<CheckerInfo> valCheckerInfo = null;

    List<ConditionalCheckConstraint> conList = null;

    List<CheckerInfo> conCheckerInfo = null;

    List<CallConstraint> calList = null;

    List<CheckerInfo> calCheckerInfo = null;

    Function function;

    public String getPsathToXML() {
        return pathToXML;
    }

    public void parse() throws ParserConfigurationException, RuleSpecificationParserException, SAXException, IOException {
            /**
             * 1. Get the document
             */
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document doc;
            if(pathToXML == null && XMLString == null)
            	throw new RuleSpecificationParserException("");
            if(pathToXML == null){
            	InputSource is = new InputSource(new StringReader(XMLString));
            	doc = documentBuilder.parse(is);
            }else
            	doc = documentBuilder.parse(pathToXML);
            	
            Element root = doc.getDocumentElement();
            NodeList nodesList = root.getChildNodes();
            int countEleFunc = 0, countEleCnsts = 0;
            Element eleFun = null, eleCnst = null;
            for (int i = 0; i < nodesList.getLength(); i++) {
                Node node = nodesList.item(i);
                if (node instanceof Element) {
                    if (node.getNodeName().equals("Function")) {
                        eleFun = (Element) node;
                        countEleFunc++;
                    }
                    if (node.getNodeName().equals("Constraints")) {
                        eleCnst = (Element) node;
                        countEleCnsts++;
                    }
                }
            }

            if (countEleFunc == 0 || countEleFunc > 1)
                throw new RuleSpecificationParserException(
                    "Specification should have one and only one Function element under the root element");
            if (countEleCnsts == 0 || countEleCnsts > 1)
                throw new RuleSpecificationParserException(
                    "Specification should have one and only one Constraints element under the root element");
            /**
             * 2. Get the function element
             */
            this.function = parseFunction(eleFun);
            /**
             * 3. Get the constraints element
             */
            NodeList valConstraints = eleCnst.getElementsByTagName("ValueConstraint");
            NodeList calConstraints = eleCnst.getElementsByTagName("CallConstraint");
            NodeList conConstraints = eleCnst.getElementsByTagName("ConditionalCheckConstraint");

            if (valConstraints != null && valConstraints.getLength() > 0) {
                for (int i = 0; i < valConstraints.getLength(); i++) {
                    Element e = (Element) valConstraints.item(i);
                    ValueConstraint valueConstraint = parseVal(e);
                    /**
                     * Bug Fix: propagate inferred types
                     */
                    valueConstraint.getFunction().propagateInferedParamType();
                    if (valList == null)
                        valList = new ArrayList<ValueConstraint>();
                    valList.add(valueConstraint);

                    CheckerInfo checkerInfo = getCheckerInfoVal(valueConstraint);
                    if (this.valCheckerInfo == null)
                        this.valCheckerInfo = new ArrayList<CheckerInfo>();
                    this.valCheckerInfo.add(checkerInfo);
                }
            }

            if (calConstraints != null && calConstraints.getLength() > 0) {
                for (int i = 0; i < calConstraints.getLength(); i++) {
                    Element e = (Element) calConstraints.item(i);
                    CallConstraint calConstraint = parseCal(e);
                    if (calList == null)
                        calList = new ArrayList<CallConstraint>();
                    calList.add(calConstraint);
                    /**
                     * Bug Fix: propagate inferred types
                     */
                    calConstraint.getFunction_1().propagateInferedParamType();
                    calConstraint.getFunction_2().propagateInferedParamType();
                    CheckerInfo checkerInfo = getCheckerInfoCal(calConstraint);
                    if (this.calCheckerInfo == null)
                        this.calCheckerInfo = new ArrayList<CheckerInfo>();
                    this.calCheckerInfo.add(checkerInfo);
                }
            }

            if (conConstraints != null && conConstraints.getLength() > 0) {
                for (int i = 0; i < conConstraints.getLength(); i++) {
                    Element e = (Element) conConstraints.item(i);
                    ConditionalCheckConstraint conConstraint = parseCon(e);
                    if (conList == null)
                        conList = new ArrayList<ConditionalCheckConstraint>();
                    conList.add(conConstraint);
                    /**
                     * Bug Fix: propagate inferred types
                     */
                    conConstraint.getFunction().propagateInferedParamType();
                    CheckerInfo checkerInfo = getCheckerInfoCon(conConstraint);
                    if (this.conCheckerInfo == null)
                        this.conCheckerInfo = new ArrayList<CheckerInfo>();
                    this.conCheckerInfo.add(checkerInfo);
                }
            }
            
            
            
            if (valList == null && calList == null && conList == null)
                throw new RuleSpecificationParserException("No constraints specified in " + pathToXML);
    }

    public Function parseFunction(Element eleFun) throws RuleSpecificationParserException {
        String functionName = getTextByTagName(eleFun, "name");
        /**
         * Bug Fix: The name should not contain namespaces
         */
        if(functionName.contains("::")){
        	String[] tokens = functionName.split("::");
        	functionName = tokens[tokens.length-1];
        }
        //Get the name without namespace
        if (functionName.contains("::")){
            int start = functionName.lastIndexOf("::") + 2;
            functionName = functionName.substring(start);
        }
        List<Parameter> parameterList = new ArrayList<Parameter>();
        NodeList retList = eleFun.getElementsByTagName("return");
        if (retList == null || retList.getLength() != 1)
            throw new RuleSpecificationParserException(
                "boya.research.abb.checkergenerator.RuleSpecificationParser.parseFunction: No element with name \"return\" or the number of elements is more than one");
        Parameter ret = parseParameter((Element) retList.item(0));
        parameterList.add(ret);
        NodeList paramList = eleFun.getElementsByTagName("param");
        for (int i = 0; i < paramList.getLength(); i++) {
            Parameter param = parseParameter((Element) paramList.item(i));
            parameterList.add(param);
        }

        return new Function(functionName, parameterList);
    }

    Parameter parseParameter(Element eleParam) throws RuleSpecificationParserException {
        /**
         * 1. Get text of type
         */
        String typeText = getTextByTagName(eleParam, "type");
        if (typeText == null)
            throw new RuleSpecificationParserException(
                "boya.research.abb.checkergenerator.RuleSpecificationParser.parseParameter: No type defined for element "
                    + eleParam.getNodeName() + " " + eleParam.getAttribute("id"));
        /**
         * 2. Parse text of type Get pointer level; Remove the qualifiers and
         * get the type of the variable
         */
        //2.1 Get pointer level
        String typeText_1 = typeText.replaceAll("\\[\\]", "\\*");
        String typeWithoutStar = typeText_1.replaceAll("\\*", "");
        int pointerLevel = typeText_1.length() - typeWithoutStar.length();
        String type = "";
        String[] tokens = typeWithoutStar.split(" +");
        if (tokens.length < 1)
            throw new RuleSpecificationParserException(
                "boya.research.abb.checkergenerator.RuleSpecificationParser.parseParameter: Illegal type "
                    + getTextByTagName(eleParam, "type"));
        int i = 0;
        
        //2.2 Remove "const" quantifier
        if (tokens[0].equals("const"))
            i++;
        if (i >= tokens.length)
            throw new RuleSpecificationParserException(
                "boya.research.abb.checkergenerator.RuleSpecificationParser.parseParameter: Illegal type "
                    + getTextByTagName(eleParam, "type"));
        //2.3 For type tokens that are in the form of NAMESPACE::CLASSMEMBER, only keep the classmember
        for(int j = i; j < tokens.length; j++){
        	System.out.println(tokens[j]);
            String[] singleTypeTokens = tokens[j].split("::");
            if(singleTypeTokens != null && singleTypeTokens.length > 1)
                tokens[j] = singleTypeTokens[singleTypeTokens.length - 1];            
        }
        for (; i < tokens.length; i++){
        	if(tokens[i].equals("const"))
        		continue;
            if (i != tokens.length - 1)
                type += tokens[i] + " ";
            else
                type += tokens[i];
        }
        
        if(type.equals("unsigned"))
            type = "unsigned int";
        if(type != null)
            type = type.trim();
        /**
         * 3. Get declared name
         */
        String declname = getTextByTagName(eleParam, "declname");
        if(declname != null)
            declname = declname.trim();
        /**
         * 4. Get ID of param
         */
        String id = eleParam.getAttribute("id");
        int idIndex = -1;
        if (isInt(id))
            idIndex = Integer.valueOf(id);
        return new Parameter(idIndex, type, declname, pointerLevel);
    }

    /**
     * The type of a parameter could be inferred from the value of a constraint,
     * and it should be updated for the certain parameter of the function
     * Currently, the following types are supported: int, double, null
     * 
     * @param e
     * @return ValueConstraint
     * @throws RuleSpecificationParserException
     */
    ValueConstraint parseVal(Element e) throws RuleSpecificationParserException {
        /**
         * 1. Get parameter involved
         */

        NodeList paramList = e.getElementsByTagName("param");
        if (paramList == null || paramList.getLength() == 0)
            throw new RuleSpecificationParserException("No parameter specified for the ValueConstraint");
        Element param = (Element) paramList.item(0);
        String id = getTextByTagName(param, "id");
        Parameter paramInConstraint = this.function.getParam(Integer.valueOf(id));
        if (id == null)
            throw new RuleSpecificationParserException("No id specified for the parameter ValueConstraint");
        /**
         * 2. Get field involved
         */
        NodeList fieldList = param.getElementsByTagName("field");
        boolean constraintOnField = false;
        Parameter fieldInConstraint = null;
        if (fieldList != null && fieldList.getLength() > 0) {
            if (fieldList.getLength() > 1)
                throw new RuleSpecificationParserException(
                    "There should be only one field element under the tag param of ValueConstraint");
            Element field = (Element) fieldList.item(0);
            fieldInConstraint = parseParameter(field);
            this.function.getParam(Integer.valueOf(id)).setField(fieldInConstraint);
            constraintOnField = true;
        }
        /**
         * 3. Get constraint involved
         */
        Parameter parameter = fieldInConstraint == null ? paramInConstraint : fieldInConstraint;
        Map<String, VConstraint> map = new HashMap<String, VConstraint>();
        int flag = 0;
        flag += parseValConstraint(map, ValueConstraint.G, e, parameter); // >
        flag += parseValConstraint(map, ValueConstraint.GE, e, parameter); // >=
        flag += parseValConstraint(map, ValueConstraint.S, e, parameter); // <
        flag += parseValConstraint(map, ValueConstraint.SE, e, parameter); // <=
        flag += parseValConstraint(map, ValueConstraint.NE, e, parameter); // !=
        flag += parseValConstraint(map, ValueConstraint.E, e, parameter); // =

        if (flag == 0)
            throw new RuleSpecificationParserException("No value constraints for ValueConstraint");
        return new ValueConstraint(function, Integer.valueOf(id), constraintOnField, null, map);
    }

    /**
     * Currently allowed constraint types: int, double or null
     * 
     * Infered parameter types: int, double, pointer
     * 
     * @param map
     * @param vcType
     * @param e
     * @param id
     * @throws RuleSpecificationParserException
     */
    private int parseValConstraint(Map<String, VConstraint> map, String vcType, Element e, Parameter param)
        throws RuleSpecificationParserException {
        String value = getTextByTagName(e, vcType);
        VConstraint vConstraint;
        if (value != null) {
            if (value.equals("NULL")) {
                if (!vcType.equals(ValueConstraint.NE) && !vcType.equals(ValueConstraint.E))
                    throw new RuleSpecificationParserException(
                        "The Greater(Equals) and Smaller(Equals) constraints cannot be NULL");
                vConstraint = new VConstraint("NULL", value);
                param.setInferedParamType("pointer");
            } else if (isInt(value)) {
                vConstraint = new VConstraint("int", value);
                param.setInferedParamType("int");
            } else if (isDouble(value)) {
                vConstraint = new VConstraint("double", value);
                param.setInferedParamType("double");
            } else
                throw new RuleSpecificationParserException("Value constraint can only be int, double or null");
            map.put(vcType, vConstraint);
            return 1;
        }
        return 0;
    }

    private boolean isInt(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    private boolean isDouble(String s) {
        try {
            Double.parseDouble(s);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    ConditionalCheckConstraint parseCon(Element e) throws RuleSpecificationParserException {
        /**
         * 1. Get parameter involved
         */
        NodeList paramList = e.getElementsByTagName("param");
        if (paramList == null || paramList.getLength() == 0)
            throw new RuleSpecificationParserException("No parameter specified for the ConditionalCheckConstraint");
        Element param = (Element) paramList.item(0);
        String id = getTextByTagName(param, "id");
        if (id == null)
            throw new RuleSpecificationParserException(
                "No id specified for the parameter ConditionalCheckConstraint");
        /**
         * 2. Get field involved
         */
        NodeList fieldList = param.getElementsByTagName("field");
        boolean constraintOnField = false;
        if (fieldList != null && fieldList.getLength() > 0) {
            if (fieldList.getLength() > 1)
                throw new RuleSpecificationParserException(
                    "There should be only one field element under the tag param of ValueConstraint");
            Element field = (Element) fieldList.item(0);
            Parameter parField = parseParameter(field);
            this.function.getParam(Integer.valueOf(id)).setField(parField);
            constraintOnField = true;
        }

        /**
         * 3. Get location
         */
        String location = getTextByTagName(e, "Location");

        return new ConditionalCheckConstraint(function, Integer.valueOf(id), constraintOnField, location);

    }

    CallConstraint parseCal(Element e) throws RuleSpecificationParserException {
        Function fun_1 = this.function;
        /**
         * 1. Get the other function
         */
        NodeList funList = e.getElementsByTagName("Function");
        if (funList == null || funList.getLength() != 1)
            throw new RuleSpecificationParserException(
                "There should be one and only one Function specification for the CallConstraint");
        Element eleFun = (Element) funList.item(0);
        Function fun_2 = parseFunction(eleFun);
        /**
         * 2. get ParameterPair
         */
        NodeList parameterPairList = e.getElementsByTagName("ParameterPair");
        if (parameterPairList == null || parameterPairList.getLength() != 1)
            throw new RuleSpecificationParserException(
                "There should be one and only one ParameterPair specification for the CallConstraint");
        Element eleParamPair = (Element) parameterPairList.item(0);
        String id_1 = getTextByTagName(eleParamPair, "First");
        String id_2 = getTextByTagName(eleParamPair, "Second");
        if (id_1 == null || id_2 == null)
            throw new RuleSpecificationParserException(
                "Both parameter pairs should be specified in ParameterPair of CallConstraint");
        /**
         * 3. Get Location
         */
        String location = getTextByTagName(e, "Location");
        /**
         * 4. Get reverse
         */
        String reverse = getTextByTagName(e, "Reverse");
        if (reverse != null && reverse.equals("Yes")) {
            /**
             * 4.1 Reverse functions
             */
            Function tmp = fun_1;
            fun_1 = fun_2;
            fun_2 = tmp;

            /**
             * 4.2 Reverse CallPair
             */
            String id_tmp = id_1;
            id_1 = id_2;
            id_2 = id_tmp;

            /**
             * 4.3 Reverse Pre <-->Post
             */
            if (location.equals("pre"))
                location = "post";
            else if (location.equals("post"))
                location = "pre";
        }

        CallPair cp = new CallPair();
        cp.i_1 = Integer.valueOf(id_1);
        cp.i_2 = Integer.valueOf(id_2);

        return new CallConstraint(fun_1, fun_2, cp, location);
    }

    CheckerInfo getCheckerInfoVal(ValueConstraint valueConstraint) {
        /**
         * 1. Get checker name
         */
        String legalFunName = Utils.getLegalFuncNameForChecker(valueConstraint.getFunction().getName());
        String constraintName = "ValParam" + valueConstraint.getParameterId();
        if (valueConstraint.isConstraintOnField())
            constraintName += "FIELD";
        String checkerName = legalFunName + constraintName;
        /**
         * 2. get Source Trigger Message
         */
        String sourceTriggerMsg = "A variable is assigned some value.";
        /**
         * 3. get Sink Trigger Message
         */
        Parameter param = valueConstraint.getFunction()
            .getParam(Integer.valueOf(valueConstraint.getParameterId()));
        String sinkTriggerMsg = "";
        if (valueConstraint.isConstraintOnField())
            sinkTriggerMsg = "Field \\\"" + param.getField().getDeclName() + "\\\" of ";
        if (valueConstraint.getParameterId() == 0) {
            sinkTriggerMsg += valueConstraint.isConstraintOnField() ? "return value" : "Return value";
        } else {
            sinkTriggerMsg += valueConstraint.isConstraintOnField() ? "parameter-" : "Parameter-";
            sinkTriggerMsg += valueConstraint.getParameterId();
        }
        sinkTriggerMsg += " of function " + valueConstraint.getFunction().getName() + " might be ";
        Map<String, VConstraint> map = valueConstraint.getMap();
        Iterator<String> it = map.keySet().iterator();
        boolean flagG = false, flagS = false;
        while (it.hasNext()) {
            String str = it.next();
            VConstraint constraint = map.get(str);
            if (str.equals(ValueConstraint.G)) {
                if (flagS == true)
                    sinkTriggerMsg += ", or";
                sinkTriggerMsg += "smaller or equals to " + constraint.getValue();
                flagG = true;
            }
            if (str.equals(ValueConstraint.GE)) {
                if (flagS == true)
                    sinkTriggerMsg += " ,or";
                sinkTriggerMsg += "smaller than " + constraint.getValue();
                flagG = true;
            }
            if (str.equals(ValueConstraint.S)) {
                if (flagG == true)
                    sinkTriggerMsg += ", or";
                sinkTriggerMsg += "greater or equals to " + constraint.getValue();
                flagS = true;
            }
            if (str.equals(ValueConstraint.SE)) {
                if (flagG == true)
                    sinkTriggerMsg += ", or";
                sinkTriggerMsg += "greater than " + constraint.getValue();
                flagS = true;
            }
            if (str.equals(ValueConstraint.NE))
                sinkTriggerMsg += "equals to " + constraint.getValue();
            if (str.equals(ValueConstraint.E))
                sinkTriggerMsg += "not equals to " + constraint.getValue();

        }
        sinkTriggerMsg += ".";
        
        /**
         * 4. get Checker Message
         */
        String checkerMsg = sinkTriggerMsg;
        /**
         * 5. get Checker title
         */
        String checkerTitle = "Check whether " + sinkTriggerMsg.toLowerCase().replace("might be", "is");
        
        return new CheckerInfo(checkerName, sourceTriggerMsg, sinkTriggerMsg, checkerMsg, checkerTitle);
    }

    CheckerInfo getCheckerInfoCal(CallConstraint calConstraint) {
        /**
         * 1. Get checker name
         */
        String fName_1 = Utils.getLegalFuncNameForChecker(calConstraint.getFunction_1().getName());
        String fName_2 = Utils.getLegalFuncNameForChecker(calConstraint.getFunction_2().getName());
        String checkerName = fName_1 + "Cal" + fName_2 + calConstraint.getLocation().toUpperCase();
        String sourceTriggerMsg = "A variable is assigned a value.";
        String sinkTriggerMsg = "The variable is used in function " + calConstraint.getFunction_1().getName()
            + ", such that the function " + calConstraint.getFunction_2().getName() + " is not called ";
        String loc = "";
        if (calConstraint.getLocation().equals("pre"))
            loc = "before";
        else
            loc = "after";
        sinkTriggerMsg += loc + " it is called";
        
        String checkerMessage = calConstraint.getFunction_2().getName() + " " + " is not called " + loc + " " + calConstraint.getFunction_1().getName() + " is called";
        String checkerTitle = "Check whether " + calConstraint.getFunction_2().getName() + " " + " is called " + loc + " " + calConstraint.getFunction_1().getName() + " is called";
        return new CheckerInfo(checkerName, sourceTriggerMsg, sinkTriggerMsg, checkerMessage, checkerTitle);
    }

    CheckerInfo getCheckerInfoCon(ConditionalCheckConstraint conConstraint) {
        String legalFunName = Utils.getLegalFuncNameForChecker(conConstraint.getFunction().getName());
        String checkerName = legalFunName + "ConParam" + conConstraint.getParameterId();
        String sourceTriggerMsg = "A variable is assigned a value.";
        String sinkTriggerMsg = "The variable is used in function " + conConstraint.getFunction().getName()
            + ", such that ";
        String tmp = "";
        if (conConstraint.getParameterId() == 0)
            tmp += "return value ";
        else
            tmp += "parameter-" + conConstraint.getParameterId() + " ";
        tmp += "is not involved in a conditional check ";
        if (conConstraint.getLocation().equals("pre"))
            tmp += "before it is called.";
        else
            tmp += "after it is called";
        sinkTriggerMsg = sinkTriggerMsg + tmp;
        String checkerMessage = sinkTriggerMsg;
        String checkerTitle = "Check whether " + tmp.replace("is not", "is");
        return new CheckerInfo(checkerName, sourceTriggerMsg, sinkTriggerMsg, checkerMessage, checkerTitle);
    }

    private String getTextByTagName(Element e, String tag) throws RuleSpecificationParserException {
        NodeList children = e.getElementsByTagName(tag);
        if (children == null || children.getLength() == 0)
            return null;
        if (children.getLength() > 1)
            throw new RuleSpecificationParserException(
                "boya.research.abb.checkergenerator.RuleSpecificationParser.getTextByTagName: " + e.getNodeName()
                    + " should have one and only one child of " + tag);
        return children.item(0).getFirstChild().getNodeValue();
    }

    /**************************** Gettters **********************************/
    public List<CheckerInfo> getValCheckerInfo() {
        return valCheckerInfo;
    }

    public List<CheckerInfo> getConCheckerInfo() {
        return conCheckerInfo;
    }

    public List<CheckerInfo> getCalCheckerInfo() {
        return calCheckerInfo;
    }

    public List<ValueConstraint> getValList() {
        return valList;
    }

    public List<ConditionalCheckConstraint> getConList() {
        return conList;
    }

    public List<CallConstraint> getCalList() {
        return calList;
    }

    public CheckerInfo getCheckerInfo() {
        return checkerInfo;
    }

    CheckerInfo checkerInfo;

    public RuleSpecificationParser(){
        
    }
    
    public RuleSpecificationParser(String pathToXML) {
        this.pathToXML = pathToXML;
    }
    
    public Function getFunction() {
        return function;
    }

    /*********************** Tests **************************************/
    public static void main(String[] args) throws Exception {
        testParseFunc();
    }
    
    static void testSpec(){
        String pathToXML = "data\\tests\\unitTests\\P614FunPostCond.xml";
        RuleSpecificationParser parser = new RuleSpecificationParser(pathToXML);
		try{
			parser.parse();
		}catch(Exception e){
			e.printStackTrace();
		}
        List<ValueConstraint> valueConstraints = parser.getValList();
        if (valueConstraints != null) {
            for (int i = 0; i < valueConstraints.size(); i++) {
                ValueConstraint valueConstraint = valueConstraints.get(i);
                printValueConstraint(valueConstraint);
                printCheckerInfo(parser.getValCheckerInfo().get(i));
            }
        }

        List<CallConstraint> callConstraints = parser.getCalList();
        if (callConstraints != null) {
            for (int i = 0; i < callConstraints.size(); i++) {
                CallConstraint callConstraint = callConstraints.get(i);
                printCallConstraint(callConstraint);
                printCheckerInfo(parser.getCalCheckerInfo().get(i));
            }
        }

        List<ConditionalCheckConstraint> conConstraints = parser.getConList();
        if (conConstraints != null) {
            for (int i = 0; i < conConstraints.size(); i++) {
                ConditionalCheckConstraint conConstraint = conConstraints.get(i);
                printConConstraint(conConstraint);
                printCheckerInfo(parser.getConCheckerInfo().get(i));
            }
        }
    }

    static void testParseFunc() throws Exception {
        String pathToXML = "data\\tests\\unitTests\\function.xml";
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document doc = documentBuilder.parse(pathToXML);
        Element root = doc.getDocumentElement();
        RuleSpecificationParser parser = new RuleSpecificationParser(pathToXML);
        Function function = parser.parseFunction(root);
        System.out.println(function.getName());
        for (int i = 0; i < function.getNumParams(); i++)
            printParam(function.getParam(i));

    }

    static void testParseParam() throws Exception {
        String pathToXML = "data\\tests\\unitTests\\function.xml";
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document doc = documentBuilder.parse(pathToXML);
        Element root = doc.getDocumentElement();
        RuleSpecificationParser parser = new RuleSpecificationParser(pathToXML);
        Parameter param = parser.parseParameter(root);
        printParam(param);
    }

    static void printParam(Parameter param) {
        System.out.println("\t" + param.getParameterId() + " " + param.getParameterType() + " "
            + param.getDeclName() + " " + param.getPointerLevel() + " " + param.getInferedParamType());
        if (param.getField() != null)
            printParam(param.getField());
    }

    static void printFunc(Function func) {
        System.out.println(func.getName());
        for (int i = 0; i < func.getNumParams(); i++)
            printParam(func.getParam(i));

    }

    static void printValueConstraint(ValueConstraint valueConstraint) {
        System.out.println("===========================");
        Function func = valueConstraint.getFunction();
        printFunc(func);
        System.out.println("\tValue constraint on " + valueConstraint.getParameterId());
        System.out.println("\tValue constarint on field " + valueConstraint.isConstraintOnField());
        Map<String, VConstraint> map = valueConstraint.getMap();
        Iterator<String> it = map.keySet().iterator();
        while (it.hasNext()) {
            String str = it.next();
            VConstraint vConstraint = map.get(str);
            System.out.println("\t" + str + " " + vConstraint.getType() + " " + vConstraint.getValue());
        }

    }

    static void printCallConstraint(CallConstraint calConstraint) {
        System.out.println("===========================");
        System.out.println("Call Constraint: ");
        Function func_1 = calConstraint.getFunction_1();
        System.out.println("Function_1");
        printFunc(func_1);
        Function func_2 = calConstraint.getFunction_2();
        System.out.println("Function_2");
        printFunc(func_2);
        System.out.println("Location: " + calConstraint.getLocation());
        System.out.println("ParamPair: " + calConstraint.getParamPair().i_1 + " "
            + calConstraint.getParamPair().i_2);

    }

    static void printConConstraint(ConditionalCheckConstraint conConstraint) {
        System.out.println("===========================");
        Function func = conConstraint.getFunction();
        printFunc(func);
        System.out.println("Constraint on param: " + conConstraint.getParameterId());
        System.out.println("Location: " + conConstraint.getLocation());
    }

    static void printCheckerInfo(CheckerInfo cInfo) {
        System.out.println("\t" + "CheckerName: " + cInfo.getCheckerName());
        System.out.println("\t" + "Source trigger message: " + cInfo.getSourceTriggerMessage());
        System.out.println("\t" + "Sink trigger message: " + cInfo.getSinkTriggerMessage());
        System.out.println("\t" + "Checker Message: " + cInfo.getCheckerMessage());
        System.out.println("\t" + "Checker title: " + cInfo.getCheckerTitle());
    }
}
