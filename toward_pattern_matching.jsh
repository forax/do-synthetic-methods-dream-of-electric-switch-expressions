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
// - get ride of bean model

// ## A simple record
// Declaration
record Point(int x, int y) { }

// Usage
var point = new Point(12, 56);
System.out.println(point);


// ## Record components
// Record components are implicitly final
record Friend(String name) { }

// explicit syntax is not supported
record Friend(final int name) { }


// ## Members of a record
// inside a record, a record component is transformed to a field
record Delay(long time, TimeUnit unit) {
  public void sleep() throws InterruptedException {
    unit.sleep(time);
  }
}
System.out.println("hello !");
new Delay(2, TimeUnit.SECONDS).sleep();
System.out.println("hello again !");

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


// ## Record accessors
// From outside, a record provide an accessor for each component
record Book(String title, String author) { }
var lotr = new Book("Lord of the Ring", "J.R.R Tolkien");
System.out.println(lotr.title());
System.out.println(lotr.author());

// ## Record java.lang.Object methods
// The compiler provides `equals`/`hashCode`/`toString`
record Operator(String name) { }
var add = new Operator("+");
var add2 = new Operator("+");
System.out.println(add + " equals " + add2);
System.out.println(add.equals(add2));
System.out.println(add.hashCode() == add2.hashCode());

// ## Record and NaN
// `equals` is reflective even in Double.NaN
record FloatingPoint(double value) { }
System.out.println(Double.NaN == Double.NaN);
var fp = new FloatingPoint(Double.NaN);
var fp2 = new FloatingPoint(Double.NaN);
System.out.println(fp.equals(fp2));

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

// ## Unmodifiable record
// Records are only unmodifiable
// - requires more code to make it immutable
// - List.of() / List.copyOf() may help !



// ## Record and arrays


// ## Record Reflection API


// ## Record and Serialization

