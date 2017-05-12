// vars/sayHello.groovy
def call(String name = 'name', String otherName = null) {
    // Any valid steps can be called from this code, just like in other
    // Scripted Pipeline
    echo "Hello, ${name}."
    if (otherName) {
        echo "Hello, ${otherName}."
    }
}

def call(String... args) {
    echo "$args"
}