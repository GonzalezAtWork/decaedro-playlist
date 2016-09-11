package net.decaedro.playlist;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.NodeList;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView.OnEditorActionListener;

import net.decaedro.playlist.R;

public class MainActivity extends Activity implements  OnItemClickListener {

    public static final String STR_ID="ID";
    public static final String STR_URL="URL";
    public static final String STR_TITLE="TITLE";
    public static final String STR_DURATION="DURATION";
    public String url;
    private int playing;
    public boolean processing = false;
    Intent playbackServiceIntent;

    public EditText _InputList, _InputQuery;
    public Button _LoadList, _LoadUser, _StopSound;

    private ListView videoList;
    private ListAdapter adapter;
    private List<YoutubeItem> list;
    public static final String PROCESS_DIALOG_MSG="Loading...";
    
    @Override
    public void onResume() {
      super.onResume();
      LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("my-event"));
      adapter.notifyDataSetChanged();
    }
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	      play(playing + 1);
	    }
	  };
	  
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (savedInstanceState == null) {
            PlaceholderFragment pf = new PlaceholderFragment();
            pf.setActivity(this);
			getFragmentManager().beginTransaction()
					.add(R.id.container, pf).commit();
		}
	}

	public void createContent(){
		list=new ArrayList<YoutubeItem>();
        adapter=new ListAdapter(MainActivity.this, list);  
		videoList=(ListView)this.findViewById(R.id.movie_list_view);

        videoList.setAdapter(adapter);
        videoList.setOnItemClickListener(this);     
        _InputQuery = (EditText) this.findViewById(R.id.InputQuery);
        _InputQuery.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId==EditorInfo.IME_ACTION_UNSPECIFIED){
                	doSearch();
                }
                return false;
            }
        });
	}
    public void doSearch(){
   	 if(!processing){
	    	 _InputQuery.clearFocus();
	 		url = "http://gdata.youtube.com/feeds/api/videos?q="+ java.net.URLEncoder.encode( _InputQuery.getText().toString() ) +"&max-results=50&v=2";
	 	     startLoading(url);
   	 }
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_stop) {
			try{
				stopService(playbackServiceIntent);
			}catch(Exception e){}
			return true;
		}
		if (id == R.id.action_quit) {
			try{
				stopService(playbackServiceIntent);
			}catch(Exception e){}
			finish();
			return true;
		}
		if (id == R.id.action_about) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		MainActivity mActivity;
        public PlaceholderFragment() {}
        public void setActivity(MainActivity _activity){
            mActivity = _activity;
        }

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
		@Override
	    public void onViewCreated(View view, Bundle savedInstanceState) {
			mActivity.createContent();
	        super.onViewCreated(view, savedInstanceState);
	    }
	}
	
    public void executeServer(int indice){
    	playing = indice;
		try{
			stopService(playbackServiceIntent);
		}catch(Exception e){}
    	for( int i = 0; i< list.size(); i++){
    		list.get(i).setPlaying(false);
    	}
    	YoutubeItem video = list.get(playing);
    	video.setPlaying(true);
    	String video_url = "" + video.getVideo_url();
    	Toast.makeText(this, "Playing: " + video.getTitle(), Toast.LENGTH_LONG).show();
    	playbackServiceIntent = new Intent(this, MainService.class);
    	playbackServiceIntent.putExtra(MainActivity.STR_ID, video.getVideo_id());
    	playbackServiceIntent.putExtra(MainActivity.STR_URL, video_url);
    	playbackServiceIntent.putExtra(MainActivity.STR_TITLE, video.getTitle());
    	playbackServiceIntent.putExtra(MainActivity.STR_DURATION, video.getDuration());
    	startService(playbackServiceIntent);
    	adapter.notifyDataSetChanged();
    	videoList.smoothScrollToPosition(playing);
    }	
    public void play(int indice){
    	playing = indice;
		try{
			stopService(playbackServiceIntent);
		}catch(Exception e){}
    	YoutubeItem video = list.get(playing);
    	//String _url = "https://www.youtube.com/get_video_info?video_id="+ video.getVideo_id() +"&el=embedded&authuser=0&asv=3&sts=16421&hl=en_US";
    	String _url = "https://m.youtube.com/watch?v="+ video.getVideo_id();
        new LoadAudioUrlAsync().execute(_url);
    }
    public void startLoading(String url){
    	list.clear();
    	processing = true;
        new LoadMoviesAsync().execute(url);
    }
    class LoadMoviesAsync extends AsyncTask<String,YoutubeItem,Void>{
        ProgressDialog dialog;
        @Override
        protected void onPreExecute() {
            dialog=new ProgressDialog(MainActivity.this);
            dialog.setMessage(PROCESS_DIALOG_MSG);
            dialog.show();
            super.onPreExecute();
        }       
        @Override
        protected Void doInBackground(String... params) {
            String url=params[0];           
            Parser parser=new Parser();
            NodeList movieContentLst=parser.getResponceNodeList(url);
            //Log.i("MainActivity",""+movieContentLst.getLength());
            if(movieContentLst!=null){
                for(int i=0;i<movieContentLst.getLength();i++){
                    publishProgress(parser.getResult(movieContentLst, i));
                }
            }
             
            return null;
        }
         
        @Override
        protected void onProgressUpdate(YoutubeItem... values) {
            // TODO Auto-generated method stub
            super.onProgressUpdate(values);
            addItem(values);
            adapter.notifyDataSetChanged();
        	processing = false;
        }   
        public void addItem(YoutubeItem... items){
            for(YoutubeItem item: items){
            	if(item.getVideo_url() != null){
            		list.add(item);
            	}
            }   
        }
        @Override
        protected void onPostExecute(Void result) {
            if(dialog.isShowing()){
                dialog.dismiss();
            }       
            Toast.makeText(getBaseContext(), ""+list.size(),Toast.LENGTH_SHORT).show();
            super.onPostExecute(result);
        }
    }
    class LoadAudioUrlAsync extends AsyncTask<String,String,Void>{
        ProgressDialog dialog;
        @Override
        protected void onPreExecute() {
            dialog=new ProgressDialog(MainActivity.this);
            dialog.setMessage(PROCESS_DIALOG_MSG);
            dialog.show();
            super.onPreExecute();
        }       
        @Override
        protected Void doInBackground(String... params) {
            String url=params[0];   
            Parser parser=new Parser();
            String audio_url=parser.getResponceString(url);
            //Log.i("MainActivity",""+movieContentLst.getLength());
            if(audio_url!=null && !audio_url.equals("")){
            	YoutubeItem video = list.get(playing);
            	video.setVideo_url(audio_url);
            }
            return null;
        }
         
        @Override
        protected void onProgressUpdate(String... values) {
            // TODO Auto-generated method stub
            super.onProgressUpdate(values);
        	processing = false;
        }   

        @Override
        protected void onPostExecute(Void result) {
            if(dialog.isShowing()){
                dialog.dismiss();
            }       
            executeServer(playing);
            super.onPostExecute(result);
        }
    }
      @Override
      public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
      	play(arg2);
      }
}
