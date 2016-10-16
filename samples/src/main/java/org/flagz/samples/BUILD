package(default_visibility = ["//visibility:public"])

java_binary(
    name = "jmx",
    srcs = ["JmxSampleApp.java"],
    main_class = "org.flagz.samples.JmxSampleApp",
    deps = [
        "//flagz-java/src/main/java/org/flagz",
        "//third_party/guava",
        "//third_party/slf4j:api",
        "//third_party/slf4j:simple-stdout",
    ],
)

java_binary(
    name = "etcd",
    srcs = ["EtcdSampleApp.java"],
    main_class = "org.flagz.samples.EtcdSampleApp",
    deps = [
        "//flagz-java/src/main/java/org/flagz",
        "//flagz-etcd/src/main/java/org/flagz",
        "//third_party/etcd4j",
        "//third_party/guava",
        "//third_party/slf4j:api",
        "//third_party/slf4j:simple-stdout",
    ],
)