load("@rules_java//java:java_library.bzl", "java_library")

#
# This file is based on Bazel plugin for IntelliJ by The Bazel Authors, licensed under Apache-2.0;
# It was modified by JetBrains s.r.o. and contributors
#
load("//rules_intellij/intellij_platform_sdk:build_defs.bzl", "select_for_plugin_api")

java_library(
    name = "devkit_internal",
    visibility = ["//visibility:private"],
    exports = select_for_plugin_api({
        "intellij-2025.1": ["@devkit_2025_1//:devkit"],
        "intellij-2025.2": ["@devkit_2025_2//:devkit"],
        "default": [],
    }),
)

java_library(
    name = "devkit",
    neverlink = 1,
    visibility = ["//visibility:public"],
    exports = [":devkit_internal"],
)

java_library(
    name = "devkit_for_tests",
    testonly = 1,
    visibility = ["//plugin_dev:__pkg__"],
    exports = [":devkit_internal"],
)
