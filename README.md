<div align="center">

# ImGui Java Bindings

[SWIG](https://github.com/swig/swig) generated [Dear ImGui](https://github.com/ocornut/imgui) bindings for Java.

[![Build and Release (SWIG JNI)](https://github.com/KartoffelL/imgui-java-bindings/actions/workflows/generate-and-build.yml/badge.svg)](https://github.com/KartoffelL/imgui-java-bindings/actions/workflows/generate-and-build.yml)
![License](https://img.shields.io/github/license/KartoffelL/imgui-java-bindings)
![Release](https://img.shields.io/github/v/release/KartoffelL/imgui-java-bindings/imgui-java-bindings)

</div>

## Header
imgui-java-bindings is an automatically generated library (plus some handwritten code) that enables the usage of Dear ImGui inside java.
This project originated to work as an alternative to [ImGui Java](https://github.com/SpaiR/imgui-java), but being a bit simpler and smaller in size.
As of such, the library is split into two parts: the generated part located in the imguijb package, and an edited version of [ImGui Java](https://github.com/SpaiR/imgui-java)'s fantastic
imgui.app to work as a simple framework.

Everything's still work in progress, so production-usage can't be advised, but to a certain degree, the library should be usable.

## Body
### Notes
* The Library is *SWIG* Based and uses *JNI* as the binding technology
* For full usage of the library, LWJGL is required (generally the latest is tested)
* The Library, as of now, makes use of Java features up until JDK 25
* This Repo only compiles for windows x64 and linux x64
### Usage
  The Release Section contains a cross-platform jar for the supported versions. Just include it like any other jar. Artifacts are not yet published to maven.
### Maintaining
This repository is meant to be rather easily maint- and forkable. to build, the generate-and-build.yml workflow is triggered which generates the Java API from the header files using bindings.i, then static binaries are build for all platforms containing
the implementations and finally everything is bundled (and released). 
The dependencies folder contains some compile-time dependencies of the library (manually added), The include folder contains all submodules and the build.cpp.
The build.cpp is compiled into the native library and should include all native code. 
Some notes:
* *To update to a newer version of ImGui*<br>
  update the affected submodules and adjust bindings.i (and perhaps the handwritten api)
* *To add additional libraries (eg. ImGui extensions)*<br>
  add a submodule to include/, reference it's header files inside bindings.i and the implementation files inside build.cpp
  
