# Wix how to structure

A component belongs to a a `Directory`   we define it in a `<DirectoryRef>` tag.
So its like.

### Dir def

```XML

 <Directory Id="" Name="">
   <directory Id="" Name="">
     <directory Id="" Name="">
     <.../>
   <.../>
 <Directory/>
```

### DirRef

Then we have the components def in `<DirectoryRef Id>` in which we define the component
in which we define what's in it (e.g. a file).

```XML
<DirectoryRef Id="">
    <Component Id="" Guid="">
         <File Id="" Name="" Source=""> </File>
    </Component>
</DirectoryRef>
```

Note a component can also be smth more complex, e.g.

```XML


<DirectoryRef Id="_res112800xxx">
    <Component Id="_res112800xxxPathC" Guid="f6b57254-bcfc-4814-88e0-6ec8ec46ac7f">
        <CreateFolder />
        <Environment Id="COURSE_DESCRIPTION_AUTOMATION_HOME"
            Name="COURSE_DESCRIPTION_AUTOMATION_HOME" Value="[INSTALLDIR]" Permanent="no"
            Action="set" System="no" />
        <Environment Id="PATH" Name="PATH" Value="[INSTALLDIR]res" Permanent="no"
            Part="last" Action="set" System="no" />
    </Component>
</DirectoryRef>
```

I have no idea whether it only works for some kind of specific variable but we
don't really care since the goal of this document is to just show whats mandatory
when we want to add new stuff an installer.


### Features

Now we have to define `features` and reference **each single** `component` in those feature
with a `ComponentRef`. Each declared component **has** to be referenced in a feature (wheather its actually useful or not).

A feature is a "branch" that we can opt in are out (with a checkbox) during installation.

At some point we'll get a screen with a tree of feature to install, it this those.
(Feature can be nested).


```XML

<Feature Id="Complete" Title="Project files" Description="All the mandatory files for the project to run.">
    <ComponentRef Id="" />
    <ComponentRef Id="" />
    .
    .
    .
</Feature>

```


## Summary

When we want to add, say a dir with new resource, to the installer, we have to create:

*   A `<Directory>` if necessary  `(id, name)`

*   A `<DirectoryRef>` to put components in it `(id)`

*   A `<Component>` in the `<DirectoryRef>` `Id, guid`

*   A `<ComponentRef>`  `(Id)`

*   A `<File>` for each file. in the `<ComponentRef>`, `(Id, name, source)` (source is the path to the file)


Example structure

```
  ├ ﰠ  Directory              
  │ ├ ﰠ  Directory            
  │ │ └ ﰠ  Directory          
  │ └ ﰠ  Directory            
  │   └ ﰠ  Directory          
  │     └ ﰠ  Directory        
  │       └ ﰠ  Directory      
  │         ├ ﰠ  Directory    
  ├ ﰠ  DirectoryRef           
  │ └ ﰠ  Component            
  │   └ ﰠ  File               
  ├ ﰠ  DirectoryRef           
  │ └ ﰠ  Component            
  │   └ ﰠ  File               
  ├ ﰠ  DirectoryRef           
  │ └ ﰠ  Component            
  │   └ ﰠ  File               
  ├ ﰠ  DirectoryRef           
  │ └ ﰠ  Component            
  │   └ ﰠ  File               
  ├ ﰠ  Feature                
  │ ├ ﰠ  ComponentRef         
  │ ├ ﰠ  ComponentRef         
  │ ├ ﰠ  ComponentRef 
  ...
```
