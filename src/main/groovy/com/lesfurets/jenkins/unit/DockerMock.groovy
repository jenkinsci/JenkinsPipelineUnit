package com.lesfurets.jenkins.unit


@SuppressWarnings(['EmptyMethod', 'MethodReturnTypeRequired', 'UnusedMethodParameter'])
class DockerMock implements Serializable {

    // Helper and binding used to apply per-instance interception to the Image/Container
    // objects this mock creates, so their calls are recorded on the owning helper's call stack.
    // Transient: these are test-infra references and must not be carried by CPS serialization.
    private transient PipelineTestHelper helper
    private transient Binding binding

    DockerMock(PipelineTestHelper helper, Binding binding) {
        this.helper = helper
        this.binding = binding
    }

    /**
     * Apply per-instance method interception to a freshly created Image/Container so that calls
     * on it are recorded on this mock's helper. No-op when the mock was created without a helper
     * (e.g. after CPS deserialization), keeping behavior safe rather than throwing.
     */
    private <T> T intercept(T instance) {
        if (helper != null) {
            InterceptingGCL.interceptInstanceMethods(instance, helper, binding)
        }
        return instance
    }

    class Container implements Serializable {
        String id

        Container(String id = 'mock-container') {
            this.id = id
        }

        def port() {
            return '1234'
        }

        def stop() {}
    }

    class Image implements Serializable {
        String id
        String tagname

        Image(String id) {
            this.id = id
            this.tagname = 'latest'
        }

        def imageName() {
            return id
        }

        def inside(String args = '', Closure body) {
            return body(DockerMock.this.intercept(new Container()))
        }

        def pull() {}

        def push(String tagname = '') {
            if (tagname) {
                tag(tagname)
            }
        }

        def run(String args = '', String command = '') {
            return DockerMock.this.intercept(new Container())
        }

        def tag(String tagname = '') {
            this.tagname = tagname
        }

        def withRun(String args = '', String command = '', Closure body) {
            return body(DockerMock.this.intercept(new Container()))
        }
    }

    Image build(String image, String args = '') {
        return intercept(new Image(image))
    }

    Image image(String id) {
        return intercept(new Image(id))
    }

    void withRegistry(String url, String credentialsId = '', Closure body) {
        body()
    }

    void withServer(String uri, String credentialsId = '', Closure body) {
        body()
    }

    void withTool(String toolName, Closure body) {
        body()
    }
}
