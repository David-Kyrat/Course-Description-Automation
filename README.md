<div align="center">

# Unige - 12X015 | Course Description Automator

Application to automatically generate printable 1-2 page PDF of course descriptions.

Made with:

<a href="https://unige.ch/">![unige](https://tinyurl.com/unige-logo)</a>
![Rust](https://img.shields.io/badge/Rust-000000?style=for-the-badge&logo=rust&logoColor=white)
![Scala](https://img.shields.io/badge/Scala-DC322F?style=for-the-badge&logo=scala&logoColor=white)
![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Neovim](https://img.shields.io/badge/NeoVim-%2357A143.svg?&style=for-the-badge&logo=neovim&logoColor=white)

<!-- ![IntelliJ IDEA](https://img.shields.io/badge/IntelliJIDEA-000000.svg?style=for-the-badge&logo=intellij-idea&logoColor=white) -->

![GitHub commits](https://img.shields.io/github/commits-since/David-Kyrat/Course-Description-Automation/e5da194?label=Commits&logo=github&style=flat)
![GitHub last commit](https://img.shields.io/github/last-commit/David-Kyrat/Course-Description-Automation?style=flat&logo=time)
![GitHub repo size](https://img.shields.io/github/repo-size/David-Kyrat/Course-Description-Automation?color=blueviolet&style=flat)
![wakatime](https://wakatime.com/badge/user/4c0f5fbb-26be-4446-be74-49aa7d6a693c/project/324a5a77-93f1-4e9f-8d7f-0dbb577686ce.svg)


---


Example given:


<p align="center">
<img style="display: block; margin: 0 auto; margin-bottom: 3em" src="files/res/readme-example2.png" width=650em>
</p>

<br/>
<div style=" margin: 0 auto; width: 70%; " align="center">

|                                      Client                                      |                 Student / Developer                 |                                    Supervisor                                     |
|:--------------------------------------------------------------------------------:|:---------------------------------------------------:|:---------------------------------------------------------------------------------:|
| [Anne-Isabelle Giuntini](https://www.unige.ch/dinfo/contacts/contacts-francais/) | [Noah Munz](https://www.linkedin.com/in/noah-munz/) | [Dr. Guillaume Chanel](https://www.unige.ch/cisa/center/members/chanel-guillaume) |

</div>
<br>

</div>


## Table of Contents

<!-- vim-markdown-toc GFM -->

* [Quick Intro](#quick-intro)
* [Meeting Reports](#meeting-reports)
* [Documentation](#documentation)
* [Building From Source](#building-from-source)
    * [Tl;Dr](#tldr)
    * [More infos about build](#more-infos-about-build)
        * [Dependencies](#dependencies)
            * [JavaFx Gui Part](#javafx-gui-part)
            * [Scala](#scala)
            * [Rust](#rust)
        * [Actual Building](#actual-building)
            * [Scala Part](#scala-part)
        * [Rust Part](#rust-part)
    * [End Packaged Structure](#end-packaged-structure)

<!-- vim-markdown-toc -->

***

## Quick Intro

This project was made using 

1. a [JavaFx](https://openjfx.io/) JavaFx GUI using mainly the framework/library [Jfoenix](https://mvnrepository.com/artifact/org.rationalityfrontline.workaround/jfoenix/19.0.1)
2. a [Scala](https://www.scala-lang.org/) application to fetch data from the unige database
and generate markdown containing that data that each pdf will hold
3. a [Rust](https://www.rust-lang.org/) part to convert those markdown files to pdfs with pandoc 
and to link everything as well as act as a native launcher.  
The rust launcher also handles error handling displaying windowed pop-up for the user...

An image is worth a 1000 words so here is the use case diagram:

![use case diagram](./report-diagrams/svg/use-case-diagram.svg)

<h3>NB: The scala part is available in the <a href="https://github.com/David-Kyrat/Course-Description-Automation/tree/scalight">scala</a> branch</h3>


## Meeting Reports

Meeting reports are available __[here](https://github.com/David-Kyrat/Course-Description-Automation/tree/master/PV)__.
They are numbered and dated and contains a quick summary
of what was discussed during the meeting as well as a list of all the participants.

## Documentation

The file __[howto.md](https://github.com/David-Kyrat/Course-Description-Automation/blob/master/howto.md)__
contains a quick summary of which/how technology has been used as well as some brief commands.

Its goal is not to be documentation on how to use the project but rather
to categorize the "major steps" this project went through and to give some information on how to reproduce some of
these steps.

"Published" documentation is available here: __[Scala-Documentation](https://raw.githack.com/David-Kyrat/Course-Description-Automation-Docs/master/ch/index.html)__


-----

<br/>

## Building From Source

### Tl;Dr

If you just want to know which command to run
and look at the details after / only if something 
goes wrong, just do this. 

Download [wkhtmltopdf](https://wkhtmltopdf.org/downloads.html), [sbt](https://www.scala-sbt.org/) and [cargo](https://www.rust-lang.org/tools/install). Then, we only have 2 commands.

- `sbt assembly`
- `cargo build --release`.

That's all.You just have to put the jar produced by sbt in files/res/java and and the executable generated by cargo should work. (Once you placed it at the root of the repo)

*From clone to executable:*

```sh
git clone -b scalight https://github.com/David-Kyrat/Course-Description-Automation.git
cd Course-Description-Automation
sbt                         # (sbt shell will popup, then enter:)
    clean; reload; assembly
    exit

cp target/scala-2.13/course-description-automation.jar .. # copy the jar outside

# Now build rust
git checkout master
# put jar produced by sbt in java folder
mv ../course-description-automation.jar  files/res/java  
cd src/main/rust
cargo build --release
# put the executable at the root of the repo
cp target/release/Course-Description-Automation.exe ../../../ 
```

Now while this may look big and tedious, 70% of what's done here
is just cloning and copying/moving a built artifact. 
The only Interaction we've had with the build tools were, in fact, just `sbt assembly` and  
`cargo build --release`.

***

### More infos about build

#### Dependencies

- **[wkhtmltopdf](https://wkhtmltopdf.org/)** (pandoc dependency)

`wkhtmltopdf` must be findable by pandoc (i.e. in `$PATH`) for this to work.


##### JavaFx Gui Part

Building the gui is currently not supported. Please use the following to launch it.
(depends on)
- core-[version].jar
- fancyform.jar
- javafx-swt.jar
- jfoenix-[version].jar
- jvm.driver-[version].jar

command:

`java --module-path <javaFx/lib directory>  --add-modules javafx.controls,javafx.fxml,javafx.graphics -jar fancyform.jar <path to abbrev.tsv file (usually /files/res/abbrev.tsv)>`

e.g. (in `/files/res/java`)

`jdk-17/bin/java --module-path javafx-sdk-17/lib --add-modules javafx.controls,javafx.fxml,javafx.graphics -jar fancyform.jar ../abbrev.tsv`


**NB:** On windows use *javafx-sdk-**19*** instead of 17. (17 is the mac version)


NB: If you want to make your own, it is very easy to integrate it with the actual project.  
Everything the JavafxGui is sending 1 string as argument to the scala application.

the string is of the form `course_id1,course_id2,...#student_plan_id1,student_plan_id2...`.  
The `#` is not optional.  
i.e. `course_id1#` or `#student_plan_id1` is a valid input, but not `course_id1` or `student_plan_id1`.



##### Scala

- **Scala build tool:** [sbt](https://www.scala-sbt.org/)

Others are directly managed via sbt. (configured via `build.sbt`)  

(We have the *scala lang base* module, *scala lang parallel collections* module and *Gson*, the google json library for java)


##### Rust

- **Rust build tool:** [Cargo](https://doc.rust-lang.org/cargo/)
installable via [https://www.rust-lang.org/tools/install](https://www.rust-lang.org/tools/install)


***

#### Actual Building

##### Scala Part

To build the scala part go to the `scalight` branch that contains only scala related source & resource. 
(We will see later why this is needed / important)  
and simply run `sbt` at the root of the project the sbt shell should pop up.

Once it has, enter the command `clean; reload; assembly` like so:

```scala
sbt:Course-Description-Automation> clean; reload; assembly
```

(if you haven't modified `build.sbt`
you can just enter `assembly`).

`assembly` will compile & package everything in the `src` directory as long as the library to make one "fat-jar" located at `target/scala-2.13/course-description-automation.jar`.

(If we had our rust code in this branch, then sbt would've added the entiere rust project with it as well as some uneeded resources present only in `master`).

#### Rust Part

Like for sbt this is pretty straightforward.

Go to `src/main/rust` and enter `cargo build --release` like so:

```sh
.../Course-Description-Automation/src/main/rust $:  cargo build --release
```

the compiled binary should be at `src/main/rust/target/release/Course-Description-Automation`


***

### End Packaged Structure

Now that you've build the project, it should have the following structure to work.


    ./
    ├── Course-Description-Automation (executable)
    │ 
    ├── files/
    │   └── res/
    │       └── bin-converters/
    │           ├── pandoc
    │           ├── java/
    │           │   ├── javafx-sdk-17
    │           │   ├── jdk17
    │           │   ├── core-[version].jar
    │           │   ├── fancyform.jar
    │           │   ├── javafx-swt.jar
    │           │   ├── jfoenix-[version].jar
    │           │   ├── jvm.driver-[version].jar
    │           │   │ 
    │           │   └── course-description-automation.jar
    │           │
    │           ├── log/ (optional will get generated at runtime)
    │           ├── md/
    │           ├── pdf/
    │           ├── templates/
    │           │   ├── course-desc.css
    │           │   ├── template.html
    │           │   └── unige.png
    │           ├── abbrev.tsv
    │           └── logging_config.yaml
    │ 
    └── pdfs/ --> ./files/res/pdf/  (shortcut)
    │ 
    └── LICENSE


You can get the `files` directory from a release on the release page. Its the same one.  
The symlink to `./files/res/pdf` is not mandatory but its convenient for the user.


<br/>

<!-- #### Supported Platforms

- ![windows](https://img.shields.io/badge/Windows-0078D6?style=for-the-badge&logo=windows&logoColor=white) -->











<!-- 
https://img.shields.io/badge/UNIGE-e11b67?logo=data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHhtbDpzcGFjZT0icHJlc2VydmUiIHZlcnNpb249IjEuMCIgdmlld0JveD0iMCAwIDE4NiAyMDAiPjxwYXRoIGQ9Im00NC4zIDQ1IDQuMyA1LjYgMy4yLTIuNS03LTkuMS0yLjYgMS45YzEgMS41LS40IDMuNC0yLjEgNC43bDEuMiAxLjcgMy0yLjNNNTUuOSAzOS43Yy4yLTEgLjgtMS41IDEuOC0yIDEuNC0uNyAyLjUtLjQgMyAuNi42IDEtLjEgMi4yLTEuNCAyLjktMSAuNS0yLjUuNi0zLS4yTDUzIDQyLjhjMS40IDIuMyA0LjMgMiA3LjMuNCAzLjMtMS43IDUtNC4yIDMuOC02LjctMS4xLTIuMi0zLjYtMi41LTYuNC0xLTEgLjUtMiAxLjMtMi40IDIuMWwtLjQtMiA2LjEtMy4zLTEtMi04LjkgNC40IDEuNyA2LjYgMy0xLjZNMTI5LjUgMzYuMWMtMS0uNS0yLjEtLjgtMy0uNmgtLjFsMS40LTEuN0wxMzQgMzdsMS4xLTIuMi04LjgtNC40LTQuMyA1LjEgMyAxLjZjMS0uNCAxLjctLjIgMi43LjMgMS40LjcgMS44IDEuOCAxLjMgMi44LS41IDEtMS44IDEuMi0zLjEuNS0xLS41LTItMS42LTEuNi0yLjRsLTMuNC0xLjdjLTEgMi42LjkgNC42IDQgNi4yIDMuMiAxLjYgNi4zIDEuNiA3LjYtMSAxLTIuMS0uMS00LjItMy01LjdNMTQzLjIgNDUuNWMtLjcuOC0yIC45LTMuMy0uMS0xLjItMS0xLjQtMi4yLS44LTMgLjYtLjkgMi0uOSAzLjEgMCAxLjMgMSAxLjYgMi4zIDEgM3ptLjQtNWMtMi42LTItNS44LTIuMy03LjMtLjMtMS40IDEuOC0uOCA0LjQgMS43IDYuMyAxIC44IDIuNCAxLjQgMy41IDEuMi0xIDEuMS0yLjQgMS44LTQgLjUtLjQtLjItLjctLjYtMS0xLS4xLS40LS4yLS44IDAtMS4xbC0yLjktMi4yYy0uOSAyIC4yIDQuMSAyLjQgNS44IDQgMy4xIDcuMiAyIDkuMi0uNyAyLTIuNCAyLTUuNy0xLjYtOC41TTE1LjUgODguMWMyIC43IDMuNS0xLjMgNC43LTMuMyAxLjItMi4xIDItNC4yIDMtNCAuNy4zLjUgMS40LjMgMi4zLS40IDEuMi0xLjIgMi40LTIuMiAyTDIwIDg5YzIuNS44IDQuNi0yIDUuNi01IDEtMi44IDEuMS02LjUtMS4zLTcuMy0yLjEtLjctMy42IDEuNC00LjggMy40LTEuMiAyLTIgNC4xLTMgMy44LS45LS4zLS44LTEuNi0uNC0yLjguNS0xLjcgMS4zLTIuNiAyLjctMi4zbDEuMy00Yy0yLjktLjktNSAxLjgtNi4yIDUuMy0xLjEgMy40LTEuMyA3IDEuNiA3LjlNMTQuMiAxMDhsLjMtNGMtMS40LS4zLTIuMS0xLjUtMi0zLjUuMy0yLjcgMi0zLjYgMy44LTMuNCAxLjguMSAzLjUgMS4zIDMuMyA0LS4yIDEuNy0uOSAzLTIuMSAzbC0uMyA0LjJjMi44LjEgNC44LTMuNSA1LTcgLjQtNC44LTIuMS04LjEtNS41LTguNC0zLjUtLjItNi40IDIuNy02LjggNy41LS4zIDQgMS4yIDcuMiA0LjMgNy43TTIzLjMgMTIxLjVsLTQuMy43LS45LTUuNCA0LjMtLjctLjctNC4xLTExLjcgMiAuNyA0IDQuOC0uNyAxIDUuNC00LjkuOC43IDQuMSAxMS43LTItLjctNC4xTTI0LjEgMTQxLjZjLTEuNi43LTMuNi41LTQuNy0yLTEuMS0yLjQgMC00IDEuNy00LjggMS42LS43IDMuNi0uNSA0LjcgMiAxLjEgMi40IDAgNC0xLjcgNC44em00LjItNmMtMi4xLTQuNS01LjgtNi05LTQuNi0zIDEuNC00LjQgNS4xLTIuMyA5LjcgMiA0LjYgNS43IDYuMSA4LjggNC43IDMuMi0xLjQgNC41LTUuMSAyLjUtOS43TTM1LjYgMTQ4LjhsLTIuNS0zLjMtOS41IDcuMiA3LjEgOS4zIDIuMS0xLjctNC41LTYgNy4zLTUuNU00My4zIDE2Ny42bC0yLjgtMi40IDQuMS0ydi4xbC0xLjMgNC4zem0tMTAuNC0zIDMuMiAyLjcgMi4yLTEgNC4yIDMuNy0uNiAyLjMgMy4zIDIuOCAzLjItMTMtMy4yLTIuNy0xMi4zIDUuMU03OC4zIDE3OWMxLjYuMyAyLjggMS4xIDIuNyAyLjJsNC4xLjhjLjMtMy0zLTUtNi4yLTUuNy00LjctMS04LjQgMS05IDQuNS0uOCAzLjMgMS43IDYuNyA2LjUgNy42IDEuNC4zIDMuNC4zIDQuNy0uNnYxLjRsMi42LjUgMS4zLTYuNC02LjYtMS4zLS40IDIuMiAyLjguNmMtLjQgMS0yIDEuNC00IDEtMi42LS41LTMuMi0yLjUtMi45LTQuMi40LTEuNyAxLjctMy4yIDQuNC0yLjdNOTUuNSAxODcuM2wtLjEtMi4zIDcuOC0uNHYtMi40bC04IC40di0ybDguNS0uNXYtMi41bC0xMi44LjcuNiAxMS44IDEzLS42LS4yLTIuNy04LjguNU0xMTcuNSAxNzRsMi4yIDctNy41LTUuNC00LjEgMS4zIDMuNSAxMS4zIDMuOC0xLjEtMi4yLTcuMSA3LjUgNS40IDQuMS0xLjMtMy41LTExLjMtMy44IDEuMU0xMzQgMTc2LjNsLTEuMS0xLjkgNi42LTQuMS0xLjItMi02LjcgNC0xLjEtMS43IDcuMy00LjUtMS4zLTIuMS0xMC45IDYuNyA2LjMgMTAuMSAxMS02LjgtMS4zLTIuMy03LjYgNC42TTE0Ni41IDE1NC42bDQgOC41LTguNS00LTMgMy4xIDEyIDQuOCAzLjMtMy40LTQuOC0xMi0zIDNNMTYxLjUgMTUwbC0yLTEuMSA0LjEtNi43LTItMS4zLTQuMSA2LjctMS44LTEgNC41LTcuNC0yLjEtMS4zLTYuNyAxMSAxMC4yIDYuMSA2LjctMTEuMS0yLjItMS40LTQuNiA3LjZNMTYyLjggMTI0LjNsNyAyLjEtOC42IDMuMy0xLjIgNCAxMS4zIDMuNSAxLjItMy44LTcuMS0yLjEgOC43LTMuMiAxLjItNC4xLTExLjMtMy41LTEuMiAzLjhNMTczLjQgMTAzYy0yLjIgMC0zIDIuMy0zLjcgNC41LS41IDIuNC0uOCA0LjctMS43IDQuNi0uOCAwLTEtMS4xLTEtMiAuMS0xLjMuNS0yLjcgMS42LTIuNmwuMi00LjFjLTIuNy0uMS00IDMtNC4xIDYuMi0uMiAzIC43IDYuNiAzLjMgNi43IDIuMi4xIDMtMi4zIDMuNi00LjUuNi0yLjMgMS00LjYgMi00LjYuOCAwIDEuMSAxLjQgMSAyLjcgMCAxLjctLjUgMi45LTIgMi45bC0uMSA0LjJjMyAwIDQuNC0zLjEgNC41LTYuOC4yLTMuNS0uNi03LTMuNi03LjJNMTY0LjYgOTguMmwxMS43LTEuNy0uNi00LjFMMTY0IDk0bC42IDQuMU0xNjYuNCA4OC4zYzItLjcgMi0zLjMgMS44LTUuNi0uMy0yLjMtLjktNC42IDAtNSAxLS4yIDEuNyAxIDIgMi4xLjYgMS43LjYgMy0uNyAzLjVsMS40IDRjMi44LTEgMy00LjUgMS43LTgtMS4xLTMuNC0zLjItNi4zLTYtNS4zLTIgLjctMiAzLjEtMS43IDUuNS4zIDIuMy45IDQuNiAwIDQuOS0uOC4yLTEuMy0uOC0xLjYtMS42LS40LTEuMi0uNi0yLjYuNS0zbC0xLjQtMy45Yy0yLjUgMS0yLjUgNC4zLTEuNSA3LjQgMSAyLjggMyA1LjggNS41IDVNNTUuOSAxNzVhMi44IDIuOCAwIDEgMCA0LjUgMy40IDIuOCAyLjggMCAwIDAtNC41LTMuNU0zMi43IDYxLjhhMi44IDIuOCAwIDEgMC00LjYtMy40IDIuOCAyLjggMCAwIDAgNC42IDMuNE0xNTMuMiA1OC40YTIuOCAyLjggMCAxIDAgNC42IDMuNCAyLjggMi44IDAgMCAwLTQuNi0zLjRNMTAwLjIgMjcuNWwtMi4zIDMuNmg1LjZ2LTEuN2gtMi4ybDEuMi0xLjktMS4yLTEuOGgyLjJ2LTEuOGgtNS42bDIuMyAzLjZNODggMjkuNGgtMS40di0zLjhIODhWMjRoLTQuNnYxLjhoMS4zdjMuN2gtMS40VjMxSDg4di0xLjdNOTQuNiAyNi41aC0zdi0yLjZoLTJWMzFoMnYtMi45aDN2M2gyLjF2LTcuM2gtMi4xdjIuNk05Mi4yIDE5LjZjMC0uNS40LS45IDEtLjkuNSAwIC45LjQuOSAxdjIuNWg0VjIwaC0yLjV2LS41YzAtMS42LS43LTIuOC0yLjQtMi44LTEuNCAwLTIuMy44LTIuNSAyVjIwaC0yLjV2Mi4yaDR2LTIuNk05My4yIDMyLjhhMS4zIDEuMyAwIDEgMCAwIDIuNiAxLjMgMS4zIDAgMCAwIDAtMi42IiBzdHlsZT0iZmlsbDojOWM5YTliO2ZpbGwtb3BhY2l0eToxO2ZpbGwtcnVsZTpub256ZXJvO3N0cm9rZTpub25lIi8+PHBhdGggZD0iTTkzIDE5My45QTg3LjYgODcuNiAwIDAgMSA3Ni43IDIwLjVMNzggMjJsLTEwLjUgNEw3OCAzMGwtNyA4LjggMTEtMS44LS4zIDIuM0E2OC4yIDY4LjIgMCAwIDAgOTMgMTc0LjZhNjguMiA2OC4yIDAgMCAwIDExLjUtMTM1LjNsLS4zLTIuMiAxMC44IDEuNy02LjgtOC44IDEwLjItNC0xMC4zLTQuMiAxLTEuM0E4Ny42IDg3LjYgMCAwIDEgOTMgMTkzLjl6TTgwLjIgNTYuNWwxLjUtMi43aC02LjVMNzYgNTFhMi40IDIuNCAwIDAgMC0zLjMtMi4ydi0uN2ExLjYgMS42IDAgMSAwLTMuMSAwdi43bC0uOS0uMmMtMS4zIDAtMi40IDEuMS0yLjQgMi40bC44IDIuOGgtMS44Yy0xLjEgMC0yLjEuOS0yLjMgMmwtMS4yLS4yYTQgNCAwIDAgMC0zLjUgNS45IDQgNCAwIDAgMSA3IDBjLjMuNC40LjguNSAxLjJoLTguMWE0IDQgMCAwIDEtNC00djIuNmE0IDQgMCAwIDAgNCA0aDcuNmE0IDQgMCAwIDEtMy41IDJoLS4yYTQgNCAwIDAgMCA3LS44aDEwLjhhNCA0IDAgMCAxIDQgNGMwIDEgMCAyLjUtMSAzLjZsLTEgMS41TDc0LjYgODZhNCA0IDAgMCAxLTUuNSAxIDQgNCAwIDAgMS0xLjEtNS42YzEuNi0uOSAyLjItMi43IDItNC42LS41LTIuNC0yLjYtNC01LTMuN2gtMS4zYy04LjUgMC0xNC45LTUuOC0xNS44LTE0IDktOC42IDIwLjUtMTQuNiAzMy4yLTE3bC0xIDYgOC43LTcuMSAzIDcuNXYxMS42bC0xMS42LTMuN3ptLTM1IDkyLjhjMC0uNC4yLS43LjYtLjlsOC4zLTQuNyAyLjMgMi40LjYuNi40LjQgMyAyLjdhMS40IDEuNCAwIDAgMS0xIDIuNGgtMS43bDIuMiAyLjFoLjJhNS41IDUuNSAwIDAgMCA1LjYtNS41YzAtMy0yLjMtNS40LTUuMi01LjVsLTItMmMxLjItMSAyNC40LTE1LjYgMjYuNS0xNy0uNyA4LjEtNy43IDE3LjYtMTQuMyAyMy4zLTMgMy00LjEgNi40LTQuMSA5LjMgMCAyLjYuOSA0LjggMi4yIDYuMWE0IDQgMCAwIDAgNC40LjlsLjktLjQtLjctLjdjLTEuMi0xLjItMS42LTMuMi0xLjYtNC44YTggOCAwIDAgMSAyLjMtNi4xbDEuOCAxLjhjLS42IDEuNy0uOSAzLjQtLjkgNSAwIDMuMyAxLjIgNi4yIDMgOC4xLjUuNCA0LjcgNCA2LjUgMy4zbDEuMi0uNC0xLS44Yy0uNy0uNC0yLjctMi44LTMuMy00LjJhOS4yIDkuMiAwIDAgMS0xLTMuOWMwLTIuMyAxLTUgMy4xLTdsNy4xLTE3LjFhMjUxLjUgMjUxLjUgMCAwIDEgMCA0LjljMCA2LjYtLjQgMTIuNS0zLjMgMTUuNC0xLjggMS44LTUuMiAyLjgtNS4yIDIuOGwxIDFzMS40IDIuMiAzLjcgMi4zYy41IDAgMS0uMyAxLjQtLjUuMSA1LjEgMS4xIDcuMiAyLjggOS4xYTY1LjEgNjUuMSAwIDAgMS00NC41LTE5LjRsLTEuMi0yYTEuNCAxLjQgMCAwIDEtLjItMXptLTUuMS01LjhjMC0uNC4xLS45LjQtMS4yLjQtLjMuOC0uNSAxLjMtLjVoNGE3IDcgMCAwIDAtNC43IDMuNmMtLjUtLjctMS0xLjUtMS0xLjl6bS03LjYtNTEuNiAxNy0zLjF2MS40bC41LjgtMTYuNiA2LjRhNC42IDQuNiAwIDAgMC0xLjYgNi41IDQuNiA0LjYgMCAwIDAgNi4zIDEuNWwxNC42LTkgLjYgMS4yLjguNi0xMy4yIDEyYTQuNyA0LjcgMCAwIDAgLjggNi42YzIgMS41IDUgMS4xIDYuNS0uOUw1OC43IDEwMmMuMi40LjUuOCAxIDFsLjYuMmguMkw1My4yIDExOGMtLjcgMi41LjYgNSAzIDUuNyAyLjQuNyA0LjktLjcgNS42LTNsNC4yLTE0LjhhMTkuOSAxOS45IDAgMCAwIDkuOS4zdjUuNWwtMTMuNCAxNC41Yy0yLjMgMS45LTMuMiAyLTQuNy41bC0uNC0uNC00IDMuMi41LjVjMS41IDEuNiAzIDIuNiA1IDIuOGwtNS44IDMtMy42LTMuOGE1LjUgNS41IDAgMCAwLTExIC4zdi4zbDIgMi4xVjEzM2MwLS4zLjItLjcuNS0xIC41LS41IDEuNC0uNSAyIDBsMi43IDMgLjQuNS41LjUgMS4yIDEuM2gtMS41YTUuNSA1LjUgMCAwIDAtNy42LjVjLS44LjgtMS4yIDEuNy0xLjQgMi44YTY1IDY1IDAgMCAxLTcuOC00OS40Yy44LjcgMS44IDEgMyAuN3ptNi4xLTIxLjZjMi0yLjkgNC01LjYgNi40LTguMSAxLjIgNC4xIDMuNyA3LjggNy4xIDEwLjUtLjQuMy0uOC43LTEgMS4yLS4yLjQtLjIuNy0uMiAxbC0xMi4zLTQuNnpNNDkuMyA4MGMtLjQuNS0uNiAxLS42IDEuNiAwIC41LjEuOC4zIDEuMkgzMmwyLjYtNS42TDQ5LjMgODB6bTQ5LjQgNDguNGMwLTIuMyAxLjctNC4zIDQtNC43LjEgMi44IDIuNSA1IDUuMyA1SDExOWMyLjkgMCA1LjItMi4yIDUuNC01IDIuMi40IDQgMi40IDQgNC43IDAgOS4yLTYuNCAxNC43LTE0LjggMTYuOC04LjUtMi0xNC44LTcuNi0xNC44LTE2Ljh6bTkuNyAyMWE1IDUgMCAwIDAgMS45IDkuOGg2LjNhNSA1IDAgMCAwIDEuOS05LjhjOC42LTMgMTQuOS0xMS4zIDE0LjktMjEgMC01LTMuNy05LTguNi05LjhWMTE1bC03LjYtN1Y5My40aDQuNHY2LjFoNy40Vjk1aDUuNXY0LjZoNy40VjgzLjFoLTcuNHY0SDEyOXYtNEgxMjN2LTVoNi4xdi00aDUuNXY0aDcuNFY2M2E2NS40IDY1LjQgMCAwIDEtNDcuMyAxMDguNlYxMzVjMiA2LjcgNy4yIDEyIDEzLjggMTQuNHpNMTA1IDQyLjJhNjUuMyA2NS4zIDAgMCAxIDM1LjUgMTkuNGgtNlY2NkgxMjl2LTQuNGgtNy40djYuMWgtNC40di05LjRhMy44IDMuOCAwIDAgMC03LjYgMHY0OS42bC03LjUgN3YzLjdhMTAgMTAgMCAwIDAtNy42IDUuNlY0OC41bDIuNi03LjQgOC42IDctLjgtNS45ek05MyAxMy40YTEyLjggMTIuOCAwIDEgMSAwIDI1LjUgMTIuOCAxMi44IDAgMCAxIDAtMjUuNXptMjAgMi4yIDEuOS0yLjMtMTEgMS42IDEuNy0xMS04LjggNy4yTDkzIC42IDg5IDExIDgwLjIgNCA4MiAxNWwtMTEuMS0xLjcgMS45IDIuNEE5My4xIDkzLjEgMCAxIDAgMTg2IDEwNi41YTkzLjEgOTMuMSAwIDAgMC03My05MC45IiBzdHlsZT0iZmlsbDojOWM5YTliO2ZpbGwtb3BhY2l0eToxO2ZpbGwtcnVsZTpub256ZXJvO3N0cm9rZTpub25lIi8+PC9zdmc+&style=for-the-badge
-->
