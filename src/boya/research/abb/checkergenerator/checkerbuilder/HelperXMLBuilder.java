package boya.research.abb.checkergenerator.checkerbuilder;

import boya.research.abb.checkergenerator.data.CheckerInfo;
import boya.research.abb.checkergenerator.utils.Utils;

public class HelperXMLBuilder {
    public static String getHelperXML(CheckerInfo cInfo){
        String str = Utils.readFile(GetFile.getFile("help.xml"));
        str = str.replaceAll("<CheckerName>", cInfo.getCheckerName());
        str = str.replaceAll("<CheckerMessage>", cInfo.getCheckerMessage());
        str = str.replaceAll("<CheckerTitle>", cInfo.getCheckerTitle());
        return str;
    }
}
