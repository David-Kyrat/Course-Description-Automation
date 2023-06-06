# Directory Structure once fully packaged

To be able to run correctly, one could verify that the unzipped directory has the following structure
   

    ./
    ├── Course-Description-Automation (executable)
    │ 
    ├── files/
    │   └── res/
    │       └── bin-converters/
    │           ├── pandoc
    │           ├── java/
    │           │   ├── javafx-sdk-17
    │           │   ├── jdk17
    │           │   ├── core-[version].jar
    │           │   ├── fancyform.jar
    │           │   ├── javafx-swt.jar
    │           │   ├── jfoenix-[version].jar
    │           │   ├── jvm.driver-[version].jar
    │           │   │ 
    │           │   └── course-description-automation.jar
    │           │
    │           ├── log/ (optional will get generated at runtime)
    │           ├── md/
    │           ├── pdf/
    │           ├── templates/
    │           │   ├── course-desc.css
    │           │   ├── template.html
    │           │   └── unige.png
    │           ├── abbrev.tsv
    │           └── logging_config.yaml
    │ 
    └── pdfs/ --> ./files/res/pdf/  (shortcut)
    │ 
    └── LICENSE


***

i.e. :

- Course-Description-Automation/
  - files/
    - res/
      - bin-converters/
        - pandoc
        - java/
          - javafx-sdk-17
          - jdk17
          - core-[version].jar
          - fancyform.jar
          - javafx-swt.jar
          - jforenix-[version].jar
          - jvm.driver-[version].jar
        - log/
        - md/
        - pdf/
        - templates/
          - course-desc.css
          - template.html
          - unige.png
        - abbrev.tsv
        - logging_config.yaml
  - Course-Description-Automation (executable)
  - pdfs (shortcut to files/res/pdf)
  - LICENSE

