package com.fl.mfs;

import android.app.AlertDialog;
import android.app.ProgressDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Looper;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownServiceException;
import java.util.ArrayList;

public class ItemAdapter extends BaseAdapter {

    ArrayList<Info> infos;
    int rid;
    MainActivity context;

    public ItemAdapter(MainActivity context,int rid,ArrayList<Info> infos){
        this.infos = infos;
        this.rid = rid;
        this.context = context;
    }

    @Override
    public int getCount() {
        return infos.size();
    }

    @Override
    public Object getItem(int position) {
        return infos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Info info = (Info)getItem(position);
        View view = LayoutInflater.from(context).inflate(rid,parent,false);
        TextView text = view.findViewById(R.id.title);
        text.setText(info.title);
        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                Uri content_uri = Uri.parse(info.url);
                intent.setData(content_uri);
                context.startActivity(intent);
            }
        });
        Button button = view.findViewById(R.id.remove);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                            new AlertDialog.Builder(context).setMessage("真的不追我了吗呜呜呜~").setNegativeButton("有一种爱叫做放手(ㄒoㄒ)",new DialogInterface.OnClickListener(){

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    context.pd = ProgressDialog.show(context,"","别急姐妹，已经在做法了！ε=( o｀ω′)ノ",true,false);
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                           try{
                                               if(!post(info.topic)){
                                                   context.pd.dismiss();
                                                   Looper.prepare();
                                                   Toast.makeText(context, "糟糕！∑( 口 ||出错了！", Toast.LENGTH_LONG).show();
                                                   Looper.loop();
                                                   return;
                                               }
                                               MyService.channel.queueUnbind(MyService.userId,"db.mfs",info.topic);
                                               context.infos.remove(info);
                                               context.putFile(context.infos);
                                               context.handler.post(new Runnable() {
                                                  @Override
                                                  public void run() {
                                                     BaseAdapter ba=(BaseAdapter)context.infoList.getAdapter();
                                                     ba.notifyDataSetChanged();
                                                  }
                                               });
                                               context.pd.dismiss();
                                               Looper.prepare();
                                               Toast.makeText(context, "biubiu~施法完成！！", Toast.LENGTH_LONG).show();
                                               Looper.loop();
                                           }catch (Exception e){
                                               context.pd.dismiss();
                                               Looper.prepare();
                                               Toast.makeText(context, "糟糕！∑( 口 ||出错了！", Toast.LENGTH_LONG).show();
                                               Looper.loop();
                                               e.printStackTrace();
                                           }
                                        }
                                    }).start();
                                }

                            }).setPositiveButton("虚幻一枪而已啦(*^_^*)",null).show();



            }
        });

        return view;
    }

    public boolean post(String topic){
        String userId = userId = MyService.userId;
        String urlStr = "http://106.15.60.91:8182/mfs/unsubscribeTopic";
        URL url = null;
        HttpURLConnection conn = null;
        DataOutputStream os = null;
        InputStreamReader in = null;
        BufferedReader bf = null;
        try {
            JSONObject info = new JSONObject();
            info.put("topic",topic);
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
                return true;
            }else{
                return false;
            }
        }catch (UnknownServiceException e){

            e.printStackTrace();
        }catch (IOException e){

            e.printStackTrace();
        }catch (Exception e){

            e.printStackTrace();
        }finally {
            try {
                if (bf != null) bf.close();
                if (in != null) in.close();
                if (os != null) os.close();
                if (conn != null) conn.disconnect();
            } catch (Exception e) {

            }
        }
        return false;
    }
}
