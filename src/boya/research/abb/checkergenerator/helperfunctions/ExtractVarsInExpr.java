package boya.research.abb.checkergenerator.helperfunctions;

import boya.research.abb.checkergenerator.utils.Utils;

public class ExtractVarsInExpr extends HelperFunction{

    public static String extractVarsInExpr = "extractVarsInExpr.txt";
    @Override
    public String generateCode() {
        return Utils.readFile(getFile(this.extractVarsInExpr));
    }

    @Override
    public String generateDecl() {
        return "void extractVarsInExpr(expr_t, TriggerResult*, node_t, function_t);";
    }

}
