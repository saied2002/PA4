import java.util.Map;
import java.util.TreeMap;


/**
* Stores song ratings and calculates statistics
* for the music rating analysis program.
*/
public class MusicData {

    private final Map<String, SongStats> songStats = new TreeMap<>();
    private final Map<String, Map<String, Double>> userRatings = new TreeMap<>();

    
   /**
    * Adds a song rating for a user and updates
    * the song statistics.
    *
    * @param song The name of the song.
    * @param user The name of the user.
    * @param rating The rating given to the song.
    */
    public void addRating(String song, String user, double rating) {
        songStats.putIfAbsent(song, new SongStats());
        songStats.get(song).addRating(rating);

        userRatings.putIfAbsent(user, new TreeMap<>());
        userRatings.get(user).put(song, rating);
    }

    
   /**
    * Returns the statistics for all songs.
    *
    * @return A map of song names and their statistics.
    */
    public Map<String, SongStats> getSongStats() {
        return songStats;
    }

    
   /**
    * Returns the song ratings stored for each user.
    *
    * @return A map of users and their song ratings.
    */
    public Map<String, Map<String, Double>> getUserRatings() {
        return userRatings;
    }
}