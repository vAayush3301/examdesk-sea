package av.sea.examdesk.model;

import java.io.Serializable;
import java.util.List;

public class Test implements Serializable {
    private String testId;
    private String testName;
    private List<Question> questions;
    private int duration;
    private List<Image> imageKeys;

    public Test() {
    }

    public Test(String testId, String testName, List<Question> questions, List<Image> imageKeys, int duration) {
        this.testId = testId;
        this.testName = testName;
        this.questions = questions;
        this.imageKeys = imageKeys;
        this.duration = duration;
    }

    public List<Image> getImageKeys() {
        return imageKeys;
    }

    public void setImageKeys(List<Image> imageKeys) {
        this.imageKeys = imageKeys;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public String getTestId() {
        return testId;
    }

    public void setTestId(String testId) {
        this.testId = testId;
    }
}
