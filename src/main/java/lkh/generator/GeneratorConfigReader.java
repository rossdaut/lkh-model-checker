package lkh.generator;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import lkh.expression.Expression;
import lkh.expression.parser.ParseException;

public final class GeneratorConfigReader {
  private LtsGeneratorConfig generatorConfig;
  private Path dotOutputPath;
  private Path witnessOutputPath;

  public void load(Path configPath) throws IOException, ParseException {
    Properties properties = new Properties();
    try (Reader reader = Files.newBufferedReader(configPath)) {
      properties.load(reader);
    }

    generatorConfig = new LtsGeneratorConfig(
        booleanProperty(properties, "deterministic"),
        intProperty(properties, "minNodeCount"),
        intProperty(properties, "minEdgeCount"),
        intProperty(properties, "actionCount"),
        intProperty(properties, "propositionCount"),
        Expression.of(requiredProperty(properties, "initialCondition")),
        Expression.of(requiredProperty(properties, "goalCondition")),
        intProperty(properties, "initialStateCount"),
        intProperty(properties, "goalStateCount"),
        intProperty(properties, "witnessCount"),
        intProperty(properties, "minWitnessActionCount"),
        longProperty(properties, "seed")
    );
    dotOutputPath = outputPath(properties, configPath, "dotOutput", ".dot");
    witnessOutputPath = outputPath(properties, configPath, "witnessOutput", ".txt");
  }

  public LtsGeneratorConfig generatorConfig() {
    return requireLoaded(generatorConfig, "generatorConfig");
  }

  public Path dotOutputPath() {
    return requireLoaded(dotOutputPath, "dotOutputPath");
  }

  public Path witnessOutputPath() {
    return requireLoaded(witnessOutputPath, "witnessOutputPath");
  }

  private static <T> T requireLoaded(T value, String fieldName) {
    if (value == null) {
      throw new IllegalStateException("GeneratorConfigReader.load(...) must be called before reading " + fieldName);
    }
    return value;
  }

  private static String requiredProperty(Properties properties, String key) {
    String value = properties.getProperty(key);
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("Missing required property: " + key);
    }
    return value.trim();
  }

  private static boolean booleanProperty(Properties properties, String key) {
    return Boolean.parseBoolean(requiredProperty(properties, key));
  }

  private static int intProperty(Properties properties, String key) {
    return Integer.parseInt(requiredProperty(properties, key));
  }

  private static long longProperty(Properties properties, String key) {
    return Long.parseLong(requiredProperty(properties, key));
  }

  private static Path outputPath(Properties properties, Path configPath, String key, String extension) {
    String configured = properties.getProperty(key);
    if (configured != null && !configured.isBlank()) {
      Path configuredPath = Path.of(configured.trim());
      if (configuredPath.isAbsolute()) {
        return configuredPath;
      }
      return configPath.toAbsolutePath().getParent().resolve(configuredPath);
    }

    return configPath.toAbsolutePath().getParent().resolve(baseName(configPath) + extension);
  }

  private static String baseName(Path path) {
    String fileName = path.getFileName().toString();
    int extensionIndex = fileName.lastIndexOf('.');
    return extensionIndex >= 0 ? fileName.substring(0, extensionIndex) : fileName;
  }
}
