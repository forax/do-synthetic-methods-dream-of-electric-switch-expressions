// To starts, run jshell --enable-preview which is a program able to interpret Java syntax
// then cut and paste the following lines to see how it works
// To exit jshell type /exit

// # Helpful NullPointerException

// # Provide more context in the message of a NPE

// ## Design forces
// SAP JVM has meaningful error message for NPEs since 2006

// It really helps
//   - beginners
//   - to find an error when only runtime logs are available 

// ## Example
void printFirst(List<String> list) {
  System.out.println(list.get(1).length());
}

// When you have several `.` on the same line, it's not obvious
// where the code throws a NullPointerException.

// ## Meaningful NPE error message
void printFirst(List<String> list) {
  System.out.println(list.get(1).length());
}
printFirst(Arrays.asList("foo", null, "bar"));

// ## Java 14 / Java 15
// For Java 14, this feature is under the flag
// ```
// -XX:+ShowCodeDetailsInExceptionMessages
// ```
// to avoid performance regression.

// Should be enabled by default in Java 15.

// > Note: It's enable for this notebook !

// # Some Examples

// ## Objects method call
record Person(String name) {
  int nameLength() {
    return name.length();
  }
}
Person nobody = null;
System.out.println(nobody.nameLength());

// ## Objects field access
record Person(String name) {
  int nameLength() {
    return name.length();
  }
}
Person nobody = new Person(null);
System.out.println(nobody.nameLength());

// ## Switch on String or Enum
String s = null;
switch(s) {
  case "hello":
}

enum Hero { JEDI, SITH }
Hero zero = null;
switch(zero) { }

// ## Other control flow
// `synchronized`
Object o = null;
synchronized(o) { }

// `throw`
throw null;

// ## Array
// Array length
Object[] array = null;
array.length

// Array access
 // array[0]
array[0] = 3;

// ## Wrapper type
// unboxing
Integer integer = null;
int i = integer;


// # Improvements for the next releases

// ## `Objects.requireNonNull()`
record Person(String name) {
  public Person {
    Objects.requireNonNull(name);
  }
}
new Person(null);

// In `requireNonNull!`, the NPE is created in user code
// not by the VM

// ## Implicit `requireNonNull()`
// The compiler also insert a couple of `requireNonNull()`

String s = null;
switch(s) { }

PrintStream out = null;
Runnable r = out::println;

// ## Implicit `requireNonNull()` (2)
// The compiler inserts a `requireNonNull()` if the outer class is null
class A {
  class B {
  }
}
A a = null;
a.new B();

// # All those cases need a little more love :)
