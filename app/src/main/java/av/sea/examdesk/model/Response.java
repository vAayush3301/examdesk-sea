package av.sea.examdesk.model;

public class Response {
    private Question question;
    private int responseCode;

    public Response(Question question, int responseCode) {
        this.question = question;
        this.responseCode = responseCode;
    }

    public Response() {
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }
}
