package boya.research.abb.checkergenerator.helperfunctions;

import boya.research.abb.checkergenerator.utils.Utils;

public class Traverse extends HelperFunction{

    static String traverse = "traverse.txt";
    @Override
    public String generateCode() {
        return Utils.readFile(getFile(traverse));
    }

    @Override
    public String generateDecl() {
        return "bool traverse(node_t,  memitem_t, bool(*isInteresting)(node_t, memitem_t), bool);";
    }

}
