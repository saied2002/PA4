import java.util.Set;


/**
* Validates command-line arguments and checks
* requirements for the music rating program.
*/
public class ArgumentValidator {

    private static final Set<String> ALLOWED_MODES =
        Set.of("-a", "-u", "-p");

    
   /**
    * Checks that the command-line arguments are valid.
    *
    * @param args The command-line arguments.
    * @throws IllegalArgumentException if the arguments are invalid.
    */
    public static void validateArguments(String[] args) {

        if (args == null || args.length < 2 || args.length > 3) {
            throw new IllegalArgumentException(
                "Error: incorrect number of arguments"
            );
        }

        if (args.length == 3 && !ALLOWED_MODES.contains(args[2])) {
            throw new IllegalArgumentException(
                "Error: invalid third argument"
            );
        }
    }

    
   /**
    * Checks that at least two cooperative users are available.
    *
    * @param count The number of cooperative users.
    * @throws IllegalArgumentException if fewer than two users are available.
    */
    public static void requireCooperativeUsers(int count) {

        if (count < 2) {
            throw new IllegalArgumentException(
                "Error: at least two cooperative users are required"
            );
        }
    }

    
   /**
    * Checks whether there are missing ratings to predict.
    *
    * @param missing True if missing ratings exist.
    * @throws IllegalArgumentException if there are no missing ratings.
    */
    public static void requireMissingRatings(boolean missing) {

        if (!missing) {
            throw new IllegalArgumentException(
                "Error: no predictions to be made"
            );
        }
    }
}