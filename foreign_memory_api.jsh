// To starts, run jshell --enable-preview which is a program able to interpret Java syntax
// then cut and paste the following lines to see how it works
// To exit jshell type /exit

// # Foreign Memory API

// ## Different kinds of memories
// - Java GCed memory
// - C dynamic memory (malloc)
// - file mapped memory (mmap)
// - non volatile memory (NVRAM)

// ## Current two APIs in Java
// - java.nio.ByteBuffer
// - sun.misc.Unsafe*

// > sun.misc.Unsafe is not really supported  

// ## Example of ByteBuffer
// ```java
// var nativeOrder = ByteOrder.nativeOrder();
// var byteBuffer = ByteBuffer.allocateDirect(1024 * 4).order(nativeOrder);
// for (var i = 0; i < 1024; i++) {
//   byteBuffer.putInt(i * 4, 42);
// }
// unsafe.invokeCleaner(byteBuffer);
// ```

// ## ByteBuffer
// - very IO buffer oriented
// - fast
// - releasing direct memory issue
// - use int for index (2G limit)

// ## Example of Unsafe
// ```java
// long unsafe_addr = unsafe.allocateMemory(1024 * 4);
// for (var i = 0; i < 1024; i++) {
//   unsafe.putInt(unsafe_addr + (i * 4) , 42);
// }
// unsafe.freeMemory(unsafe_addr);
// ```

// ## Unsafe
// - 2 modes
//   - direct / indirect
// - Really unsafe !
//   - crash
//   - buffer overflow
// - Not that JIT friendly
//   - aliasing issue
//   - no loop vectorisation

// ## Need a third API ?
// - Unsafe replacement
// - More low level than ByteBuffer
//   - keep ByteBuffer fast but solve the deallocation issue
// - Panama (new C <-> Java bridge)
//   - model structured data 
//   - nice with small data structure
// - 100% safe !

// ## Foreign Memory API

// ## Foreign Memory API - Incubator
// This api is currently in incubator mode
// so for `java` and `javac` you need to add
// the module `jdk.incubator.foreign` in the module graph
// ```
// --add-modules jdk.incubator.foreign
// ```

// then you can import it
import jdk.incubator.foreign.*;

// ## This API is still in flux
// So it may change depending on the Java version
System.out.println("runtime version " + Runtime.version());

// > make sure you are at least using Java 15

// # Memory Segment

// ## Memory Segment
// A memory segment is an area of memory that can be created
// from different kind of memories
// - `MemorySegment.ofArray(array)`
// - `MemorySegment.allocateNative(8192)`
// - `MemorySegment.mapFromPath​(path, bytesSize, mapMode)`
// - `MemorySegment.ofByteBuffer(byteBuffer)`

// ## Temporally Bounded
// The memory management is __explicit__
// - `allocateNative()` allocate the memory
// - `close()` release it.
var segment = MemorySegment.allocateNative(8192);
segment.close();
System.out.println(segment.isAlive());

// ## Temporally Bounded (2)
// An access after a `close()` results in a runtime exception
var segment = MemorySegment.allocateNative(8192);
segment.close();
System.out.println(segment.asByteBuffer());

// ## Temporally Bounded (3)
// A _try-with-resources_ make the syntax cleaner
try (var segment = MemorySegment.allocateNative(8192)) {
  System.out.println(segment.isAlive());
  System.out.println(segment);
} // the memory is released

// ## Spatially Bounded
// You can not access to outside the bounds of the segment
try (var segment = MemorySegment.allocateNative(8192)) {
  var buffer = segment.asByteBuffer();
  var indexTooBig = 8192;
  System.out.println(buffer.get(indexTooBig));
}

// ## Thread Bounded
// Only the thread that has allocated the segment can access
// to the data of that segment
try (var segment = MemorySegment.allocateNative(8192)) {
  var thread = new Thread(() -> {
    var buffer = segment.asByteBuffer();
  });
  thread.start();
  thread.join();
}

// ## Thread Bounded (2)
// A thread can ask explicitly using `acquire()` to share the segment
// An acquired segment must be closed before the segment can be closed.
try (var segment = MemorySegment.allocateNative(8192)) {
  var thread = new Thread(() -> {
    try(var acquiredSegment = segment.acquire()) {
      var buffer = acquiredSegment.asByteBuffer();
    }
  });
  thread.start();
  thread.join();
}

// > Sharing segments may lead to concurrency issues

// ## Memory Segment - Summary
// - Thread Bounded
// - Temporally Bounded
// - Spatially Bounded


// # MemoryAddress and VarHandle

// ## MemoryAddress
// An offset inside the segment
try (var segment = MemorySegment.allocateNative(8192)) {
  var base = segment.baseAddress();
  System.out.println(base);
  var newBase = base.offset(16);
  System.out.println(newBase);
}

// ## MemoryAddress is value based
// MemoryAddress is not a classical class
// - no identity
//   - synchronized, wait/notify are not supported
// - acts like a primitive type

// > Not fully implemented yet !

// ## VarHandle
// A class representing how of access to a value
// - primitive type (and array of them)
// - byte order (`java.nio.ByteOrder`)
// - alignment
// - semantics (plain, volatile, opaque)
import java.nio.ByteOrder;
var nativeOrder = ByteOrder.nativeOrder();
System.out.println(nativeOrder);

import java.lang.invoke.VarHandle;
VarHandle intHandle = MemoryHandles.varHandle(int.class, nativeOrder);
System.out.println(intHandle);

// ## Get/set one `int` at address 32  
try (var segment = MemorySegment.allocateNative(8192)) {
  var base = segment.baseAddress();
  intHandle.set(base.offset(32), 42);
  System.out.println(intHandle.get(base.offset(32)));
}

// ## VarHandle Addressing mode
// - using a direct mode
//   `handle.get(MemoryAddress)`
var intHandle = MemoryHandles.varHandle(int.class, nativeOrder);
System.out.println(intHandle);

// - using an array mode:
//   `handle.get(MemoryAddress, long)`
var intHandle = MemoryHandles.varHandle(int.class, nativeOrder);
var intArrayHandle = MemoryHandles.withStride(intHandle, 4);
System.out.println(intArrayHandle);

// ## Get/set an array of `int`s  
try (var segment = MemorySegment.allocateNative(8192)) {
  var base = segment.baseAddress().offset(32);
  for (var i = 0 ; i < 128 ; i++) {
    intArrayHandle.set(base, i, 42);
  }
  System.out.println(intArrayHandle.get(base, 64));
}

// ## Without using a VarHandle with a stride
// Maybe slower because the _stride_ is not hoisted
// out of the loop
try (var segment = MemorySegment.allocateNative(8192)) {
  var base = segment.baseAddress().offset(32);
  for (var i = 0 ; i < 128 ; i++) {
    intHandle.set(base.offset(i * 4), 42);
  }
  System.out.println(intHandle.get(base.offset(64 * 4)));
}

// ## MemoryAddress and VarHandle
// - MemoryAddress is an offset in the segment
// - VarHandle specifies the addressing mode

// > VarHandle can also specifies an _offset_ and multiple array _dimensions_


// # MemoryLayout

// ## Example of MemoryLayout
// Represents a C memory layout
// ```c
// struct {
//   int a;
//   byte b[12];
// }
// ```
// - __value__ (number of bits + order)
// - __group__ (struct or union)
// - __sequence__ (array, sized or free)

// ## Example of MemoryLayout (2)
// ```c
// struct {
//   int a;
//   byte b[12];
// }
// ```
import static jdk.incubator.foreign.MemoryLayout.*;
var layout1 = ofStruct(
    ofValueBits(32, nativeOrder).withName("a"),
    ofSequence(12,
        ofValueBits(8, nativeOrder)
    ).withName("b")
).withBitAlignment(32);
System.out.println(layout1);


// ## Another example of MemoryLayout
// ```c
// struct {
//   double x;
//   double y;
// } []
// ```
var layout2 = ofSequence(
    ofStruct(
        ofValueBits(64, nativeOrder).withName("x"),
        ofValueBits(64, nativeOrder).withName("y")
    ).withBitAlignment(64)
);
System.out.println(layout2);

// ## VarHandle from a MemoryLayout
// Using a `PathElement` (`groupElement` or `sequenceElement`) to locate
// a field or an array inside a layout 
import static jdk.incubator.foreign.MemoryLayout.PathElement.*;
var aHandle = layout1.varHandle(int.class, groupElement("a"));
System.out.println(aHandle);

// If the primitive type is not compatible with the size
var aHandle = layout1.varHandle(long.class, groupElement("a"));
System.out.println(aHandle);

// ## VarHandle from MemoryLayout (2)
// The VarHandle has a supplementary parameter if the array has a free dimension
System.out.println(layout1);
var bHandle = layout1.varHandle(byte.class, groupElement("b"), sequenceElement());
System.out.println(bHandle);

System.out.println(layout2);
var xHandle = layout2.varHandle(double.class, sequenceElement(), groupElement("x"));
System.out.println(xHandle);

// ## A MemorySegment from a MemoryLayout
// You can ask for a segment of the right size from a layout
var aHandle = layout1.varHandle(int.class, groupElement("a"));
var bHandle = layout1.varHandle(byte.class, groupElement("b"), sequenceElement());
try (var segment = MemorySegment.allocateNative(layout1)) {
  var base = segment.baseAddress();
  aHandle.set(base, 42);
  bHandle.set(base, 7, (byte)42);
  System.out.println(aHandle.get(base) + " " + bHandle.get(base, 7));
}


// # Missing methods ??

// ## Provide elementCounts for the sequences after having created the MemoryLayout
MemoryLayout withElementCounts(MemoryLayout layout, Iterator<Integer> counts) {
  if (!counts.hasNext()) {
    return layout;
  }
  if (layout instanceof SequenceLayout seq) {
    var elementCount = seq.elementCount().orElse(counts.next());
    var result = ofSequence(elementCount, withElementCounts(seq.elementLayout(), counts)).withBitAlignment(seq.bitAlignment());
    return seq.name().map(result::withName).orElse(result);
  }
  if (layout instanceof GroupLayout group) {
    if (group.isUnion()) { throw new UnsupportedOperationException(); }
    var result = ofStruct(group.memberLayouts().stream().map(l -> withElementCounts(l, counts)).toArray(MemoryLayout[]::new)).withBitAlignment(group.bitAlignment());
    return group.name().map(result::withName).orElse(result);
  }
  return layout;
}
MemoryLayout withElementCounts(MemoryLayout layout, int... counts) {
  return withElementCounts(layout, Arrays.stream(counts).boxed().iterator());
}

// ## How to use it ?
var partialArrayLayout = ofSequence(ofSequence(
    ofStruct(
        ofValueBits(64, nativeOrder).withName("x"),
        ofValueBits(64, nativeOrder).withName("y")
    ).withBitAlignment(64)
));
System.out.println(partialArrayLayout);

var matrix8x8Layout = withElementCounts(partialArrayLayout, 8, 8);
System.out.println(matrix8x8Layout);


// ## Provide an API entry point to create a MemoryLayout from the String representation instead of using a Constable
// One of these format is lighter than the other, no ?
System.out.println(partialArrayLayout);
System.out.println(partialArrayLayout.describeConstable());


