import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Preprocessor {

    private static final String SRC_DIR = "../main-app/src/main/java";
    private static final String OUTPUT_DIR = "../main-app/target/generated-sources/java";

    private static final Pattern CLASS_PATTERN = Pattern.compile(
        "\\b(class|interface|record)\\b\\s+[A-Za-z0-9_]+"
    );

    public static void main(String[] args) throws IOException {
        processDirectory(SRC_DIR);
    }

    private static void processDirectory(String directory) throws IOException {
        Path path = Paths.get(directory);
        if (Files.exists(path)) {
            Files.walkFileTree(path, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.toString().endsWith(".java")) {
                        processJavaFile(file);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    private static void processJavaFile(Path file) throws IOException {
        List<String> lines = Files.readAllLines(file);
        String content = String.join("\n", lines);

        Path relativePath = Paths.get(SRC_DIR).relativize(file);
        Path outputFile = Paths.get(OUTPUT_DIR).resolve(relativePath);

        Files.createDirectories(outputFile.getParent());

        if (CLASS_PATTERN.matcher(content).find()) {
            Files.copy(file, outputFile, StandardCopyOption.REPLACE_EXISTING);
        } else {
            String fileName = file.getFileName().toString().replace(".java", "");
            String newContent = "public class " + fileName + " {\n" + indentContent(content) + "\n}";

            Files.write(outputFile, newContent.getBytes());
            System.out.println("✔ Добавлен класс в: " + outputFile);
        }
    }

    private static String indentContent(String content) {
        return content.lines()
                .map(line -> "    " + line)
                .collect(Collectors.joining("\n"));
    }
}
