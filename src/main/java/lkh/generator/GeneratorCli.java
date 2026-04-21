package lkh.generator;

import lkh.expression.parser.ParseException;

import java.io.IOException;
import java.nio.file.Path;

public final class GeneratorCli {
  private GeneratorCli() {
  }

  public static void main(String[] args) throws IOException, ParseException {
    if (args.length != 1) {
      throw new IllegalArgumentException("Usage: GeneratorCli <config.properties>");
    }

    run(Path.of(args[0]));
  }

  public static GeneratedLts run(Path configPath) throws IOException, ParseException {
    GeneratorConfigReader fileConfig = new GeneratorConfigReader();
    fileConfig.load(configPath);
    GeneratedLts generated = new RandomLtsGenerator(fileConfig.generatorConfig()).generate();
    GeneratedLtsWriter.write(generated, fileConfig.dotOutputPath(), fileConfig.witnessOutputPath());

    System.out.println("LTS written to: " + fileConfig.dotOutputPath().toAbsolutePath());
    System.out.println("Witnesses written to: " + fileConfig.witnessOutputPath().toAbsolutePath());
    System.out.println(generated.report());

    return generated;
  }
}
