// To starts, run jshell --enable-preview which is a program able to interpret Java syntax
// then cut and paste the following lines to see how it works
// To exit jshell type /exit

// # Sealed Types

// ## Design forces
// - we need to be exhaustive on types

// ## Open hierarchy
// In Java, if an interface is visible
interface Vehicle {
}
record Car(String color) implements Vehicle { }

// anyone can add a new subtype
record Sedan(String color) implements Vehicle { }

// ## We want to do a switch on type
// We need to know if the cases are exhaustive
// ```java
// var color = switch(vehicle) {
//    case Car(var color),
//         Sedan(var color) -> {
//      yield color;
//    }
//    case Bus _ -> "yellow";
// };
// System.out.println(color);
// ```
// > not the definitive syntax !

// # sealed interface

// ## keyword `sealed`
//
sealed interface permits Car, Bus {
}
record Car(String color) implements Vehicle { }
record Bus() implements Vehicle { }

