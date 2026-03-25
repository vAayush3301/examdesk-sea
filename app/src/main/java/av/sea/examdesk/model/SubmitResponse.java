package av.sea.examdesk.model;

import java.util.List;

public class SubmitResponse {
    public String testId;
    public String userId;
    public String date;
    public List<Response> responses;

    public SubmitResponse() {}

    public SubmitResponse(String testId, String userId, String date, List<Response> responses) {
        this.testId = testId;
        this.userId = userId;
        this.date = date;
        this.responses = responses;
    }
}
