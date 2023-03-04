<?php

// autoload_static.php @generated by Composer

namespace Composer\Autoload;

class ComposerStaticInit6b243d37ff5d44d159bce029f6c3c88c
{
    public static $prefixLengthsPsr4 = array (
        'M' => 
        array (
            'Michelf\\' => 8,
        ),
    );

    public static $prefixDirsPsr4 = array (
        'Michelf\\' => 
        array (
            0 => __DIR__ . '/..' . '/michelf/php-markdown/Michelf',
        ),
    );

    public static $prefixesPsr0 = array (
        'M' => 
        array (
            'Michelf' => 
            array (
                0 => __DIR__ . '/..' . '/michelf/php-smartypants',
            ),
        ),
    );

    public static $classMap = array (
        'Composer\\InstalledVersions' => __DIR__ . '/..' . '/composer/InstalledVersions.php',
    );

    public static function getInitializer(ClassLoader $loader)
    {
        return \Closure::bind(function () use ($loader) {
            $loader->prefixLengthsPsr4 = ComposerStaticInit6b243d37ff5d44d159bce029f6c3c88c::$prefixLengthsPsr4;
            $loader->prefixDirsPsr4 = ComposerStaticInit6b243d37ff5d44d159bce029f6c3c88c::$prefixDirsPsr4;
            $loader->prefixesPsr0 = ComposerStaticInit6b243d37ff5d44d159bce029f6c3c88c::$prefixesPsr0;
            $loader->classMap = ComposerStaticInit6b243d37ff5d44d159bce029f6c3c88c::$classMap;

        }, null, ClassLoader::class);
    }
}
