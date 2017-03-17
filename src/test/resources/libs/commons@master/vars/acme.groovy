// vars/acme.groovy
class acme implements Serializable {
    private String name = "something"
    def setName(value) {
        name = value
    }
    def getName() {
        name
    }
    def caution(message) {
        echo "Hello, ${name}! CAUTION: ${message}"
    }
}