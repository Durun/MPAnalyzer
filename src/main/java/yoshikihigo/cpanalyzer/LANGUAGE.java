package yoshikihigo.cpanalyzer;


import io.github.durun.nitron.core.config.loader.NitronConfigLoader;
import yoshikihigo.cpanalyzer.binding.nitron.NitronBindConfig;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO
public class LANGUAGE {
  // static fields
  private static final Map<String, LANGUAGE> languages = new HashMap<>();
  // instance fields
  final private String value;
  final private Collection<String> extensions;

  static { // init
    register("C", List.of(".c", ".h"));
    register("CPP", List.of(".cc", "cpp", "cxx", ".hh", "hpp", "hxx"));
    register("JAVA", List.of(".java"));
    register("PYTHON", List.of(".py"));
    NitronConfigLoader.INSTANCE
            .load(NitronBindConfig.configFile)
            .getLangConfig()
            .forEach((langName, langConfig) -> {
              register(langName, langConfig.getExtensions());
            });
  }

  private LANGUAGE(final String value, final Collection<String> extensions) {
    this.value = value;
    this.extensions = extensions;
  }

  // static methods
  public static LANGUAGE valueOf(final String key) {
    return languages.get(key);
  }

  public static Collection<LANGUAGE> values() {
    return languages.values();
  }

  public static void register(final String langName, final Collection<String> extensions) {
    final LANGUAGE newLanguage = new LANGUAGE(langName.toUpperCase(), extensions);
    register(newLanguage);
  }

  private static void register(final LANGUAGE newLanguage) {
    final String key = newLanguage.name();
    languages.put(key, newLanguage);  // languages can be overwritten
  }

  // instance methods
  public String name() {
    return this.value;
  }

  public boolean isTarget(final String fileName) {
    return this.extensions.stream().anyMatch(fileName::endsWith);
  }
}