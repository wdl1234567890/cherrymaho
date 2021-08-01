package com.fl.mfs;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import androidx.core.view.DragStartHelper;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class MyService extends Service {

    ConnectionFactory factory = null;
    static Channel channel;
    Connection conn=null;
    NotificationManager notificationManager;
    NotificationCompat.Builder builder = null;
    NotificationChannel chan;
    String[] resource = new String[]{
            "a",
            "b",
            "c",
            "d",
            "e",
            "f",
            "g",
            "h",
            "i",
            "j",
            "k"
    };

    String[] titles = new String[]{
            "什么奖赏？当然是更新了~",
            "妈耶，更了！黑泽到底行不行！",
            "我们结婚了，速来！",
            "更新了！快来免费rua安达！",
            "更新了！资源是免费的！",
            "听说美酒佳人和更新更配哦~",
            "恭喜你，终于熬到更新了！",
            "参加婚礼也别忘了来看看更新哦~",
            "打工人在居酒屋里等来了更新！",
            "更新了，好高兴！玩起！",
            "这大概就是等到更新的心情吧！！"
    };

    static String userId = null;

    public MyService() {
    }

    @Override
    public void onCreate(){

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        factory = new ConnectionFactory();
        factory.setHost("106.15.60.91");
        factory.setPort(5672);
        factory.setUsername("fuling");
        factory.setPassword("fuling");
        factory.setAutomaticRecoveryEnabled(true);
        userId = Settings.Secure.getString(getBaseContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Info info = new Info();
                info.title = "、、、、";
                info.url="https://www.baidu.com";
                info.topic="、、、、";
                notifyInfo(info);
                Log.v("qqqqqq","qqqqq");
                try{
                    int count = 0;
                    while ((conn == null || !conn.isOpen())&&count<4){
                        info.title = "rrrr";
                        info.url="https://www.baidu.com";
                        info.topic="rrr";
                        notifyInfo(info);

                        conn = factory.newConnection();
                        count++;
                    }
                    if(channel==null)channel = conn.createChannel();

                    channel.exchangeDeclare("db.mfs", "topic", true);
                    channel.queueDeclare(userId, true, false, false, null);
                    Log.v("consumerCount!!!!",String.valueOf(channel.consumerCount(userId)));
                    if(channel.consumerCount(userId)==0){
                        info.title = "yyyy";
                        info.url="https://www.baidu.com";
                        info.topic="yyy";
                        notifyInfo(info);
                        Log.v("create?","create!!!!");

                        channel.basicConsume(userId, false, userId,
                                new DefaultConsumer(channel) {
                                    @Override
                                    public void handleDelivery(String consumerTag,
                                                               Envelope envelope,
                                                               AMQP.BasicProperties properties,
                                                               byte[] body)
                                            throws IOException
                                    {
                                        long deliveryTag = envelope.getDeliveryTag();
                                        channel.basicAck(deliveryTag, false);
                                        String message = new String(body);
                                        try{
                                            Info info = new Info();

                                            JSONObject jsonMessage = new JSONObject(message);
                                            info.title = jsonMessage.getString("title");
                                            info.url = jsonMessage.getString("url");
                                            notifyInfo(info);
                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }


                                    }
                                });
                    }
                }catch (Exception e){
                    try{
                        int count = 0;
                        while ((conn == null || !conn.isOpen())&&count<4){
                            info.title = "rrrr";
                            info.url="https://www.baidu.com";
                            info.topic="rrr";
                            notifyInfo(info);

                            conn = factory.newConnection();
                            count++;
                        }
                    }catch (Exception ee){

                    }
                }
            }
        },2000,3000);
    }

    @Override
    public void onDestroy(){
        Log.e("!!!!~~~~~~","0099999stop");
        this.startService(new Intent(this,MyService.class));

    }

    @Override
    public int onStartCommand(Intent intent, int flags,int startId){
        return  START_STICKY;
    }
   @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    public void notifyInfo(Info info){

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            if(chan==null){
                chan = new NotificationChannel("cherry","cherry maho",notificationManager.IMPORTANCE_DEFAULT);
                chan.setLightColor(Color.GREEN);
                chan.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

                notificationManager.createNotificationChannel(chan);
            }else{

            }

            builder = new NotificationCompat.Builder(this,"cherry");
        }else{
            builder = new NotificationCompat.Builder(this);

        }
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(info.url));
        final PendingIntent pi = PendingIntent.getActivity(this,0,intent,0);

        int rand = (int)(Math.random()*resource.length);

        builder.setSmallIcon(R.mipmap.icon_round);
        builder.setCategory(Notification.CATEGORY_MESSAGE);
        builder.setAutoCancel(true);
        RemoteViews remoteViews = new RemoteViews(getPackageName(),R.layout.layout);
        remoteViews.setImageViewResource(R.id.imageView,this.getResources().getIdentifier(resource[rand],"drawable",getPackageName()));
        remoteViews.setTextViewText(R.id.notifytitle,titles[rand]);
        remoteViews.setTextViewText(R.id.notifycontent,info.title);
        remoteViews.setTextViewTextSize(R.id.notifytitle, TypedValue.COMPLEX_UNIT_PX,40);
        remoteViews.setTextViewTextSize(R.id.notifycontent, TypedValue.COMPLEX_UNIT_PX,50);

        builder.setContent(remoteViews);
        builder.setContentIntent(pi);

        builder.setPriority(NotificationCompat.PRIORITY_MAX);

        builder.setContentIntent(pi);
        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        int id = (int)(Math.random() * Integer.MAX_VALUE);
        notificationManager.notify(id,notification);
    }
}