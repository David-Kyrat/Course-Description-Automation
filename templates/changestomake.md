## "Manual" changes to make to generated html file


#### _Will be automated later_


- change `.Wrap` class to `max-width: 1580px;`
- remove content of `<div class="SidebarItems">` and
`<div class="toggle">`
- remove h2 from rule with page-content that gives 40px up&down margin (search. font-size: 1.7rem) or change it to 20 px

- remove padding for `<hr>` (search also  border-top: 1px solid #eee)
- remove `margin: 3rem 0;` of .page-content
- maybe set a max size for table
- put table inside `div` with class `SidebarItems` 
- change `var(--text-color)` in `style.setProperty('--header-text-color', 'var(--text-color)');` to `style.setProperty('--header-text-color', '#029093')`
      
