// To starts, run jshell --enable-preview which is a program able to interpret Java syntax
// then cut and paste the following lines to see how it works
// To exit jshell type /exit

// # Java 17

// ## <img src="images/duke-covid-vaccine.png" alt="drawing" width="500"/>

// ## Plan
// - Record and Sealed Types
// - Pattern Matching
//   - arrow switch / switch expression
//   - instanceof + type pattern
//   - switch on types
// - Text Block

// ## Java Evolution
// - Java 1.0 - OOP              (1995)
// - Java 1.5 - Enum & Generics  (2004)
// - Java 1.8 - Lambdas          (2014)
// - Java 21  - Pattern Matching (2023)
//   - Java 17  - Records & Sealed Types (2021)

// # Record

// ## Record ?
// a record is a named tuple with no encapsulation
record Person(String name, int age) {}

// a record is unmodifiable

// ## component & accessors
// for each component, the compiler generates an accessor method
var bob = new Person("Bob", 32);
System.out.println("bob name" + bob.name());  // Bob
System.out.println("bob age" + bob.age());  // 32

// ## equals/hashCode
// the compiler also generates equals and hashCode that delegates to the components
var bob = new Person("Bob", 32);
var bob2 = new Person("Bob", 32);
System.out.println(bob.hashCode());  // 2075947
System.out.println(bob2.hashCode());  // 2075947
System.out.println(bob.equals(bob2));  // true

// ## toString
// it also generates a method toString
System.out.println(bob);  // Person[name=Bob, age=32]

// the exact text can change with the JDK version

// ## overriding
// a generated method can be overridden
record Person(String name, int age) {
  @Override
  public String toString() {
    return "Person{name='" + name + "', age=" + age + '}';
  }
}

// ## Canonical constructor
// the compiler generates a constructor that initializes all
// the components
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
// other constructors must delegate to the canonical constructor
record Person(String name, int age) {
  public Person(int age) {
    this("unnamed", age);  // delegation
  }
}

// ## Constructor and checked exception
// the canonical constructor can not throw a checked exception
// ```java
// record Person(String name, int age) {
//   public Person(String name, int age) throws IOException { ...}
//   }  // does not compile !
// }
// ```

// ## No inheritance
// a record can not inherit a class
// ```java
// record Foo() extends java.util.Date {
//   // does not compile !
// }
// ```

// ## java.lang.Record
// a record always __extends__ java.lang.Record

// (like an enum with java.lang.Enum)

// ## Member / Local record
// can declare a record inside a class or a method
class Foo {
  record Point(int x, int y) {}  // implicitly static
  public void bar() {
    // ok here too !
    record Point(int x, int y) {}
  }
}

// ## reflection API
// the components are available at runtime
record Person(String name, int age) {}
List<java.lang.reflect.RecordComponent> components =
    List.of(Person.class.getRecordComponents());

// ## annotation on components
// A new target RECORD_COMPONENT is added
@Target(ElementType.RECORD_COMPONENT)
@interface NotNull {}
record Person(@NotNull String name, int age) {}

// ## annotation legacy
// Annotation on a component are propagated to the corresponding field
// and accessor depending on the annotation target
@Target({ElementType.METHOD, ElementType.FIELD})
@interface NotNull {}
record Person(@NotNull String name, int age) {}

// ## serialization
// a record is serializable/deserializable automatically if
// - it implements __Serializable__
// - all the components are serializable

// the canonical constructor is called when deserializing (yai !)

// ## records as serialization proxies
class Foo implements Serializable {
  private String s;
  public Foo(String s) { this.s = Objects.requireNonNull(s); }
  private Object writeReplace() {
    return new FooProxy(s);
  }
  private record FooProxy(String s) implements Serializable {
    private Object readResolve() {
      return new Foo(s);
    }
  }
}


// # Sealed Types
// ## Sealed types ??
// __final__ forbids inheritance but there _was_ no mechanism to restrict
// the set of subtypes

// __sealed__ types have a __permits__ clause that list all permitted
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
// an empty __permits__ clause is forbidden

// __final__ should be used instead

// ## Constraints on subtypes
// subtypes of a sealed type must be
// - in the same package
// - in the same module, if there is a module

// ## inference
// if all subtypes are in the same file,
// the compiler infers the __permits__ clause if not declared
sealed interface Paiment {  // no permits clause
  record CreditCard() implements Paiment { }
  record DebitCard() implements Paiment { }
}

// only "stable" names are alllowed (no anonymous class/lambda)

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

// ## Expression problem
// a classical hierarchy allows adding new subtypes but no other operations
interface Vehicle {
  int computeTax();
}
record Bus() implements Vehicle {
  public int computeTax() { return 12; }
}
record Car() implements Vehicle {
  public int computeTax() { return 6; }
}

// (if you are not the maintainer of the code)

// ## Expression problem (2)
// a sealed hierarchy allows adding new operations but no other subtypes
sealed interface Vehicle permits Bus, Car { }
record Bus() implements Vehicle { }
record Car() implements Vehicle { }

int computeTax(Vehicle vehicle) {
  return switch(vehicle) {  // is exhaustive
    case Bus bus -> 12;
    case Car car -> 6;
  };
}

// (if you are not the maintainer of the code)

// ## Pattern Matching
// - Allow __users__ of existing types to specify new operations
// - Ease __maintenance__ by showing the whole algorithm in one place

// Not new to OOP, this is the Visitor Pattern

// ## Pattern Matching maintenance
// problem when adding a new subtype
// - for library
//   - ok but for non-public types
// - for application
//   - use exhaustive switches

// same rules as with enums

// ## Plan to introduce Pattern Matching in Java
// 1. fix switch syntax
// 2. add a variant that works with expressions
// 3. add different kinds of patterns (we are here now !)
// 4. deal with class encapsulation ?

// ## The good old switch
// The switch in C is alien to C
int seats = 3;
String type;
switch(seats) {
  case 1:
    type = "small";
    break;
  case 2:
  case 3:
    int s = seats;
    type = "medium " + s;
    break;
  default:  // do not compile if uncommented, why ?
    //String s = "debug";
    type = "big";
}
System.out.println(type);

// ## Fix the switch
// reuse the lambda syntax
// (retcon: now the lambda syntax uses the arrow switch syntax)

// 1. fix fallthrough, use a block if more than one instruction
// 2. need an OR operator between the values (use ",")
// 3. fix weird scope, one scope per case

// ## Arrow switch
// use '->' instead of ':'
int seats = 3;
String type;
switch(seats) {
  case 1 -> type = "small";
  case 2, 3 -> {
    int s = seats;
    type = "medium " + s;
  }
  default -> {
    String s = "debug";
    type = "big";
  }
}
System.out.println(type);

// ## switch expression
// the compiler verifies that "type" is initialized for each branch,
// would be more readable to allow the switch to "return" a value

// But we can not use the keyword __return__

// ## switch expression (2)
// we use a new keyword __yield__
var seats = 3;
var type = switch(seats) {
  case 1 -> "small";
  case 2, 3 -> {
    var s = seats;
    yield "medium " + s;
  }
  default -> {
    var s = "debug";
    yield "big";
  }
};  // <-- don't forget the semicolon !
System.out.println(type);

// # and with the legacy syntax
var seats = 3;
var type = switch(seats) {
  case 1:
    yield "small";
  case 2:
  case 3:
    var s = seats;
    yield "medium " + s;
  default:
    //var s = "debug";
    yield "big";
};  // <-- don't forget the semicolon !
System.out.println(type);

// # instanceof type pattern

// ## instanceof with a type pattern
// instanceof is enhanced to support a type pattern
Object fileref = Path.of("path/to/foo.txt");
if (fileref instanceof Path path) {
  System.out.println(path.getFileName());
}

// ## instanceof + generics
// the corresponding cast must be safe
Collection<String> list = List.of("foo");
if (list instanceof List<?> list) { }  // ok
if (list instanceof List<String> list) { }  // ok
//if (list instanceof List<Integer> list) { }  // error

// ## instanceof with not ("!")
// a weird case supported because it's a common idiom
record Foo(String s) {
  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Foo foo)) {
      return false;
    }
    // can access to foo here !
    return s.equals(foo.s);
  }
}

// ## instanceof with &&
// but it can be written in a better way
record Foo(String s) {
  @Override
  public boolean equals(Object o) {
    return o instanceof Foo foo && s.equals(foo.s);
  }
}

// # switch on types

// ## preview feature in 17
// must be enabled at compile time
// ```sh
//  javac --enable-preview --source 17 *.java
// ```
// and at runtime
// ```sh
//  java --enable-preview MyApp
// ```

// ## switch on types
// extends the switch to any type
Object fileref = Path.of("path/to/foo.txt");
switch(fileref) {
  case Path path -> System.out.println(path.getFileName());
  case File file -> System.out.println(file.getName());
  default -> System.out.println("default !");
}

// a __default__ is needed because all possible types are not covered

// ## switch on type
// is semantically equivalent to a cascade of
// __if__ __instanceof__ + __else__
Object fileref = Path.of("path/to/foo.txt");
if (fileref instanceof Path path) {
  System.out.println(path.getFileName());
} else if (fileref instanceof File file) {
  System.out.println(file.getName());
} else {
  System.out.println("default !");
}

// ## order of the type patterns
// goes from the most specific to the less specific
Object fileref = Path.of("path/to/foo.txt");
switch(fileref) {
  case Path path -> System.out.println(path.getFileName());
  case Object o -> System.out.println("Object !");
}

// like the catchs of a try/catch

// ## switch on type with constants
// constants dominate their type, so
// should be written before
Integer value = 42;
switch(value) {
  case 0 -> System.out.println("zero");
  case Integer i -> System.out.println(i);
}

// case 0 is before case Integer

// ## Type Pattern & null
// by default, a switch throws a NPE on null
// ```java
// Object fileref = null;
// switch(fileref) {  // throw a NPE
//   ...
// }
// ```

// ## Capturing null
// - case __null__
  switch(fileref) {
    case null -> System.out.println("null !");
    default -> System.out.println("not null");
  }
// - total pattern
  Object fileref = null;
  switch(fileref) {
    case Object o -> System.out.println("object !");
  }

// ## Exhaustive
// if all cases are covered, __default__/total pattern is not necessary
sealed interface Cake permits Cookie {}
record Cookie(boolean chunky) implements Cake {}

Cake cake = new Cookie(false);
switch(cake) {
  case Cookie cookie -> System.out.println("get a cookie !");
  // no default required
}

// like with an enum

// ## Patterns
// The following patterns are recognized by switch
// - **type** pattern
//   - String s
//   - var s  (Java 18)
// - **guard** pattern
//   - _pattern_ && _boolean_condition_
// - **parenthesis** pattern
//   - ( _pattern_ )

// ## guards
// && introduces guards
sealed interface Cake permits Cookie {}
record Cookie(boolean chunky) implements Cake {}

var good = switch(cake) {
  case Cookie cookie && cookie.chunky() -> true;
  case Cookie cookie -> false;
};

// guards have access to the binding names

// # Future ...

// ## Future Patterns (Java 18)
// - record pattern
//   - Point(int x, int y) ->  // record with 2 components
// - array pattern
//   - String[] { var s, var t } ->  // array of length 2
//   - Point[] { var p, ... } ->  // array of length >= 1

// ## Example of a record pattern
// access to the record components as binding names
// ```java
// sealed interface Cake permits Cookie {}
// record Cookie(boolean chunky) implements Cake {}
// var good = switch(cake) {
//  case Cookie(var chunky) && chunky -> true;
//  case Cookie cookie -> false;
// };
// ```

// ## Future (Java 19+)
// - destructuring assignment
// ```java
// Point(var x, var y) = point;
// ```
// - de-constructor, pattern __methods__ on classes
// ```java
// class Point {
//   private int x;
//   private int y;
//   ...
//   (int a, int b) deconstructor() {
//     return (x, y);
//   }
// }
// ```

// # Text Block

// ## Design forces
// - add multiline strings in Java (the Kotlin way)
// - try to be smart about accidental indentation

// ## Plain Old String
System.out.println(
    "  SELECT *\n" +
    "  FROM users\n" +
    "  WHERE login == \"Bob\""
  );

// ## Text Block
// Use `"""` to start and end the String
System.out.println("""
    SELECT *
    FROM users
    WHERE login == "Bob"
  """);


// # Incidental white spaces

// ## Text Block <=> a Box of Text
// Acts as a box around the text
// ```
// |----------------------|
// |..SELECT *            |
// |..FROM users          |
// |..WHERE login == "Bob"|
// |----------------------|
// ```

// ## Block of Text
// By default, spaces on the right are removed,
// so it's more like
// ```
// |----------|
// |  SELECT *|
// |  FROM users|
// |  WHERE login == "Bob"|
// |----------------------|
// ```

// ## Spaces on the right
// Use `\s` to keep the space on the right
System.out.println("""
     SELECT *             \s
     FROM users           \s
     WHERE login == "Bob" \s
   """);


// # Escaping

// ## Not a raw String !
// Escaping works as usual (`\t`, `\n`, `\"`, etc.)
System.out.println("""
   \tSELECT *
   \tFROM users
   \tWHERE login == "Bob"
   """
  );

// ## Remove the `\n`
// You can ask to not have the implicit newlines with `\`
System.out.println("""
   SELECT * \
   FROM users \
   WHERE login == "Bob" \
   """
  );


// ## Other OpenJDK projects
//  - Panama (already incubating)
//    - Vector(SIMD) API / Foreign Memory / Foreign Linker
//  - Loom
//    - support other OSes (continuations are now hidden)
//  - Valhalla
//    - Primitive class + Parametric VM
//  - CRaC

// ## Codes on Github
// https://github.com/forax/do-synthetic-methods-dream-of-electric-switch-expressions