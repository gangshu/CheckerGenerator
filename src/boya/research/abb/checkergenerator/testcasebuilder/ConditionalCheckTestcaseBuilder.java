package boya.research.abb.checkergenerator.testcasebuilder;

import java.util.List;

import boya.research.abb.checkergenerator.RuleSpecificationParser;
import boya.research.abb.checkergenerator.constraints.ConditionalCheckConstraint;
import boya.research.abb.checkergenerator.constraints.ValueConstraint;
import boya.research.abb.checkergenerator.data.CheckerInfo;
import boya.research.abb.checkergenerator.data.Function;
import boya.research.abb.checkergenerator.data.Parameter;

public class ConditionalCheckTestcaseBuilder extends TestCaseBuilder {

	ConditionalCheckConstraint constraint;

	public ConditionalCheckTestcaseBuilder(ConditionalCheckConstraint constraint) {
		super();
		this.constraint = constraint;
	}

	@Override
	public String getTypeDef() {
		return getTypeDef(constraint.getFunction(), null);
	}

	@Override
	public String getFunctionDef() {
		return getFunctionDef(constraint.getFunction());
	}

	@Override
	public String getPositiveTest() {
		String ret = "";
		ret += getTestFunctionDecl(true, constraint.getFunction());
		ret += "{\n";
		ret += "\t" + getCall(constraint.getFunction()) + ";\n";
		ret += "}";
		return ret;
	}

	@Override
	public String getNegativeTest() {
		if (constraint.getParameterId() != 0)
			return getNegativeTestParam();
		else
			return getNegativeTestRet();
	}

	/**
	 * Add a conditional check before calling the function: if(x) f(x)
	 * 
	 * @return
	 */
	private String getNegativeTestParam() {
		String ret = "";
		Function func = constraint.getFunction();
		ret += getTestFunctionDecl(false, func);
		ret += "{\n";

		/**
		 * Bug Fix: A struct cannot be directly involved in a conditional
		 */
		if (!this.primitiveTypes.containsKey(func.getParam(constraint.getParameterId())
				.getParameterType())
				&& func.getParam(constraint.getParameterId()).getPointerLevel() == 0){
			String[] tokens = func.getParam(constraint.getParameterId()).getParameterType().split(" +");
			if(tokens[0].equals("struct") && func.getParam(constraint.getParameterId()).getPointerLevel() != 0)
				ret += "\tif(" + "param" + constraint.getParameterId() + "->stub)";
			else
				ret += "\tif(" + "param" + constraint.getParameterId() + ".stub)";
		}
		else
			ret += "\tif(" + "param" + constraint.getParameterId() + ")";

		ret += "\n";
		ret += "\t\t" + getCall(constraint.getFunction()) + ";";

		ret += "\n\treturn;";
		ret += "\n";
		ret += "}";
		return ret;
	}

	/**
	 * Add a conditional check after calling the function: int y = f(); if(y)
	 * return;
	 * 
	 * @return
	 */
	private String getNegativeTestRet() {
		Parameter param = constraint.getFunction().getParam(0);
		String ret = "";
		Function func = constraint.getFunction();
		ret += getTestFunctionDecl(false, constraint.getFunction());
		ret += "{\n";
		ret += "\t" + param.getParameterType()
				+ getPointers(param.getPointerLevel()) + " __x = "
				+ getCall(constraint.getFunction()) + ";";
		ret += "\n";
		if (!this.primitiveTypes.containsKey(func.getParam(constraint.getParameterId())
				.getParameterType())
				&& func.getParam(constraint.getParameterId()).getPointerLevel() == 0){
			String[] tokens = func.getParam(constraint.getParameterId()).getParameterType().split(" +");
			if(tokens[0].equals("struct") && func.getParam(constraint.getParameterId()).getPointerLevel() != 0)
				ret += "\tif(" + "__x" + "->stub)";
			else
				ret += "\tif(" + "__x" + ".stub)";
		}
		else
			ret += "\tif(__x)\n";
		ret += "\t\treturn;";

		ret += "\n\treturn;";
		ret += "\n";
		ret += "}";
		return ret;
	}

	public static void main(String[] args) {
		String pathToSpec = "data\\tests\\test_1\\Specification\\PxxFunPreCond2.xml";
		RuleSpecificationParser parser = new RuleSpecificationParser(pathToSpec);
		try {
			parser.parse();
		} catch (Exception e) {
			e.printStackTrace();
		}
		List<CheckerInfo> checkerinfoList = parser.getValCheckerInfo();
		List<ConditionalCheckConstraint> constList = parser.getConList();
		for (int i = 0; i < constList.size(); i++) {
			ConditionalCheckConstraint constraint = constList.get(i);
			TestCaseBuilder tcbuilder = new ConditionalCheckTestcaseBuilder(
					constraint);
			System.out.println(tcbuilder.getTypeDef());
			System.out.println(tcbuilder.getFunctionDef());
			System.out.println(tcbuilder.getPositiveTest());
			System.out.println(tcbuilder.getNegativeTest());
		}
	}

}
