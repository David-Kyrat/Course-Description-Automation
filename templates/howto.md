## build / compile / parse info

- mdgen.php is used to parse a markdown file in [php markdown extra](https://michelf.ca/projects/php-markdown/extra/)
into html. it's used like so:
  1. First launch a server: `php -S localhost:8000 &`
  2. Go at `http://localhost:8000/output.html`
  3. Launch parser: `./mdgen.php input.md > output.html`  
- vendor folder and composer.* files are required for php script to work
---

- R markdown parser is used like so: `R.exe -e "rmarkdown::render('$in')"`  (or use `knit()` function defined in `profile.ps1`)
- To convert a html generated output into pdf see `html_to_pdf`

