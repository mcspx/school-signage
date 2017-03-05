package space.mstack.digitalsignage;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface UploadInterface {
    @Multipart
    @POST("uploads")
    Call<UploadResponse> uploads(
            @Part("student_card") RequestBody student_card,
            @Part MultipartBody.Part student_file
    );

    /*
    @Multipart
    @POST("uploads")
    Call<UploadResponse> uploadMultipleFile(
            @Part("student_card") RequestBody student_card,
            @Part MultipartBody.Part student_file1,
            @Part MultipartBody.Part student_file2
    );
    */
}
