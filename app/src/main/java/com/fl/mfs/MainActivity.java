package com.fl.mfs;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;

import android.content.Intent;

import android.content.IntentFilter;
import android.os.Bundle;

import android.os.Handler;
import android.os.Looper;

import android.util.Log;

import android.view.View;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownServiceException;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.Map;


public class MainActivity extends AppCompatActivity {

    Button addTopic = null;

    static ArrayList<Info> infos = null;

    ListView infoList;
    EditText editText = null;
    Handler handler = null;
    ProgressDialog pd = null;
    BootReceiver bootReceiver = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startService(new Intent(this,MyService.class));
        setContentView(R.layout.activity_main);
        editText = findViewById(R.id.url);
        handler = new Handler();
        infos = pullFile();
        if(infos==null)infos = new ArrayList<>();
        initInfoList();
        initer();

        bootReceiver = new BootReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        intentFilter.addAction("android.intent.action.SCREEN_ON");
        intentFilter.addAction("android.intent.action.USER_PRESENT");
        registerReceiver(bootReceiver,intentFilter);
    }


    private void initInfoList(){
        infoList = findViewById(R.id.infolist);
        infoList.setAdapter(new ItemAdapter(this,R.layout.lv_item,infos));
    }

    public void putFile(ArrayList<Info> infos){
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
            try{
                fos = this.openFileOutput("mfs.bin",this.MODE_PRIVATE);
                oos = new ObjectOutputStream(fos);
                oos.writeObject(infos);
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                try{
                    oos.close();
                    fos.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

    }

    private ArrayList<Info> pullFile(){
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try{
            fis = this.openFileInput("mfs.bin");
            if(fis == null)return null;
            ois = new ObjectInputStream(fis);
            ArrayList<Info> infos = (ArrayList<Info>) ois.readObject();
            return infos;
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try{
                if(ois != null)ois.close();
                if(fis != null)fis.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return null;

    }


    public static JSONObject post(Map<String,Object> map){
        String urlStr = "http://106.15.60.91:8182/mfs/subscribeTopic";
        URL url = null;
        HttpURLConnection conn = null;
        DataOutputStream os = null;
        InputStreamReader in = null;
        BufferedReader bf = null;
        try {
            JSONObject info = new JSONObject(map);
            url = new URL(urlStr);
            conn = (HttpURLConnection)url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type","application/json");
            os = new DataOutputStream(conn.getOutputStream());
            os.writeBytes(info.toString());
            os.flush();
            os.close();
            if(conn.getResponseCode()==HttpURLConnection.HTTP_OK){
                in = new InputStreamReader(conn.getInputStream());
                bf = new BufferedReader(in);
                String recieveData = null;
                String result = "";
                while ((recieveData = bf.readLine())!=null){
                    result += recieveData;
                }
                JSONObject re = new JSONObject(result);
                return re;
            }else{

            }
        }catch (UnknownServiceException e){

            e.printStackTrace();
        }catch (IOException e){

            e.printStackTrace();
        }catch (Exception e){

            e.printStackTrace();
        }finally {
            try {
                if(bf != null)bf.close();
                if(in != null)in.close();
                if(os != null)os.close();
                if(conn != null)conn.disconnect();
            }catch (Exception e){

            }
        }

        return null;

    }

    public void initer(){

        try{


            addTopic=(Button) findViewById(R.id.add);

            addTopic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url =  editText.getText().toString();
                    if(!url.matches("^https://www.douban.com/group/topic/\\d+/$")){
                        Toast.makeText(MainActivity.this, "黑安懵逼(・∀・(・∀・：这是个什么东西？？？", Toast.LENGTH_LONG).show();
                        return;
                    }
                    pd = ProgressDialog.show(MainActivity.this,"","别急姐妹，已经在做法了！ε=( o｀ω′)ノ",true,false);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Info info = new Info();
                                info.topic = url.substring(url.substring(0,url.lastIndexOf("/")).lastIndexOf("/")+1,url.lastIndexOf("/"));
                                if(!infos.contains(info)){
                                    URL testUrl = new URL(url);
                                    HttpURLConnection HT = (HttpURLConnection) testUrl.openConnection();
                                    HT.setRequestProperty("User-agent","Mozilla/4.0");
                                    HT.setConnectTimeout(2000);
                                    HT.setReadTimeout(2000);
                                    HT.connect();

                                    Log.e("!!!!000",String.valueOf(HT.getResponseCode()));
                                    if(HT.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND){
                                        pd.dismiss();
                                        Looper.prepare();
                                        Toast.makeText(MainActivity.this, "如果这个帖子是真的，那黑安就是假的！！", Toast.LENGTH_LONG).show();
                                        Looper.loop();
                                        return;
                                    }
                                    Map<String,Object> m = new HashMap<>();
                                    m.put("url",url);
                                    JSONObject jsStr = post(m);
                                    if(jsStr!=null){
                                        info.topic = jsStr.getString("topic");
                                        info.title = jsStr.getString("title");
                                        info.url = jsStr.getString("url");


                                        MyService.channel.queueBind(MyService.userId, "db.mfs", info.topic);
                                        infos.add(info);
                                        putFile(infos);
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                BaseAdapter ba=(BaseAdapter)infoList.getAdapter();
                                                ba.notifyDataSetChanged();
                                                editText.setText("");
                                            }
                                        });
                                        pd.dismiss();
                                        Looper.prepare();
                                        Toast.makeText(MainActivity.this, "施法完成！！", Toast.LENGTH_LONG).show();
                                        Looper.loop();

                                    }else{
                                        pd.dismiss();
                                        Looper.prepare();
                                        Toast.makeText(MainActivity.this, "没有魔法了，施法失败！！", Toast.LENGTH_LONG).show();
                                        Looper.loop();
                                    }

                                }else{
                                    pd.dismiss();
                                    Looper.prepare();
                                    Toast.makeText(MainActivity.this, "你这个已经有魔法了哦，把机会给别的吧(＾Ｕ＾)", Toast.LENGTH_LONG).show();
                                    Looper.loop();
                                }


                            }catch (Exception e){
                                Log.e("error!!!!!!",e.getLocalizedMessage());
                                pd.dismiss();
                                Looper.prepare();
                                Toast.makeText(MainActivity.this, "没有魔法了，施法失败！！", Toast.LENGTH_LONG).show();
                                Looper.loop();
                                e.printStackTrace();
                            }

                        }
                    }).start();
                }
            });
        }catch (Exception e){
            Toast.makeText(MainActivity.this, "没有魔法了，施法失败！！", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

    }
}
