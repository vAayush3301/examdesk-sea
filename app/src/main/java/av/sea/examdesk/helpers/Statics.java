package av.sea.examdesk.helpers;

public class Statics {
    public static final String CLIENT_ID = "GGPSD";
    public static final String BASE_URL = "https://ggps-entrance-xpiz.onrender.com";
    public static final String GET_TEST_ENDPOINT = "/api/test/get_tests";
    public static final String UPLOAD_IMAGE_ENDPOINT = "/api/image/upload";
    public static final String DELETE_IMAGE_ENDPOINT = "/api/image";
    public static final String CREATE_TEST_ENDPOINT = "/api/test/create";
    public static final String DELETE_TEST_ENDPOINT = "/api/test/deleteTest";
    public static final String GET_TEST_RESULT_ENDPOINT = "/api/test/get_results";

    public static String Test_Info = """
            Exam Pattern:
            • %d Questions (MCQs)
            • +4 for correct, -1 for wrong
            • No reattempt after submission
                            
            Duration:
            • %d minutes
            • Timer will auto-submit when time ends
                            
            Rules:
            • Switching tabs or losing focus 3 times will auto-submit
            • Minimizing the window counts as focus loss
            • Do not refresh or close the application
            • Use Do not disturb mode to avoid interruptions
             
            YOUR DEVICE MUST HAVE CONSTANT INTERNET CONNECTIVITY THROUGHOUT THE TEST
                            
            Click Continue to begin.
            """;
}
