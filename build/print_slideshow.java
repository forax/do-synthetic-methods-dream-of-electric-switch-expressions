import static java.util.Comparator.comparing;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

public class print_slideshow {
  private static String removeExtension(String filename) {
    var index = filename.lastIndexOf('.');
    if (index == -1) {
      return filename;
    }
    return filename.substring(0, index);
  }
  
  private static String shortName(String filename) {
    var index = filename.indexOf('-');
    if (index == -1) {
      return filename;
    }
    return filename.substring(index + 1);
  }
  
  public static void main(String[] args) throws IOException, InterruptedException {
    var NPM_BIN = Path.of("node_modules/.bin");
    var DECKTAPE = "decktape";
    var INPUT_FOLDER = Path.of("slideshow");
    var OUTPUT_FOLDER = Path.of("pdf");
    
    var PLUGIN = "rise";
    var PLUGIN_SIZE = "1920x1080";
    var PLUGIN_LOAD_PAUSE = "1000";
    var PLUGIN_PAUSE = "200";
    
    var command = NPM_BIN.resolve(DECKTAPE);
    if (!Files.exists(command)) {
      throw new IOException("command " + command + " not found");
    }
    
    Files.createDirectories(OUTPUT_FOLDER);
    try(var list = Files.list(INPUT_FOLDER)) {
      for(var path: (Iterable<Path>)list.filter(p -> p.toString().endsWith(".ipynb")).sorted(comparing(Path::toString))::iterator) {   
        var inputURI = URI.create("http://localhost:8888/notebooks/" + path.toString());
        var shortName = shortName(path.getFileName().toString());
        var outputPath = OUTPUT_FOLDER.resolve(removeExtension(shortName) + ".pdf");
 
        System.out.println("decktape " + inputURI + " to " + outputPath);
        
        var proccess = new ProcessBuilder(command.toString(),
            PLUGIN,
            "--size", PLUGIN_SIZE,
            "--load-pause", PLUGIN_LOAD_PAUSE,
            "--pause", PLUGIN_PAUSE,
            inputURI.toString(),
            outputPath.toString())
            .redirectInput(Redirect.INHERIT)
            .redirectOutput(Redirect.INHERIT)
            .redirectError(Redirect.INHERIT)
            .start();
        var exitCode = proccess.waitFor();
        if (exitCode != 0) {
          System.exit(exitCode);
          return;
        }
      }
    }
  }
}
