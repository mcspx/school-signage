package space.mstack.digitalsignage;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitConfig {
    public static String BASE_URL = "http://192.168.10.200:8008/";

    public static Retrofit getRetrofit() {
        return new Retrofit.Builder()
                .baseUrl(RetrofitConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}
