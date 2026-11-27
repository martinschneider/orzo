# 128-bit integers and floating-point numbers

Plan for adding two numeric types to Orzo: `int128`, a 128-bit two's-complement integer, and
`float128`, an IEEE 754 binary128 ("quadruple precision") floating-point number. Nothing of this is
implemented yet.

## Language

### Types and literals

|    Type    |   Size   |                           Range / precision                            |       Literal       |                                   Example                                   |
|------------|----------|------------------------------------------------------------------------|---------------------|-----------------------------------------------------------------------------|
| `int128`   | 128 bits | −2¹²⁷ … 2¹²⁷ − 1                                                       | suffix `LL`         | `170141183460469231731687303715884105727LL`, `0xFFFF_FFFF_FFFF_FFFF_FFFFLL` |
| `float128` | 128 bits | 113-bit significand (about 34 decimal digits), exponent −16382 … 16383 | suffix `Q` (or `q`) | `3.14159265358979323846264338327950288Q`, `1e4000Q`                         |

- Decimal, hexadecimal, octal and binary integer literals, with underscores, like `long` literals.
  As in Java, `-170141183460469231731687303715884105728LL` (the minimum) is only valid as the
  operand of a unary minus.
- `float128` literals: decimal and hexadecimal (`0x1.8p1Q`) floating-point literals, rounded to
  nearest (ties to even). An integer literal with a `Q` suffix is a `float128`, like `1d` is a
  `double`.
- `int128` and `float128` are keywords where a type is expected and names everywhere else (like
  `var` and `record`), so Java code using them as names still compiles.

### Conversions and promotion

Two more steps on Java's widening ladders:

```
byte < short < int < long < int128
char < int
float < double < float128
```

- Widening: every integer type converts to `int128` exactly; `int`, `long`, `float`, `double` and
  `int128` convert to `float128` (exactly, except `int128`, which is rounded to 113 bits, like
  `long` to `double`).
- Binary numeric promotion (JLS 5.6) extended: if either operand is `float128`, both become
  `float128`; else if either is `double`, `double` (an `int128` operand is rounded, like `long`
  with `float`); …; else if either is `int128`, `int128`.
- Narrowing casts: `(long) x` keeps the low 64 bits (like `(int)` on a `long`); `(double) q` and
  `(float) q` round to nearest; `(int128) q` and `(long) q` truncate towards zero and saturate, NaN
  is 0 (like `d2l`).
- Assignment conversion: like a `long`, an `int128` (also a constant) needs a cast to become a
  smaller type.
- Boxing: `int128` and `float128` are represented by objects already (see "Representation"), so
  there's no separate wrapper class; they can be type arguments, e.g. `List<int128>`.

### Operators and semantics

- All arithmetic, comparison, bitwise and shift operators, compound assignments, `++`/`--`, and
  Orzo's `**`, `√`, `⌊⌋` and `÷`.
- `int128`: two's complement, overflow wraps around; `/` and `%` truncate like Java's and throw
  `ArithmeticException` for a zero divisor; `MIN_VALUE / -1` is `MIN_VALUE`; shifts use the low 7
  bits of the count (`>>>` shifts in zeros).
- `float128`: IEEE 754 binary128 with round-to-nearest-even, subnormals, signed zeros, infinities
  and NaN; `%` is Java's `fmod`-like remainder; comparisons with NaN are false (`!=` true); `==`
  treats −0 and +0 as equal.
- `equals()`/`hashCode()` like Java's `Long` and `Double` (for `float128`: NaN equals NaN, −0
  doesn't equal +0).
- String conversion: `"" + x` and `System.out.println(x)`. `int128` in decimal; `float128` in the
  shortest representation that reads back as the same value, formatted like `Double.toString()`
  (scientific notation outside 10⁻³ … 10⁷).
- Library: `Int128`/`Float128` static members like the wrappers': `MIN_VALUE`, `MAX_VALUE`,
  `parse…`, `toString(x, radix)`, `compare`, `compareUnsigned`, `divideUnsigned`,
  `remainderUnsigned`, `bitCount`, `numberOfLeadingZeros`, `abs`, `signum`; for `float128` also
  `sqrt`, `fma`, `floor`, `ceil`, `rint`, `isNaN`, `isInfinite`, `ulp`, `NaN`,
  `POSITIVE_INFINITY`, `NEGATIVE_INFINITY`, `MIN_NORMAL`.
- Interop with Java APIs: conversions to and from `BigInteger` (`int128`) and `BigDecimal` (both,
  `float128` exactly), e.g. `Int128.of(bigInteger)` and `x.toBigInteger()`.

## Representation on the JVM

The JVM has no 128-bit primitive types. The plan has two stages.

### Stage 1: immutable value classes

- `int128` is the final class `io.github.martinschneider.orzo.lang.Int128` with two `long` fields
  (`hi`, `lo`); `float128` is `...lang.Float128` (sign, exponent and significand packed into two
  `long`s, the binary128 encoding).
- The compiler maps the operators to static methods, e.g. `a + b` to `Int128.add(a, b)` and
  `x < y` to `Int128.compare(x, y) < 0`; conversions to `Int128.of(long)`, `x.toDouble()`, ...
- Fields, arrays, parameters, return values and generics need nothing special: they are
  references. A default value is needed where Java has one: fields and array elements of type
  `int128` are initialized to `Int128.ZERO` (a field initializer, and a fill after `new int128[n]`),
  not `null`.
- The runtime classes are written in Java (or Orzo) and live in Orzo's jar; a compiled program
  that uses them gets their class files copied into its output directory, so that it still runs
  with a plain `java Main`.
- The JIT's escape analysis removes many of the allocations of temporaries.

### Stage 2: unboxed locals (later)

- Inside a method, local variables and intermediate values of type `int128` are kept as two `long`
  slots (and `float128` as its two encoding words), with the arithmetic inlined: addition with the
  carry from `Long.compareUnsigned`, multiplication with `Math.multiplyHigh` and
  `Math.unsignedMultiplyHigh`, comparisons as compares of the high and then the low word.
- Values are boxed only where they leave the method: fields, array elements, arguments, return
  values (the JVM can't return two `long`s).
- Division, conversions to and from strings and floating point, and all of `float128`'s
  arithmetic stay calls into the runtime classes.
- This needs the attribution pass (types known before code generation) and changes to the stack
  map frames and local variable allocation (four-slot values).

(Valhalla's value classes would eventually give stage 2 for free, with the stage 1 classes
declared as value classes.)

## Arithmetic

### int128

- `add`, `subtract`, `negate`: on the two words with carry/borrow.
- `multiply`: low 128 bits of the product of the four 64-bit partial products
  (`unsignedMultiplyHigh` for the high parts).
- `divide`, `remainder` (signed and unsigned): Knuth's algorithm D on 32-bit digits, with fast
  paths for divisors that fit in 64 bits.
- `shiftLeft`, `shiftRight`, `shiftRightUnsigned`, bitwise operators, `bitCount`, leading and
  trailing zeros: on the words.
- `toString(radix)`: repeated division by the largest power of the radix that fits in a `long`
  (10¹⁸ for decimal); `parse`: the reverse, with overflow checks.
- Conversions to `double`/`float`: correctly rounded (the top 54 bits plus sticky bit).

### float128 (software floating point)

- Encoding: 1 sign bit, 15 exponent bits (bias 16383), 112 fraction bits; arithmetic on the
  significand as an `int128` (113 bits plus guard, round and sticky bits).
- `add`/`subtract` (alignment, normalization), `multiply` (226-bit product from four `int128`
  partial products), `divide` (long division of significands or Newton-Raphson), `sqrt` (digit by
  digit), `fma` (exact product before the single rounding), all correctly rounded to nearest even,
  with subnormals, overflow to infinity and NaN propagation.
- Conversions: to and from `int`, `long`, `int128` (exact or correctly rounded), `float` and
  `double` (exact widening, correctly rounded narrowing), `BigDecimal` (exact).
- `toString`: the shortest decimal that rounds back to the value (Ryu-style, or Steele & White /
  dragon4 with `BigInteger`s first, which is simpler and slower); `parse`: correctly rounded (exact
  `BigInteger` arithmetic for the hard cases).
- Only round-to-nearest-even (like Java), no exception flags.

## Compiler changes

- Lexer: `int128`/`float128` as types (contextual, see above), the literal suffixes `LL` and `Q`,
  128-bit literal values (`IntLiteral` already holds a `BigInteger`; `FPLiteral` needs an exact
  `BigDecimal` value for `float128`).
- Types: `Type`, `TypeUtils` (descriptors `Lio/github/martinschneider/orzo/lang/Int128;` and
  `...Float128;`, not primitive but numeric), `ExpressionTypes` (promotion, the widening ladders,
  numeric checks), `NumExprTypeDecider`, `BasicGenerator.convert()` (conversions as calls),
  default values of fields and arrays.
- Code generation: `ExpressionTreeGenerator` (operators as calls), compound assignments and
  increments, comparisons and conditions, string concatenation, `PushGenerator` (constants as
  `Int128.of(hi, lo)` calls, or `ldc` of a `CONSTANT_Dynamic` later), switch on `int128` (as an
  if-chain), `println` overloads.
- Constant folding of `int128` and `float128` constant expressions in the compiler (needs the same
  arithmetic: the compiler uses the runtime classes).
- Semantic checks: incompatible types, lossy conversions without a cast, division by a constant
  zero.
- Native backends (NATIVE.md): the IR gets `i128` and `f128` types; x86-64 and AArch64 lower
  `int128` to register pairs (`add`/`adc`, `adds`/`adcs`, `mul` into `rdx:rax`, `umulh`);
  division and all of `float128` call the runtime (neither has 128-bit floating point hardware).

## Testing

- `int128`: unit tests of the runtime class against `BigInteger` (random operands and the edge
  cases: 0, ±1, `MIN_VALUE`, `MAX_VALUE`, carries across the words, divisors of 1 and 2 words).
- `float128`: against `BigDecimal` with exact arithmetic and explicit rounding (random operands,
  subnormals, overflow, NaN, ±0, ties); `toString`/`parse` round trips.
- A second reference where available: C programs with GCC's `__int128` and `_Float128` (or
  `long double` on AArch64 Linux), compared output, skipped without the tools.
- Compiler: parser and lexer tests for literals and types, `CompileErrorTest` cases (lossy
  conversions, literal out of range), program tests with expected output (`tests/Int128*.java`),
  and the self-compilation (unaffected unless Orzo uses the types itself).

## Milestones

1. **`int128` runtime class** (arithmetic, comparisons, conversions, `toString`/`parse`,
   `BigInteger` interop) with its unit tests.
2. **`int128` in the language**: type, literals, promotion and casts, operators, compound
   assignments, `println` and string concatenation, defaults of fields and arrays, constant folding;
   program tests.
3. **`float128` runtime class**: encoding, conversions, add/subtract/multiply/divide, comparisons.
4. **`float128` completed**: `sqrt`, `fma`, `%`, `toString`/`parse` with correct rounding, the
   `Math`-like functions.
5. **`float128` in the language**: like milestone 2, and mixed `int128`/`float128` arithmetic.
6. **Stage 2**: unboxed `int128` locals and inlined arithmetic (after the attribution pass).
7. **Native lowering** (with the native backends).

## Open questions

- `uint128` as a type of its own, or unsigned operations as library methods only (like Java's
  `Long.compareUnsigned()`)? The plan assumes the latter.
- The names: `int128`/`float128`, or e.g. `long128`/`quad`? And the suffixes `LL` and `Q`?
- Double-double (faster, about 106 bits, not IEEE) as an alternative implementation of `float128`?
  The plan assumes IEEE binary128.
- Should the runtime classes be copied into the output (self-contained programs, as planned) or
  be a library jar on the class path?

