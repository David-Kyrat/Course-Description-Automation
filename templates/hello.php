<html>
<head> <title>PHP Test</title> </head>

<body>
    <?php
    ini_set('memory_limit', '-1');
    require 'vendor/autoload.php';
    $my_text = file_get_contents("desc-ipa.md");

    use Michelf\MarkdownExtra;

    $my_html = MarkdownExtra::defaultTransform($my_text);

    echo $my_html

    ?>
</body>

</html>
