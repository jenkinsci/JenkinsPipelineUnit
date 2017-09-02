package com.lesfurets.jenkins.unit

/**
 * Class to mock the return of a step, with different behaviour depending on the arguments used when the step is called.
 * For each configuration, you can match the arguments with a string, a mock file or a closure.
 *
 * The Mock files are supposed to be located in the {@link StepMock#mockBaseDirectory},
 * but you can change this property if needed.
 *
 * @see {@link PipelineTestHelper#registerMockForMethod(com.lesfurets.jenkins.unit.MethodSignature, groovy.lang.Closure)} for usage
 *
 * It has been initially designed for sh step, but it can be extended for others, bat or anything.
 */
class StepMock {

    /**
     * List of rules to follow when this step is called with a map as an Argument
     */
    private Map<String, Closure> rules = [:]

    /**
     * Step signature to be mocked
     */
    protected MethodSignature signature

    /**
     * Location
     */
    public String mockBaseDirectory = '/mocks'

    /**
     * Predicate called with the step arguments to decide how this step will be mocked
     */
    protected Closure<Boolean> predicate

    /**
     * Create a mock for a given step signature
     * @param signature signature of the step to mock
     * @param helper helper used to register this mock
     * @param predicate predicate called with the step arguments to decide how this step will be mocked
     * @see {@link #mockWithClosure(java.lang.String, groovy.lang.Closure)}
     */
    public StepMock(MethodSignature signature, PipelineTestHelper helper, Closure<Boolean> predicate) {
        this.signature = signature
        helper.registerAllowedMethod(signature, { Object... it -> this.applyBestRule(it) })
        this.predicate = predicate
    }

    /**
     * Add a rule to the mock: if the ruleMatcher matches the parameters, the mockClosure will be executed with the step parameters as arguments
     * and its result will be returned as the step result
     * @param ruleMatcher matcher of the rule
     * @param mockClosure closure to call when the mock matches the parameter
     * @return the current mock
     */
    public StepMock mockWithClosure(String matcher, Closure mockClosure) {
        rules[matcher] = mockClosure
        return this
    }

    /**
     * Add a rule to the mock: if the ruleMatcher matches the parameters, the mockResult string will be returned
     * @param ruleMatcher matcher of the rule
     * @param mockResult what the mock is supposed to return when the step parameters match the rule
     * @return the current mock
     */
    public StepMock mockWithString(String ruleMatcher, String result) {
        mockWithClosure(ruleMatcher, { return result })
        return this
    }

    /**
     * Add a rule to the mock: if the ruleMatcher matches the parameters, a mock file will be rendered as the result
     * of the mock.
     * @param ruleMatcher matcher of the rule
     * @param clazz mockfile will be retrieved from fhis resource class
     * @param mockFilename name of the mock file
     * @return the current mock
     */
    public StepMock mockWithFile(String ruleMatcher, Class clazz, final String mockFilename) {
        try {
            mockWithClosure(ruleMatcher, {getMockResource(clazz, mockFilename)})
        } catch (IllegalArgumentException e) {
            String errorMessage = "Unable to load $mockFilename for step ${signature.name}: ${e.getMessage()}"
            System.err.println(errorMessage) // print the error, in case it is catched by any Jenkins script
            throw new IllegalArgumentException(errorMessage, e)
        }
        return this
    }

    /**
     * Remove a rule in the mock
     * @param ruleMatcher name of the rule
     * @return the current mock
     */
    public StepMock removeRule(String ruleMatcher) {
        rules.remove(ruleMatcher)
        return this
    }

    /**
     * Apply a rule for the given args of the step.<br/>
     * The return type may be either String or Integer.
     * It can be a String if you use it for such a sh:
     * <code>
     *     sh(returnStdout: true, script: "curl -L http://example")
     * </code>
     * It can be a int if you use it for this:
     * <code>
     *     def branchExists = sh(returnStatus: true, script: "git ls-remote -q --exit-code . $branchName")
     * </code>
     * @param stepArgs args of the step
     * @return the mocked result of the step
     */
    protected def applyBestRule(Object...  stepArgs) {
        Map<MethodSignature, Closure<Map>> matchingRules = rules.findAll {
            return predicate.curry(it.key).call(stepArgs)
        }
        if (matchingRules.size() != 1) {
            String errorMessage = "No unique rule for ${signature.name} with args [${stepArgs}].\nRules found:${matchingRules.keySet()}"
            System.err.println(errorMessage) // print the error, in case it is catched by any Jenkins script
            throw new IllegalStateException(errorMessage)
        }
        Closure foundClosure = matchingRules.find { true /* first result */ }.value
        foundClosure.curry(stepArgs).call()
    }

    protected String getMockResource(Class c, String name) {
        String filename = "$mockBaseDirectory/${c.simpleName}/${name}"
        try {
            new File(c.getResource(filename).toURI()).text
        } catch (NullPointerException npe) {
            String errorMessage = "$filename not found"
            System.err.println(errorMessage) // print the error, in case it is catched by any Jenkins script
            throw new IllegalArgumentException(errorMessage)
        }
    }
}
