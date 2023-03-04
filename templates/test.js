const fs = require('fs');

// full options list (equivalent to defaults)
var md = require('markdown-it')()
            .use(require('markdown-it-multimd-table'), {
              multiline:  true,
              rowspan:    true,
              headerless: true,
              multibody:  true,
              aotolabel:  true,
            });

//var md = require('markdown-it')().use(require('markdown-it-multimd-table'));

var table = ""
fs.readFile('table.md', 'utf8', function (err, data) {
    if (err) throw err;
    table = data;
    console.log(md.render(table));
});

  const exampleTable =
    "|             |          Grouping           || \n" +
    "First Header  | Second Header | Third Header | \n" +
    " ------------ | :-----------: | -----------: | \n" +
    "Content       |          *Long Cell*        || \n" +
    "Content       |   **Cell**    |         Cell | \n" +
    "                                               \n" +
    "New section   |     More      |         Data | \n" +
    "And more      | With an escaped '\\|'       || \n" +
    "[Prototype table]                              \n";

//    console.log(md.render(exampleTable));

