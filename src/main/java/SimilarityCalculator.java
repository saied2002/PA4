
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class SimilarityCalculator {

    private final Map<String, Map<String, Double>> normalizedRatings;

    public SimilarityCalculator(
            Map<String, Map<String, Double>> normalizedRatings) {
        this.normalizedRatings = normalizedRatings;
    }
    public double calculateUserSimilarity(
            String user1, String user2) {

        Map<String, Double> ratings1 = normalizedRatings.get(user1);
        Map<String, Double> ratings2 = normalizedRatings.get(user2);

        if (ratings1 == null || ratings2 == null) {
            return Double.NaN;
        }

        double sum = 0.0;
        int commonSongs = 0;

        for (String song : ratings1.keySet()) {
            if (ratings2.containsKey(song)) {
                double difference =
                    ratings1.get(song) - ratings2.get(song);

                sum += difference * difference;
                commonSongs++;
            }
        }        if (commonSongs == 0) {
            return Double.NaN;
        }

        return Math.sqrt(sum);
    }

    public List<String> getSortedSimilarUsers(String targetUser) {

        List<String> users = new ArrayList<>();

        for (String user : normalizedRatings.keySet()) {
            if (!user.equals(targetUser)
                    && !Double.isNaN(
                        calculateUserSimilarity(targetUser, user))) {
                users.add(user);
            }
        }

        users.sort(
            Comparator.comparingDouble(
                (String user) ->
                    calculateUserSimilarity(targetUser, user)
            ).thenComparing(Comparator.naturalOrder())
        );

        return users;
    }
}