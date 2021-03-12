# JSON Event Sourcing Serdes CLI

With this command line interface you can convert GZIP compressed CBOR to JSON with the ```-f``` or ```--from``` option and back with the ```-t``` or ```--to``` option. It reads from ```stdin``` and writes to ```stdout```. It can be useful in combination with the Kafka console consumer and producer when working with topics that are used by JSON Event Sourcing.

You can build the tool with ```mvn clean package```. This will produce a self-contained JAR-file in the ```target``` directory with the form ```pincette-jes-serdes-<version>-jar-with-dependencies.jar```. You can launch this JAR like this:

```
> java -jar pincette-jes-serdes-<version>-jar-with-dependencies.jar help
```