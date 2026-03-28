package av.sea.examdesk.helpers;

import java.util.List;

import av.sea.examdesk.model.SubmitResponse;
import av.sea.examdesk.model.Test;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface ApiService {
    @GET(Statics.GET_TEST_ENDPOINT)
    Call<List<Test>> getTests(@Query("clientId") String clientId);

    @Multipart
    @POST(Statics.UPLOAD_IMAGE_ENDPOINT)
    Call<ResponseBody> uploadImage(@Part MultipartBody.Part file);

    @DELETE(Statics.DELETE_IMAGE_ENDPOINT)
    Call<ResponseBody> deleteImage(@Query("key") String key);

    @POST(Statics.CREATE_TEST_ENDPOINT)
    Call<ResponseBody> createTest(@Query("clientId") String clientId, @Body Test test);

    @POST(Statics.DELETE_TEST_ENDPOINT)
    Call<ResponseBody> deleteTest(@Query("clientId") String clientId, @Body Test test);

    @GET(Statics.GET_TEST_RESULT_ENDPOINT)
    Call<List<SubmitResponse>> getTestResult(@Query("testId") String testId, @Query("clientId") String clientId);
}
