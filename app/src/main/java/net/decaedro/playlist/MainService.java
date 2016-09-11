package net.decaedro.playlist;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import net.decaedro.playlist.R;

public class MainService extends Service implements OnCompletionListener {
  static public int NOTIFY_ID = 666;
  public String video_id;
  public String video_url;
  public String video_title;
  public String video_duration;
  public MediaPlayer mediaPlayer = new MediaPlayer();

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }


  @Override
  public void onCreate() {
	      
    //mediaPlayer = MediaPlayer.create(this, Uri.parse(video_url));
	//  mediaPlayer = new MediaPlayer();
    //mediaPlayer.setOnCompletionListener(this);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {

	video_id = intent.getStringExtra(MainActivity.STR_ID);  
	video_url = intent.getStringExtra(MainActivity.STR_URL);  
	video_title = intent.getStringExtra(MainActivity.STR_TITLE);  
	video_duration = intent.getStringExtra(MainActivity.STR_DURATION);    
    createNotification();
    try{
		mediaPlayer = MediaPlayer.create(this, Uri.parse(video_url));
	    mediaPlayer.setOnCompletionListener(this);
	    if (!mediaPlayer.isPlaying()) {
	      mediaPlayer.start();
	    }
    }catch(Exception e){
    	Toast.makeText(this.getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
    }
    return START_STICKY;
  }

  public void onDestroy() {
	stopForeground(true);
    if (mediaPlayer.isPlaying()) {
      mediaPlayer.stop();
    }
    mediaPlayer.release();
  }

  public void onCompletion(MediaPlayer _mediaPlayer) {

	  Intent intent = new Intent("my-event");
	  intent.putExtra("message", "next");
	  LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
		
	  stopSelf();
  }
  public void createNotification(){
	  Intent notIntent = new Intent(this, MainActivity.class);
	  notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	  PendingIntent pendInt = PendingIntent.getActivity(this, 0,
	    notIntent, PendingIntent.FLAG_UPDATE_CURRENT);
	   
	  Notification.Builder builder = new Notification.Builder(this);
	   
	  builder.setContentIntent(pendInt)
	    .setSmallIcon(R.drawable.ic_play)
	    .setTicker(video_title)
	    .setOngoing(true)
	    .setContentTitle(video_title)
	    .setContentText(video_duration);
	  Notification not = builder.build();
	   
	  startForeground(NOTIFY_ID, not);
  }

}