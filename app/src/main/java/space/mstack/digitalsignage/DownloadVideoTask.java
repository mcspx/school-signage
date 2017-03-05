package space.mstack.digitalsignage;

import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Callback;

public class DownloadVideoTask extends AsyncTask<Void, Void, Boolean> {
    private List<String> signageA = new ArrayList<String>();
    private List<String> signageB = new ArrayList<String>();
    private List<String> signageU = new ArrayList<String>();
    private List<String> signageX = new ArrayList<String>();

    @Override
    protected Boolean doInBackground(Void... params) {
        // Get list of file on local
        File dirs = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/school-signage");

        if (dirs.exists()) {
            File[] files = dirs.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(".mp4");
                }
            });

            for (File file: files) {
                signageA.add(file.getName());
            }
        }

        // Get list of file on server
        SignageInterface getSignage = RetrofitConfig.getRetrofit().create(SignageInterface.class);

        retrofit2.Call<SignageResponse> callS = getSignage.signage();

        callS.enqueue(new Callback<SignageResponse>() {
            @Override
            public void onResponse(retrofit2.Call<SignageResponse> call, retrofit2.Response<SignageResponse> response) {
                SignageResponse serverResponse = response.body();

                String[] signage = serverResponse.getResultData();

                for (String item: signage) {
                    signageB.add(item);
                }

                signageX.addAll(signageA);
                signageX.removeAll(signageB);

                for (String item : signageX) {
                    File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/school-signage/" + item);

                    file.delete();
                }

                signageU.addAll(signageB);
                signageU.removeAll(signageA);

                for (final String item: signageU) {
                    new Thread(new Runnable() {
                        public void run() {
                            downloadVideo(item);
                        }
                    }).start();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<SignageResponse> call, Throwable t) {

            }
        });

        return  (signageU.size() > 0);
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
    }

    private void downloadVideo(String filename) {
        String from = RetrofitConfig.BASE_URL + "download/" + filename;
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/school-signage/" + filename;

        OkHttpClient client = new OkHttpClient();

        Call call = client.newCall(new Request.Builder().url(from).get().build());

        try {
            Response response = call.execute();

            if (response.code() == 200 || response.code() == 201) {
                InputStream inputStream = null;

                try {
                    inputStream = response.body().byteStream();

                    byte[] buff = new byte[1024 * 4];

                    OutputStream output = new FileOutputStream(path);

                    while (true) {
                        int readed = inputStream.read(buff);

                        if (readed == -1) {
                            break;
                        }

                        output.write(buff, 0, readed);
                    }

                    output.flush();
                    output.close();

                } catch (IOException ignore) {
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
