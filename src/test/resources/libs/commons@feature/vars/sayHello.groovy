// file: vars/sayHello.groovy
def call(body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    echo 'say Hello!'
}

def hello(name) {
    echo "Hello, ${name}"
}