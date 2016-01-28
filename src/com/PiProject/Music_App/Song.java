package com.PiProject.Music_App;

public class Song {

    // Variablen voor elk nummer
    private long id;
    private String title;
    private String artist;

    //get methodes voor de variabelen in de constructor
    public long getID() {return id;}
    public String getTitle() {return title;}
    public String getArtist() {return artist;}

    public Song(long id, String songTitle, String songArtist) {
        id=songID;
        title=songTitle;
        artist=songArtist
    }
}
