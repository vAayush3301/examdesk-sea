package av.sea.examdesk.model;

public class User {
    String userId, token;

    public User(String userId, String token) {
        this.userId = userId;
        this.token = token;
    }

    public User() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
