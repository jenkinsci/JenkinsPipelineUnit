/*
 * Copyright (C) by Courtanet, All Rights Reserved.
 */
package com.lesfurets.jenkins;

import static com.lesfurets.jenkins.helpers.MethodSignature.method;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.Consumer;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

import com.lesfurets.jenkins.helpers.BasePipelineTest;

import groovy.lang.Script;

public class TestPipelineJava extends BasePipelineTest {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        Consumer println = System.out::println;
        getHelper().registerAllowedMethod(method("step", String.class), println);
    }

    @Test
    public void should_return_cleanname() throws Exception {
        Script script = loadScript("lib/utils.jenkins");
        assertThat(script.invokeMethod("cleanName", new Object[] { "some thing"})).isEqualTo("SOME_THING");

    }
}
