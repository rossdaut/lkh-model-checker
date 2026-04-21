package lkh.generator;

import lkh.dot.DotWriter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class GeneratedLtsWriter {
  private GeneratedLtsWriter() {
  }

  public static void write(GeneratedLts generatedLts, Path dotOutputPath, Path witnessOutputPath) throws IOException {
    createParentDirectories(dotOutputPath);
    createParentDirectories(witnessOutputPath);

    DotWriter.writeLTS(generatedLts.lts(), dotOutputPath.toString());
    Files.write(witnessOutputPath, witnessLines(generatedLts), StandardCharsets.UTF_8);
  }

  private static void createParentDirectories(Path path) throws IOException {
    Path parent = path.toAbsolutePath().getParent();
    if (parent != null) {
      Files.createDirectories(parent);
    }
  }

  private static List<String> witnessLines(GeneratedLts generatedLts) {
    return generatedLts.implantedWitnesses().stream()
        .map(witness -> String.join(" ", witness))
        .toList();
  }
}
