package(default_visibility = ["//visibility:public"])

load("@io_bazel_rules_scala//scala:scala.bzl", "scala_library", "scala_binary", "scala_test")


scala_library(
    name = "testclasses",
    srcs = glob(["*.scala"]),
    deps = [
        "//flagz-java/src/main/java/org/flagz",
        "//flagz-scala/src/main/scala/org/flagz",
    ],
)