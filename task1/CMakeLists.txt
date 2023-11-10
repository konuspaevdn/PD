cmake_minimum_required(VERSION 3.25)
project(task1)

set(CMAKE_CXX_STANDARD 17)

find_package(MPI REQUIRED)

add_executable(task main.cpp)
target_link_libraries(MY_PROJECT PUBLIC MPI::MPI_CXX)
