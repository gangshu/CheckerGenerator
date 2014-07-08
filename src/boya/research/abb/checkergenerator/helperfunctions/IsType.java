package boya.research.abb.checkergenerator.helperfunctions;

import boya.research.abb.checkergenerator.utils.Utils;

public class IsType extends HelperFunction{

    static String isType = "isType.txt";
    @Override
    public String generateCode() {
        return Utils.readFile(getFile(isType));
    }

    @Override
    public String generateDecl() {
        return "bool isType(expr_t, char*);";
    }

}
