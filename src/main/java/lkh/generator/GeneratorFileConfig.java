package lkh.generator;

import lkh.expression.Expression;
import lkh.expression.parser.ParseException;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public record GeneratorFileConfig(
    LtsGeneratorConfig generatorConfig,
    Path dotOutputPath,
    Path witnessOutputPath
) {
  public static GeneratorFileConfig from(Path configPath) throws IOException, ParseException {
    Properties p = new Properties();
    try (Reader reader = Files.newBufferedReader(configPath)) {
      p.load(reader);
    }
    return new GeneratorFileConfig(
        new LtsGeneratorConfig(
            booleanProperty(p, "deterministic"),
            intProperty(p, "minNodeCount"),
            intProperty(p, "minEdgeCount"),
            intProperty(p, "actionCount"),
            intProperty(p, "propositionCount"),
            Expression.of(requiredProperty(p, "initialCondition")),
            Expression.of(requiredProperty(p, "goalCondition")),
            intProperty(p, "initialStateCount"),
            intProperty(p, "goalStateCount"),
            intProperty(p, "witnessCount"),
            intProperty(p, "minWitnessActionCount"),
            longProperty(p, "seed")
        ),
        outputPath(p, configPath, "dotOutput", ".dot"),
        outputPath(p, configPath, "witnessOutput", ".txt")
    );
  }

  private static String requiredProperty(Properties p, String key) {
    String value = p.getProperty(key);
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("Missing required property: " + key);
    }
    return value.trim();
  }

  private static boolean booleanProperty(Properties p, String key) {
    return Boolean.parseBoolean(requiredProperty(p, key));
  }

  private static int intProperty(Properties p, String key) {
    return Integer.parseInt(requiredProperty(p, key));
  }

  private static long longProperty(Properties p, String key) {
    return Long.parseLong(requiredProperty(p, key));
  }

  private static Path outputPath(Properties p, Path configPath, String key, String extension) {
    String configured = p.getProperty(key);
    if (configured != null && !configured.isBlank()) {
      Path configuredPath = Path.of(configured.trim());
      return configuredPath.isAbsolute()
          ? configuredPath
          : configPath.toAbsolutePath().getParent().resolve(configuredPath);
    }
    return configPath.toAbsolutePath().getParent().resolve(baseName(configPath) + extension);
  }

  private static String baseName(Path path) {
    String fileName = path.getFileName().toString();
    int dot = fileName.lastIndexOf('.');
    return dot >= 0 ? fileName.substring(0, dot) : fileName;
  }
}
