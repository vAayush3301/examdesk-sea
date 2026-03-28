package av.sea.examdesk.model;

public class UserResult {
    private String userId;
    public int marksObtained, numCorrect;
    private int numAttempted;

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setNumAttempted(int numAttempted) {
        this.numAttempted = numAttempted;
    }

    public String getUserId() {
        return userId;
    }

    public int getMarksObtained() {
        return marksObtained;
    }

    public int getNumCorrect() {
        return numCorrect;
    }

    public int getNumAttempted() {
        return numAttempted;
    }
}
