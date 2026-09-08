## Introduction

This document contains a set of introductory examples on the TypeScript type
system. Some of the examples are also present in the following
[TypeScript Playground](https://www.typescriptlang.org/play/?ssl=30&ssc=1&pln=38&pc=2#code/PTAEF51AFAnBLAtvALvAbgU1CgngB0wGcAoEgYwHsA7IlUAQwGUUFqBzALlDrfYlAByABaYANmMqCKNOowByAV0QAjTLG7Vla2AIAsAJgB0ABhm16DAEKVKYzA2rcVt+44GtFmEgDNF1cjQaRgATEIAKAA9NbXUAGlBcTi1VdQBKGNTdAG8SUFBYTBRFWGpQSNAAakSSAF8yEAgoAGFKRHxKIlRsPEJSEkbIKABBWFgGXBwCYjIqC0YWPlHxpNBliYAeXngOAD4BAG0RcUlBBMEAd0pYMRDBAF1zOQYlLPXV942UnX2oA5MEgBGBIGR5zZ7Ud4AeR80J8ix27CI3E+n22e1+oAOR1EEik5yuNzu9wSRzsAEOzkJEP4QlJ7o8BmAhqAACqKfD2KZ9WaySyvHTQBjwDRY77xUDi2D3Q7A0Cgp6WBEcYbUEIC9RCkXcA7o9gJKUyv4AIhomGNILBfMYrIQnMwOr1BtisASLjsDmoRqxpuo5pBCU8mEZg0goAAYv5AvBgr0ZorQPAiPIaPJMOwGGgsNxwjt8IoUJkdGkIPt3W4ylBUKXE-RdlAzEymlAAKrUGNlOP9cH8l1Q2DKriSl2gAA+PFYiIExtxkmNCcclBQolgGtg-cHRfUY4nfAEBhI9noNJQDBU9jXG8nHC3unHeuns8o85PZ4vfYH1-4UAPjVf58wS9Pz3KAfAYMQiGwABablsCIXA6EwRBQHUWBrlAGD-3fLIrz3chHGoJdQDURhiNcT0yD8AIgjKEJMHIQpEEwagUCiW8dz1EtcnyRpCmKUpygw0BASEuMeAQlAkJQsZ0Jgip8LKcCiEoYjsAYXdEQSC5hHgchhETIhJSIxQiDfbALlQfTl2wAADKCbNAShCHGFBrjqBpmTDKEVAAK3o+gu15eZwN0h1QGyagGCY7gnUlFRRSlWoBG4yUorCwRhjEUKzjyOLRUBAwAGY9DidymzDNMLlg0A6J8HZUA7foxJYRQ6JY5LUuijSOAAbjy9j6h7MiVG4Fq2voKAUsirrBBsFQcvyah4u4ArCoAVlK+oSDEoDBwEKUOK-BNCKIQFuF2r9HxOZ9js6Axzo-PaoEMUAPKaUBFkUQISnA6r4MQxAyDE1lpk+77xgkXAAElxt08DWUoYYxuYibws6sK9T6paEpHWoeoTHxbG4EHCDB-jwLEaHYfwsQEaR4pxoEELyEwfHfCjGiDIAUXaPBc2ofNC3C+wOGXZIXVqDIyI9dwUr4koyjzAsjBF9hl2bUAzHqJMefwPnjXUvVjTSEgdd53BwmNY3TaIXW+YOQF7hNs29Ytg4nabLmACUvahL3ufN8JARN8qoAAcWY9RdNgprphgQoQjhySNlZTF+cF4mS3AMtyMcBMkyhMJuDgTBE-w5OpUxKjoxodOCy43L5YEtcjCTKGWPTdQ65QEsADJe8TAWC1AABSeUNcBMrQzZOPIpkiyODenbHsuqADvvI7q85ukmDaIodI4cJpoxr8EnQcCvAenDgMRBv8ngHxwjjSgfFAc+xC8DXBD1QQ7-yUBGg7BwDpQy8VHB6VADZJWKAHLCAYIZMSNk9QORgsuJMBlQA03sCEaqc80IL3YLlfITcyg2QACQRTSklKAxoKHvy8LUY0NlcpJXEJBcKRCAFgCAWg0B4wAj6SgUPGBoA4EILjjZKUKDgHoPQVg0uuCGDz0RJwkhkCKHH2oaAChABZTMwgjBoVpOEehmA0i1GYfkeoSU3phgADLdAhjHIGccvYKJoQnY0r1GgAHU1KFBqpgOq7YOCkUEHGQQjA1RGUsG-C+2B0LnwQGZIw3iwCslEI5aglNQAdCIF0ACcSP7YEJroAA+qYogZTHKv14dVdBxpPFGCbENKgkhRRuJwTQ88XgvGoIIFOVCblWl2GuNwTp05PEuMIPHJASjcCtHaZM0uXjxzGnYIUZiqzQDGh6eaWxUAAAiSYGJIB2JmBR-hGrTOwOGIoek3FEEUGIVGJBxzZFAAAax2CEbggg-QoEJJ8qCQzYBUlBdwLmMlYD1HeV8n5fzhAoBQPgKChQiAdFoJgKkdBMwmVvFtLeHYaqI0ItZWAGTEThHRc8oWdyUAPOILSv+RALIMv0tSplLyjDfLVH-fI+F2H-KKECkF0LBDcBSv-Lhij54KNcuFXlvydkAtFaCi00k0KiihVq+o0qBWyA9CrSg7BOVPO5aCowTF8kMHYGY-G+riKFAYJ8h1-89X-0FdgEQyLUXosxZBCVHDHWNDEngygFx5UqWyEq7gM5fVouIAG-0E48XImHFkD10rwRGskKaml3LcXFCIGkN10r4oOFdZwj11iDmgHbpJWAkEa6dmmP0RoUNakrn8dgK5sY20JCUT0btjAIIqR2I25tnMuwAH5tpxwABLwLscxNW+lJqqzFhmnQ9QxLNEcEwHSPhUbZCIEeoW4Qs77HQJQeAIRd1x33dQQ9D8UCqhCEuogK7RbrtAJ+79a7QADyfS+49QVnhnVACB8977-2rvVn8QQDAqSCHmo8Ro3tfb+yGqdbg0HX2weXfB39SH5okCAA).

The TypeScript language allows the addition of static type information to
JavaScript programs. This type information is used during development and build
time to:

- Provide additional information to the programmer when using an IDE
  (Integrated Development Environment).
- Check if the program is valid according to the type system rules.

Before being executed, a TypeScript program needs to be converted to JavaScript
by removing (*erasing*) the static type information. JavaScript execution
environments such as the browser may not directly support the execution of
TypeScript programs, so a previous conversion is necessary. This means that
there is a difference between the source code that developers edit and the code
that is provided to the execution environment.

## An example

Consider the following JavaScript program.

```javascript
function someFunction(value) {
    return value.toUpperCase()
}
someFunction(42)
```

The execution of this program will result in an error.

```text
Uncaught TypeError: value.toUpperCase is not a function
```

This error will happen during runtime and not during development time, which
would be much preferable. In addition, the IDE lacks information to guide the
programmer while they are using or defining the `someFunction` function.

Now consider the following TypeScript program.

```typescript
function someFunction(value: string) {
    return value.toUpperCase()
}
// someFunction(42) - type system error - `number` is not assignable to `string`
// const a: number = someFunction("hello") - type system error - `string` is not assignable to `number`.
const s: string = someFunction("hello") // OK
```

The simple addition of `: string` to the function's parameter name is enough
for the `someFunction(42)` error to be detected during development. The type
system is also capable of inferring that the return type of `someFunction` and
therefore determining that `const a: number = someFunction("hello")` is not a
valid statement.

## Primitive types

Some of the TypeScript primitive types are `string`, `number`, and `boolean`.

```typescript
const aString: string = 'hello'
const aNumber: number = 42.0
const aBoolean: boolean = true
function add(x: number, y:number): number {
  return x + y
}
```

- Note how type annotations (i.e. the `: string` part) can be added to
  variables, function parameters, and function returns.

## Composite types

As in other languages, it is possible to define types by composing other types.

### Array types

```typescript
const aStringArray: Array<string> = ['hello', 'world']
const aNumberArray: Array<number> = [0, 1, 2]
const anArrayOfArrayOfStrings: Array<Array<string>> = [['hello', 'world'], ['olá', 'mundo']]
```

### Tuple types

A bit strangely, tuples are defined using fixed-length arrays, since the
JavaScript language doesn't have support for tuples during runtime.

```typescript
const aNumberPair: [number, number] = [1, 2]
const aStringAndNumberPair: [string, number] = ["one", 2]
const aTriple: [string, number, boolean] = ["one", 2, true]
```

### Function types

Function types are first class citizens in the type world. Note how the arrow
is `=>` and not `->` (as in Kotlin).

```typescript
const isNonNegative: (input: number) => boolean = it => it >= 0
```

### Union types

Until now, the TypeScript type system seems rather straightforward, at least
for someone familiar with the Kotlin type system. Union types are probably the
first surprising thing to appear.

```typescript
const aNumberOrString: number | string = "hello"
const anotherNumberOrString: number | string = 2
let mutableNumberOrString: number | string = "hello"
mutableNumberOrString = 2
// mutableNumberOrString = false - type system error - mutableNumberOrString cannot be a boolean

function decrement(x: number | string) {
  // return x - 1 - type system error - x can also be a string, which is not usable with the `-` operator
}
```

### Object types

```typescript
const alice: {name: string, nbr: number} = {
  name: 'Alice',
  nbr: 1234,
}
```

## New type definitions

```typescript
type Student = { name: string; nbr: number }
const bob: Student = {
  name: 'Bob',
  nbr: 1235,
}

type NumberOrString = number | string
const nos1: NumberOrString = "hello"
const nos2: NumberOrString = 42
```

## Structural type system

In contrast with the Kotlin type system, the TypeScript type system is
*structural* and not *nominal*. The name of a type is not relevant; only the
type structure is relevant. For instance, if `a` is a variable of type `A` and
`b` is a variable of a different type `B`, then `b` can be assigned to `a` if
the structure of `B` is compatible with the structure of `A`, even if there
isn't any sub-type relationship between `A` and `B`.

```typescript
type TypeStructurallyIdenticalToAStudent = { name: string; nbr: number };
const foo: TypeStructurallyIdenticalToAStudent = alice;

function isEmpty(input: {length:number}): boolean {
  return input.length === 0
}
isEmpty("a string") // valid because a string has a `length` property.
isEmpty("")
isEmpty([1]) // valid because an array has a `length` property.
isEmpty([])
// ERROR isEmpty(1) - type system error - a number does not have a `length` property.
```

## Generic types

As in Kotlin, types can also be generic.

```typescript
type Predicate<T> = (input: T) => boolean
const isOdd: Predicate<number> = function(input) {
  return Number.isInteger(input) && input % 2 === 1
}
```

## Type narrowing

Type narrowing is the refinement of the type information associated with an
expression, based on flow control analysis. In Kotlin, a similar feature is
called "smart casts". In the example below, the type of `value` is different in
the *then* and *else* branches of the `if` statement:

- Outside the `if` statement, the type of `value` is `number | string`.
- Inside the *then* it is the more specific (*narrow*) `string` type.
- Inside the *else* it is the more specific `number` type.

```typescript
type NumberOrString = number | string
function doSomething(name: string, value: NumberOrString) {
  if(typeof value === 'string') {
    // in this branch `value` has type `string` - this is called type narrowing
    return `${name} = "${value}"`
  } else {
    // in this branch `value` has type `number` - this is called type narrowing
    return `${name} = ${Math.round(value)}`
  }
}
```

## Literal types

A value can be used to define a type that only allows that value. For instance
`"red"` can be used to define the type whose only value is the `"red"` string.
An example follows.

```typescript
type Red = "red"
// We are defining a 'type' and not a value or variable.
// The only possible value for _values_ of this type is "red".
// const color: Red = "blue" - type system error
const color: Red = "red"

type PrimaryColor = "red" | "green" | "blue"
```

## Discriminated union types

Discriminated union types are union types where each branch is an object type.
To distinguish between them, each object type defines a discriminator property
(e.g. `kind`, `tag`) with a distinct type.

In the example below, `FetchResult` is a union with two branches:

- `{ kind: 'network-error', error: Error}`
- `{ kind: 'http-response', status: number}`

Every branch is an object type with the same `kind` property with a different
literal type. This discriminator property can then be used to do type-
narrowing.

```typescript
// Discriminated union types
type FetchResult =
| { kind: 'network-error', error: Error}
| { kind: 'http-response', status: number}

function doAnotherThing(result: FetchResult) {
  switch (result.kind) {
    case 'network-error': {
      // type narrowed to {kind: "network-error", error: Error}
      console.log(result.error.message);
      break;
    }
    case 'http-response': {
      // type narrowed to {kind: "http-response", status: number}
      console.log(result.status);
      break;
    }
  }
}
```

## Intersection types

```typescript
// If there are union types, are there also intersection types?
type HasLength = {length: number}
type CanShift = {shift: () => void}
type CanShiftAndHasLength = HasLength & CanShift

const a1: CanShiftAndHasLength = ['a', 'b']
// const s1: CanShiftAndHasLength = 'ab' - type system error - the `string` type does not have a `shift` property.
```

## Exercise: type system introduction

1. Fix the TypeScript code in the following
  [TypeScript Playground](https://www.typescriptlang.org/play/?#code/C4TwDgpgBAKg8gETlAvFA3gEwgZwMYBOAlmMEQPYB2AXFAERgA2AhnhABbmPYFQBm5XsyihII9s2BQcnAK7coAI2h4JlAOYRMAbigBbZiCXRMRPnwgEIlKUUpQIrdlHJ48sq5TZ0AvgCg-AHpAqDBiPSIyADdoUVw-PCocKWYAZWBiDVp4JFR6dghGRnI6BKSUgDlZPWUCbMRkNABGACYAZgAWAFYyymSoZgAhci5HGlgGvIzZCADggYICQxxe-rSMu3UAQUXDety0AG06AAlC4roAGnoAdUFuOgBdOZDyRQArCDwpOJW4qHSsmwNjy6D8UAhUEozD0EH2cEu4MhlEUdQmSER-lWKUYRDY8NBSIh0NhtDoW1x3kRkKhqNorU6PSx8xw1RE4HifFkXjIVCgmHIqXIsOA7E2AAo7GBZMB4QBKDABGlmKDiuLkPhQKUy1AoNB0ZKZdR0BVgmk0xJ9UYAOmK6kllGlwGtwHIAFUwJACABhZg4CDiuVyolQHwORj+xXmyGWnA2u0Op1QAC0UDawZp-ixsZSuxAcD46SNcAIVRqlhwBKO5MpECuUAZ3WeOdCzCIBBukXYWzLtS2lEwWyLmyrUEOja61xreLrzyCIVM+HCdkkWmkbN+fn+ACVcPIpGg-AAfDAAazsmHh10sBEEtAAootBP4T+hzwOr9JgJJZJWodVaixLkeQoewBX7chRUsGAxQ0cUrFZRhZSgXdEOAU0QxwAB3SJVHgvckOtd9MAw6MITwP1oDoSgIGALDBFPZMb0EOhaDNMiLSSeNyHtBD92tZiCAzDjIVRRxT20ENMykmNKPyYBgDAZMELAJI6zYmTo1jbjeII51iOEkSlCsZgJM00MQyzPwgA).

## Exercise: `createElement` function

Consider the following TypeScript excerpt.

```typescript
function createElement(
    name: any,
    attributes: any,
    children: any
): HTMLElement {
    throw new Error("TODO")
}

// Valid usages

createElement("div")
createElement("p", null, "Hello World")
createElement("a", {href: "https://www.typescriptlang.org"}, "typescript")
createElement("button", {disabled: true}, "Press me")
createElement("ul", null,
    createElement("li", null, "Item 1"),
    createElement("li", null, "Item 2"),
)
const items = [
    "item 1",
    "item 2",
]
createElement("ul", null,
    items.map(item => createElement("li", null, item))
)

// invalid usages
createElement(true)
createElement("a", {foo: 1 })
createElement("a", {}, true)
createElement("a", {}, [true])
```

2. Change the type of the parameters so that the `any` type is *never* used,
  and:
    - The "valid usages" don't produce any compiler error.
    - The "invalid usages" produce the expected compiler error.
3. Implement the `createElement` body.

- Use the [TypeScript Playground](https://www.typescriptlang.org/play) to test
  the type-checking.

- Consider the following type.

```typescript
type Degree = "LEIC" | "MEIC"
type Student = {
    name: string,
    nbr: number,
    degree: Degree,
}
```

4. Create the following function, using the `createElement` function defined above
and representing the array of students in a table. Try not to use any DOM
functions directly.

```typescript
function render(students: Array<Student>): HTMLElement
```