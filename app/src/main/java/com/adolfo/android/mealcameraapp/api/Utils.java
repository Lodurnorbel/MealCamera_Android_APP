package com.adolfo.android.mealcameraapp.api;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.adolfo.android.mealcameraapp.R;
import com.adolfo.android.mealcameraapp.api.responses.ResponseJSON;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Utils {

    public void sendImage(Bitmap bitmap, ImageView imageView2, TextView priceText){
        File imageFile = null;

        try {
            imageFile = new File(Environment.getExternalStorageDirectory() + File.separator + "image.jpg");
            imageFile.createNewFile();

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            byte[] bitmapdata = bos.toByteArray();

            FileOutputStream fos = new FileOutputStream(imageFile);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileUploadService service = ServiceGenerator.createService(FileUploadService.class);

        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageFile);
        MultipartBody.Part body = MultipartBody.Part.createFormData("img", imageFile.getName(), requestFile);

        Call<ResponseJSON> call = service.upload(body);
        call.enqueue(new Callback<ResponseJSON>() {
            Bitmap result = null;
            @Override
            public void onResponse(Call<ResponseJSON> call, Response<ResponseJSON> response) {
                Log.v("Upload", "success");

                byte[] decodedString = Base64.decode(response.body().img.replace("b'", "").replace("'", ""), Base64.DEFAULT);
                result = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                imageView2.setImageBitmap(result);

                StringBuffer text = new StringBuffer();
                text.append(response.body().price + " €").append('\n');
                for(String t : response.body().mealList){
                    text.append(t).append('\n');
                }
                priceText.setText(text.toString());
            }
            @Override
            public void onFailure(Call<ResponseJSON> call, Throwable t) {
                Log.e("Upload error:", t.getMessage());
            }
        });
    }
}
