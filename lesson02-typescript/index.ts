function someFunction(value: string) {
    return value.toUpperCase()
}
console.log(someFunction("ola"))

// Argument of type 'number' is not assignable to parameter of type
// 'string'.ts(2345)
// console.log(someFunction(42))

// Structural Type System

type Person = {name: string}
type Student = {name: string}

const alice: Person = { name: "Alice" }

// This works because Person and Student have the same structure, even though they have different names.
// Both require a 'name' property whose type is string.
const s: Student = alice
