package boya.research.abb.checkergenerator.helperfunctions;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

public abstract class HelperFunction {
    public abstract String generateCode();
    public abstract String generateDecl();
    File getFile(String fileName){
        File file = null;
        try {
            file = new File(new URI(HelperFunction.class.getResource("").toString()+fileName));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return file;
    }
}
