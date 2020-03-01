// To starts, run jshell --enable-preview which is a program able to interpret Java syntax
// then cut and paste the following lines to see how it works
// To exit jshell type /exit

// # To the Future and beyond

// ## Missing Features
// - switch on types
// - sealed types (soon)
// - deconstruction
// - full pattern matching

// > Work in progress !

// # Switch on types

// ## With a hierarchy of types
// Let say we have this hierarchy
interface Vehicle { }
record Car(String owner, String passenger, String color) implements Vehicle { }
record Bus(String owner) implements Vehicle { }

// ## Switch on a hierarchy of types
// Extends switch statement/expression to allow any type
// ```java
// String colorOf(Vehicle vehicle) {
//   return switch(vehicle) {
//     case Car car -> car.color;
//     case Bus bus -> "yellow";
//     default -> throw new AssertionError("unknown ??");
//   };
// }
// ```

// ## Actually, we don't need a hierarchy
// Works with anything that extends `Object`
// ```java
// Object json = ...
// return switch(json) {
//   case JSONArray array -> ...
//   case JSONObject object -> ...
//   default -> throw new AssertionError("unknown ??");
// };
// ```

// # Sealed Types

// # New keyword `sealed`
// Add a keyword `sealed` to close a hierarchy
// - list all subtypes using the clause `permits`
// - enforced by the compiler and the VM
// - sub hierarchy is implicitly sealed (or use `non-sealed`)

// ## Closed hierarchy
// ```java
// sealed interface Vehicle 
//   permits Car, Bus { }
// record Car(String owner, String passenger, String color) implements Vehicle { }
// record Bus(String owner) implements Vehicle { }
// ```

// ## Exhaustiveness
// The compiler doesn't require the `default` clause anymore.
// ```java
// String colorOf(Vehicle vehicle) {
//   return switch(vehicle) {
//     case Car car -> car.color;
//     case Bus bus -> "yellow";
//   };  //no default
// }
// ```

// ## Add inference of `permits` clause ?
// The clause `permits` is inferred if everything in the same compilation unit
// ```java
// sealed interface Vehicle {} 
//   // inferrred permits Car, Bus
// record Car(String owner, String passenger, String color) implements Vehicle { }
// record Bus(String owner) implements Vehicle { }
// ```

// ## Add a special construct ?
// Another solution is to introduce a new syntax for describing closed hierarchy
// like in Mls (Caml, Haskell, etc)
// ```java
// union Vehicle {
//   Car(String owner, String passenger, String color),
//   Bus(String owner)
// }
// ```

// `union` is generated as a `sealed` interface, members as `record`


// # Deconstruction

// ## Deconstruction of instanceof
// ```java
// record Point(int x, int y) {
//   public boolean equals(Object o) {
//     return o instanceof Point p
//       && x == p.x && y == p.y;
//   }
// }
// ```

// > add a way to extract `x` and `y` directly when doing the `instanceof`

// ## Deconstruction of instanceof
// ```java
// record Point(int x, int y) {
//   public boolean equals(Object o) {
//     return o instanceof Point(int x2, int y2)
//       && x == x2 && y == y2;
//   }
// }
// ```

// ## With type inference
// Allow to use `var` instead of declaring the type of the components
// ```java
// record Point(int x, int y) {
//   public boolean equals(Object o) {
//     return o instanceof Point(var x2, var y2)
//       && x == x2 && y == y2;
//   }
// }
// ```


// # Deconstruction of local declaration

// ## Extracting values of a record
// ```java
// record Car(String owner, String passenger, String color) {}
// 
// var car = ...
// var owner = car.owner();
// var color = car.color();
// System.out.println(owner + " " + color);
// ```

// ## Use destructuring
// ```java
// record Car(String owner, String passenger, String color) {}
// 
// var car = ...
// Car(String owner, String passenger, String color) = car;
// System.out.println(owner + " " + color);
// ```

// ## With inference
// Reusing `var` and `_`
// ```java
// record Car(String owner, String passenger, String color) {}
// 
// var car = ...
// Car(var owner, _, var color) = car;
// System.out.println(owner + " " + color);
// ```

// ## More inference
// Removing the name of the type which can be inferred too
// ```java
// record Car(String owner, String passenger, String color) {}
// 
// var car = ...
// (var owner, _, var color) = car;
// System.out.println(owner + " " + color);
// ```

// ## Inference in for loop
// If we see Map.Entry as something implementing the record protocol
// i.e. being able to destructure itself
// ```java
// Map<String, Car> mapNameToCar = ...
// for((var name, var car) : mapNameToCar.entrySet()) {
//   System.out.println(name + " " + car);
// }
// ```


// # Deconstruction in switch

// ## So instead of a switch on types
// ```java
// String ownerOf(Vehicle vehicle) {
//   return switch(vehicle) {
//     case Car car -> car.owner;
//     case Bus bus -> bus.owner;
//   };  //no default
// }
// ```

// ## A switch on types + destructuring
// Allow to de-construct the content of a record
// ```java
// String ownerOf(Vehicle vehicle) {
//   return switch(vehicle) {
//     case Car(String owner, String passenger, String color),
//          Bus(String owner) -> owner;
//   };  //no default
// }
// ```

// ## And with some inference
// Use `var` and `_`
// ```java
// String ownerOf(Vehicle vehicle) {
//   return switch(vehicle) {
//     case Car(var owner, _, _),
//          Bus(var owner) -> owner;
//   };  //no default
// }
// ```


// # Full Pattern Matching

// ## Kind of patterns
// - __null pattern__ (`null`), match only `null`
// - __type test pattern__ (`Foo`) match the type (not `null`)
// - __or pattern__ (`pattern1, pattern2`) match either one side or the other 
// - __extraction pattern__ (`(..., pattern, ...)`) match a component
// - __constant pattern__ (`123`) match the constant value
// - __any pattern__ (`_`), match anything, even `null`

// `var` is just inference, no special matching

// ## An example using constants !
// ```java
// boolean vehicleWithBob(Vehicle vehicle) {
//   return switch(vehicle) {
//     case Car("Bob", _, _),
//          Car(_, "Bob", _),
//          Bus("Bob") -> true;
//     case Car _, Bus _ -> false;
//   };  // still no default
// }
// ```
