/*
 * Copyright (C) by Courtanet, All Rights Reserved.
 */
package com.lesfurets.jenkins;

import com.lesfurets.jenkins.unit.BasePipelineTest;
import groovy.lang.Script;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.lesfurets.jenkins.unit.MethodSignature.method;
import static java.util.Arrays.stream;
import static java.util.stream.Stream.concat;
import static org.assertj.core.api.Assertions.assertThat;

class TestPipelineJava extends BasePipelineTest {

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        this.setScriptRoots(concat(stream(getScriptRoots()), Stream.of("src/test/jenkins")).toArray(String[]::new));
        super.setUp();
        Consumer println = System.out::println;
        getHelper().registerAllowedMethod(method("step", String.class), println);
    }

    @Test
    void should_return_clean_name() {
        Script script = loadScript("lib/utils.jenkins");
        assertThat(script.invokeMethod("cleanName", new Object[] { "some thing"})).isEqualTo("SOME_THING");
        printCallStack();
    }
}
