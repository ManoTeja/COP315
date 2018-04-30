package com.example.lenovo.project;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.microsoft.projectoxford.vision.VisionServiceClient;
import com.microsoft.projectoxford.vision.VisionServiceRestClient;
import com.microsoft.projectoxford.vision.contract.AnalysisResult;
import com.microsoft.projectoxford.vision.contract.Caption;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class ResultActivity extends AppCompatActivity {
    // free subscription key used here for working of the below function(api)
    public VisionServiceClient visionServiceClient = new VisionServiceRestClient("e70be55101ca4c36ad02fee6cf5d5af3","https://westcentralus.api.cognitive.microsoft.com/vision/v1.0");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        //getting photo info from image_recognition
        Bitmap photo  = getIntent().getExtras().getParcelable("name");
        ImageView imageView = (ImageView)this.findViewById(R.id.imageView1);
        imageView.setImageBitmap(photo);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        //code for getting data from api after sending info regarding photo
        final AsyncTask<InputStream,String,String> visionTask = new AsyncTask<InputStream, String, String>() {
            ProgressDialog mdialog = new ProgressDialog(ResultActivity.this);
            @Override
            protected String doInBackground(InputStream... params) {
                try {
                    publishProgress("recognizing...");
                    String[] features = {"Description"};
                    String[] details = {};
                    //getting result from api and then converting it into required format
                    AnalysisResult result = visionServiceClient.analyzeImage(inputStream,features,details);
                    String strResult = new Gson().toJson(result);
                    return  strResult;
                } catch (Exception e) {
                    return e.getMessage();
                }

            }
            @Override
            protected void onPreExecute(){
                mdialog.show();
            }

            @Override
            protected void onPostExecute(String s){
                mdialog.dismiss();
                // setting the result we get from api to textview
                TextView textView = (TextView) findViewById(R.id.responseView);
                StringBuilder stringBuilder = new StringBuilder();
                try {
                    AnalysisResult result = new Gson().fromJson(s, AnalysisResult.class);
                    for (Caption caption: result.description.captions) {
                        stringBuilder.append(caption.text);
                    }
                } catch (Exception e) {
                    stringBuilder.append(e.getCause());
                }
                textView.setText(stringBuilder);
            }

            @Override
            protected void onProgressUpdate(String...values){
                mdialog.setMessage(values[0]);
            }
        };
        visionTask.execute(inputStream);

    }
}
