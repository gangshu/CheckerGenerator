package boya.research.abb.checkergenerator.web;

import java.util.ArrayList;
import java.util.List;

import boya.research.abb.checkergenerator.RuleSpecificationParser;
import boya.research.abb.checkergenerator.RuleSpecificationParserException;
import boya.research.abb.checkergenerator.checkerbuilder.CallConstraintCheckerBuilder;
import boya.research.abb.checkergenerator.checkerbuilder.CheckerBuilder;
import boya.research.abb.checkergenerator.checkerbuilder.CheckerXMLBuilder;
import boya.research.abb.checkergenerator.checkerbuilder.ConditionalCheckConstraintCheckerBuilder;
import boya.research.abb.checkergenerator.checkerbuilder.HelperXMLBuilder;
import boya.research.abb.checkergenerator.checkerbuilder.ValueConstraintCheckerBuilder;
import boya.research.abb.checkergenerator.constraints.CallConstraint;
import boya.research.abb.checkergenerator.constraints.ConditionalCheckConstraint;
import boya.research.abb.checkergenerator.constraints.ValueConstraint;
import boya.research.abb.checkergenerator.data.CheckerInfo;
import boya.research.abb.checkergenerator.testcasebuilder.CallConstraintTestcaseBuilder;
import boya.research.abb.checkergenerator.testcasebuilder.ConditionalCheckTestcaseBuilder;
import boya.research.abb.checkergenerator.testcasebuilder.TestCaseBuilder;
import boya.research.abb.checkergenerator.testcasebuilder.ValueConstraintTestcaseBuilder;
import boya.research.abb.checkergenerator.utils.ExecWrapper;
import boya.research.abb.checkergenerator.utils.Utils;

public class GetCheckerFromXML {
	private static String exception;

	public static List<Checker> getCheckerFromXMLString(String XML) {
		RuleSpecificationParser parser = new RuleSpecificationParser();
		parser.setXMLString(XML);
		try {
			parser.parse();
		} catch (Exception e) {
			exception = e.toString();
			return null;
		}
		return getCheckers(parser);
	}

	public static String getException() {
		return exception;
	}

	public static List<Checker> getCheckerFromXMLFile(String pathToXML) {
		RuleSpecificationParser parser = new RuleSpecificationParser();
		parser.setPathToXML(pathToXML);
		try {
			parser.parse();
		} catch (Exception e) {
			exception = e.toString();
			return null;
		}
		return getCheckers(parser);
	}

	private static List<Checker> getCheckers(RuleSpecificationParser parser) {
		List<Checker> ret = new ArrayList<Checker>();
		/**
		 * Value Checkers
		 */
		List<CheckerInfo> valInfoList = parser.getValCheckerInfo();
		List<ValueConstraint> valList = parser.getValList();
		if (valList != null) {
			for (int i = 0; i < valInfoList.size(); i++) {
				CheckerInfo curCheckerInfo = valInfoList.get(i);
				ValueConstraint valConstraint = valList.get(i);
				CheckerBuilder builder = new ValueConstraintCheckerBuilder(
						true /* Value checkers are interprocedural */,
						curCheckerInfo, valConstraint);
				TestCaseBuilder tcbuilder = new ValueConstraintTestcaseBuilder(
						valConstraint);
				ret.add(getChecker(curCheckerInfo, builder, tcbuilder));
			}
		}
		/**
		 * Conditional Checkers
		 */
		List<CheckerInfo> conInfoList = parser.getConCheckerInfo();
		List<ConditionalCheckConstraint> conList = parser.getConList();
		if (conList != null) {
			for (int i = 0; i < conInfoList.size(); i++) {
				CheckerInfo curCheckerInfo = conInfoList.get(i);
				ConditionalCheckConstraint conConstraint = conList.get(i);

				CheckerBuilder builder = new ConditionalCheckConstraintCheckerBuilder(
						false, curCheckerInfo, conConstraint);
				TestCaseBuilder tcbuilder = new ConditionalCheckTestcaseBuilder(
						conConstraint);
				ret.add(getChecker(curCheckerInfo, builder, tcbuilder));
			}
		}

		/**
		 * Call Checkers
		 */
		List<CheckerInfo> calInfoList = parser.getCalCheckerInfo();
		List<CallConstraint> calList = parser.getCalList();
		if (calList != null) {
			for (int i = 0; i < calInfoList.size(); i++) {
				CheckerInfo curCheckerInfo = calInfoList.get(i);
				CallConstraint calConstraint = calList.get(i);
				CheckerBuilder builder = new CallConstraintCheckerBuilder(
						false, curCheckerInfo, calConstraint);
				TestCaseBuilder tcbuilder = new CallConstraintTestcaseBuilder(
						calConstraint);
				ret.add(getChecker(curCheckerInfo, builder, tcbuilder));
			}
		}
		return ret;
	}

	private static Checker getChecker(CheckerInfo checkerInfo,
			CheckerBuilder builder, TestCaseBuilder tcbuilder) {
		String checkerCode = builder.generateChecker();
		String testcaseCode = tcbuilder.getTestCase();
		String checkerXML = CheckerXMLBuilder.getCheckerXML(checkerInfo);
		String helpXML = HelperXMLBuilder.getHelperXML(checkerInfo);
		String message = checkerInfo.getCheckerTitle();
		return new Checker(checkerCode, testcaseCode, message, checkerXML,
				helpXML);
	}
}
