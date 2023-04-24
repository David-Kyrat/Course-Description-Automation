# Table of Contents

<!-- vvim-markdown-toc GFM -->

* [Content](#content)
    * [Markdown Parsing and html / pdf generation](#markdown-parsing-and-html--pdf-generation)
        * [Parsing and Rendering Markdown](#parsing-and-rendering-markdown)
        * [Generating html with markdown based on pandoc templates](#generating-html-with-markdown-based-on-pandoc-templates)
* [DB access and Requests](#db-access-and-requests)
    * [Parsing](#parsing)
        * [Which field from the database is relevant ?](#which-field-from-the-database-is-relevant-)
            * [Course:](#course)
        * [Example of markdown structure used to generate  htmls and pdfs](#example-of-markdown-structure-used-to-generate--htmls-and-pdfs)
    * [Packaging](#packaging)
    * [Pandoc quick commands](#pandoc-quick-commands)

<!-- vim-markdown-toc -->
---


<br/>

# Content

## Markdown Parsing and html / pdf generation

### Parsing and Rendering Markdown

- mdgen.php is used to parse a markdown file in [php markdown extra](https://michelf.ca/projects/php-markdown/extra/)
  into html. it's used like so:
    1. First launch a server: `php -S localhost:8000 &`
    2. Go at `http://localhost:8000/output.html`
    3. Launch parser: `./mdgen.php input.md > output.html`
- vendor folder and composer.* files are required for php script to work

---

- R markdown parser is used like so: `R.exe -e "rmarkdown::render('$in')"`  (or use `knit()` function defined
  in `profile.ps1`)
- To convert a html generated output into pdf see `html_to_pdf`

---

### Generating html with markdown based on pandoc templates

- To fill the pandoc html template with the required info for a given course,
  fill an "empty" markdown file, with just the YAML header specifying
  each value to be filled. Make sure to exaclty match the parameter names
  setup in the html template.
  For an example / list of values, see the example in `example/desc-content-example.md`  
  Then call `pandoc <desc-content-file.md> -t html --template=<desc-template.html> -o <filled-course-desc.html>`
  Replace the name in `<>` by the actual path to the file.

---

NB: In the end php markdown extra, was not used and R markdown was
used only to generate the core/base (css) theme for the pandoc html template.
(It was inspired from the "downcute" theme available
here: [rmdformats](https://github.com/juba/rmdformats).)  
_Hence the only dependency for this part is pandoc._

<br/>

# DB access and Requests

**base url:** `https://pgc.unige.ch/main/api/`

Exemple:

    GET https://pgc.unige.ch/main/api/activities/languages  //  return available languages
    GET https://pgc.unige.ch/main/api/academical-years/2019 //  return details for the given academical-year
    GET https://pgc.unige.ch/main/api/teachings/2022-11X001 //  return details for given course at given year

## Parsing

### Which field from the database is relevant ?

Each JSON response is **significantly** big, there is **a lot** of data duplication.
E.g. if a course is given to several different section/departments then almost every information
about the teachers giving the course will appear at least more than 1 time.  
Which begs the question "which field is relevant?", which is what we will record here in
an approximate [YAML](https://en.wikipedia.org/wiki/YAML) format.

#### Course:

Request: `GET https://pgc.unige.ch/main/api/teachings/<courseYear>-<courseId>`

The necessary fields to fill the
class [Course.scala](https://github.com/David-Kyrat/Course-Description-Automation/blob/master/src/main/scala/ch/Course.scala)
are located as following:

```YAML

- academicalYear  # year
- code   # id
- activities:
    # lectures: 
    -  - title   # name 
       - duration   # hoursNb.lectures
       - periodicity   # Semester
       - objective     # objective of course is in the `objective` field of 1st element of list of activities (the lectures)
       - description   # idem as for objective
       - bibliography  # idem (documentation)
       - language
       - evaluation   # evalMode
       - intended   # study plan names
       - variousInformation   # check if all variousInfo contains the same category of info => they dont
       - comment       # idem
       - activityTeachers:
       - - displayFirstName  # n-th teacher first name
         - displayLstName    # n-th teacher last name

       - type  # presence of type = exercies indicate hoursNb.exercices > 0, idem for hoursNb.practice
    # exercices:    
    -  - duration # hoursNb.exercices 

    # pratice:    
    -  - duration # hoursNb.pratice (if present)

- facultyLabel   # faculty
- listStudyPlan:  # List of each study plan that has this course
    - studyPlanLabel
    - planCredits
    - facultyLabel

```

The information "mandatory/optional" course is given as follows:

- if `listStudyPlan[i].studyPlanLabel` contains "Cours à option" then it is not mandatory
- otherwise it is. (for each study plan `i`)

!! does not works all the time, e.g. Bachelor "Maths-Info" theres is more
than almost 30 courses in 3rd year and none of theme says "option" and
it is extremely unlikely that the bachelor actually contains > 30 courses for the 3rd year...


----

### Example of markdown structure used to generate  htmls and pdfs

```YAML
---
title: Biophotonics
author: Luigi Bonacina - 14P017
weekly_hours: 3
lectures_hours: 2
exercices_hours: 1
practice_hours: 0
total_hours: 42
course_lang: anglais
semester: Printemps
eval_mode: Examen oral
exa_session: Juillet
course_format: 
cursus:
  - {name: Maîtrise universitaire en biochimie (MSc biochimie), type: N/A, credits: 5}
  - {name: Baccalauréat universitaire en physique, type: N/A, credits: 5}
  - {name: Master in Physics with Specialization  120 ECTS, type: N/A, credits: 5}
  - {name: Master of Science in Biology 120 crédits, type: N/A, credits: 5}
  - {name: Baccalauréat universitaire en biologie, type: N/A, credits: 5}

objective:  |
            Biophotonics deals with interactions between light and biological matter. 
            The course is open to Physics and Biology students interested in the applications
            of state-of-the-art photonics to life sciences. [...]
description:  |
              La biophotonique traite des interactions entre la lumière
              et la matière biologique. Ce cours est ouvert aux étudiant-es
              de physique et de biologie qui portent un intérêt à l'état de l'art de la photonique
              et ses applications aux sciences de la vie. [...]
---
```

<br/>


Each markdown file is generated automatically by the scala application that fetches the data
from the database.  
These files and the [html-template](res/templates/desc-template.html) are then used by pandoc to generate a clean html
page that will later be converted to pdf.


<br/>  

## Packaging

Using [Wix](https://wixtoolset.org/) and
the [sbt-native-packager](https://www.scala-sbt.org/sbt-native-packager/index.html) an MSI executable can be created
with the command  
`sbt 'Windows / packageBin'`. To clean and update the changes in `build.sbt`,
run `sbt bloopInstall 'Windows / packageBin'`.

Unfortunately, this require a **heavily** configurated `build.sbt` config file.
Actually the heaviness isn't really the issue here, but rather the "outdatedness" and scarcity of actually usefull the
informations. That's what makes the packaging / installer generation *really* time consuming.

Left to do:

- [x] Find a way to bundle `res/` directory with the installer
- [x] Find a way to force project installation in document directory and add symlink the "pdf" folder next to the .exe
- [ ] Find a way to put it in smth like `C:\ProgramData\Microsoft\Windows\Start Menu\Programs` to make it searchable
  from windows seach
- [ ] Small GUI

- [x] Find a way to generate an executable instead of `.bat`
    - [x] If that's not possible just hide the `.bat` and make a ~~C~~ **Rust** wrapper that calls it.
      The wrapper will then be compiled as `.exe`

Not urgent:

- [x] Remove empty (0 Kb) / useless wix features (from "features to be installed" screen)
- [ ] Replace the lorem ipsum in the license screen by an actual license

The actual wix configuration of the installer is present in the file `target/windows/Course-Description-Automation.wxs`
file.
It's just an xml file with a specific microsoft-defined syntax see
here [Course-Description-Automation.wxs](https://github.com/David-Kyrat/Course-Description-Automation/blob/build/target/windows/Course-Description-Automation.wxs)


***

<br/>

## Pandoc quick commands

Selection of pandoc and wkhtmltopdf examples to keep track what arguments to use
since their usage can sometime be a bit obscure.

1. **Pandoc**

    ```sh
    pandoc input.md -t html --template=res/templates/template.html -o output.html
    pandoc desc-2022-12M040.html  --from=html --pdf-engine=wkhtmltopdf -o desc-2022-12M040.pdf -t html --css=res/templates/course-desc.css # doesn't give the right output
    ```


2. **wkhtmltopdf**
    ```sh
    wkhtmltopdf --enable-local-file-access -T 2 -B 0 -L 3 -R 0 input.html output.pdf
    ```

