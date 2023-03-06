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
