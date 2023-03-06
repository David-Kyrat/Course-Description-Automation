
    (function($) {
      "use strict";
      $.fn.rmarkdownStickyTabs = function() {
        var context = this;
        
        var showStuffFromHash = function() {
          var hash = window.location.hash;
          var selector = hash ? 'a[href="' + hash + '"]' : 'li.active > a';
          var $selector = $(selector, context);
          if($selector.data('toggle') === "tab") {
            $selector.tab('show');
            
            $selector.parents('.section.tabset').each(function(i, elm) {
              var link = $('a[href="#' + $(elm).attr('id') + '"]');
              if(link.data('toggle') === "tab") {
                link.tab("show");
              }
            });
          }
        };


        showStuffFromHash(context);

        $(window).on('hashchange', function() {
          showStuffFromHash(context);
        });

        $('a', context).on('click', function(e) {
          history.pushState(null, null, this.href);
          showStuffFromHash(context);
        });

        return this;
      };
    }(jQuery));

    window.buildTabsets = function(tocID) {

      function buildTabset(tabset) {

        var fade = tabset.hasClass("tabset-fade");
        var pills = tabset.hasClass("tabset-pills");
        var navClass = pills ? "nav-pills" : "nav-tabs";

        var match = tabset.attr('class').match(/level(\d) /);
        if (match === null)
          return;
        var tabsetLevel = Number(match[1]);
        var tabLevel = tabsetLevel + 1;

        var tabs = tabset.find("div.section.level" + tabLevel);
        if (!tabs.length)
          return;

        var tabList = $('<ul class="nav ' + navClass + '" role="tablist"></ul>');
        $(tabs[0]).before(tabList);
        var tabContent = $('<div class="tab-content"></div>');
        $(tabs[0]).before(tabContent);

        
        var activeTab = 0;
        tabs.each(function(i) {

          
          var tab = $(tabs[i]);

          
          var id = tab.attr('id');

          
          if (tab.hasClass('active'))
            activeTab = i;

          
          
          $("div#" + tocID + " li a[href='#" + id + "']").parent().remove();

          
          id = id.replace(/[.\/?&!#<>]/g, '').replace(/\s/g, '_');
          tab.attr('id', id);

          
          var heading = tab.find('h' + tabLevel + ':first');
          var headingText = heading.html();
          heading.remove();

          
          var a = $('<a role="tab" data-toggle="tab">' + headingText + '</a>');
          a.attr('href', '#' + id);
          a.attr('aria-controls', id);
          var li = $('<li role="presentation"></li>');
          li.append(a);
          tabList.append(li);

          
          tab.attr('role', 'tabpanel');
          tab.addClass('tab-pane');
          tab.addClass('tabbed-pane');
          if (fade)
            tab.addClass('fade');

          
          tab.detach().appendTo(tabContent);
        });

        
        $(tabList.children('li')[activeTab]).addClass('active');
        var active = $(tabContent.children('div.section')[activeTab]);
        active.addClass('active');
        if (fade)
          active.addClass('in');

        if (tabset.hasClass("tabset-sticky"))
          tabset.rmarkdownStickyTabs();
      }

      
      var tabsets = $("div.section.tabset");
      tabsets.each(function(i) {
        buildTabset($(tabsets[i]));
      });
    };
