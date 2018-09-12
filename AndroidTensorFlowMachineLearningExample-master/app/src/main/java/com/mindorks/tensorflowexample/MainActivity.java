/*
 *    Copyright (C) 2017 MINDORKS NEXTGEN PRIVATE LIMITED
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.mindorks.tensorflowexample;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.method.ScrollingMovementMethod;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class MainActivity extends AppCompatActivity {

    private static final int INPUT_SIZE = 224;
    private static final int IMAGE_MEAN = 117;
    private static final float IMAGE_STD = 1;
    private static final String INPUT_NAME = "input";
    private static final String OUTPUT_NAME = "output";

    private static final String MODEL_FILE = "file:///android_asset/tensorflow_inception_graph.pb";
    private static final String LABEL_FILE = "file:///android_asset/imagenet_comp_graph_label_strings.txt";


    private Classifier classifier;
    private Executor executor = Executors.newSingleThreadExecutor();
    private TextView textViewResult;
    private Button btnDetectObject, btnToggleCamera;
    private ImageView imageViewResult;
    private CameraView cameraView;
    private ListView menuList;
    private String imgResult;

    private ArrayList<HashMap<String, String>> Data = new ArrayList<HashMap<String, String>>();
    private HashMap<String, String> InputData1 = new HashMap<>();
    private HashMap<String, String> InputData2 = new HashMap<>();
    private HashMap<String, String> InputData3 = new HashMap<>();

    /*
            기계 번역 object
     */
    private String translationText;
    private Button translationButton;
    private TextView resultText;
    private String result;


    class BackgroundTask extends AsyncTask<Integer, Integer, Integer>{
        protected void onPreExcute(){
        }

        @Override
        protected Integer doInBackground(Integer... arg0){

            StringBuilder output = new StringBuilder();

            String clientId =  "_qEab38IKQivHfGEUJHU";
            String clientSecret = "eMF_ooo4hi";
            try {

                /*
                        구글 이미지 검색 결과
                 */
                String rs1 = imgResult.split("\\[")[1];
                String rs2 = rs1.split("\\]")[1];
                String rs = rs2.split("\\(")[0];
                translationText = rs.trim();

                String text = URLEncoder.encode(translationText, "UTF-8");
                String apiURL = "https://openapi.naver.com/v1/papago/n2mt";

                // 파파고 API와 연결을 수행
                URL url = new URL(apiURL);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("X-Naver-Client-Id", clientId);
                con.setRequestProperty("X-Naver-Client-Secret", clientSecret);

                // 번역할 문장을 파라미터로 넘김
                String postParams = "source=en&target=ko&text=" + text;
                con.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());

                wr.writeBytes(postParams);
                wr.flush();
                wr.close();

                // 번역 결과 받아오기
                int responseCode = con.getResponseCode();
                BufferedReader br;
                if (responseCode == 200) {
                    br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                } else {
                    br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                }

                String inputLine;
                while ((inputLine = br.readLine()) != null) {
                    output.append(inputLine);
                }
                br.close();
            }catch (Exception ex){
                ex.printStackTrace();
            }
            result = output.toString();
            return null;
        }

        protected void onPostExecute(Integer a){
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(result);
            if(element.getAsJsonObject().get("errorMessage") != null){

            }else if(element.getAsJsonObject().get("message") != null){
                // 번역 결과 출력 + ' ' + 번역 전 영문
                resultText.setText(element.getAsJsonObject().get("message").getAsJsonObject().get("result").getAsJsonObject().get("translatedText").getAsString());
            }

        }


    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cameraView = (CameraView) findViewById(R.id.cameraView);
        imageViewResult = (ImageView) findViewById(R.id.imageViewResult);
        textViewResult = (TextView) findViewById(R.id.textViewResult);
        textViewResult.setMovementMethod(new ScrollingMovementMethod());




        translationButton = (Button) findViewById(R.id.translationButton);
        resultText = (TextView) findViewById(R.id.resultText);
        translationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new BackgroundTask().execute();
            }
        });



        btnToggleCamera = (Button) findViewById(R.id.btnToggleCamera);
        btnDetectObject = (Button) findViewById(R.id.btnDetectObject);
        menuList = (ListView) findViewById(R.id.menuList);

        InputData1.put("title", "특허실용실안");
        Data.add(InputData1);
        InputData2.put("title", "디자인");
        Data.add(InputData2);
        InputData3.put("title", "상표");
        Data.add(InputData3);
        imgResult = "";



        final SimpleAdapter simpleAdapter = new SimpleAdapter(
                this, Data, android.R.layout.simple_list_item_2, new String[]{"title"}, new int[]{android.R.id.text1}
        );

        cameraView.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {

            }

            @Override
            public void onError(CameraKitError cameraKitError) {

            }

            @Override
            public void onImage(CameraKitImage cameraKitImage) {

                Bitmap bitmap = cameraKitImage.getBitmap();

                bitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false);

                imageViewResult.setImageBitmap(bitmap);

                final List<Classifier.Recognition> results = classifier.recognizeImage(bitmap);

                textViewResult.setText(results.toString());

                try
                {
                    imgResult = results.get(0).toString();
                }catch(IndexOutOfBoundsException e)
                {
                    imgResult = "EMPTY";
                }

            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {

            }
        });

        btnToggleCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //cameraView.toggleFacing();
                // 카메라 숨김 -> 메뉴 보이게
                if(cameraView.getVisibility() == View.VISIBLE) {
                    cameraView.setVisibility(View.GONE);
                    menuList.setVisibility(View.VISIBLE);
                }else {
                    // 카메라 보임 -> 메뉴 감추기
                    cameraView.setVisibility(View.VISIBLE);
                    menuList.setVisibility(View.GONE);
                }
            }
        });

        btnDetectObject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 번역 결과 레이블 초기화
                resultText.setText("");
//                cameraView.setVisibility(View.VISIBLE);
                // 사진 촬영 후 카메라 숨김
                cameraView.setVisibility(View.GONE);
                cameraView.captureImage();
                menuList.setVisibility(View.VISIBLE);
                menuList.setAdapter(simpleAdapter);
            }
        });






        /*
                메뉴(특허, 디자인, 상표) 클릭했을 때
         */
        menuList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                try
                {
                    /*
                            구글 이미지 검색 결과
                     */
                    String rs = resultText.getText().toString();
                    if(rs == ""){
                        /*
                            구글 이미지 검색 결과(영문 버전 그대로 사용)
                         */
                        String rs1 = imgResult.split("\\[")[1];
                        String rs2 = rs1.split("\\]")[1];
                        rs = rs2.split("\\(")[0];
                    }

                    String link = "";

                    switch(i)
                    {
                        case 0:
                            link = "http://m.kipris.or.kr/mobile/search/search_patent.do";
                            break;
                        case 1:
                            link = "http://m.kipris.or.kr/mobile/search/search_design.do";
                            break;
                        case 2:
                            link = "http://m.kipris.or.kr/mobile/search/search_trademark.do";
                            break;
                        default:
                            link = "http://m.kipris.or.kr/mobile/search/search_patent.do";
                    }

                    Toast.makeText(MainActivity.this, rs, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, WebviewActivity.class);
                    intent.putExtra("link", link);
                    intent.putExtra("result", rs);

                    startActivity(intent);
                }
                catch(ArrayIndexOutOfBoundsException e)
                {
                    Toast.makeText(MainActivity.this, "EMPTY RESULT", Toast.LENGTH_SHORT).show();
                }

            }
        });

        initTensorFlowAndLoadModel();
    }







    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }

    @Override
    protected void onPause() {
        cameraView.stop();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                classifier.close();
            }
        });
    }


    private void initTensorFlowAndLoadModel() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    classifier = TensorFlowImageClassifier.create(
                            getAssets(),
                            MODEL_FILE,
                            LABEL_FILE,
                            INPUT_SIZE,
                            IMAGE_MEAN,
                            IMAGE_STD,
                            INPUT_NAME,
                            OUTPUT_NAME);
                    makeButtonVisible();
                } catch (final Exception e) {
                    throw new RuntimeException("Error initializing TensorFlow!", e);
                }
            }
        });
    }

    private void makeButtonVisible() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnDetectObject.setVisibility(View.VISIBLE);
            }
        });
    }

    protected void onPreExecute(){

    }



}
