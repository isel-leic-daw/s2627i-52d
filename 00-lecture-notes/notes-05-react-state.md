# Introduction to React State Management

## Introduction

This document contains an introduction to React's state management, namely the `useState` hook function.

## A `Counter` component

Let's start with the challenge of creating a component to represent a counter, containing:

* A label.
* The current count value.
* A button to increment the value.

The following code defines a possible implementation using the `useState` function, which is the main subject of this lecture note.

```typescript
export function Counter({ label }: { label: string }) {
  const [observedCount, setCount] = useState<number>(0);
  console.log("render", { label: label, observedCount: observedCount });
  return (
    <div>
      <h2>{label}</h2>
      <p>Counter: {observedCount}</p>
      <button onClick={() => setCount(observedCount + 1)}>Up</button>
    </div>
  );
}
```

This component can then be used by an application in the following way.

```typescript
export function App() {
  return (
    <div>
      <h1>State</h1>
      <Counter label="First counter" />
      <Counter label="Second counter" />
    </div>
  );
}
```

Note how an application can use the same `Counter` component multiple times in different *render-tree positions*, with the expectation that they will operate independently.
An increment on the usage labelled `First counter` must not change what is shown on the usage labelled `Second counter`.

- **Q.1.** When the application is first loaded, how many times is the `Counter` function called? Why?
- **Q.2.** If you click the `Up` button on `First counter`, which parts of the user interface should change and which parts should remain unchanged?
- **Q.3.** What values do you expect to see in the console after:
  1. the initial render;
  2. one click on the first counter;
  3. one click on the second counter?

> **TIP:** Try out the previous example and observe how `Counter` components in different positions work independently. Observe the browser's console and note what happens when the application is loaded and what happens when each button is clicked.

> **NOTE:** The *render-tree position* terminology is taken from [the React official documentation](https://react.dev/learn/preserving-and-resetting-state).

Returning to the `Counter` component, except for the initial `useState` usage, the rest of the function is rather straightforward.
It returns a `div` containing an `h2` for the label, a `p` for the current count value, and a `button` with an `onClick` handler to increment the value.
The essence of this function resides in the `useState` call, which we explore next.
Before that, it is interesting to consider other ways of apparently solving this problem, which seem simpler at first sight but are incorrect.

### State as a local variable

We could be tempted to use a local variable to hold the count.

```typescript
export function Counter({ label }: { label: string }) {
  let counter = 0
  ...
  <button onClick={() => counter += 1}>Up</button>
  ...
  );
}
```

The first problem is the variable lifetime: it exists in the context of a `Counter` call.
A new call to `Counter` would create a new variable (with the same name), initialized to zero.
We need to store the counter, that is the component state, in a place that is shared between calls to `Counter`.

The second problem is the lack of reaction to a change in the variable.
The `counter += 1` is not observed by the React library and does not trigger a new render of the component, that is, a new `Counter` call.

- **Q.4.** Why does updating a local variable inside the `onClick` handler not change what is shown on the screen?
- **Q.5.** If the component function is called again, what happens to the local variable used to hold the counter value?

### State as a module variable

Given the first problem above, we could be tempted to use a global module variable to hold the count.

```typescript
let counter = 0
export function Counter({ label }: { label: string }) {
  ...
  <button onClick={() => counter += 1}>Up</button>
  ...
  );
}
```

Now we have a different problem: it is impossible to have two usages of the `Counter` component with independent counters, since there is only one variable to hold it.
In addition, the second problem (lack of reactivity) remains unsolved: an assignment to `counter` does not trigger a new `Counter` call, that is, does not trigger a new render.

- **Q.6.** Why does a module-level variable solve the lifetime problem but fail to provide independent state for each `Counter` usage?
- **Q.7.** If both counters use the same module-level variable, what behavior would you expect after clicking the button on only one of them?

> **TIP:** Try out each of the incorrect solutions and observe their behavior.

These two failed attempts help to clarify the requirements we need to solve this state management problem.
We need a way to store state:

* That can be shared between renders of a component in the *same render-tree position*.
* That is independent between two distinct *render-tree positions* of the same component.
* Whose change triggers a re-render of the component on the affected *render-tree position*.

This is exactly what `useState` provides us.

## Render-tree position state

To understand how `useState` operates it is fundamental to know that the React library manages a data structure associated to each render-tree position of a component.
The details of this data structure are not well documented, so we will use a simplified model, precise enough to explain how `useState` behaves.

The initial data structure, before any render occurred, can be modelled as:

```typescript
// data associated to the first render-tree position
{
    component: Counter,
    props: {
        label: "First counter"
    },
    state: [
        // empty
    ]
}
// data associated to the second render-tree position
{
    component: Counter,
    props: {
        label: "Second counter"
    },
    state: [
        // empty
    ]
}
```

In order to know how to show the two positions, React will call the `Counter` function twice, once for each position.

```typescript
// Call associated to the first position
call to Counter({label: "First counter"})
    call useState<number>(0)
```

The nested `useState` call will inspect the `state` property of the associated data structure and see if there is anything in the first slot (because it is the first call to `useState`).
Since the `state` array is empty, it will create a new entry using the `0` value (i.e. the value provided to `useState`).

```typescript
// data associated to the first render-tree position
{
    component: Counter,
    props: {
        label: "First counter"
    },
    state: [
        {value: 0} // state entry is created
    ]
}
```

The `useState` call then returns the pair `[0, setter_0]`, where `setter_0` is a function to set the value of the first state slot in the *first* render position.
This means that `observedCount` and `setCount` receive `0` and `setter_0` respectively.
It also means that the `onClick` handler will call `setter_0(0 + 1)` when the button is clicked.

Something similar happens when the `Counter` function is called for the second render-tree position.

```typescript
// Call associated to the second position
call to Counter({label: "Second counter"})
    call useState<number>(0)
```

The nested `useState` call inside it updates the associated data structure:

```typescript
// data associated to the second render-tree position
{
    component: Counter,
    props: {
        label: "Second counter"
    },
    state: [
        {value: 0} // state entry is created
    ]
}
```

This nested `useState` call returns `[0, setter_1]`, where `setter_1` is a function to set the value of the first state slot in the *second* render position.

When a user clicks the button on the first render position, this results in `setter_0(0 + 1)` being executed, which updates the state in the associated data structure.

```typescript
// data associated to the first render-tree position
{
    component: Counter,
    props: {
        label: "First counter"
    },
    state: [
        {value: 1} // state entry is updated due to `setter_0(1)`
    ]
}
```

The call to `setter_0` also triggers a new call to the `Counter` function, associated to the first render position, because its state changed.

```typescript
// Call associated to the first position
call to Counter({label: "First counter"})
    call useState<number>(0)
```

Now, the nested `useState` call will see that `state` already has an entry in the first slot and returns its value.
That is, the `useState` function returns `[1, setter_0]` and `observedCount` gets the value `1`.

Note how the naming of the local constant as `observedCount` and not as `counter` reflects what is happening.
The constant `observedCount` is *not* the mutable counter state.
Instead, it is an immutable observation of the current state for the associated render tree position.

- **Q.8.** In the expression `const [observedCount, setCount] = useState<number>(0)`, what is the role of:
  1. `observedCount`;
  2. `setCount`;
  3. the value `0`?
- **Q.9.** Why is `observedCount` described as an observation of the current state instead of the state itself?
- **Q.10.** On a second render of the same component position, does `useState<number>(0)` create a new state entry again? Why or why not?

## Multiple state entries per render-tree position

Consider the following evolution of the `Counter` component, that also captures and stores the last timestamp when the button was clicked:

```typescript
import { useState } from "react";

function dateStringFrom(date: Date) {
  return `${date.getHours()}:${date.getMinutes()}:${date.getSeconds()}`
}

export function Counter2({ label }: { label: string }) {
  const [observedCount, setCount] = useState<number>(0);
  const [observedTimestamp, setTimestamp] = useState<Date | null>(null);
  console.log("render", {
    label: label,
    observedCount: observedCount,
    observedTimestamp: observedTimestamp,
  });
  const lastClicked = observedTimestamp
    ? dateStringFrom(observedTimestamp)
    : "never";
  const handleClick = () => {
    setCount(observedCount + 1);
    setTimestamp(new Date());
  };
  return (
    <div>
      <h2>{label}</h2>
      <p>Counter: {observedCount}</p>
      <p>Last clicked on: {lastClicked}</p>
      <button onClick={handleClick}>Up</button>
    </div>
  );
}
```

Note how the `Counter2` component now uses two independent state *items*:

```typescript
const [observedCount, setCount] = useState<number>(0);
const [observedTimestamp, setTimestamp] = useState<Date | null>(null);
```

* The first item is the current counter value.
* The second item is the timestamp of the last click.

Since each `useState` call is matched to a state slot by its *call order*, the sequence of `useState` calls must be exactly the same on every render of a component.
If it isn't, the slots no longer line up: on `Counter2`, a render where the `setTimestamp` call happens to run first would match it to slot `0`, which holds the counter value, and `observedTimestamp` would end up with a number instead of a date.
This is why hook functions, such as `useState`, cannot be called conditionally, inside loops, or after an early `return`.

- **Q.11.** After one button click on `Counter2`, which state values are expected to change?
- **Q.12.** Why must the order of `useState` calls remain the same in every render?
- **Q.13.** What could go wrong if a `useState` call were placed inside an `if` statement?

> **NOTE:** This requirement is part of the [Rules of Hooks](https://react.dev/reference/rules/rules-of-hooks) in the official React documentation.

Modify the `App` to use `Counter2` instead of `Counter` and observe its behavior.

Using the simplified model, show the contents of the data structure associated to the first render-tree position:

* Before the first render.
* After the first render.
* After the button is clicked.
* After the second render, following the button click.

To complement this lecture note, we recommend the reading of the [Preserving and Resetting State](https://react.dev/learn/preserving-and-resetting-state) page in the official React documentation.

## Additional exercises

### `Counter` component

Create a `Counter` component with the following *props* type.

```typescript
type CounterProps = {
  label: string;
  onIncrement?: (newValue: number) => void;
};
```

This component should present:

* A textual label.
* A counter value, initialized with zero.
* A button to increment the value.

If `onIncrement` is present in the *props* object, then this function should be called every time the counter is updated.

### `InputBox` component

Create an `InputBox` component with the following *props* type.

```typescript
type InputBoxProps = {
  label: string;
  minLength: number;
  maxLength: number;
  onSubmit: (value: string) => void;
};
```

This component should present:

* The provided label.
* An `input` element with type `"text"` to collect a string.
* A submit button to submit that string.

The submit button should only be enabled when the input contents satisfy the `minLength` and `maxLength` requirements.

When the submit button is clicked, the `onSubmit` function should be called with the input's content.

An error message should be shown when the input is not empty and does not satisfy the `minLength` and `maxLength` requirements.

### `CounterList` component

> **NOTE:** This exercise suggests the use of the `useReducer` hook. See [Extracting State Logic into a Reducer](https://react.dev/learn/extracting-state-logic-into-a-reducer) for more information about this state management hook.

Create a component with an empty *props* type to show a list of counters.
This component should present:

* A list with all counters, including a button to remove each counter.
* An `InputBox` to receive a counter label. When this input is submitted, a new counter should be created with that label and added to the list.
* The sum of all the counter values.

Implement this component using the `useReducer` hook.
The state should have the `State` type.

```typescript
// State for an individual counter
type CounterState = {
  label: string;
  value: number;
};
// State for the complete component
type State = {
  sum: number;
  counters: Array<CounterState>;
};
```

The following `Action` type defines the possible actions:

```typescript
// Type of the possible actions
type Action =
  | { type: "addCounter"; label: string }
  | { type: "removeCounter"; index: number }
  | { type: "valueUpdate"; index: number; newValue: number };
```

Start by defining the reducer function, then define the component.
