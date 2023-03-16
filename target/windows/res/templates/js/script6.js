

    function toggle_style(toggler, theme) {
      if (toggler.classList.contains("checked")) {
        set_dark_style(theme);
      } else {
        set_light_style(theme);
      }
    }

    window.addEventListener("load", (event) => {


      let theme = "default";
      const downcute_root = document.getElementById("docute");
      if (downcute_root !== null && downcute_root.classList.contains('theme-chaos')) {
        theme = "chaos";
      }


      const toggler = document.querySelector(".dark-theme-toggler .toggle");
      toggle_style(toggler, theme);
      toggler.addEventListener("click", (event) => {
        toggler.classList.toggle("checked");
        toggle_style(toggler, theme);
      })


      $(".page-content table").wrap("<div class='table-wrapper'></div>");


      $('body .section.level1').each(function () {
        $(this).attr("id", $(this).attr("id").replace(/\./g, "-"));
      });
      $('body .section.level2').each(function () {
        $(this).attr("id", $(this).attr("id").replace(/\./g, "-"));
      });
      $('#toc ul li a').each(function () {
        $(this).attr("href", $(this).attr("href").replace(/\./g, "-"));
      });


      $('#toc ul').first().addClass('nav');
      $('body').scrollspy({ target: '#toc' });

      $("body").removeClass("preload");


      const prism_highlight = new Event('prism_highlight');
      document.addEventListener('prism_highlight', (e) => Prism.highlightAll())

      let code_el = document.querySelectorAll("pre[class] code");
      code_el.forEach(el => {
        const classes = el.parentNode.className.split(" ");
        classes.forEach(classe => {
          if (classe != 'sourceCode') {
            el.classList.add('language-' + classe);
          }
        })
      })
      document.dispatchEvent(prism_highlight);


    })
