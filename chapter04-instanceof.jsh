// To starts, run jshell --enable-preview which is a program able to interpret Java syntax
// then cut and paste the following lines to see how it works
// To exit jshell type /exit

// # Pattern matching for Instanceof

// ## Design forces
// - prepare instanceof for the pattern matching
// - remove the useless cast

// ## The old instanceof
record Author(String name) {
  public boolean equals(Object o) {
    if (!(o instanceof Author)) {
      return false;
    }
    var author = (Author) o;
    return name.equals(author.name);
  }
}

// ## What wrong with the old instanceof ?
// The cast is unnecessary.
record Author(String name) {
  public boolean equals(Object o) {
    if (!(o instanceof Author)) {
      return false;
    }
    var author = (Author) o;   // <-- that cast
    return name.equals(author.name);
  }
}

// > The VM routinely removes it, so it's not present
// > in the generated assembly code

// # Type test Pattern

// # ![Shanghai Blade Runner Style](images/blade-runner-shanghai.jpg)

// ## Type test pattern 
// Declare a variable available if the `instanceof` is true
record Author(String name) {
  public boolean equals(Object o) {
    return o instanceof Author author
      && name.equals(author.name);
  }
}
var author = new Author("bob");
var author2 = new Author("bob");
System.out.println(author.equals(author2));

// ## Shadowing a local variable
// Like with any other local variable declarations, shadowing is not allowed
record Point(int x, int y) { }
void m(Point p, Object o) {
  if (o instanceof Point p) {
    // p shadow the parameter p
  }
}

// ## Type pattern reusing the same name
// It's ok if each type pattern is in own branch
Number add(int v1, Number v2) {
  if (v2 instanceof Integer value) {
    return v1 + value;
  } else if (v2 instanceof Double value) {
    return v1 + value;
  } else {
    throw new IllegalArgumentException();
  }
}
System.out.println(add(3, 4));
System.out.println(add(3, 4.0));


// # Type test pattern with `||` and `&&`

// # ![Shenzen Blade Runner Style](images/blade-runner-shenzen.jpg)

// ## Or-ing Type patterns with the same name
// This fail because there is not union type in Java
Number add(int v1, Number v2) {
  if (v2 instanceof Integer value || v2 instanceof Double value) {
    return v1 + value;
  } else {
    throw new IllegalArgumentException();
  }
}

// ## Or-ing Type patterns
// This works but neither `i1` nor `i2` are available !
void plus(Number v1, Number v2) {
  if (v1 instanceof Integer i1 || v2 instanceof Integer i2) {
     // both i1 and i2 are not in the scope !
  }
}

// ## And-ing Type patterns
// This work, both `i1` and `i2` are in the scope
int plus(Number v1, Number v2) {
  if (v1 instanceof Integer i1 && v2 instanceof Integer i2) {
    return i1 + i2;
  } else {
    throw new IllegalArgumentException();
  }
}
System.out.println(plus(3, 4));


// # Type test pattern with `if`, `if`/`else`

// # ![China Blade Runner Style](images/blade-runner-china.jpg)

// ## And with an `if`/`else`
record Author(String name) {
  public boolean equals(Object o) {
    if (!(o instanceof Author author)) {
      return false;
    } else {
      return name.equals(author.name);
    }
  }
}
var author = new Author("bob");
var author2 = new Author("bob");
System.out.println(author.equals(author2));

// ## And with an `if` and no `else`
record Author(String name) {
  public boolean equals(Object o) {
    if (!(o instanceof Author author)) {
      return false;
    }
    return name.equals(author.name);
  }
}
var author = new Author("bob");
var author2 = new Author("bob");
System.out.println(author.equals(author2));


// # Future

// # ![Blade Runner 2049 Ad](images/blade-runner-ad.jpg)

// ## Use with switch !
// Use a switch instead of a cascade of `if` ... `else`

// ```java
// Number add(int v1, Number v2) {
//   return switch(v2) {
//     case Integer value -> v1 + value;
//     case Double value -> v1 + value;
//     default -> new IllegalArgumentException();
//   };
// }
// ```

// # Summary
// - add Pattern matching to Java
// - `instanceof` with a type test pattern
// - introduce a new local variable
// - doesn't work with `||`
// - works with `&&`, `if` and `if`/`else`
