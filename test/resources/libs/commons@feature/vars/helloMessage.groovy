/**
 * Something like this in the job:
 *
 * helloTest {
 *     message = "hello this is a test"
 * }
 *
 */
def call(body) {

    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    node {
        checkout scm
        echo "hello test message: ${config.message}"
    }
}