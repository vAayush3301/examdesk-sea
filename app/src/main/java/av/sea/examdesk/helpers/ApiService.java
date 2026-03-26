package av.sea.examdesk.helpers;

import java.util.List;

import av.sea.examdesk.model.Test;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    @GET(Statics.GET_TEST_ENDPOINT)
    Call<List<Test>> getTests(@Query("clientId") String clientId);
}
