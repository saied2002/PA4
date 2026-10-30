/**
* Stores song ratings and calculates basic statistics
* such as the average and standard deviation.
*/
public class SongStats {
  
   /**
    * Creates a new SongStats object.
    */
    public SongStats() {
    }


    private int count;
    private double mean;
    private double m2;

    
   /**
    * Adds a rating and updates the song statistics.
    *
    * @param rating The rating given to the song.
    */
    public void addRating(double rating) {
        count++;

        double delta = rating - mean;
        mean += delta / count;

        double delta2 = rating - mean;
        m2 += delta * delta2;
    }


   /**
    * Returns the total number of ratings for the song.
    *
    * @return The number of ratings.
    */
    public int getCount() {
        return count;
    }

    
   /**
    * Returns the average rating for the song.
    *
    * @return The average song rating.
    */
    public double getMean() {
        return mean;
    }

    
   /**
    * Calculates the standard deviation of the song ratings.
    *
    * @return The standard deviation of the ratings.
    */
    public double getStandardDeviation() {
        if (count == 0) {
            return 0.0;
        }

        return Math.sqrt(m2 / count);
    }
}