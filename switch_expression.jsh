// To starts, run jshell --enable-preview which is a program able to interpret Java syntax
// then cut and paste the following lines to see how it works
// To exit jshell type /exit

// # Switch Expression

// ## Design forces
// - enhance switch to be an expression
// - fix C switch warts ?

// ## The C switch statement
void color(String vehicle) {
  switch(vehicle) {
    case "car":
    case "sedan":
      var length = vehicle.length();
      System.out.println((length < 4)? "blue": "red");
      break;
    case "bus":
      System.out.println("yellow");
      break;
    default:
      throw new AssertionError();
  }
}
color("sedan");

// ## What wrong with the C switch ?
// `break` is easy to forget (fallthrough)
void color(String vehicle) {
  switch(vehicle) {
    case "car":
    case "sedan":
      var length = vehicle.length();
      System.out.println((length < 4)? "blue": "red");
      // oops
    case "bus":
      System.out.println("yellow");
      break;
    default:
      throw new AssertionError();
  }
}
color("sedan");

// ## What wrong with the C switch ? (2)
// The scope of the local variable is weird
void color(String vehicle) {
  switch(vehicle) {
    case "car":
    case "sedan":
      var length = vehicle.length();
      System.out.println((length < 4)? "blue": "red");
      break;
    case "bus":
      var length = 0;  // removing var works !!
      System.out.println("yellow");
      break;
    default:
      throw new AssertionError();
  }
}
color("sedan");

// ## What wrong with the C switch ? (3)
// `default` is not required
void color(String vehicle) {
  switch(vehicle) {
    case "car":
    case "sedan":
      var length = vehicle.length();
      System.out.println((length < 4)? "blue": "red");
      break;
    case "bus":
      var length = 0;  // removing var works !!
      System.out.println("yellow");
      break;
    // default:
    //  throw new AssertionError();
  }
}
color("sedan");

// # Arrow syntax

// ## Introduce a new syntax
// - avoid fallthrough: use curly braces
// - weird scope: use curly braces
void color(String vehicle) {
  switch(vehicle) {
    case "car" -> {
      var length = vehicle.length();
      System.out.println((length < 4)? "blue": "red");
    }
    case "sedan" -> {
      var length = vehicle.length();
      System.out.println((length < 4)? "blue": "red");
    }
    case "bus" -> {
      var length = 0;
      System.out.println("yellow");
    }
    default -> {
      throw new AssertionError();  
    }
  }
}
color("sedan");

// ## Comma separated values
// Allow to use comma between the values of a `case` to enable sharing
void color(String vehicle) {
  switch(vehicle) {
    case "car", "sedan" -> {
      var length = vehicle.length();
      System.out.println((length < 4)? "blue": "red");
    }
    case "bus" -> {
      System.out.println("yellow");
    }
    default -> {
      throw new AssertionError();  
    }
  }
}
color("sedan");

// ## Short syntax if one statement/expression
// Same short syntax as for a lambda
void color(String vehicle) {
  switch(vehicle) {
    case "car", "sedan" -> {
      var length = vehicle.length();
      System.out.println((length < 4)? "blue": "red");
    }
    case "bus" -> System.out.println("yellow");
    default -> {
      throw new AssertionError();  
    }
  }
}
color("sedan");

// > `throw` was overlooked !

// ## `default` still not required
// For maximum compatibility and easy refactoring
void color(String vehicle) {
  switch(vehicle) {
    case "car", "sedan" -> {
      var length = vehicle.length();
      System.out.println((length < 4)? "blue": "red");
    }
    case "bus" -> System.out.println("yellow");
    // default -> {
    //  throw new AssertionError();  
    // }
  }
}
color("sedan");

// > We may regret this decision !


// # Switch expression

// ## Allows switch to have a value
// 
void color(String vehicle) {
  var color = switch(vehicle) {
    //case "car", "sedan" -> {
    //  var length = vehicle.length();
    //  System.out.println((length < 4)? "blue": "red");
    //}
    case "bus" -> "yellow";
    default -> {
      throw new AssertionError();  
    }
  };
  System.out.println(color);
}
color("bus");

// ## Use `yield` to indicate the value
// 
void color(String vehicle) {
  var color = switch(vehicle) {
    case "car", "sedan" -> {
      var length = vehicle.length();
      yield (length < 4)? "blue": "red";
    }
    case "bus" -> "yellow";
    default -> {
      throw new AssertionError();  
    }
  };
  System.out.println(color);
}
color("sedan");

// ## backward source compatibility
// `yield` is a new keyword:
// - what about a local variable named `yield` ?
// - what about `Thread.yield()` ?

// ## `yield` is a restrited keyword
// `yield` is only enable at the beginning of a statement
void color(String vehicle) {
  Thread.yield();
  var color = switch(vehicle) {
    case "car", "sedan" -> {
      var length = vehicle.length();
      yield (length < 4)? "blue": "red";
    }
    case "bus" -> "yellow";
    default -> {
      var yield = 42;
      throw new AssertionError();  
    }
  };
  System.out.println(color);
}
color("sedan");

// ## `yield` is a restricted keyword (2)
// `yield` is only enable at the beginning of a statement
void yield() { }
void color(String vehicle) {
  yield();
}

void yield(int value) { }
void color(String vehicle) {
  yield(42);
}


// # Expression switch and `default`

// ## `default` is required if not exhaustive
// The example below doesn't compile !
void color(String vehicle) {
  var color = switch(vehicle) {
    case "car", "sedan" -> {
      var length = vehicle.length();
      yield (length < 4)? "blue": "red";
    }
    case "bus" -> "yellow";
    // default -> { throw new AssertionError(); }
  };
  System.out.println(color);
}
color("sedan");

// ## `default` not required if exhaustive
// switch on enum can be seen as exhaustive or not
enum Vehicle { CAR, SEDAN, BUS }
void color(Vehicle vehicle) {
  var color = switch(vehicle) {
    case CAR, SEDAN -> {
      var length = vehicle.name().length();
      yield (length < 4)? "blue": "red";
    }
    case BUS -> "yellow";
    // default -> { throw new AssertionError(); }
  };
  System.out.println(color);
}
color(Vehicle.SEDAN);

// ## `default` not required if exhaustive (2)
// It doesn't compile if not exhaustive !
enum Vehicle { CAR, SEDAN, BUS, JET }
void color(Vehicle vehicle) {
  var color = switch(vehicle) {
    case CAR, SEDAN -> {
      var length = vehicle.name().length();
      yield (length < 4)? "blue": "red";
    }
    case BUS -> "yellow";
  };
  System.out.println(color);
}

