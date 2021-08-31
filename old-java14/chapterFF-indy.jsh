// To starts, run jshell --enable-preview which is a program able to interpret Java syntax
// then cut and paste the following lines to see how it works
// To exit jshell type /exit

// # java.lang.invoke Cookbook

// ## Missing Features


// > Work in progress !

// # MethodHandle Basics

// ## Imports
import java.lang.invoke.*;
import java.lang.invoke.MethodHandles.Lookup;
import static java.lang.invoke.MethodType.*;
import static java.lang.invoke.MethodHandles.*;

// # MethodType

// ## MethodType
// A method type represent the descriptor of a method
// - a method type without parameter types
System.out.println(methodType(void.class));
// - a method type with parameter types
System.out.println(methodType(void.class, int.class, String.class));

// ## MethodType interning
// Creating a method type is slow because there are interned (like constant string)
var type1 = methodType(String.class, double.class);
var type2 = methodType(String.class, double.class);
System.out.println(type1 == type2);

// ## MethodHandle
// A method handle is like a function pointer
// - created on existing class members (method, field, constructor)
//   with `Lookup.find*()`
// - called
//   with `mh.invokeExact(...)`

// # Lookup

// ## Lookup and Access control
// A lookup object correspond to a class and a set of permissions to access to the members of that class
class Hello {
  static void createMH() {
    var lookup = MethodHandles.lookup();
    System.out.println(lookup.lookupClass().getName());
    var mode = lookup.lookupModes();
    System.out.println((mode & Lookup.PUBLIC) != 0);
    System.out.println((mode & Lookup.PRIVATE) != 0);
    System.out.println((mode & Lookup.PACKAGE) != 0);
    System.out.println((mode & Lookup.MODULE) != 0);
  }
}
Hello.createMH();

// ## Lookup in()
// From a lookup you can ask to see another lookup using `in()`
class AnotherHello {
  static void createMH() {
    var lookup = MethodHandles.lookup();
    var lookup2 = lookup.in(Hello.class);
    var mode = lookup2.lookupModes();
    System.out.println((mode & Lookup.PUBLIC) != 0);
    System.out.println((mode & Lookup.PRIVATE) != 0);
    System.out.println((mode & Lookup.PACKAGE) != 0);
    System.out.println((mode & Lookup.MODULE) != 0);
  }
}
AnotherHello.createMH();

// ## Lookup privateLookupIn()
// If the package is open, you ask for the private access
// This is the equivalent of `Member.setAccesssible(true)`
class YetAnotherHello {
  static void createMH() throws IllegalAccessException {
    var lookup = MethodHandles.lookup();
    var lookup2 = MethodHandles.privateLookupIn(Hello.class, lookup);
    var mode = lookup2.lookupModes();
    System.out.println((mode & Lookup.PUBLIC) != 0);
    System.out.println((mode & Lookup.PRIVATE) != 0);
    System.out.println((mode & Lookup.PACKAGE) != 0);
    System.out.println((mode & Lookup.MODULE) != 0);
  }
}
YetAnotherHello.createMH();

// # MethodHandle creation

// ## Creation from a static method
// `lookup.findStatic()` creates a method handle on a __static__ method
class Hello {
  static void hello(String text) {
    System.out.println(text);
  }
  static void createMH() throws Throwable {
    var lookup = MethodHandles.lookup();
    var mh = lookup.findStatic(Hello.class, "hello", methodType(void.class, String.class));
    System.out.println(mh);
  }
}
Hello.createMH();

// ## Creation from a virtual method
// `lookup.findVirtual()` creates a method handle on an __instance__ method
class Hello {
  void hello(String text) {
    System.out.println(text);
  }
  static void createMH() throws Throwable {
    var lookup = MethodHandles.lookup();
    var mh = lookup.findVirtual(Hello.class, "hello", methodType(void.class, String.class));
    System.out.println(mh);
  }
}
Hello.createMH();

// > Notice that the method handle takes 2 parameters,
// > for an instance method, `this` need to be provided

// ## Creation from a static field
// `lookup.findStatic[Getter|Setter]()` creates a method handle on a __static__ field
class Hello {
  static int VALUE;
  static void createMH() throws Throwable {
    var lookup = MethodHandles.lookup();
    var getter = lookup.findStaticGetter(Hello.class, "VALUE", int.class);
    System.out.println(getter);
    var setter = lookup.findStaticSetter(Hello.class, "VALUE", int.class);
    System.out.println(setter);
  }
}
Hello.createMH();

// ## Creation from an instance field
// `lookup.find[Getter|Setter]()` creates a method handle on an __instance__ field
class Hello {
  long value;
  static void createMH() throws Throwable {
    var lookup = MethodHandles.lookup();
    var getter = lookup.findGetter(Hello.class, "value", long.class);
    System.out.println(getter);
    var setter = lookup.findSetter(Hello.class, "value", long.class);
    System.out.println(setter);
  }
}
Hello.createMH();

// > Again, for instance members, `this` is needed

// ## Instance creation using the constructor
// `lookup.findConstructor()` creates a method handle on a constructor
class Hello {
  Hello(int v, String s) { }
  static void createMH() throws Throwable {
    var lookup = MethodHandles.lookup();
    var constructor = lookup.findConstructor(Hello.class, methodType(void.class, int.class, String.class));
    System.out.println(constructor);
  }
}
Hello.createMH();

// # Invoking a method handle

// ## Different semantics
// There a 3 ways of calling a method handle
// - with the exact parameter types using `invokeExact()`
// - with subtyping, boxing and primitive conversions using `invoke()`
// - with an array using `invokeWithArguments`

// # InvokeExact
// 
class Hello {
  static void hello(String text) {
    System.out.println(text);
  }
  static void createMH() throws Throwable {
    var lookup = MethodHandles.lookup();
    var mh = lookup.findStatic(Hello.class, "hello", methodType(void.class, String.class));
    mh.invokeExact("hello");
  }
}
Hello.createMH();


// # InvokeExact
// 
class Hello {
  static int add(int a, int b) { return a + b; }
  static void createMH() throws Throwable {
    var lookup = MethodHandles.lookup();
    var mh = lookup.findStatic(Hello.class, "add", methodType(int.class, int.class, int.class));
    System.out.println(mh.invokeExact(2, 3));  // wrong !
  }
}
Hello.createMH();

// # InvokeExact
// 
class Hello {
  static int add(int a, int b) { return a + b; }
  static void createMH() throws Throwable {
    var lookup = MethodHandles.lookup();
    var mh = lookup.findStatic(Hello.class, "add", methodType(int.class, int.class, int.class));
    System.out.println((int)mh.invokeExact(2, 3));  // Ok !
  }
}
Hello.createMH();


// # Performance model



// # MethodHandle combinators


