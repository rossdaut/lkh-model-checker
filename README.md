# Instructions to build

## Dependencies

The library PDDL4J version 4.0.0 is required to build the project. At the moment of writing it is not available on Maven repository so it must be installed locally.

1. Download the .jar file from https://github.com/pellierd/pddl4j.
2. Install the library in your local maven repository with the following command
```bash
mvn install:install-file \ 
  -Dfile=lib/pddl4j-4.0.0.jar \
  -DgroupId=fr.uga.pddl4j \
  -DartifactId=pddl4j \
  -Dversion=4.0.0 \
  -Dpackaging=jar
```
3. ???
4. Profit