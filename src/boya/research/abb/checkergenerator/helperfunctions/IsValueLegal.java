package boya.research.abb.checkergenerator.helperfunctions;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import boya.research.abb.checkergenerator.auxiliaryfunctions.AuxiliaryFunctions;
import boya.research.abb.checkergenerator.constraints.VConstraint;
import boya.research.abb.checkergenerator.constraints.ValueConstraint;
import boya.research.abb.checkergenerator.utils.Utils;

public class IsValueLegal extends HelperFunction {

	/**
	 * This file is the template for the function. Need to replace
	 * <ConstraintCode> with the generated constraint code
	 */
	static String isValueLegal = "isValueLegal.txt";
	ValueConstraint constraint;

	public IsValueLegal(ValueConstraint constraint) {
		this.constraint = constraint;
	}

	@Override
	public String generateCode() {
		String code = getFunctionTemplate();
		code = code.replace("<ConstraintCode>", getConstraintCode());
		return code;
	}

	@Override
	public String generateDecl() {
		return "bool isValueLegal(int, expr_t, node_t, function_t);";
	}

	public String getConstraintCode() {
		String ret = "";
		// !=
		if (constraint.getNEConstraint() != null) {
			ret = "	//==============NE Constraint=====================================\n"
					+ "	long ne_value;\n"
					+ "	if(constraint_isNE(constraint, &ne_value) && ne_value == <value>){\n"
					+ "       constraint_delete(constraint);\n"
					+ "       return true;\n"
					+ "	}\n"
					+ "	else if(constraint_isValue(constraint) && constraint_getValue(constraint) != <value>){\n"
					+ "       constraint_delete(constraint);\n"
					+ "       return true;\n"
					+ " }else if(constraint_getMinValue(constraint, &ne_value) && ne_value  > <value>){\n"
					+ "       constraint_delete(constraint);\n"
					+ "       return true;\n"
					+ " }else if(constraint_getMaxValue(constraint, &ne_value) && ne_value  < <value>){\n"
					+ "       constraint_delete(constraint);\n"
					+ "       return true;\n" + "}";
			ret = ret.replace("<value>", constraint.getNEConstraint()
					.toString());
		}
		// =
		else if (constraint.getEConstraint() != null) {
			ret = "	//==============E Constraint=====================================\n"
					+ "	long eq_value;\n"
					+ "	if(constraint_isEQ(constraint, &eq_value) && eq_value == <value>){\n"
					+ "		constraint_delete(constraint);\n"
					+ "		return true;\n"
					+ "	}\n"
					+ "else if(constraint_isValue(constraint) && constraint_getValue(constraint) == <value>){\n"
					+ "	   constraint_delete(constraint);\n"
					+ "       return true;\n"
					+ "	}\n";
			ret = ret.replace("<value>", constraint.getEConstraint().toString());
		}

		else if (constraint.getGEConstraint() != null
				|| constraint.getGConstraint() != null) {
			// Interval
			if (constraint.getSConstraint() != null
					|| constraint.getSEConstraint() != null) {
				ret = "	//==============Interval Constraint=====================================\n"
						+ "	long min = 0, max = 0;\n"
						+ "	if(constraint_getMaxValue(constraint, &max) && max <symbol_min> <value_min>)\n"
						+ "		if(constraint_getMinValue(constraint, &min) && min <symbol_max> <value_max>){\n"
						+ "			constraint_delete(constraint);\n"
						+ "			return true;\n" + "	}";

				if (constraint.getGEConstraint() != null) {
					ret = ret.replace("<value_max>", constraint
							.getGEConstraint().toString());
					ret = ret.replace("<symbol_max>", ">=");
				} else {
					ret = ret.replace("<value_max>", constraint
							.getGConstraint().toString());
					ret = ret.replace("<symbol_max>", ">");
				}

				if (constraint.getSEConstraint() != null) {
					ret = ret.replace("<value_min>", constraint
							.getSEConstraint().toString());
					ret = ret.replace("<symbol_min>", "<=");
				} else {
					ret = ret.replace("<value_min>", constraint
							.getSConstraint().toString());
					ret = ret.replace("<symbol_min>", "<");
				}
			}
			// >, >=
			else {
				ret = "	//==============G or GE Constraint=====================================\n"
						+ "	long min = 0;\n"
						+ "	if(constraint_getMinValue(constraint, &min) && min <symbol> <value>){\n"
						+ "		constraint_delete(constraint);\n"
						+ "		return true;\n" + "	}";
				if (constraint.getGEConstraint() != null) {
					ret = ret.replace("<value>", constraint.getGEConstraint()
							.toString());
					ret = ret.replace("<symbol>", ">=");
				} else {
					ret = ret.replace("<value>", constraint.getGConstraint()
							.toString());
					ret = ret.replace("<symbol>", ">");
				}
			}
		}
		// <, <=
		else if (constraint.getSConstraint() != null
				|| constraint.getSEConstraint() != null) {
			ret = "	//==============S or SE Constraint=====================================\n"
					+ "	long max = 0;\n"
					+ "	if(constraint_getMaxValue(constraint, &max) && max <symbol> <value>){\n"
					+ "		constraint_delete(constraint);\n"
					+ "		return true;\n"
					+ "	}";
			if (constraint.getSEConstraint() != null) {
				ret = ret.replace("<value>", constraint.getSEConstraint()
						.toString());
				ret = ret.replace("<symbol>", "<=");
			} else {
				ret = ret.replace("<value>", constraint.getSConstraint()
						.toString());
				ret = ret.replace("<symbol>", "<");
			}
		}

		return ret;
	}

	private String getFunctionTemplate() {
		return Utils.readFile(getFile(this.isValueLegal));
	}

	public static void main(String[] args) {
		ValueConstraint valueConstraint;
		ArrayList<ValueConstraint> maps = new ArrayList<ValueConstraint>();

		Map<String, VConstraint> map1 = new HashMap<String, VConstraint>();
		map1.put(ValueConstraint.NE, new VConstraint("NULL", "NULL"));
		ValueConstraint vc1 = new ValueConstraint(null, 0, false, null, map1);
		maps.add(vc1);

		Map<String, VConstraint> map2 = new HashMap<String, VConstraint>();
		map2.put(ValueConstraint.E, new VConstraint("int", "1"));
		ValueConstraint vc2 = new ValueConstraint(null, 0, false, null, map2);
		maps.add(vc2);

		Map<String, VConstraint> map3 = new HashMap<String, VConstraint>();
		map3.put(ValueConstraint.G, new VConstraint("int", "1"));
		ValueConstraint vc3 = new ValueConstraint(null, 0, false, null, map3);
		maps.add(vc3);

		Map<String, VConstraint> map4 = new HashMap<String, VConstraint>();
		map4.put(ValueConstraint.GE, new VConstraint("int", "1"));
		ValueConstraint vc4 = new ValueConstraint(null, 0, false, null, map4);
		maps.add(vc4);

		Map<String, VConstraint> map5 = new HashMap<String, VConstraint>();
		map5.put(ValueConstraint.S, new VConstraint("int", "1"));
		ValueConstraint vc5 = new ValueConstraint(null, 0, false, null, map5);
		maps.add(vc5);

		Map<String, VConstraint> map6 = new HashMap<String, VConstraint>();
		map6.put(ValueConstraint.SE, new VConstraint("int", "1"));
		ValueConstraint vc6 = new ValueConstraint(null, 0, false, null, map6);
		maps.add(vc6);

		Map<String, VConstraint> map7 = new HashMap<String, VConstraint>();
		map7.put(ValueConstraint.SE, new VConstraint("int", "5"));
		map7.put(ValueConstraint.GE, new VConstraint("int", "1"));
		ValueConstraint vc7 = new ValueConstraint(null, 0, false, null, map7);
		maps.add(vc7);

		Map<String, VConstraint> map8 = new HashMap<String, VConstraint>();
		map8.put(ValueConstraint.S, new VConstraint("int", "5"));
		map8.put(ValueConstraint.GE, new VConstraint("int", "1"));
		ValueConstraint vc8 = new ValueConstraint(null, 0, false, null, map8);
		maps.add(vc8);

		Map<String, VConstraint> map9 = new HashMap<String, VConstraint>();
		map9.put(ValueConstraint.SE, new VConstraint("int", "5"));
		map9.put(ValueConstraint.G, new VConstraint("int", "1"));
		ValueConstraint vc9 = new ValueConstraint(null, 0, false, null, map9);
		maps.add(vc9);

		Map<String, VConstraint> map10 = new HashMap<String, VConstraint>();
		map10.put(ValueConstraint.S, new VConstraint("int", "5"));
		map10.put(ValueConstraint.G, new VConstraint("int", "1"));
		ValueConstraint vc10 = new ValueConstraint(null, 0, false, null, map10);
		maps.add(vc10);

		for (int i = 0; i < maps.size(); i++) {
			System.out.println("map: " + (i + 1));
			IsValueLegal isValueLegal = new IsValueLegal(maps.get(i));
			System.out.println(isValueLegal.generateCode());
		}

	}
}
