package boya.research.abb.checkergenerator.web;

public class Checker {
	String checkerCode;
	String testcaseCode;
	String message;
	String checkerXML;
	String helpXML;
	
	public Checker(String checkerCode, String testcaseCode, String message, String checkerXML, String helpXML){
		this.checkerCode = checkerCode;
		this.testcaseCode = testcaseCode;
		this.message = message;
		this.checkerXML = checkerXML;
		this.helpXML = helpXML;
	}
	
	public String getCheckerCode() {
		return checkerCode;
	}
	public void setCheckerCode(String checkerCode) {
		this.checkerCode = checkerCode;
	}
	public String getTestcaseCode() {
		return testcaseCode;
	}
	public void setTestcaseCode(String testcaseCode) {
		this.testcaseCode = testcaseCode;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getCheckerXML() {
		return checkerXML;
	}
	public void setCheckerXML(String checkerXML) {
		this.checkerXML = checkerXML;
	}
	public String getHelpXML() {
		return helpXML;
	}
	public void setHelpXML(String helpXML) {
		this.helpXML = helpXML;
	}
}
