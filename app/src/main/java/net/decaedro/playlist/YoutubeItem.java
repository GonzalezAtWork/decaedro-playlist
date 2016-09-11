package net.decaedro.playlist;

public class YoutubeItem {
	 
    String title;
    String video_id;
    String duration;
    String icon_url;
    String video_url;
    String playlist_id;
    boolean playing = false;

    public boolean getPlaying() {
        return playing;
    }
    public void setPlaying(boolean playing){
        this.playing = playing;
    }
    
    public String getPlaylist_id() {
        return playlist_id;
    }
    public void setPlaylist_id(String playlist_id){
        this.playlist_id = playlist_id;
    }

    public String getVideo_url() {
        return video_url;
    }
    public void setVideo_url(String video_url) {
        this.video_url = video_url;
    }
    public String getIcon_url() {
        return icon_url;
    }
    public void setIcon_url(String icon_url) {
        this.icon_url = icon_url;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getVideo_id() {
        return video_id;
    }
    public void setVideo_id(String video_id) {
        this.video_id = video_id;
    }
    public String getDuration() {
        return duration;
    }
    public void setDuration(String duration) {
        this.duration = duration;
    }
     
}