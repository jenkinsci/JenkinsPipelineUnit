// vars/sayHello.groovy
def call(String name, String otherName) {
    // Any valid steps can be called from this code, just like in other
    // Scripted Pipeline
    echo "Hello, ${name}."
    if (otherName) {
        echo "Hello, ${otherName}."
    }
}