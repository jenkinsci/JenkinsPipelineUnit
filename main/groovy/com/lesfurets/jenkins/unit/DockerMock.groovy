package com.lesfurets.jenkins.unit


@SuppressWarnings(['EmptyMethod', 'MethodReturnTypeRequired', 'UnusedMethodParameter'])
class DockerMock implements Serializable {
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
            return body(new Container())
        }

        def pull() {}

        def push(String tagname = '') {
            if (tagname) {
                tag(tagname)
            }
        }

        def run(String args = '', String command = '') {
            return new Container()
        }

        def tag(String tagname = '') {
            this.tagname = tagname
        }

        def withRun(String args = '', String command = '', Closure body) {
            return body(new Container())
        }
    }

    Image build(String image, String args = '') {
        return new Image(image)
    }

    Image image(String id) {
        return new Image(id)
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
