package net.pincette.jes.serdes;

import static com.fasterxml.jackson.dataformat.cbor.CBORFactory.builder;
import static javax.json.stream.JsonParser.Event.START_OBJECT;
import static net.pincette.json.JsonUtil.createReader;
import static net.pincette.util.Util.tryToDoRethrow;
import static net.pincette.util.Util.tryToDoWithRethrow;
import static net.pincette.util.Util.tryToGetWithSilent;

import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.json.JsonObject;
import javax.json.stream.JsonParser;
import net.pincette.json.JsonUtil;
import net.pincette.json.filter.JacksonGenerator;
import net.pincette.json.filter.JacksonParser;
import net.pincette.json.filter.JsonParserWrapper;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

public class Application implements Runnable {
  private final CBORFactory factory = builder().build();

  @ArgGroup(exclusive = true, multiplicity = "1")
  private Exclusive exclusive;

  private static JsonObject getObject(final JsonParser parser) {
    if (parser.next() != START_OBJECT) {
      throw new IllegalStateException("Not an object");
    }

    return parser.getObject();
  }

  public static void main(final String[] args) {
    Optional.of(new CommandLine(new Application()).execute(args))
        .filter(code -> code != 0)
        .ifPresent(System::exit);
  }

  private Optional<JsonObject> fromCbor(final InputStream in) {
    return tryToGetWithSilent(
        () ->
            new JsonParserWrapper(new JacksonParser(factory.createParser(new GZIPInputStream(in)))),
        Application::getObject);
  }

  @SuppressWarnings("java:S106") // Not logging.
  public void run() {
    if (exclusive.from) {
      System.out.println(fromCbor(System.in).map(JsonUtil::string).orElse("Not CBOR"));
    } else {
      tryToDoWithRethrow(
          () -> createReader(System.in),
          reader -> toCompressedCbor(reader.readObject(), System.out));
    }
  }

  private void toCompressedCbor(final JsonObject json, final OutputStream out) {
    tryToDoRethrow(
        () ->
            new JacksonGenerator(factory.createGenerator(new GZIPOutputStream(out)))
                .write(json)
                .close());
  }

  private static class Exclusive {
    @Option(
        names = {"-f", "--from"},
        required = true,
        description = "Converts from gzip compressed CBOR to JSON.")
    private boolean from;

    @Option(
        names = {"-t", "--to"},
        required = true,
        description = "Converts from JSON to gzip compressed CBOR.")
    private boolean to;
  }
}
