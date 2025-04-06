package com.example.myapplication;

import static com.example.myapplication.EncryptionUtil.decryptFile;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.*;
import okio.BufferedSink;
import retrofit2.*;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.Executors;

import javax.crypto.SecretKey;

public class MainActivity extends AppCompatActivity {

    private ApiService apiService;
    private FileAdapter adapter;
    private List<String> fileList = new ArrayList<>();
    private TextView textViewServerStatus;

    private ActivityResultLauncher<Intent> filePickerLauncher;

    private SecretKey key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://test-a5m4.onrender.com/") // 10.0.2.2 localhost emulateur
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewFiles);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FileAdapter(fileList, new FileAdapter.OnItemActionListener() {
            @Override
            public void onDownload(String filename) {
                downloadFile(filename);
            }

            @Override
            public void onDelete(String filename) {
                deleteFileFromServer(filename);
            }
        });
        recyclerView.setAdapter(adapter);

        Button buttonUpload = findViewById(R.id.buttonUpload);
        buttonUpload.setOnClickListener(v -> openFilePicker());


        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri fileUri = result.getData().getData();
                        uploadFile(fileUri);
                    }
                }
        );

        fetchFiles();
        Button buttonRefresh = findViewById(R.id.buttonRefresh);
        buttonRefresh.setOnClickListener(v -> fetchFiles());

        textViewServerStatus = findViewById(R.id.textViewServerStatus);


//        try {
//            key = EncryptionUtil.generateKey();
//        } catch (NoSuchAlgorithmException e) {
//            throw new RuntimeException(e);
//        }

        key = EncryptionUtil.getHardCodedKey();

    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        filePickerLauncher.launch(intent);
    }


    private void uploadFile(Uri uri) {
        try {
            String fileName = getFileName(uri);
            InputStream inputStream = getContentResolver().openInputStream(uri);
            byte[] fileBytes = new byte[inputStream.available()];
            inputStream.read(fileBytes);

            byte[] encryptedFile = EncryptionUtil.encryptBytes(fileBytes, key);

            RequestBody requestFile = RequestBody.create(encryptedFile, MediaType.parse("application/octet-stream"));
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", fileName, requestFile);

            apiService.uploadFile(body).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Toast.makeText(MainActivity.this, "Fichier uploadé", Toast.LENGTH_SHORT).show();
                    fetchFiles();
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(MainActivity.this, "Erreur d’upload", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void fetchFiles() {
        apiService.listFiles().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String body = response.body().string();
                        fileList.clear();

                        for (String name : body.split("\n")) {
                            if (!name.trim().isEmpty()) {
                                fileList.add(name.trim());
                            }
                        }
                        adapter.notifyDataSetChanged();
                        textViewServerStatus.setText("Serveur en ligne");
                        textViewServerStatus.setTextColor(Color.parseColor("#ff669900"));
                    } catch (IOException e) {
                        e.printStackTrace();
                        textViewServerStatus.setText("Erreur lors du traitement des fichiers");
                        textViewServerStatus.setTextColor(Color.parseColor("#ffcc0000"));
                    }
                } else {
                    textViewServerStatus.setText("Serveur inaccessible");
                    textViewServerStatus.setTextColor(Color.parseColor("#ffcc0000"));
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                textViewServerStatus.setText("Serveur hors-ligne");
                textViewServerStatus.setTextColor(Color.parseColor("#ffcc0000"));
                Toast.makeText(MainActivity.this, "Erreur de récupération", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void downloadFile(String filename) {
        apiService.downloadFile(filename).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        try {
                            byte[] encryptedData = response.body().bytes();

                            File outputFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), filename);

                            decryptFile(encryptedData, key, outputFile);

                            runOnUiThread(() ->
                                    Toast.makeText(MainActivity.this, "téléchargé : " + filename, Toast.LENGTH_SHORT).show()
                            );
                        } catch (Exception e) {
                            e.printStackTrace();
                            runOnUiThread(() ->
                                    Toast.makeText(MainActivity.this, "erreur écriture", Toast.LENGTH_SHORT).show()
                            );
                        }
                    });
                } else {
                    Toast.makeText(MainActivity.this, "réponse invalide : " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(MainActivity.this, "erreur téléchargement", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteFileFromServer(String filename) {
        apiService.deleteFile(filename).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Toast.makeText(MainActivity.this, "eupprime : " + filename, Toast.LENGTH_SHORT).show();
                fetchFiles();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(MainActivity.this, "erreur suppression", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("Range")
    private String getFileName(Uri uri) {
        String result = null;
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            cursor.close();
        }
        return result != null ? result : "fichier inconnu";
    }
}
