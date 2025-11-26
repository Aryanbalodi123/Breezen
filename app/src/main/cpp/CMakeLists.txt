cmake_minimum_required(VERSION 3.10.2)
project("secrets_level3")


set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -Wl,-z,max-page-size=16384")
add_library(secrets SHARED keys.cpp)

find_library(log-lib log)

target_link_libraries(secrets ${log-lib})

# release: strip symbols and optimize
set_target_properties(secrets PROPERTIES
        C_VISIBILITY_PRESET hidden
        CXX_VISIBILITY_PRESET hidden
        VISIBILITY_INLINES_HIDDEN 1
)

# make sure to strip debug info in release (strip will be done by build system/gradle strip tool)
