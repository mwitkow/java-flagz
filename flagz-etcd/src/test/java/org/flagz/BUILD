java_test(
  name = "tests",
  srcs = glob(['*.java']),
  size = "large",
  deps = [
    "//flagz-java/src/main/java/org/flagz",
    "//flagz-etcd/src/main/java/org/flagz",
    "//third_party/etcd4j",
    "//third_party/guava",
    "//third_party/slf4j:api",
    "//third_party/slf4j:simple-stdout",
    "//third_party/testing",
  ],
  tags=["exclusive"],
)
