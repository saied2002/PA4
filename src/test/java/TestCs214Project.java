import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class TestCs214Project {

    @TempDir
    Path tempDir;

    @Test
    public void testPa1Mode() throws Exception {

        Path output = tempDir.resolve("pa1.csv");

        Cs214Project.main(new String[] {
            "database/files/file1.csv",
            output.toString()
        });

        assertTrue(Files.exists(output));

        String contents = Files.readString(output);

        assertTrue(
            contents.startsWith(
                "song,number of ratings,mean,standard deviation"
            )
        );
    }

    @Test
    public void testPa2Mode() throws Exception {

        Path output = tempDir.resolve("pa2.csv");

        Cs214Project.main(new String[] {
            "database/files/file4_pa2.csv",
            output.toString(),
            "-a"
        });

        assertTrue(Files.exists(output));

        String contents = Files.readString(output);

        assertTrue(
            contents.startsWith(
                "username,song,rating"
            )
        );

        assertTrue(
            contents.contains(
                "alex,Bohemian Rhapsody,NaN"
            )
        );

        assertFalse(
            contents.contains("cameron")
        );
    }

    @Test
    public void testIncorrectArgumentCount() {

        assertDoesNotThrow(() ->
            Cs214Project.main(new String[] {})
        );
    }

    @Test
    public void testInvalidRating() throws Exception {

        Path input = tempDir.resolve("bad.csv");

        Files.writeString(
            input,
            "song1,user1,3.0\n"
        );

        Path output = tempDir.resolve("output.csv");

        assertDoesNotThrow(() ->
            Cs214Project.main(new String[] {
                input.toString(),
                output.toString(),
                "-a"
            })
        );

        assertFalse(Files.exists(output));
    }
}