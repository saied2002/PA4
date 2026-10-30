import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class UserAnalyzer {

    private final Map<String, Map<String, Double>> userProfiles;

    public UserAnalyzer(Map<String, Map<String, Double>> userRatings) {

        userProfiles = new TreeMap<>();

        for (Map.Entry<String, Map<String, Double>> entry
                : userRatings.entrySet()) {

            userProfiles.put(
                entry.getKey(),
                new TreeMap<>(entry.getValue())
            );
        }

        removeUncooperativeUsers();
    }

    private void removeUncooperativeUsers() {

        Iterator<Map.Entry<String, Map<String, Double>>> iterator =
            userProfiles.entrySet().iterator();

        while (iterator.hasNext()) {

            Map<String, Double> ratings =
                iterator.next().getValue();

            Set<Double> distinctRatings =
                new HashSet<>(ratings.values());

            if (distinctRatings.size() <= 1) {
                iterator.remove();
            }
        }
    }

    
   /**
    * Calculates normalized ratings for each cooperative user.
    *
    * @return A map containing the normalized ratings for each user.
    */

    public Map<String, Map<String, Double>> getNormalizedUserProfiles() {

    Map<String, Map<String, Double>> normalizedProfiles =
        new TreeMap<>();

    for (Map.Entry<String, Map<String, Double>> userEntry
            : userProfiles.entrySet()) {

        Map<String, Double> ratings = userEntry.getValue();

        double sum = 0.0;

        for (double rating : ratings.values()) {
            sum += rating;
        }

        double mean = sum / ratings.size();

        double squaredDifferences = 0.0;

        for (double rating : ratings.values()) {
            double difference = rating - mean;
            squaredDifferences += difference * difference;
        }

        double standardDeviation =
            Math.sqrt(squaredDifferences / ratings.size());

        Map<String, Double> normalizedRatings =
            new TreeMap<>();

        for (Map.Entry<String, Double> ratingEntry
                : ratings.entrySet()) {

            double normalizedRating =
                (ratingEntry.getValue() - mean)
                / standardDeviation;

            normalizedRatings.put(
                ratingEntry.getKey(),
                normalizedRating
            );
        }

        normalizedProfiles.put(
            userEntry.getKey(),
            normalizedRatings
        );
    }

    return normalizedProfiles;
    }

    public Map<String, Map<String, Double>> getNormalizedSongProfiles() {

    Map<String, Map<String, Double>> normalizedUsers =
        getNormalizedUserProfiles();

    Map<String, Map<String, Double>> songProfiles =
        new TreeMap<>();

    for (String song : getSongs()) {

        Map<String, Double> userRatings =
            new TreeMap<>();

        for (String user : normalizedUsers.keySet()) {

            Double rating =
                normalizedUsers.get(user).get(song);

            if (rating == null) {
                userRatings.put(user, Double.NaN);
            } else {
                userRatings.put(user, rating);
            }
        }

        songProfiles.put(song, userRatings);
    }

    return songProfiles;
    }

    public double calculateSongSimilarity(String song1, String song2) {

    Map<String, Map<String, Double>> songProfiles =
        getNormalizedSongProfiles();

    Map<String, Double> ratings1 =
        songProfiles.get(song1);

    Map<String, Double> ratings2 =
        songProfiles.get(song2);

    double sumSquaredDifferences = 0.0;
    int commonUsers = 0;

    for (String user : ratings1.keySet()) {

        double rating1 = ratings1.get(user);
        double rating2 = ratings2.get(user);

        if (!Double.isNaN(rating1) &&
            !Double.isNaN(rating2)) {

            double difference = rating1 - rating2;

            sumSquaredDifferences +=
                difference * difference;

            commonUsers++;
        }
    }

    if (commonUsers == 0) {
        return Double.NaN;
    }

    return Math.sqrt(sumSquaredDifferences);
    }

    
    public double calculateUserSimilarity(String user1, String user2) {

        Map<String, Map<String, Double>> profiles =
            getNormalizedUserProfiles();

        Map<String, Double> ratings1 = profiles.get(user1);
        Map<String, Double> ratings2 = profiles.get(user2);

        double sumSquaredDifferences = 0.0;
        int commonSongs = 0;

        for (String song : ratings1.keySet()) {

            if (ratings2.containsKey(song)) {

                double difference =
                    ratings1.get(song) - ratings2.get(song);

                sumSquaredDifferences += difference * difference;
                commonSongs++;
            }
        }

        if (commonSongs == 0) {
            return Double.NaN;
        }

        return Math.sqrt(sumSquaredDifferences);
    }

    
    public double predictRating(String targetUser, String song) {

        Map<String, Map<String, Double>> profiles =
            getNormalizedUserProfiles();

        Map<String, Double> targetRatings =
            userProfiles.get(targetUser);

        if (targetRatings == null || targetRatings.containsKey(song)) {
            return Double.NaN;
        }

        double mean = 0.0;

        for (double rating : targetRatings.values()) {
            mean += rating;
        }

        mean /= targetRatings.size();

        double squaredDifferences = 0.0;

        for (double rating : targetRatings.values()) {
            double difference = rating - mean;
            squaredDifferences += difference * difference;
        }

        double standardDeviation =
            Math.sqrt(squaredDifferences / targetRatings.size());

        java.util.List<String> otherUsers =
            new java.util.ArrayList<>(profiles.keySet());

        otherUsers.remove(targetUser);

        otherUsers.sort(
            java.util.Comparator
                .comparingDouble(
                    (String user) ->
                        calculateUserSimilarity(targetUser, user)
                )
                .thenComparing(java.util.Comparator.naturalOrder())
        );

        for (String otherUser : otherUsers) {

            double similarity =
                calculateUserSimilarity(targetUser, otherUser);

            if (Double.isNaN(similarity)) {
                continue;
            }

            Double normalizedRating =
                profiles.get(otherUser).get(song);

            if (normalizedRating != null
                    && !Double.isNaN(normalizedRating)) {

                double prediction =
                    normalizedRating * standardDeviation + mean;

                long rounded = Math.round(prediction);

                return Math.max(1, Math.min(5, rounded));
            }
        }

        return Double.NaN;
    }


    
   /**
    * Returns the ratings stored for each cooperative user.
    *
    * @return A map of users and their song ratings.
    */
    public Map<String, Map<String, Double>> getUserProfiles() {
        return userProfiles;
    }

    
   /**
    * Gets all songs rated by cooperative users.
    *
    * @return A set containing the song names.
    */
    public Set<String> getSongs() {

        Set<String> songs = new TreeSet<>();

        for (Map<String, Double> ratings : userProfiles.values()) {
            songs.addAll(ratings.keySet());
        }

        return songs;
    }
}