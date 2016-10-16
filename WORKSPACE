workspace(name = "org_flagz")

maven_jar(
  name = "org_reflections_artifact",
	artifact = "org.reflections:reflections::0.9.10",
)

maven_jar(
  name = "org_javassist_artifact",
	artifact = "org.javassist:javassist::3.20.0-GA",
)

maven_jar(
  name = "org_slf4j_api_artifact",
  artifact= "org.slf4j:slf4j-api:1.7.13",
)

maven_jar(
  name = "org_slf4j_simple_artifact",
  artifact = "org.slf4j:slf4j-simple:1.7.13",
)

maven_jar(
  name = "com_google_guava_artifact",
  artifact = "com.google.guava:guava:19.0",
)

maven_jar(
  name = "com_google_code_findbugs_jsr305_artifact",
  artifact = "com.google.code.findbugs:jsr305:1.3.9",
)

maven_jar(
  name = "junit_artifact",
  artifact = "junit:junit:4.12",
)

maven_jar(
  name = "hamcrest_artifact",
  artifact = "org.hamcrest:hamcrest-all:1.3",
)

maven_jar(
  name = "org_mockito_artifact",
  artifact = "org.mockito:mockito-all:1.10.19",
)

maven_jar(
  name = "org_mousio_etcd4j_artifact",
  artifact = "org.mousio:etcd4j:2.12.0",
)

maven_jar(
  name = "io_netty_artifact",
  artifact = "io.netty:netty-all:4.1.3.Final",
)


maven_jar(
  name = "com_fasterxml_jackson_annotations_artifact",
  artifact = "com.fasterxml.jackson.core:jackson-annotations:2.8.0",
)

maven_jar(
  name = "com_fasterxml_jackson_core_artifact",
  artifact = "com.fasterxml.jackson.core:jackson-core:2.8.0",
)

maven_jar(
  name = "com_fasterxml_jackson_databind_artifact",
  artifact = "com.fasterxml.jackson.core:jackson-databind:2.8.0",
)

maven_jar(
  name = "com_fasterxml_jackson_afterburner_artifact",
  artifact = "com.fasterxml.jackson.module:jackson-module-afterburner:2.8.0",
)


# Scala stuff.
git_repository(
    name = "io_bazel",
    remote = "git://github.com/bazelbuild/bazel.git",
    tag = "0.3.2",
)

git_repository(
    name = "io_bazel_rules_scala",
    remote = "https://github.com/bazelbuild/rules_scala.git",
    commit = "48977a511ab0f15af694a8bf5e0f85357f1d8ea6", # update this as needed
)

load("@io_bazel_rules_scala//scala:scala.bzl", "scala_repositories")
scala_repositories()