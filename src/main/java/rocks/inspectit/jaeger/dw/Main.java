package rocks.inspectit.jaeger.dw;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import rocks.inspectit.jaeger.model.config.Configuration;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        if (args.length != 1) {
            logger.info("Usage: <file.yml>");
            return;
        }

        Yaml yaml = new Yaml();
        try (InputStream in = Files.newInputStream(Paths.get(args[0]))) {
            Configuration config = yaml.loadAs(in, Configuration.class);
            Analyzer analyzer = new Analyzer(config);
            analyzer.start();
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

}
