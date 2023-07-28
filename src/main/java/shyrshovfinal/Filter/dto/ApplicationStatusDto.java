package shyrshovfinal.Filter.dto;

public class ApplicationStatusDto {
    private int TotalCount;
    private int InWork;
    private int New;
    private int Agree;
    private int Disagree;
    private int Double;

    public int getTotalCount() {
        return TotalCount;
    }

    public void setTotalCount(int totalCount) {
        TotalCount = totalCount;
    }

    public int getInWork() {
        return InWork;
    }

    public void setInWork(int inWork) {
        InWork = inWork;
    }

    public int getNew() {
        return New;
    }

    public void setNew(int aNew) {
        New = aNew;
    }

    public int getAgree() {
        return Agree;
    }

    public void setAgree(int agree) {
        Agree = agree;
    }

    public int getDisagree() {
        return Disagree;
    }

    public void setDisagree(int disagree) {
        Disagree = disagree;
    }

    public int getDouble() {
        return Double;
    }

    public void setDouble(int aDouble) {
        Double = aDouble;
    }
}
