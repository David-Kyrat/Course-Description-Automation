# Markdown Parsing and html / pdf generation


### Parsing & Rendering Markdown


- mdgen.php is used to parse a markdown file in [php markdown extra](https://michelf.ca/projects/php-markdown/extra/)
into html. it's used like so:
  1. First launch a server: `php -S localhost:8000 &`
  2. Go at `http://localhost:8000/output.html`
  3. Launch parser: `./mdgen.php input.md > output.html`  
- vendor folder and composer.* files are required for php script to work
---

- R markdown parser is used like so: `R.exe -e "rmarkdown::render('$in')"`  (or use `knit()` function defined in `profile.ps1`)
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


# DB access & Requests


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


The necessary fields to fill the class [Course.scala](https://github.com/David-Kyrat/Course-Description-Automation/blob/master/src/main/scala/ch/Course.scala) are located as following:


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
       - variousInfo   # check if all variousInfo contains the same category of info => they dont
       - comment       # idem
       - type  # presence of type = exercies indicate hoursNb.exercices > 0, idem for hoursNb.practice
    # exercices:    
    -  - duration # hoursNb.exercices 

    # pratice:    
    -  - duration # hoursNb.pratice (if present)

- facultyLabel   # faculty
- ListStudyPlan:  # List of each study plan that has this course
    - studyPlanLabel
    - planCredits
    - facultyLabel

```

