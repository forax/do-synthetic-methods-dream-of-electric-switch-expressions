// To starts, run jshell --enable-preview which is a program able to interpret Java syntax
// then cut and paste the following lines to see how it works
// To exit jshell type /exit

// # Toward Pattern Matching

// ## Java Releases
// - Java 12: March 2019
//   - switch expression (preview)
// - Java 13: September 2019
//   - switch expression (preview 2)
// - Java 14: __March, 17th 2020__
//   - switch expression (release)
//   - record (preview)
//   - instanceof (preview)

// ## Java Releases
// - Java 12: March 2019
// - Java 13: September 2019
// - Java 14: March 2020
//   - switch expression (release)
//   - record (preview)
//   - instanceof (preview)
// - Java 15: __September, 15th 2020__
//   - record (preview 2)
//   - instanceof (preview 2)
//   - selead type (preview)

// # Records

// ## Design forces
// - we need to see any object fields to do pattern matching on them
// - get ride of bean model ?
// - more opinionated ??

// ## A simple record
// Declaration
record Point(int x, int y) { }

// Usage
var point = new Point(12, 56);
System.out.println(point);

// ## Record components
// A record is a named tuple
record Detective(String name, String job) { }

// Each component can be accessed by an accessor
var detective = new Detective("Rick Deckard", "Bounty Hunter");
System.out.println(detective.name());
System.out.println(detective.job());

// ## Record accessors are not getters
// Accessors do not follow the Bean specification
record Movie(String title, int releaseDate) { }
var bladeRunner = new Movie("Blade Runner", 1982);
var bladeRunner2 = new Movie("Blade Runner 2049", 2017);
System.out.println(bladeRunner.title());
System.out.println(bladeRunner2.releaseDate());

> More opinionated
> beans are evil because they skip constructor

// ## Record components are not modifiable
// Record components are implicitly final
record Replicant(boolean canDream) { }

// explicit syntax is not supported
record Replicant(final boolean canDream) { }

// ## Members of a record
// inside a record, a record component is generated as a field
record Life(long time, TimeUnit unit) {
  public void dream() throws InterruptedException {
    unit.sleep(time);
  }
}
System.out.println("hello !");
new Life(2, TimeUnit.SECONDS).dream();
System.out.println("hello again !");


// # Record Constructor

// ## Record canonical constructor
// You can define your own constructor
record Exchange(String name, int employees) {
  public Exchange(String name, int employees) {
    this.name = Objects.requireNonNull(name);
    if (employees <= 1) {
      throw new IllegalArgumentException("employee <= 1");
    }
    this.employees = employees;
  }
}
var exchange = new Exchange("Bitcoin Ponzi", 1);
System.out.println(exchange);

// ## Record canonical constructor
// The parameter names has to match the component names
record BadExchange(String name, int employees) {
  public BadExchange(String pname, int pemployees) {
    name = pname;
    employees = pemployees;
  }
}

// ## Record compact constructor
// Field assignments is done by the compiler
record Exchange(String name, int employees) {
  public Exchange {
    Objects.requireNonNull(name);
    if (employees <= 1) {
      throw new IllegalArgumentException("employee <= 1");
    }
  }
}
var exchange = new Exchange("Bitcoin Ponzi", 2);
System.out.println(exchange);

// ## Limitation of the canonical constructor
// The canonical constructor have to be `public`
record BadExchange(String name, int employees) {
  /*public*/ BadExchange { 
  }
}

// ## Limitation of constructors
// Other constructors must delegate to another constructor
record BadExchange(String name, int employees) {
  BadExchange(String name) {
    this.name = name;
    this.employees = 100;
  }
}

// ## Limitation of constructors (2)
// Other constructors must delegate to another constructor
record Exchange(String name, int employees) {
  Exchange(String name) {
    this(name, 100);
  }
}
var exchange = new Exchange("Speculative Money");
System.out.println(exchange);

// ## Record instance initializers
// Instance initializers are not supported 
record Fail() {
  {
    // instance initializers are not allowed 
  }
}

// ## Record java.lang.Object methods
// The compiler provides `equals`/`hashCode`/`toString`
record Operator(String name) { }
var add = new Operator("+");
var add2 = new Operator("+");
System.out.println(add + " equals " + add2);
System.out.println(add.equals(add2));
System.out.println(add.hashCode() == add2.hashCode());

// ## Record and NaN
// `equals` is reflective even with Double.NaN
record FloatingPoint(double value) { }
System.out.println(Double.NaN == Double.NaN);
var fp = new FloatingPoint(Double.NaN);
var fp2 = new FloatingPoint(Double.NaN);
System.out.println(fp.equals(fp2));


// # Record and Inheritance

// ## java.lang.Record
// Records implicitly inherits from java.lang.Record
record Painter(String name, String painting) { }
var painter = new Painter("Michelangelo", "Sistine Chaptel");
Record record = painter;
System.out.println(record);

// > `record` is a magic keyword !

// ## Inheritance is not supported
// Can not inherits a class !
class Famous { }
record Painter(String name, String painting) extends Famous { }

// Can not inherits from another record
record Famous() { }
record Painter(String name, String painting) extends Famous { }

// ## Implementing interfaces is supported
// Declaration
interface Famous {
  String name();
}
record Painter(String name, String painting) implements Famous { }
// Usage
Famous famous = new Painter("Leonardo Da Vinci", "Joconde");
System.out.println(famous.name());

// ## Record and inheritance
// - records implicitly inherits from java.lang.Record
// - records do not support inheritance (no `extends`)
// - records implement interfaces

// > Use subtyping not inheritance !


// # Record and Immutability

// ## Record unmodifiable vs Immutable
// Records are unmodifiable, not immutable !
record Book(String title) { }
record Library(List<Book> books) { }
var books = new ArrayList<Book>();
books.add(new Book("Lord of the ring"));
var library = new Library(books);
System.out.println(library);

// can still mutate books
books.remove(new Book("Lord of the ring"));
System.out.println(library);

// ## Defensive copy
// Is a defensive copy enough ?
record Book(String title) { }
record Library(List<Book> books) {
  public Library {
    books = new ArrayList<>(books);
  }
}
var books = new ArrayList<Book>();
books.add(new Book("Lord of the ring"));
var library = new Library(books);
System.out.println(library);

books.remove(new Book("Lord of the ring"));
System.out.println(library);

// ## Defensive copy (2)
// Beware of the accessor !
record Book(String title) { }
record Library(List<Book> books) {
  public Library {
    books = new ArrayList<>(books);
  }
}
var books = new ArrayList<Book>();
books.add(new Book("Lord of the ring"));
var library = new Library(books);
System.out.println(library);

library.books().remove(new Book("Lord of the ring"));
System.out.println(library);

// ## Defensive copy (2)
// Defensive copies everywhere !
record Book(String title) { }
record Library(List<Book> books) {
  public Library {
    books = new ArrayList<>(books);
  }
  public List<Book> books() {
    return Collections.unmodifiableList(books);
  }
}
var books = new ArrayList<Book>();
books.add(new Book("Lord of the ring"));
var library = new Library(books);
System.out.println(library);

library.books().remove(new Book("Lord of the ring"));
System.out.println(library);

// ## Unmodifiable List to the rescue
record Book(String title) { }
record Library(List<Book> books) {
  public Library {
    books = List.copyOf(books);
  }
}
var books = new ArrayList<Book>();
books.add(new Book("Lord of the ring"));
var library = new Library(books);
System.out.println(library);

library.books().remove(new Book("Lord of the ring"));
System.out.println(library);

// ## Record and arrays
// Arrays are always modifiable, so same issue
record User(String login, char[] password) {
  public User {
    login = Objects.requireNonNull(login);
    password = password.clone();
  }
  public char[] password() { return "*".repeat(password.length).toCharArray(); }
  public boolean equals(Object o) {
    return o instanceof User user && login.equals(user.login) && Arrays.equals(password, user.password);
  }
  public int hashCode() { return Objects.hash(login, Arrays.hashCode(password)); }
  public String toString() { return "User " + login + " " + "*".repeat(password.length); }
}
var user1 = new User("bob", "df15cb4e019ec2eac654fb2e486c56df285c8c7b".toCharArray());
var user2 = new User("bob", "df15cb4e019ec2eac654fb2e486c56df285c8c7b".toCharArray());
System.out.println(user1.equals(user2));
System.out.println(user1.hashCode() == user2.hashCode());
System.out.println(user1);

// ## Unmodifiable/Immutable record
// Records are only unmodifiable
// - may require more code to make them immutable
// - List.of() / List.copyOf() may help !


// # Nested and Local Record

// ## Nested record
// Records are always static (like enums)
class Person {
  private final String name;
  public Person(String name) { this.name = name; }
  // a record can not use field of outer class
  record Vip(String vipName) {
    public String toString() {
      return vipName + " " + name;
    }
  }
}

// ## Local record
// You can declare a record inside a method
double average(List<Integer> list) {
  record Stat(int count, int sum) {
    Stat add(int value) {
      return new Stat(count + 1, sum + value);
    }
    Stat merge(Stat stat) {
      return new Stat(count + stat.count, sum + stat.sum);
    }
    double average() {
      return sum / (double)count;
    }
  }
  return list.stream().reduce(new Stat(0, 0), Stat::add, Stat::merge).average();
} 
var list = List.of(1, 2, 3, 4, 5);
System.out.println(average(list));

// ## Local record (2)
// A local record can not access to local variables (static access)
int add(int v1, int v2) {
  record Local() {
    int sum() {
      return v1 + v2;
    }
  }
  return new Local().sum();
} 
System.out.println(add(40, 2));

// ## Local class vs local record
// A local class can not declare static members
// but a local record can !
class Enclosing {
  void m() {
    class Local { static int VALUE = 3; }
  }
}

class Enclosing {
  void m() {
    record Local() { static int VALUE = 3; }
  }
}

// ## Local Class rules
// Rules for local class/enum/record are a mess
// - local class can always access (effectively) local variables
// - static local class are not allowed
// - local enum are not allowed
// - local record are allowed but can not access local variables

// > ahh, too many crufty rules, need a cleanup !  


// # Record Reflection API

// ## Is a record ?
// At runtine, a record knows it is a Record
record Character(String name, String isHuman) { }
var deckard = new Character("Deckard", "Maybe?");

// using `Class.isRecord()`
System.out.println(deckard.getClass().isRecord());

// ## Get all record components
// A record known reflectively all its components
record Character(String name, String isHuman) { }
var deckard = new Character("Deckard", "Maybe?");

// using `Class.getRecordComponents()`
var components = List.of(Character.class.getRecordComponents());
System.out.println(components);

// ## Record component accessor
// A record component knows its accessor !
import static java.util.stream.Collectors.joining;
Object invoke(Record record, java.lang.reflect.Method accessor) {
  try {
    return accessor.invoke(record);
  } catch(Exception e) {
    throw new AssertionError(e);  //FIXME
  }
}
String toJSON(Record record) {
  return Arrays.stream(record.getClass().getRecordComponents())
    .map(c -> "\"" + c.getName() + "\": \"" + invoke(record, c.getAccessor()) + "\"")
    .collect(joining(", ", "{", "}")); 
}
System.out.println(toJSON(deckard));

// ## Record canonical constructor
import java.lang.reflect.*;
Object call(Constructor constructor, Object... args) {
  try {
    return constructor.newInstance(args);
  } catch(Exception e) {
    throw new AssertionError(e);  //FIXME
  }
}
Object replicate(Record record) throws Exception {
  var components = List.of(record.getClass().getRecordComponents());
  var constructor = record.getClass().getConstructor(components.stream().map(RecordComponent::getType).toArray(Class[]::new));
  return call(constructor, components.stream().map(c -> invoke(record, c.getAccessor())).toArray());
}
System.out.println(replicate(deckard));

// ## Record Reflection API
// A record is fully aware of itself
// - get record components (name, type and accessor)
// - can convert any list of values to a record and vice-versa 


// # Record and Serialization

// ## Implements `Serializable`
// Records use a builtin serialization implementation
record Replicant(String name, int age) implements Serializable { }

// 
var baos = new ByteArrayOutputStream();
try(var oos = new ObjectOutputStream(baos)) {
  oos.writeObject(new Replicant("Bryant", 20));  
}
Object replicated;
try(var bais = new ByteArrayInputStream(baos.toByteArray());
    var ois = new ObjectInputStream(bais)) {
    replicated = ois.readObject();
}
System.out.println(replicated);

// ## Serialization and record
// Deserialization uses the constructors
// - validation during deserialization
// - harder to create fake data

// > We need a better deserialization mechanism for classes
