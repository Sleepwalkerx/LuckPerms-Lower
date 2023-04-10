package com.sleepwalker.luckperms.common.plugin.classpath;

import me.lucko.luckperms.common.plugin.classpath.ClassPathAppender;

import java.nio.file.Path;

public class DummyClassPathAppender implements ClassPathAppender {

    @Override
    public void addJarToClasspath(Path file) {
        //Dummy -_-
    }
}
