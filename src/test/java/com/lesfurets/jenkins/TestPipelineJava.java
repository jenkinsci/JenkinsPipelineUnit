/*
 * Copyright (C) by Courtanet, All Rights Reserved.
 */
package com.lesfurets.jenkins;

import static com.lesfurets.jenkins.unit.MethodSignature.method;
import static java.util.Arrays.stream;
import static java.util.stream.Stream.concat;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.Consumer;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;

import com.lesfurets.jenkins.unit.BasePipelineTest;

import groovy.lang.Script;

public class TestPipelineJava extends BasePipelineTest {

    @Override
    @Before
    public void setUp() throws Exception {
        this.setScriptRoots(concat(stream(getScriptRoots()), Stream.of("src/test/jenkins")).toArray(String[]::new));
        super.setUp();
        Consumer println = System.out::println;
        getHelper().registerAllowedMethod(method("step", String.class), println);
    }

    @Test
    public void should_return_cleanname() throws Exception {
        Script script = (Script) loadScript("lib/utils.jenkins");
        assertThat(script.invokeMethod("cleanName", new Object[] { "some thing"})).isEqualTo("SOME_THING");
        printCallStack();
    }
}
