<div align="center">
  <img src="logo.svg" width="500" alt="VRP on GPU" />
</div>

<!-- START badges-list -->
<div align="center">

![](https://img.shields.io/static/v1.svg?label=version&message=-.-.-&color=red&style=flat-square)
![](https://img.shields.io/static/v1.svg?label=release%20date&message=??-??-????&color=red&style=flat-square)
![](https://img.shields.io/static/v1.svg?label=license&message=TODO&color=green&style=flat-square)

![](https://img.shields.io/static/v1.svg?label=java&message=1.8&color=informational&style=flat-square)
![](https://img.shields.io/static/v1.svg?label=aparapi&message=1.7.0&color=informational&style=flat-square)

</div>
<!-- END badges-list -->

<div align="center">
<h4>VRP solver using the GPU</h4>
</div>

<div align="center">
[License](LICENSE) •
[Contributing](CONTRIBUTING.md) •
[Change log](CHANGELOG.md)

[Detailed usage](doc/usage.md) •
[FAQ](doc/faq.md)
</div>

## About

TODO: General description of the project

## Installation

This project requires a working installation of :

* a Java JDK

* OpenCL (no mandatory, unless you wish to use your GPU)

* [Apache Maven](http://maven.apache.org/)

## Get started

The project is compiled, tested and built using the regular maven commands in a
terminal located in the base directory of the project :

* `mvn compile` to compile the sources into the build directory ;

* `mvn test` to run the unit tests of the application ;

* `mvn package` to build an all-in-one jar containing the library ;

* `mvn package -Dmaven.test.skip=true` to build an all-in-one jar containing the library without running the unit tests.

The main class of the project is `fr.univ_artois.lgi2a.vrpgpu.Main`, and takes
as an argument the path of the file describing the VRP problem to solve.
Such files can be found in the `data/` directory.

Assuming that the all-in-one jar is built, then running the program is done using
a command like :

```
java -jar target/vrpgpu-0.1-SNAPSHOT-jar-with-dependencies.jar data/Solomon/50/c101.txt
```

## Supported versions

The project has been developped and checked in a MacOS environment.
It relies on the following programs :

| Dependency | Version |
|------------|---------|
| Java JDK   | 1.8     |
| OpenCL     |         |

It also uses libraries, which versions are described [in the network dependencies
page](https://github.com/sohaibafifi/vrp-gpu/network/dependencies).

__The project provides no warrancy on its behavior in other systems / version.__

## Acknowledgements

This tool is licensed under the [TODO license](LICENSE).

Registered contributors are:

* Sohaib LAFIFI - _<sohaib.lafifi@univ-artois.fr>_ - designer,developer
