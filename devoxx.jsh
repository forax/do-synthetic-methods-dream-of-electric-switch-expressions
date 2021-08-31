// To starts, run jshell --enable-preview which is a program able to interpret Java syntax
// then cut and paste the following lines to see how it works
// To exit jshell type /exit

// # Java 17

// ## Java 1.0: OOP
// - Class: state + behaviors
//   - Encapsulation
// - Subtyping / Interface
// - Late binding / Polymophic method calls

// ## Java Evolution
// - Java 1.0 - OOP              (1995)
// - Java 1.5 - Enum & Generics  (2004)
// - Java 1.8 - Lambdas          (2014)
// - Java 26  - Pattern Matching (2024)
//   - Java 17  - Records        (2021)

// ## Ideas to move forward (1.6+)
// - better support _new_ kind of applications
// - be more functional
//   - lambda, pattern matching ?
//   - push for immutability ?
// - be more modern
//   - move away from inheritance ?

// # Java 8

// ## Lambdas
// Functional interface + lambda + type inference

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;Runnable code = () -> {
   System.out.println("hello");
};
Comparator<String> cmp =
    (s1, s2) -> s1.compareToIgnoreCase(s2);

// make easy to define anonymous functions

// ## Stream & Collector
// Higher-order methods using anonymous functions
List<String> strings =
    IntStream.range(0, 10).
      mapToObj(i -> "" + i).
      collect(Collectors.toList());

// # Java 11

// ## Module
//  Modularize the Java Platform
//  - more encapsulation (less reflection)
//  - the JDK is not monolithic anymore
//    - move the java EE crufts to Maven Central
//    - no more JRE, use jlink instead

// ## Language changes
// inference of local variable types
var x = 3;
var s = "hello";
// also infer intersection types / anonymous classes
var box = new Object() { int x; };
System.out.println(box.x);

// ## Rehabilitate interfaces
// Private method in interface
interface Foo {
  private void bar() {
    System.out.println("hello");
  }
}

// ## Enhance the diamond syntax
// Diamond syntax for anonymous classes
Comparator<String> cmp = new Comparator<>() {
  public int compare(String s1, String s2) {
    return s1.compareToIgnoreCase(s2);
  }
};

// ## And also
// - @SafeVargs on private instance methods
// - try-with-resources with an existing variable
// - '_' is not a valid identifier anymore

// ## Nestmate Access
// The VM understand private access between classes
// of the same Java file
class Foo {
  static class Bar {
    private int x;
  }
  public static void main(String[] args) {
    System.out.println(new Bar().x);
  }
}

// no method access$000 anymore

// ## String Enhancements
// - Compact String
//   -  String have a double representation ISO Latin 1 / UTF-16
// - String concatenation
//   - use 1 invokedynamic instead of new StringBuilder() + append() + toString()

// ## Unmodifiable Collections
// unnamed, compact and null free
var list = List.of(1, 2);
var set = Set.of("foo", "bar");
var map = Map.of("dog", 12, "cat", 42);
//  defensive copy with List/Set/Map.copyOf()
var list2 = List.copyOf(list);
// do nothing if the list is already unmodifiable



// # Java 17

// # Text Block

// ## Design forces
// - add multiline strings in Java (the Kotlin way)
// - try to be smart about accidental indentation

// ## Plain Old String
System.out.println(
    "  SELECT *\n" +
    "  FROM users\n" +
    "  WHERE login == Bob"
);

// ## Text Block
// Use `"""` to start and end the String
System.out.println("""
    SELECT *
    FROM users
    WHERE login == Bob
  """);


// # Incidental white spaces

// ## Text Block <=> a Box of Text
// Acts as a box around the text
// ```
// |--------------------|
// |..SELECT *          |
// |..FROM users        |
// |..WHERE login == Bob|
// |--------------------|
// ```

// ## Block of Text
// By default, spaces on the right are removed,
// so it's more like
// ```
// |----------|
// |  SELECT *|
// |  FROM users|
// |  WHERE login == Bob|
// |--------------------|
// ```

// ## Spaces on the right
// Use `\s` to keep the space on the right
System.out.println("""
     SELECT *           \s
     FROM users         \s
     WHERE login == Bob \s
   """);


// # Escaping

// ## Not a raw String !
// Escaping works as usual (`\t`, `\n`, `\"`, etc.)
System.out.println("""
     SELECT *
     FROM users
     WHERE login == \"Bob\"
   """
  );

// ## Remove the `\n`
// You can ask to not have the implicit newlines with `\`
System.out.println("""
   SELECT * \
   FROM users \
   WHERE login == Bob \
   """
  );

// # Value Based Class

// ## VBC will become primitive classes
// a primitive class is created by a factory / has no header
// - new VBC(...) is deprecated with removal
var value = new Integer(3);  // warning
// - can not synchronize on them
var empty = Optional.empty();
synchronized (empty) {}  // warning

// # Record

// ## Record ?
// a record is a named tuple with no encapsulation
record Person(String name, int age) {}

// ## component & accessors
// for each component, the compiler generates an accessor method
var bob = new Person("Bob", 32);
System.out.println("bob name" + bob.name());  // Bob
System.out.println("bob age" + bob.age());  // 32

// ## equals/hashCode
// the compiler also generates equals and hashCode that delegates to the components
var bob2 = new Person("Bob", 32);
System.out.println(bob.hashCode());  // 2075947
System.out.println(bob2.hashCode());  // 2075947
System.out.println(bob.equals(bob2));  // true

// ## toString
// it also generates a method toString()
System.out.println(bob);  // Person[name=Bob, age=32]

// the exact text can change with the JDK version

// ## overriding
// the generated method can be overridden
record Person(String name, int age) {
  @Override
  public String toString() {
    return "Person{name='" + name + "', age=" + age + '}';
  }
}

// ## Canonical constructor
// the compiler generate a constructor to initialize all
// the component
record Person(String name, int age) {
  public Person(String name, int age) {
    this.name = Objects.requireNonNull(name);
    this.age = age;
  }
}

// ## Compact constructor
// there is a compact form that avoid the repetition
record Person(String name, int age) {
  public Person { // no parenthesis
    Objects.requireNonNull(name);
  }
}

// the compiler assigns the parameters to the fields automatically

// ## Other Constructors
// another constructor must delegate to the canonical constructor
record Person(String name, int age) {
  public Person(int age) throws IOException {
    this("unnamed", age);  // delegation
  }
}

// ## Constructor and checked exception
// a constructor can not throw a checked exception
record Person(String name, int age) {
//public Person(String name, int age) throws IOException { ...}
//}  // does not compile !
}

// ## No inheritance
// a record can not inherit a class
//record Foo() extends java.util.Date {}
//does not compile !

// ## java.lang.Record
// a record always extends java.lang.Record
// (like an enum with java.lang.Enum)

// ## Member / Local record
class Foo {
  record Point(int x, int y) {}  // implicitly static
  public void bar() {
    // ok here too !
    record Point(int x, int y) {}
  }
}

// ## annotation on component
// Annotation on component are propagated to the field and accessor
// depending on the annotation target
record Person(@Override String name, int age) {}

// @Override is propagated to the accessor but not to the field

// ## serialization
// All records are serializable/deserializable automatically if
// all their components is serializable

// record can be a nice serialization proxy

// # Sealed Types
// ## Sealed types ??
// __final__ forbids inheritance but there _was_ no mechanism to list
// all possible subtypes

// __sealed__ types have a __permits__ clause thet list all permitted
// direct subtypes

// ## Sealed interface
sealed interface Vehicle permits Bus, Car {}
record Bus() implements Vehicle {}
record Car() implements Vehicle {}

// sealed defined a closed hierarchy of types

// ## Permits list names not types
sealed interface Expr<T> permits Literal, Add {}
record Literal(int value) implements Expr<Integer> {}
record Add<T>(T left, T right) implements Expr<T> {}

// __permits__ only list the name of the types

// ## Empty permits
// empty __permits__ clause is forbidden
// because __final__ should be used instead

// ## inference
// if all subtypes are in the same compilation unit,
// the compiler can infer the permit clause if not declared
sealed interface Paiment {  // no permits clause
  record CreditCard() implements Paiment { }
  record DebitCard() implements Paiment { }
}

// ## Sealed Hierarchy is closed by default
// so we need a keyword to re-allow subtypes
sealed interface Component {
  // records are implicitly final
  record Label(String name) implements Component {}
  final class Button implements Component {}
  non-sealed class Canvas implements Component {}
}

// __non-sealed__ allows to re-open the hierarchy

// # pattern matching

// ## The expression problem
// a classical hierarchy allows adding new subtypes but no other operation
interface Vehicle {
  int computeTax();
}
record Bus() implements Vehicle {
  public int computeTax() { return 12; }
}
record Car() implements Vehicle {
  public int computeTax() { return 6; }
}

// ## The expression problem (2)
// a sealed hierarchy allows adding new operations but no other subtype
sealed interface Vehicle permits Bus, Car { }
record Bus() implements Vehicle { }
record Car() implements Vehicle { }

int computeTax(Vehicle vehicle) {
  return switch(vehicle) {  // must be exhaustive
    case Bus bus -> 12;
    case Car car -> 6;
  };
}

// ## switch
//  - arrow switch (fix error of the past)
//  - yield
//  - switch expression
//  - switch on type
//  - guards
//  - instanceof
//  - class vs type
//  - future:
//  - syntaxe pour les tableaux
//  - deconstruction pour les records
//  - deconstructeur pour les classes
//  - destructuration de l'assignation

// ## pattern Matching: Under the hood ?
//  - arrow switch: same code
//  - switch expression: 1 supplementary local variable
//  - switch on type : invokedynamic
//  - guard (index + goto/loop)

// ## Beau Graphique
// - Interface / Annotation
// - Class / Enum / Lambda / Record

// ## Future
//  - Loom: Java 19 ?
//    - support other OSes (continuations are hidden)
//  - Panama
//    - Vector(SIMD) API / Foreign Memory / Foreign Linker
//  - Valhalla
//    - Primitive class + Parametric VM
//  - CRaC



