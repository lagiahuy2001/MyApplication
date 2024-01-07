package com.example.myapplication;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;

import com.android.volley.toolbox.JsonObjectRequest;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import java.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.googlecode.tesseract.android.TessBaseAPI;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.json.JSONException;

public class MainActivity extends AppCompatActivity {
    final String URL_BACK_END = "https://29fb-14-177-70-127.ngrok-free.app";
    final String URL_CLOUD = "https://nasty-roses-sort.loca.lt";
    private TessBaseAPI tessBaseAPI;
    private TextView imageResult;
    private TextView nqueenResult;
    private ImageView imageView;
    private Button btnImgae1;
    private Button btnImgae2;
    private Button btnImgae3;
    private Button btnnqueenp1;
    private Button btnnqueenp2;
    private Button btnnqueenp3;
    private EditText numbern;
    private long startTime;
    private long endTime;
    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageResult = findViewById(R.id.imageResult);
        nqueenResult = findViewById(R.id.nqueenresult);
        imageView = findViewById(R.id.clickToUploadImg);
        btnImgae1 = findViewById(R.id.btnImgae1);
        btnImgae2 = findViewById(R.id.btnImgae2);
        btnImgae3 = findViewById(R.id.btnImgae3);
        btnnqueenp1 = findViewById(R.id.btnnqueenp1);
        btnnqueenp2 = findViewById(R.id.btnnqueenp2);
        btnnqueenp3 = findViewById(R.id.btnnqueenp3);
        numbern = findViewById(R.id.numbern);

        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    Uri uri = data.getData();
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        imageView.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intent);
            }
        });

        btnImgae1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageResult.setText("");
                startTime = System.currentTimeMillis();

                String datapath = getFilesDir() + "/tesseract/";
                String tessdataPath = datapath + "tessdata/";
                File tessdataDir = new File(tessdataPath);
                if (!tessdataDir.exists() && !tessdataDir.mkdirs()) {
                    throw new RuntimeException("Error creating directory");
                }

                copyTessData(datapath);

                tessBaseAPI = new TessBaseAPI();
                tessBaseAPI.init(datapath, "eng");
                recognizeText(bitmap);
                String urlResult = URL_BACK_END + "/api/v1/processimage-p2";
                sendResult(urlResult, 1);
            }
        });


        btnImgae2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageResult.setText("");
                startTime = System.currentTimeMillis();

                String url = URL_CLOUD + "/upload";
                uploadImage(url);
            }
        });

        btnImgae3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageResult.setText("");
                startTime = System.currentTimeMillis();

                String url = URL_BACK_END + "/api/v1/processimage-p1";
                uploadImage(url);
            }
        });


        btnnqueenp1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nqueenResult.setText("");
                startTime = System.currentTimeMillis();

                int n = Integer.parseInt(String.valueOf(numbern.getText()));

                NQueenSolver nQueenSolver = new NQueenSolver(n);
                nQueenSolver.solveNQueens();
                String result = nQueenSolver.getResult();
                int count = nQueenSolver.getQueenCount();
                nqueenResult.setText(result + "\n Số lượng quân hậu: " + count);
                String urlResult = URL_BACK_END + "/api/v1/nqueen-p2";
                sendResult(urlResult, 2);
            }
        });

        btnnqueenp2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nqueenResult.setText("");
                startTime = System.currentTimeMillis();

                String url = URL_CLOUD + "/n-queen";
                int n = Integer.parseInt(String.valueOf(numbern.getText()));
                sendN(url, n);
            }
        });
        btnnqueenp3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nqueenResult.setText("");
                startTime = System.currentTimeMillis();

                String url = URL_BACK_END + "/api/v1/nqueen-p1";
                int n = Integer.parseInt(String.valueOf(numbern.getText()));
                sendN(url, n);
            }
        });
    }

    private void copyTessData(String datapath) {
        try {
            String filepath = datapath + "tessdata/eng.traineddata";
            InputStream inputStream = getAssets().open("tessdata/eng.traineddata");
            OutputStream outputStream = new FileOutputStream(filepath);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void recognizeText(Bitmap bitmap) {
        tessBaseAPI.setImage(bitmap);
        String recognizedText = tessBaseAPI.getUTF8Text();
        imageResult.setText(recognizedText);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tessBaseAPI.end();
    }

    private void uploadImage(String url) {

        ByteArrayOutputStream byteArrayOutputStream;
        byteArrayOutputStream = new ByteArrayOutputStream();
        if (bitmap != null) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] bytes = byteArrayOutputStream.toByteArray();
            String base64Image = "";
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                Base64.Encoder encoder = Base64.getEncoder();
                base64Image = encoder.encodeToString(bytes);
            }
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

            final String finalBase64Image = base64Image;
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                String result = response.getString("result");
                                imageResult.setText(result);
                                endTime = System.currentTimeMillis();
                                long elapsedTime = endTime - startTime;
                                Toast.makeText(getApplicationContext(), "Thời gian: " + elapsedTime + "ms", Toast.LENGTH_LONG).show();

                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getApplicationContext(), error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        }
                    }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() {
                    try {
                        JSONObject jsonBody = new JSONObject();
                        jsonBody.put("image", finalBase64Image);

                        return jsonBody.toString().getBytes();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return null;
                    }
                }

            };

            queue.add(jsonObjectRequest);
        } else {
            Toast.makeText(getApplicationContext(), "Pls Select Image", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendN(String url, int numberN) {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String result = response.getString("result");
                            int count = response.getInt("count");
                            nqueenResult.setText(result + "\n Số lượng quân hậu: " + count);
                            endTime = System.currentTimeMillis();
                            long elapsedTime = endTime - startTime;
                            Toast.makeText(getApplicationContext(), "Thời gian: " + elapsedTime + "ms", Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            public String getBodyContentType() {
                return "application/json";
            }

            @Override
            public byte[] getBody() {
                try {
                    JSONObject jsonBody = new JSONObject();
                    jsonBody.put("nInput", numberN);

                    return jsonBody.toString().getBytes("utf-8");
                } catch (JSONException | UnsupportedEncodingException e) {
                    e.printStackTrace();
                    return null;
                }
            }

        };

        queue.add(jsonObjectRequest);
    }

    private void sendResult(String url, int type) {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        endTime = System.currentTimeMillis();
                        long elapsedTime = endTime - startTime;
                        Toast.makeText(getApplicationContext(), "Thời gian: " + elapsedTime + "ms", Toast.LENGTH_LONG).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            public String getBodyContentType() {
                return "application/json";
            }

            @Override
            public byte[] getBody() {
                try {
                    String result = "";
                    if(type == 1){
                         result = (String) imageResult.getText();
                    }else {
                         result = (String) nqueenResult.getText();
                    }

                    JSONObject jsonBody = new JSONObject();
                    jsonBody.put("result", result);

                    return jsonBody.toString().getBytes("utf-8");
                } catch (JSONException | UnsupportedEncodingException e) {
                    e.printStackTrace();
                    return null;
                }
            }

        };

        queue.add(jsonObjectRequest);
    }


}