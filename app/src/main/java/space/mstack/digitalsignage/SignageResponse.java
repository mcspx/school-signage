package space.mstack.digitalsignage;

import com.google.gson.annotations.SerializedName;

public class SignageResponse {
    @SerializedName("resultCode")
    private String resultCode;
    @SerializedName("resultData")
    private String[] resultData;

    public String getResultCode() {
        return resultCode;
    }

    public String[] getResultData() {
        return resultData;
    }
}
