package org.flagz;

import com.google.common.collect.ImmutableList;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Contains a cache of Reflection results to speed up multiple reflection operations. */
class ReflectionsCache {

  static final Map<List<String>, Reflections> packagePrefixesToReflections = new HashMap<>();

  static synchronized Reflections reflectionsForPrefixes(List<String> prefixes) {
    if (packagePrefixesToReflections.containsKey(prefixes)) {
      return packagePrefixesToReflections.get(prefixes);
    }
    ConfigurationBuilder builder = new ConfigurationBuilder()
        .setUrls(urlsToReflect(prefixes))
        .setScanners(
            new FieldAnnotationsScanner(),
            new SubTypesScanner());
    Reflections reflections = new Reflections(builder);
    packagePrefixesToReflections.put(prefixes, reflections);
    return reflections;
  }

  /**
   * Get the appropriate set of URLs.
   *
   * This returns a set of URLs for a given set of package prefixes. You can use an empty string
   * to get everything but it can be slow for big apps.
   */
  private static Set<URL> urlsToReflect(List<String> packagePrefixes) {
    List<String> prefixesWithFlags = ImmutableList.<String>builder().addAll(packagePrefixes)
        .add("org.flagz").build();
    return prefixesWithFlags
        .stream()
        .flatMap(prefix -> ClasspathHelper.forPackage(prefix).stream())
        .collect(Collectors.toSet());
  }
}
