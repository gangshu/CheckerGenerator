package boya.research.abb.checkergenerator.data;

public class CheckerInfo {
    String checkerName, sourceTriggerMessage, sinkTriggerMessage, checkerMessage, checkerTitle;
    
    public CheckerInfo(String checkerName, String sourceTriggerMessage, String sinkTriggerMessage, String checkerMessage, String checkerTitle){
        this.checkerName = checkerName;
        this.sourceTriggerMessage = sourceTriggerMessage;
        this.sinkTriggerMessage = sinkTriggerMessage;
        this.checkerMessage = checkerMessage;
        this.checkerTitle = checkerTitle;
    }
    
    public String getCheckerMessage() {
        return checkerMessage;
    }

    public void setCheckerName(String str){
        this.checkerName = str;
    }

    public String getCheckerName() {
        return checkerName;
    }

    public String getCheckerTitle() {
        return checkerTitle;
    }

    public String getSourceTriggerMessage() {
        return sourceTriggerMessage;
    }

    public String getSinkTriggerMessage() {
        return sinkTriggerMessage;
    }
}
