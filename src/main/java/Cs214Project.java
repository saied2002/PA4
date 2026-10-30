import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;



/**
* Main class for the music rating analysis program.
* Reads rating data and generates analysis,
* similarity, and prediction results.
*/
public class Cs214Project {

    
   /**
    * Starts the music rating analysis program.
    *
    * @param args The command-line arguments containing
    *             the input file, output file, and optional mode.
    */
    public static void main(String[] args) {

        try {
            ArgumentValidator.validateArguments(args);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            return;
        }

        String inputFile = args[0];
        String outputFile = args[1];

        try {
            validateInputFile(inputFile);
        } catch (IllegalArgumentException | IOException e) {
            System.err.println(e.getMessage());
            return;
        }

        try {
            validateOutputFile(outputFile);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            return;
        }

        MusicData data = new MusicData();

        try (
            BufferedReader reader = Files.newBufferedReader(Paths.get(inputFile));
            CSVParser parser = CSVFormat.DEFAULT.parse(reader)
        ) {

        for (CSVRecord record : parser) {

            if (record.size() != 3) {
                throw new IllegalArgumentException(
                    "Error: each CSV row must contain exactly 3 fields"
                );
            }

            String song = record.get(0).trim();
            String user = record.get(1).trim();
            String ratingText = record.get(2).trim();

            if (song.isEmpty() || user.isEmpty() || ratingText.isEmpty()) {
                throw new IllegalArgumentException(
                    "Error: CSV row contains a missing field"
                );
            }

            int rating;

            try {
                rating = Integer.parseInt(ratingText);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                    "Error: rating must be an integer"
                );
            }

            if (rating < 1 || rating > 5) {
                throw new IllegalArgumentException(
                    "Error: rating must be between 1 and 5"
                );
            }

            data.addRating(song, user, rating);
        }

            Path outputPath = Paths.get(outputFile);

            if (args.length == 3 && args[2].equals("-a")) {
                try {
                    writeUserAnalysis(outputPath, data);
                } catch (IOException e) {
                    System.err.println("Error: could not write output file");
                }
                return;
            }

            if (args.length == 3 && args[2].equals("-u")) {
                try {
                    writeUserSimilarity(outputPath, data);
                } catch (IllegalArgumentException | IOException e) {
                    System.err.println(e.getMessage());
                }
                return;
            }


            if (args.length == 3 && args[2].equals("-p")) {
                try {
                    writePredictions(outputPath, data);
                } catch (IllegalArgumentException | IOException e) {
                    System.err.println(e.getMessage());
                }
                return;
            }


            try (
                BufferedWriter writer = Files.newBufferedWriter(outputPath);
                CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT)
            ) {

                printer.printRecord(
                    "song",
                    "number of ratings",
                    "mean",
                    "standard deviation"
                );

                for (Map.Entry<String, SongStats> entry :
                        data.getSongStats().entrySet()) {

                    SongStats stats = entry.getValue();

                    printer.printRecord(
                        entry.getKey(),
                        stats.getCount(),
                        stats.getMean(),
                        stats.getStandardDeviation()
                    );
                }
            }

        } catch (IllegalArgumentException | IOException e) {
            System.err.println(e.getMessage());
            return;
        }
    }
    
    private static void validateArguments(String[] args) {

    if (args == null || (args.length != 2 && args.length != 3)) {
        throw new IllegalArgumentException(
            "Error: incorrect number of arguments"
        );
    }

    if (args.length == 3 &&
        !args[2].equals("-a") &&
        !args[2].equals("-u") &&
        !args[2].equals("-p")) {

        throw new IllegalArgumentException(
            "Error: unsupported argument '" + args[2] + "'"
        );
    }
}
    
private static void validateInputFile(String inputFile) throws IOException {

    if (!inputFile.endsWith(".csv")) {
        throw new IllegalArgumentException(
            "Error: input file must be a CSV file"
        );
    }

    Path inputPath = Paths.get(inputFile);

    if (!Files.exists(inputPath)) {
        throw new IOException(
            "Error: input file does not exist"
        );
    }

    if (Files.size(inputPath) == 0) {
        throw new IOException(
            "Error: input file is empty"
        );
    }
}
    private static void validateOutputFile(String outputFile) {
        if (!outputFile.endsWith(".csv")) {
            throw new IllegalArgumentException(
                "Error: output file must be a CSV file"
            );
        }

        Path outputPath = Paths.get(outputFile);
        Path parent = outputPath.getParent();

        if (parent != null && !Files.exists(parent)) {
            throw new IllegalArgumentException(
                "Error: output directory does not exist"
            );
        }
    }

    private static void writeUserAnalysis(
            Path outputPath,
            MusicData data
    ) throws IOException {

        UserAnalyzer analyzer =
            new UserAnalyzer(data.getUserRatings());

        try (
            BufferedWriter writer =
                Files.newBufferedWriter(outputPath);

            CSVPrinter printer =
                new CSVPrinter(writer, CSVFormat.DEFAULT)
        ) {

            printer.printRecord(
                "username",
                "song",
                "rating"
            );

            for (Map.Entry<String, Map<String, Double>> userEntry
                    : analyzer.getUserProfiles().entrySet()) {

                String username = userEntry.getKey();
                Map<String, Double> ratings = userEntry.getValue();

                for (String song : analyzer.getSongs()) {

                    Double rating = ratings.get(song);

                    if (rating == null) {
                        printer.printRecord(
                            username,
                            song,
                            "NaN"
                        );
                    } else {
                        printer.printRecord(
                            username,
                            song,
                            rating.intValue()
                        );
                    }
                }
            }
        }
    }

    private static void writeSongSimilarity(
        Path outputPath,
        MusicData data
    ) throws IOException {

        UserAnalyzer analyzer =
            new UserAnalyzer(data.getUserRatings());

        if (analyzer.getUserProfiles().size() < 2) {
            throw new IllegalArgumentException(
                "Error: at least two cooperative users are required for song similarity"
            );
        }

        String[] songs =
            analyzer.getSongs().toArray(new String[0]);

        try (
            BufferedWriter writer =
                Files.newBufferedWriter(outputPath);

            CSVPrinter printer =
                new CSVPrinter(writer, CSVFormat.DEFAULT)
        ) {

            printer.printRecord(
                "name1",
                "name2",
                "similarity"
            );

            for (int i = 0; i < songs.length; i++) {

                for (int j = i + 1; j < songs.length; j++) {

                    double similarity =
                        analyzer.calculateSongSimilarity(
                            songs[i],
                            songs[j]
                        );

                    printer.printRecord(
                        songs[i],
                        songs[j],
                        similarity
                    );
                }
            }
        }
    }

    
    private static void writeUserSimilarity(
            Path outputPath,
            MusicData data
    ) throws IOException {

        UserAnalyzer analyzer =
            new UserAnalyzer(data.getUserRatings());

        String[] users =
            analyzer.getUserProfiles().keySet().toArray(new String[0]);

        java.util.Arrays.sort(users);

        if (users.length < 2) {
            throw new IllegalArgumentException(
                "Error: at least two cooperative users are required"
            );
        }

        try (
            BufferedWriter writer =
                Files.newBufferedWriter(outputPath);
            CSVPrinter printer =
                new CSVPrinter(writer, CSVFormat.DEFAULT)
        ) {

            printer.printRecord(
                "name1",
                "name2",
                "similarity"
            );

            for (int i = 0; i < users.length; i++) {
                for (int j = i + 1; j < users.length; j++) {

                    double similarity =
                        analyzer.calculateUserSimilarity(
                            users[i],
                            users[j]
                        );

                    printer.printRecord(
                        users[i],
                        users[j],
                        similarity
                    );
                }
            }
        }
    }

    
    private static void writePredictions(
            Path outputPath,
            MusicData data
    ) throws IOException {

        UserAnalyzer analyzer =
            new UserAnalyzer(data.getUserRatings());
            
        RatingPredictor predictor = new RatingPredictor(analyzer);


        if (analyzer.getUserProfiles().size() < 2) {
            throw new IllegalArgumentException(
                "Error: at least two cooperative users are required"
            );
        }

        java.util.List<String> songs =
            new java.util.ArrayList<>(analyzer.getSongs());

        java.util.List<String> users =
            new java.util.ArrayList<>(
                analyzer.getUserProfiles().keySet()
            );

        java.util.Collections.sort(songs);
        java.util.Collections.sort(users);

        boolean hasMissingRatings = false;

        for (String song : songs) {
            for (String user : users) {
                if (!analyzer.getUserProfiles()
                        .get(user).containsKey(song)) {
                    hasMissingRatings = true;
                }
            }
        }

        if (!hasMissingRatings) {
            throw new IllegalArgumentException(
                "Error: no predictions to be made"
            );
        }

        try (
            BufferedWriter writer =
                Files.newBufferedWriter(outputPath);
            CSVPrinter printer =
                new CSVPrinter(writer, CSVFormat.DEFAULT)
        ) {

            printer.printRecord(
                "song",
                "user",
                "predicted rating"
            );

            for (String song : songs) {
                for (String user : users) {

                    if (!analyzer.getUserProfiles()
                            .get(user).containsKey(song)) {

                        
                        double prediction =
                            predictor.predictRating(user, song);


                        
                        printer.printRecord(
                            song,
                            user,
                            Double.isNaN(prediction)
                                ? "NaN"
                                : Integer.toString((int) prediction)
                        );

                    }
                }
            }
        }
    }
    }