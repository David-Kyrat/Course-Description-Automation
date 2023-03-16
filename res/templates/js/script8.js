 $(document).ready(function () {
     window.buildTabsets("toc");
   });
    $(document).ready(function () {
     $('.tabset-dropdown > .nav-tabs > li').click(function () {
       $(this).parent().toggleClass('nav-tabs-open')
     });
   });