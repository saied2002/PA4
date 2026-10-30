import java.util.Map;

public class RatingPredictor {

    private final Map<String, Map<String, Double>> userRatings;
    private final Map<String, Map<String, Double>> normalizedRatings;
    private final SimilarityCalculator similarityCalculator;

    public RatingPredictor(UserAnalyzer analyzer) {
        this.userRatings = analyzer.getUserProfiles();
        this.normalizedRatings = analyzer.getNormalizedUserProfiles();
        this.similarityCalculator =
            new SimilarityCalculator(normalizedRatings);
    }

    public double predictRating(String targetUser, String song) {

        Map<String, Double> ratings = userRatings.get(targetUser);

        if (ratings == null || ratings.isEmpty()
                || ratings.containsKey(song)) {
            return Double.NaN;
        }

        double mean = 0.0;

        for (double rating : ratings.values()) {
            mean += rating;
        }

        mean /= ratings.size();

        double squaredDifferences = 0.0;

        for (double rating : ratings.values()) {
            double difference = rating - mean;
            squaredDifferences += difference * difference;
        }

        double standardDeviation =
            Math.sqrt(squaredDifferences / ratings.size());
        for (String otherUser :
                similarityCalculator.getSortedSimilarUsers(targetUser)) {

            Double normalizedRating =
                normalizedRatings.get(otherUser).get(song);

            if (normalizedRating != null
                    && Double.isFinite(normalizedRating)) {

                double predictedRating =
                    normalizedRating * standardDeviation + mean;

                long rounded = Math.round(predictedRating);

                return Math.max(1, Math.min(5, rounded));
            }
        }

        return Double.NaN;
    }
}