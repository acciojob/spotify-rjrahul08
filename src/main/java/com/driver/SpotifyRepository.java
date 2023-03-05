package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class SpotifyRepository {

    public HashMap<Artist, List<Album>> artistAlbumMap;
    public HashMap<Album, List<Song>> albumSongMap;
    public HashMap<Playlist, List<Song>> playlistSongMap;
    public HashMap<Playlist, List<User>> playlistListenerMap;
    public HashMap<User, Playlist> creatorPlaylistMap;
    public HashMap<User, List<Playlist>> userPlaylistMap;
    public HashMap<Song, List<User>> songLikeMap;

    public List<User> users;
    public List<Song> songs;
    public List<Playlist> playlists;
    public List<Album> albums;
    public List<Artist> artists;

    public SpotifyRepository(){
        //To avoid hitting apis multiple times, initialize all the hashmaps here with some dummy data
        artistAlbumMap = new HashMap<>();
        albumSongMap = new HashMap<>();
        playlistSongMap = new HashMap<>();
        playlistListenerMap = new HashMap<>();
        creatorPlaylistMap = new HashMap<>();
        userPlaylistMap = new HashMap<>();
        songLikeMap = new HashMap<>();

        users = new ArrayList<>();
        songs = new ArrayList<>();
        playlists = new ArrayList<>();
        albums = new ArrayList<>();
        artists = new ArrayList<>();
    }

    public User createUser(String name, String mobile) {
        //create the user with given name and number
        User user = new User(name,mobile);
        user.setMobile(mobile);
        user.setName(name);
        users.add(user);
        return user;
    }

    public Artist createArtist(String name) {
        //create the artist with given name
        Artist artist = new Artist(name);
        artist.setName(name);
        artist.setLikes(0);

        artists.add(artist);
        return artist;
    }
    public Album createAlbum(String title, String artistName) {
        //If the artist does not exist, first create an artist with given name
        //Create an album with given title and artist
        Artist currArtist = null;
        for(Artist artist : artists){
            if(artist.getName() == artistName){
                currArtist = artist;
                break;
            }
        }
        if (currArtist == null){
            currArtist = createArtist(artistName);
            Album album = new Album();

            album.setTitle(title);
            album.setReleaseDate(new Date());

            albums.add(album);

            List<Album> list = new ArrayList<>();
            list.add(album);
            artistAlbumMap.put(currArtist,list);

            return album;
        }
        else {
            Album album = new Album();

            album.setTitle(title);
            album.setReleaseDate(new Date());

            albums.add(album);

            List<Album> list = artistAlbumMap.get(currArtist);
            if (list == null) {
                list = new ArrayList<>();
            }
            list.add(album);

            artistAlbumMap.put(currArtist, list);

            return album;
        }
    }

    public Song createSong(String title, String albumName, int length) throws Exception{
        //If the album does not exist in database, throw "Album does not exist" exception
        //Create and add the song to respective album
        Album album = null;
        for(Album currAlbum : albums){
            if(currAlbum.getTitle() == albumName){
                album = currAlbum;
                break;
            }
        }
        if(album == null){
            throw new Exception( "Album does not exist" );
        }
        else {
            Song song = new Song();
            song.setLength(length);
            song.setLikes(0);
            song.setTitle(title);

            songs.add(song);

            if(albumSongMap.containsKey(album)){
                List<Song> list = albumSongMap.get(album);
                list.add(song);
                albumSongMap.put(album,list);
            }
            else{
                List<Song> list = new ArrayList<>();
                list.add(song);
                albumSongMap.put(album,list);
            }
            return song;
        }
    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {
        //Create a playlist with given title and add all songs having the given length in the database to that playlist
        //The creater of the playlist will be the given user and will also be the only listener at the time of playlist creation
        //If the user does not exist, throw "User does not exist" exception
        User user = null;
        for(User currUser : users){
            if(currUser.getMobile() == mobile){
                user = currUser;
                break;
            }
        }
        if(user == null){
            throw new Exception("User does not exist");
        }
        else{
            Playlist playlist = new Playlist();
            playlist.setTitle(title);
            playlists.add(playlist);

            List<Song> list = new ArrayList<>();
            for(Song song : songs){
                if(song.getLength() == length){
                    list.add(song);
                }
            }
            playlistSongMap.put(playlist,list);

            List<User> userList =  new ArrayList<>();
            userList.add(user);
            playlistListenerMap.put(playlist,userList);
            creatorPlaylistMap.put(user,playlist);

            if(userPlaylistMap.containsKey(user)){
                List<Playlist> userPlaylist = userPlaylistMap.get(user);
                userPlaylist.add(playlist);
                userPlaylistMap.put(user,userPlaylist);
            }
            else {
                List<Playlist> plays = new ArrayList<>();
                plays.add(playlist);
                userPlaylistMap.put(user,plays);
            }
            return playlist;
        }
    }

    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {
        //Create a playlist with given title and add all songs having the given titles in the database to that playlist
        //The creater of the playlist will be the given user and will also be the only listener at the time of playlist creation
        //If the user does not exist, throw "User does not exist" exception
        User user = null;
        for(User currUser : users){
            if(currUser.getMobile() == mobile){
                user = currUser;
                break;
            }
        }
        if(user == null){
            throw new Exception("User does not exist");
        }
        else{
            Playlist playlist = new Playlist();
            playlist.setTitle(title);
            playlists.add(playlist);

            List<Song> songList = new ArrayList<>();
            for(Song currSong : songs){
                if(songTitles.contains(currSong.getTitle())){
                    songList.add(currSong);
                }
            }

            playlistSongMap.put(playlist,songList);

            List<User> userList = new ArrayList<>();
            userList.add(user);
            playlistListenerMap.put(playlist,userList);

            creatorPlaylistMap.put(user,playlist);

            if(creatorPlaylistMap.containsKey(user)){
                List<Playlist> userPlayList = userPlaylistMap.get(user);
                userPlayList.add(playlist);
                userPlaylistMap.put(user,userPlayList);
            }
            else{
                List<Playlist> plays = new ArrayList<>();
                plays.add(playlist);
                userPlaylistMap.put(user,plays);
            }
            return playlist;
        }
    }

    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
        //Find the playlist with given title and add user as listener of that playlist and update user accordingly
        //If the user is creater or already a listener, do nothing
        //If the user does not exist, throw "User does not exist" exception
        //If the playlist does not exists, throw "Playlist does not exist" exception
        // Return the playlist after updating

        User user = null;
        for(User currUser : users){
            if(currUser.getMobile() == mobile){
                user = currUser;
                break;
            }
        }
        if(user == null){
            throw new Exception("User does not exist");
        }
        else{
            Playlist playlist = null;
            for (Playlist currPlayList : playlists){
                if(currPlayList.getTitle()== playlistTitle){
                    playlist = currPlayList;
                    break;
                }
            }
            if(playlist == null){
                throw new Exception("Playlist does not exist");
            }

            List<User> listener = playlistListenerMap.get(playlist);
            for(User currUser : users){
                if (currUser == user){
                    return playlist;
                }
            }
            listener.add(user);
            playlistListenerMap.put(playlist,listener);

            List<Playlist> playlists1 = userPlaylistMap.get(user);
            if(playlists1 == null){
                playlists1 = new ArrayList<>();
            }
            playlists1.add(playlist);
            userPlaylistMap.put(user,playlists1);
            return playlist;
        }
    }

    public Song likeSong(String mobile, String songTitle) throws Exception {
        //The user likes the given song. The corresponding artist of the song gets auto-liked
        //A song can be liked by a user only once. If a user tried to like a song multiple times, do nothing
        //However, an artist can indirectly have multiple likes from a user, if the user has liked multiple songs of that artist.
        //If the user does not exist, throw "User does not exist" exception
        //If the song does not exist, throw "Song does not exist" exception
        //Return the song after updating

        User user = null;
        for(User currUser : users){
            if(currUser.getMobile() == mobile){
                user = currUser;
                break;
            }
        }
        if(user == null){
            throw new Exception("User does not exist");
        }

        Song song = null;
        for(Song currSong : songs){
            if(currSong.getTitle()== songTitle){
                song = currSong;
                break;
            }
        }
        if(song == null){
            throw new Exception("Song does not exist");
        }

        if(songLikeMap.containsKey(song)){
            List<User> userList = songLikeMap.get(song);
            if(userList.contains(user)){
                return song;
            }
            else{
                int likes = song.getLikes() + 1;
                song.setLikes(likes);
                userList.add(user);
                songLikeMap.put(song,userList);

                Album album = null;
                for (Album currAlbum : albums){
                    List<Song > songList = albumSongMap.get(currAlbum);
                    if(songList.contains(song)){
                        album = currAlbum;
                        break;
                    }
                }
                Artist artist = null;
                for(Artist currArtist : artists){
                    List<Album> albumList = artistAlbumMap.get(currArtist);
                    if (albumList.contains(album)){
                        artist = currArtist;
                        break;
                    }
                }
                int like1 = artist.getLikes() + 1;
                artist.setLikes(like1);
                artists.add(artist);
                return song;
            }
        }else{
            int likes = song.getLikes() + 1;
            song.setLikes(likes);
            List<User> list = new ArrayList<>();
            list.add(user);
            songLikeMap.put(song,list);
            Album album = null;
            for (Album currAlbum : albums){
                List<Song > songList = albumSongMap.get(currAlbum);
                if(songList.contains(song)){
                    album = currAlbum;
                    break;
                }
            }
            Artist artist = null;
            for(Artist currArtist : artists){
                List<Album> albumList = artistAlbumMap.get(currArtist);
                if (albumList.contains(album)){
                    artist = currArtist;
                    break;
                }
            }
            int likes1 = artist.getLikes()+1;
            artist.setLikes(likes1);
            artists.add(artist);

            return song;
        }
    }

    public String mostPopularArtist() {
        //Return the artist name with maximum likes
        int max = 0;
        Artist artist1=null;

        for(Artist artist:artists){
            if(artist.getLikes()>=max){
                artist1=artist;
                max = artist.getLikes();
            }
        }
        if(artist1==null)
            return null;
        else
            return artist1.getName();
    }

    public String mostPopularSong() {
        //return the song title with maximum likes
        int max=0;
        Song song = null;

        for(Song song1:songLikeMap.keySet()){
            if(song1.getLikes()>=max){
                song=song1;
                max = song1.getLikes();
            }
        }
        if(song==null)
            return null;
        else
            return song.getTitle();
    }


}
