load("@rules_kotlin//kotlin:core.bzl", "kt_compiler_plugin")
load("//rules/kotlin:macros.bzl", "kt_library")

kt_compiler_plugin(
    name = "open_for_testing_plugin",
    compile_phase = True,
    id = "org.jetbrains.kotlin.allopen",
    options = {
        "annotation": "plugin.allopen.OpenForTesting",
    },
    stubs_phase = True,
    deps = [
        "@rules_kotlin//kotlin/compiler:allopen-compiler-plugin",
    ],
)

kt_library(
    name = "open_for_testing",
    srcs = ["OpenForTesting.kt"],
)

kt_library(
    name = "user",
    srcs = ["User.kt"],
    plugins = [
        ":open_for_testing_plugin",
    ],
    deps = [
        ":open_for_testing",
    ],
)

kt_library(
    name = "user_is_open_test",
    srcs = ["UserIsOpenTest.kt"],
    deps = [
        ":user",
    ],
)
