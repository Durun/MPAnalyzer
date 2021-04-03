package yoshikihigo.cpanalyzer;

import java.util.Arrays;
import java.util.function.Consumer;

public class JarEntryPoint {

  public static void main(final String[] args) {

    if (0 == args.length) {
      printUsage();
      System.exit(0);
    }

    final String[] realArgs = Arrays.copyOfRange(args, 1, args.length);
    final Consumer<String[]> mainMethod;
    switch (args[0]) {
      case "changes":
        mainMethod = ChangeExtractor::main;
        break;
      case "patterns":
        mainMethod = ChangePatternMaker::main;
        break;
      case "bugfixes":
        mainMethod = BugFixAllMaker::main;
        break;
      default:
        System.err.println("invalid command:" + args[0]);
        mainMethod = null;
        printUsage();
        System.exit(0);
    }

    mainMethod.accept(realArgs);
  }

  private static void printUsage() {
    System.err.println("One the following names must be specified as the first argument.");
    System.err.println(" changes: to extract changes from a repository");
    System.err.println(" patterns: to make change patterns from the extracted changes");
    System.err.println(" bugfixs: to identify bugfix-related change patterns");    
  }
}
