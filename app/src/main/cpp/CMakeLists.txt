cmake_minimum_required(VERSION 3.10.2)
project("core_runtime")

# Create shared library named core_runtime
add_library(
        core_runtime
        SHARED
        keys.cpp
)

# Android log library
find_library(log-lib log)

target_link_libraries(
        core_runtime
        ${log-lib}
)

# Hide symbols in release
set_target_properties(core_runtime PROPERTIES
        C_VISIBILITY_PRESET hidden
        CXX_VISIBILITY_PRESET hidden
        VISIBILITY_INLINES_HIDDEN 1
)
