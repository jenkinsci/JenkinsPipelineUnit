import org.example.testsharedlib.TestClass

def call() {
    def b = 4
    def test_obj = new TestClass()
    def g = 8
    return "${test_obj.test1()}"
}
