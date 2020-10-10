package com.sqs.carddetect;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class HomeActivity extends Activity {
    RecyclerView populatelist̥;
    HomeCardAdapter homeCardAdapter;
    ImageView camera_icon, galary_icon;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_view);
        galary_icon = findViewById(R.id.galary_icon);
        camera_icon = findViewById(R.id.camera_icon);
        populatelist̥ = findViewById(R.id.populatelist̥);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        populatelist̥.setLayoutManager(mLayoutManager);
        homeCardAdapter = new HomeCardAdapter();
        populatelist̥.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        populatelist̥.setAdapter(homeCardAdapter);
        readAllFile().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(stringBitmapHashMap -> {
            if (stringBitmapHashMap != null && stringBitmapHashMap.size() > 0) {
                homeCardAdapter.UpdateList(stringBitmapHashMap);
            }
        });
        camera_icon.setOnClickListener(view -> {
            startActivity(new Intent(HomeActivity.this, RectDetection.class));
        });
    }

    Observable<List<Card_Data>> readAllFile() {
        return Observable.create(emitter -> {
            String path = Environment.getExternalStorageDirectory().toString() + "/frames";
            Log.d("Files", "Path: " + path);
            File directory = new File(path);
            File[] files = directory.listFiles();
            if (files != null) {
                Log.d("Files", "Size: " + files.length);
                List<Card_Data> allFies = new ArrayList<>();
                for (int i = 0; i < files.length; i++) {
                    String ret = "";
                    FileInputStream inputStream = null;
                    try {
                        Log.d("Files", "FileName:" + files[i].getName());
                        inputStream = new FileInputStream(new File(files[i].getAbsolutePath()));
                        if (inputStream != null) {

                            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                            String receiveString = "";
                            StringBuilder stringBuilder = new StringBuilder();

                            while ((receiveString = bufferedReader.readLine()) != null) {
                                stringBuilder.append("\n").append(receiveString);
                            }


                            Mat mat = StaticCall.matFromJson(stringBuilder.toString());
                            Bitmap bmp = null;
                            bmp = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
                            Utils.matToBitmap(mat, bmp);
                            Card_Data card_data = new Card_Data();
                            card_data.setImage(bmp);
                            card_data.setName(files[i].getName());
                            allFies.add(card_data);
                        }
                    } catch (Exception E) {
                        E.printStackTrace();
                    } finally {
                        if (inputStream != null)
                            inputStream.close();
                    }


                }
                emitter.onNext(allFies);
                emitter.onComplete();
            }

        });

    }
    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Toast.makeText(getApplicationContext(), "There is a problem", Toast.LENGTH_SHORT).show();
        } else {
            baseLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS);
        }
    }
    BaseLoaderCallback  baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    Log.v("aashari-log", "Loader interface success");
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

}
