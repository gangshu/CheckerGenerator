package boya.research.abb.checkergenerator.testcasebuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import boya.research.abb.checkergenerator.RuleSpecificationParser;
import boya.research.abb.checkergenerator.constraints.ValueConstraint;
import boya.research.abb.checkergenerator.data.CheckerInfo;
import boya.research.abb.checkergenerator.data.Function;
import boya.research.abb.checkergenerator.data.Parameter;

/**
 * Notes: (1) The positive_test() and negative_test() use the same decl name as
 * the original function (2) Use "__x" as the auxiliary variable to avoid the
 * same names of variables. * @author Boya Sun
 * 
 */
public abstract class TestCaseBuilder {
	public abstract String getTypeDef();

	public abstract String getFunctionDef();

	public abstract String getPositiveTest();

	public abstract String getNegativeTest();

	Map<String, String> primitiveTypes = new HashMap<String, String>();
	String[] primTypes = { "char", "signed char", "short", "int", "long",
			"__int64", "unsigned char", "unsigned short", "unsigned int",
			"unsigned long", "unsigned __int64", "float", "double", "void", "bool", "size_t", "intptr_t", "enum"};

	public TestCaseBuilder() {
		for (int i = 0; i < primTypes.length; i++)
			primitiveTypes.put(primTypes[i], primTypes[i]);
	}

	public String getTestCase() {
		String ret = "";
		ret += "#include <string.h>\n";
		ret += "\n";
		ret += getTypeDef();
		ret += "\n";
		ret += getFunctionDef();
		ret += "\n";
		ret += getPositiveTest();
		ret += "\n";
		ret += getNegativeTest();
		ret += "\n";
		return ret;
	}

	/**
	 * Get the preceeding code on type definition. If there is only one
	 * function, pass null to fun_2
	 * 
	 * (1) If a type has an inferred type of int or double, directly use this as
	 * its type; (2) If a type has an infered type of pointer: Define a struct;
	 * Define the type as the pointer to the above struct
	 * 
	 * @param fun_1
	 * @param fun_2
	 * @return
	 */
	public String getTypeDef(Function fun_1, Function fun_2) {
		String ret = "";
		if (fun_1 == null && fun_2 == null)
			return null;
		Map<String, String> traversedTypes = new HashMap<String, String>();
		if (fun_1 != null)
			for (int i = 0; i < fun_1.getNumParams(); i++) {
				Parameter param = fun_1.getParam(i);
				if (traversedTypes.containsKey(param.getParameterType()))
					continue;
				/**
				 * Bug Fix: Handle the case of struct someStruct
				 */
				String[] tokens = param.getParameterType().split(" +");
				if(tokens[0].equals("struct")){
					ret += param.getParameterType() + "{\n\tint stub;\n};\n";
					continue;
				}
				/**
				 * Bug Fix: Handle infered types
				 */
				if (!this.primitiveTypes.containsKey(param.getParameterType())) {
					traversedTypes.put(param.getParameterType(), param
							.getParameterType());

					if (param.getInferedParamType() != null) {
						if (!param.getInferedParamType().equals("pointer"))
							ret += "typedef " + param.getInferedParamType()
									+ " " + param.getParameterType() + ";\n";
						else{
							ret += "typedef struct{\n" + "    int stub;\n" + "} ";
							ret += param.getParameterType() + "_struct";
							ret += ";";
							ret += "\n";
							ret += "typedef " + param.getParameterType() + "_struct*" + " " + param.getParameterType() + ";\n";
						}
					} else {
						ret += "typedef struct{\n" + "    int stub;\n" + "} ";
						ret += param.getParameterType();
						ret += ";";
						ret += "\n";
					}
				}
			}

		if (fun_2 != null)
			for (int i = 0; i < fun_2.getNumParams(); i++) {
				Parameter param = fun_2.getParam(i);
				if (traversedTypes.containsKey(param.getParameterType()))
					continue;
				/**
				 * Bug Fix: Handle the case of struct someStruct
				 */
				String[] tokens = param.getParameterType().split(" +");
				if(tokens[0].equals("struct")){
					ret += param.getParameterType() + "{\n\tint stub;\n};";
					continue;
				}
				/**
				 * Bug Fix: Handle infered types
				 */
				if (!this.primitiveTypes.containsKey(param.getParameterType())) {
					traversedTypes.put(param.getParameterType(), param
							.getParameterType());

					if (param.getInferedParamType() != null) {
						if (!param.getInferedParamType().equals("pointer"))
							ret += "typedef " + param.getInferedParamType()
									+ " " + param.getParameterType() + ";\n";
						else{
							ret += "typedef struct{\n" + "    int stub;\n" + "} ";
							ret += param.getParameterType() + "_struct";
							ret += ";";
							ret += "\n";
							ret += "typedef " + param.getParameterType() + "_struct*" + " " + param.getParameterType() + ";\n";
						}
					} else {
						ret += "typedef struct{\n" + "    int stub;\n" + "} ";
						ret += param.getParameterType();
						ret += ";";
						ret += "\n";
					}
				}
			}

		return ret;
	}

	/**
	 * Get function definition for all but the post value constraints
	 * 
	 * @param func
	 * @return
	 */
	public String getFunctionDef(Function func) {
		String str = "";
		str += getFunctionDecl(func);
		str += "{\n";
		if (!(func.getParam(0).getParameterType().equals("void") && func.getParam(0).getPointerLevel() == 0)
				&& !func.getParam(0).getParameterType().equals("empty")) {
			/**
			 * Bug fix: For structures, cannot directly cast to 0
			 */
			if (!this.primitiveTypes.containsKey(func.getParam(0)
					.getParameterType())
					&& func.getParam(0).getPointerLevel() == 0) {
				str += "\t" + func.getParam(0).getParameterType() + " __x;\n";
				str += "\tmemset(&__x, 0, sizeof __x);\n";
				str += "\treturn __x;\n";
			} else
				str += "\treturn (" + func.getParam(0).getParameterType()
						+ getPointers(func.getParam(0).getPointerLevel())
						+ ") 0; \n";
		}
		str += "}\n";
		return str;
	}

	/**
	 * Get the declaration of a function
	 * 
	 * @param func
	 * @return
	 */
	public String getFunctionDecl(Function func) {
		String ret = "";
		/**
		 * 1. Get ret Type
		 */
		int count = 0;
		String retType = func.getParam(0).getParameterType();
		if (retType.equals("empty"))
			retType = "void";
		ret += retType + getPointers(func.getParam(0).getPointerLevel()) + " ";
		/**
		 * 2. Get function name
		 */
		ret += func.getName() + "(";
		/**
		 * 3. Get parameter list
		 */
		for (int i = 1; i < func.getNumParams(); i++) {
			Parameter param = func.getParam(i);
//			if (param.getDeclName() == null)
//			continue;
			if (param.getParameterType().equals("void") && param.getPointerLevel() == 0)
				continue;
			ret += param.getParameterType()
					+ getPointers(param.getPointerLevel()) + " "
					+ (param.getDeclName() == null ? "param_" + (count++) : param.getDeclName());
			if (i != func.getNumParams() - 1)
				ret += ", ";
		}

		ret += ")";
		return ret;

	}

	public String getPointers(int x) {
		String ret = "";
		for (int i = 0; i < x; i++)
			ret += "*";
		return ret;
	}

	public String getAddrs(int x) {
		String ret = "";
		for (int i = 0; i < x; i++)
			ret += "&";
		return ret;
	}

	public double getIllegalValue(ValueConstraint constraint) {
		if (constraint.getEConstraint() != null)
			return constraint.getEConstraint() + 1;
		if (constraint.getNEConstraint() != null)
			return constraint.getNEConstraint();
		if (constraint.getGConstraint() != null)
			return constraint.getGConstraint() - 1;
		if (constraint.getGEConstraint() != null)
			return constraint.getGEConstraint() - 1;
		if (constraint.getSConstraint() != null)
			return constraint.getSConstraint() + 1;
		if (constraint.getSEConstraint() != null)
			return constraint.getSEConstraint() + 1;
		throw new IllegalArgumentException("No value constraint is specified");
	}

	public double getLegalValue(ValueConstraint constraint) {
		if (constraint.getEConstraint() != null)
			return constraint.getEConstraint();
		if (constraint.getNEConstraint() != null)
			return constraint.getNEConstraint() + 1;
		if (constraint.getGConstraint() != null
				&& constraint.getSConstraint() != null) {
			double low = constraint.getGConstraint();
			double high = constraint.getSConstraint();
			double ret = (low + high) / 2.0d;
			// We prefer an interger
			if (Math.round(ret) > low && Math.round(ret) < high)
				return Math.round(ret);
			else
				return ret;
		}

		if (constraint.getGEConstraint() != null
				&& constraint.getSConstraint() != null) {
			double low = constraint.getGEConstraint();
			double high = constraint.getSConstraint();
			double ret = (low + high) / 2.0d;
			// We prefer an interger
			if (Math.round(ret) >= low && Math.round(ret) < high)
				return Math.round(ret);
			else
				return ret;
		}

		if (constraint.getGConstraint() != null
				&& constraint.getSEConstraint() != null) {
			double low = constraint.getGConstraint();
			double high = constraint.getSEConstraint();
			double ret = (low + high) / 2.0d;
			// We prefer an interger
			if (Math.round(ret) > low && Math.round(ret) <= high)
				return Math.round(ret);
			else
				return ret;
		}

		if (constraint.getGEConstraint() != null
				&& constraint.getSEConstraint() != null) {
			double low = constraint.getGEConstraint();
			double high = constraint.getSEConstraint();
			double ret = (low + high) / 2.0d;
			// We prefer an interger
			if (Math.round(ret) >= low && Math.round(ret) <= high)
				return Math.round(ret);
			else
				return ret;
		}
		if (constraint.getGConstraint() != null)
			return constraint.getGConstraint() + 1;
		if (constraint.getGEConstraint() != null)
			return constraint.getGEConstraint() + 1;
		if (constraint.getSConstraint() != null)
			return constraint.getSConstraint() - 1;
		if (constraint.getSEConstraint() != null)
			return constraint.getSEConstraint() - 1;
		throw new IllegalArgumentException("No value constraint is specified");
	}

	/**
	 * Get Declaration for the test functions for all but the call constraint
	 * checkers The parameters are named as param1, param2, etc.
	 * 
	 * @param isPositive
	 * @param func
	 * @return
	 */
	String getTestFunctionDecl(boolean isPositive, Function func) {
		String ret = "";
		String funName = isPositive ? "positive_test" : "negative_test";
		ret += "void " + funName + "(";
		for (int i = 1; i < func.getNumParams(); i++) {
			Parameter param = func.getParam(i);
			/**
			 * Bug fix: Some parameter has empty DeclName
			 */
//			if (param.getDeclName() == null)
//				continue;
			if (param.getParameterType().equals("void") && param.getPointerLevel() == 0)
				continue;
			ret += param.getParameterType()
					+ getPointers(param.getPointerLevel()) + " " + "param" + i;
			if (i != func.getNumParams() - 1)
				ret += ", ";
		}
		ret += ")";
		return ret;
	}

	String getCall(Function func) {
		String ret = "";
		ret += func.getName();
		ret += "(";
		for (int i = 1; i < func.getNumParams(); i++) {
			Parameter param = func.getParam(i);
//			if (param.getDeclName() == null)
//			continue;
			if (param.getParameterType().equals("void") && param.getPointerLevel() == 0)
				continue;
			ret += "param" + i;
			if (i != func.getNumParams() - 1)
				ret += ", ";
		}
		ret += ")";
		return ret;
	}

}
