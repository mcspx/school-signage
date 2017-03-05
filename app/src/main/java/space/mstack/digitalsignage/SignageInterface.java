package space.mstack.digitalsignage;

import retrofit2.Call;
import retrofit2.http.POST;

public interface SignageInterface {
    @POST("signage")
    Call<SignageResponse> signage();
}
