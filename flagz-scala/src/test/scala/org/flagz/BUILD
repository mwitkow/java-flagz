package(default_visibility = ["//visibility:public"])

load("@io_bazel_rules_scala//scala:scala.bzl", "scala_library", "scala_binary", "scala_test")


scala_test(
    name = "flagz",
    srcs = glob(["*.scala"]) + glob(["*.java"]),
    deps = [
        "//flagz-scala/src/main/scala/org/flagz",
        "//flagz-scala/src/test/scala/org/flagz/testclasses",
        "//flagz-java/src/main/java/org/flagz",
        "//third_party/etcd4j",
        "//third_party/guava",
        "//third_party/slf4j:api",
    ],
)