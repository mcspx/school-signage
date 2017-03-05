package space.mstack.digitalsignage;

import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SaveImageTask extends AsyncTask<byte[], Void, Void> {
    private String fileName;
    private String cardName;

    public SaveImageTask(String filename, String cardname) {
        fileName = filename;
        cardName = cardname;
    }

    @Override
    protected Void doInBackground(byte[]... data) {
        try {
            // Save file
            FileOutputStream fos = new FileOutputStream(Environment.getExternalStorageDirectory().getAbsolutePath() + "/school-signage/" + fileName, false);

            fos.write(data[0]);
            fos.flush();
            fos.close();

            // Upload file to server
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/school-signage/" + fileName);

            RequestBody student_card = createPartFromString(cardName);
            MultipartBody.Part student_file = createPartFromFile("student_file", file);

            UploadInterface getUpload = RetrofitConfig.getRetrofit().create(UploadInterface.class);

            Call<UploadResponse> call = getUpload.uploads(student_card, student_file);

            call.enqueue(new Callback<UploadResponse>() {
                @Override
                public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {

                }

                @Override
                public void onFailure(Call<UploadResponse> call, Throwable t) {

                }
            });

            return null;
        } catch (IOException e) {
            return null;
        }
    }

    @NonNull
    private RequestBody createPartFromString(String s) {
        return RequestBody.create(MultipartBody.FORM, s);
    }

    @NonNull
    private MultipartBody.Part createPartFromFile(String name, File file) {
        RequestBody requestFile = RequestBody.create(MediaType.parse("*"), file);

        return MultipartBody.Part.createFormData(name, file.getName(), requestFile);
    }
}
