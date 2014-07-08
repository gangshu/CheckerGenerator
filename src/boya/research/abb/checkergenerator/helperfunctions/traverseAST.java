package boya.research.abb.checkergenerator.helperfunctions;

import boya.research.abb.checkergenerator.utils.Utils;

public class traverseAST extends HelperFunction{
    @Override
    public String generateCode() {
        return Utils.readFile(getFile("traverseAST.txt"));
    }

    @Override
    public String generateDecl() {
        return "bool traverseAST(expr_t, memitem_t);\n";
    }
}
