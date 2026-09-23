# Introduction to React Effects

## Introduction

This document contains an introduction to React's effect management, namely the `useEffect` *hook* function.

## A `Timer` component

Let's start with the challenge of creating a component to represent a very simplified timer, containing:

* A label.
* The current count value, automatically incremented every second.

A way to accomplish this task is to use the [`setInterval`](https://developer.mozilla.org/en-US/docs/Web/API/Window/setInterval) function:

* The component will have a `number` *state item* holding the count value, used to present the current value in the UI, and defined using the `useState` *hook* function.
* The callback provided to the `setInterval` call will use the state setter function returned by `useState` to increment the count value every second.
* The value returned by `setInterval` needs to be used to cancel the interval when it is no longer needed, by providing it as an argument to a `clearInterval` call.

The challenge in this task is where to call `setInterval` and `clearInterval`:

* The `setInterval` needs to be called every time a component is placed in the render-tree.
* The `clearInterval` needs to be called once that placement is removed.
* We cannot call `setInterval` directly during the render of the component, since the component function needs to be free of any side effects. In particular, we don't know how many times that function is called and we don't want to end up with an uncontrolled number of active _intervals_ running.
* The callback provided to `setInterval` needs to have access to the state setter.

Let's start by writing the example application that uses this timer.
```typescript
import React, { useState } from "react";
import { Timer } from "./Timer.tsx";
export function App() {
    const [observedShowSecondTimer, setShowSecondTimer] =
    useState<boolean>(false);
    const buttonText = observedShowSecondTimer
    ? "disable second timer"
    : "enable second timer";
    return (
    <div>
        <div>
        <button onClick={() => setShowSecondTimer(!observedShowSecondTimer)}>
            {buttonText}
        </button>
        </div>
        <Timer label="First timer." />
        {observedShowSecondTimer && <Timer label="Second timer." />}
    </div>
    );
}
```
To make things more interesting, the first `Timer` placement is unconditional, while the second placement is conditional, depending on a boolean state item managed by `App`.
A click on the button toggles this boolean state.
The _conditional placement_ of a component is achieved by the following expression

```typescript
{observedShowSecondTimer && <Timer label="Second timer." />}
```
Remember the behavior of the `&&` operator in JavaScript:

* If the first operand is *falsy*, then the second operand to the `&&` is not evaluated and the overall expression evaluates to the value of the *first* operand, which in this case is `false` and is accepted (and ignored) as a `createElement` child.
* If the first operand is *truthy*, then the overall expression evaluates to the value of the second operand, that is `<Timer label="Second timer." />`.

> **Caution:** Since the result is the value of the first operand, a *falsy* operand that is not a boolean may end up being rendered.
For instance, `{items.length && <List/>}` shows a `0` when the array is empty, because `0` is *falsy* but, unlike `false`, is *not* ignored as a child.

Remember also that we can use JavaScript _expressions_ inside JSX expressions but not _statements_, which is why an `if` statement cannot be used.

Note also how the button text changes depending on the boolean state.

The following code defines a first attempt at the `Timer` implementation, using the `useEffect` *hook* function.
```typescript
import React, { useState, useEffect } from "react";

type TimerProps = {
    label: string;
};

export function Timer(props: TimerProps) {
    const [observedCount, setCount] = useState(0);
    console.log("Render:", { ...props, observedCount });
    useEffect(() => {
    console.log("Effect:", { ...props, observedCount });
    const intervalId = setInterval(() => {
        console.log("setInterval callback:", { ...props, observedCount });
        setCount(observedCount + 1);
    }, 1000);
    return () => {
        console.log("Effect cleanup:", { ...props, observedCount });
        clearInterval(intervalId);
    };
    }, []);
    return (
    <div>
        <h2>{props.label}</h2>
        <p>{observedCount}</p>
    </div>
    );
}
```

The `useEffect` function receives an _effect function_ as the first argument and ensures that function is executed a controlled (deterministic) number of times, outside the render.
If the second argument is an empty array, then the _effect function_ will be called by React exactly once for each placement of the hosting component (i.e. the `Timer` component) in the render-tree.

> **Important:** the `useEffect` function and _effect function* (also called the *effect callback_) are two distinct things. In the previous code, the _effect function_ is the lambda expression passed as the first argument to `useEffect`.

> **Important:** the `useEffect` function will be called on *every* render of the component. The _effect function* will be called *once_ for each placement of the component in the render-tree (when using an empty dependency array).

The _effect function_ can return either `undefined` or a _cleanup function_.
In the second case, the _cleanup function_ is called:

* Before each subsequent execution of the _effect function_.
* When the component is no longer placed in that position in the render-tree.

### The `useEffect` call

There is a lot going on in the following statement:
```typescript
    useEffect(() => {
    console.log("Effect:", { ...props, observedCount });
    const intervalId = setInterval(() => {
        console.log("setInterval callback:", { ...props, observedCount });
        setCount(observedCount + 1);
    }, 1000);
    return () => {
        console.log("Effect cleanup:", { ...props, observedCount });
        clearInterval(intervalId);
    };
    }, []);
```

* `useEffect` is called with two arguments: 
    * The _effect function_ defined via a lambda expression.
    * The empty _dependencies_ array (more on this dependencies array later).

* The _effect function_ calls `setInterval`, providing two arguments:
    * The _interval callback function_, called every second, also defined via a lambda expression. This function calls the setter with `observedCount + 1`.
    * The interval period in milliseconds.

* Finally the _effect function_ returns a _cleanup function_ (returns the function, does *not* call the function), also defined via a lambda expression. 
When called, this _cleanup function_ clears the interval, stopping the periodic calls to the previously provided _interval callback function_.

Now it is a good time to run the application, observe its UI and console log.

A bit surprisingly, the shown value updates from `0` to `1` after one second but remains at this value afterwards.
The observation of the console logs provides some more useful information:

* There is an initial render for the component
```
Render: {label: 'First timer.', observedCount: 0}
```
* This is immediately followed by the _effect function_ running, as a consequence of the `useEffect` call in the initial render.
```
Effect: {label: 'First timer.', observedCount: 0}
```
* After one second, the `setInterval` callback runs and updates the state with `observedCount + 1`, that is `0 + 1`.
```
setInterval callback: {label: 'First timer.', observedCount: 0}
```
* As a consequence of the state update, React renders the component again, now with `observedCount` holding `1`.
```
Render: {label: 'First timer.', observedCount: 1}
```
* Even though `useEffect` is called again in the second render, that doesn't trigger another call to the _effect function_ since `useEffect` is ensuring a single deterministic call per component placement in the render-tree.
This is exactly what is intended, since the first `setInterval` will ensure a repeating periodic call to the provided callback. `setInterval` only needs to be called once. In fact, after another second, the callback runs for the second time, as expected.
```
setInterval callback: {label: 'First timer.', observedCount: 0}
```
* Perhaps a bit surprisingly, `observedCount` is `0` on the second execution of the `setInterval` callback, *after* the first callback execution changed the state from 0 to 1. 
As a consequence, the second callback sets the state to `0 + 1`, and not `1 + 1`, resulting in the state remaining unchanged.

* The log reveals that the `setInterval` callback keeps being called every second, but it always sets the state to `0 + 1` because it sees `observedCount` set to `0`.

This last observation explains why the count value remains stuck at `1`.
What remains to be explained is why `observedCount` is `0` in the `setInterval` callback.
To do so, it is important to remember that `observedCount` is both _constant_ and *local* to the `Timer` function.
Each `observedCount` does _not_ change, but every execution of the `Timer` function will have a distinct `observedCount`.
Given this, we can ask which of these `observedCount` values is being used by the `setInterval` callback.
The answer resides in when this callback function was defined.
Going back to the source code, we see that the callback function was defined as a lambda expression inside the _effect function_ lambda expression passed as argument to the `useEffect` in the *first* render.
```typescript
    useEffect(() => {
    console.log("Effect:", { ...props, observedCount });
    const intervalId = setInterval(() => {
        console.log("setInterval callback:", { ...props, observedCount });
        setCount(observedCount + 1);
    }, 1000);
    ...
    }, [])
```
This means that the callback function captures the _constant_ `observedCount` observed during the first render and uses it on *every* periodic call.

> **Note:** The reader may be wondering what happened to the _effect functions_ passed to the `useEffect` calls in the _second and subsequent renders. 
The answer is that they were ignored because `useEffect` (with an empty dependency array) ensures the effect runs only once.

With the observed behavior explained, the following question is how to change the source code in order to obtain the desired behavior.
A possible answer is to use an overload of the *state setter* function that receives an _update function_ instead of the desired new value.
```typescript
setCount(currState => currState + 1);
// instead of setCount(observedCount + 1)
```
This overload avoids the stale captured `observedCount` and asks React to increment the state based on the current state value.

> **Note:** The `currState` in the `currState + 1` is the name bound to the function input and its name can be anything. 
For instance `setCount(it => it + 1)` would have exactly the same result.

The final form for the effect is
```typescript
    useEffect(() => {
    console.log("Effect:", { ...props, observedCount });
    const intervalId = setInterval(() => {
        console.log("setInterval callback:", { ...props, observedCount });
        setCount(currState => currState + 1);
    }, 1000);
    return () => {
        console.log("Effect cleanup:", { ...props, observedCount });
        clearInterval(intervalId);
    };
    }, []);
```

> **Important:** The error in the first `Timer` implementation, due to the usage of a stale state observation from a lambda expression environment capture, is not a detail specific to the timer functionality.
On the contrary, it is something that we need to always look for on any React-based solution.
React ensures the render function is called when either the state or the props change.
We, as component authors, need to ensure that stale values for those state or props observations are _not_ used.
The common cause for this is capturing values from a render into lambda expressions, whose functions are evaluated in the future, when those values are no longer valid.

### Enabling and disabling the second timer

Let's turn our attention to what happens when we enable and disable the second timer.
From a UI perspective, everything seems to behave as intended:

* The second timer only appears when enabled and disappears when disabled.
* The value shown on the second timer is independent of the first timer value.

Looking into the console log messages, we observe:

* When the second timer is enabled, a second callback starts to run every second.
* There are also two periodic renders, one for each `Timer` placement in the render-tree.
* When the second timer is disabled, the cleanup function runs and stops the associated interval. 
After this, only the `setInterval` for the first timer runs.
This means that there is no *resource leakage*: a timer that was removed from the render-tree closes all external resources (i.e. the interval). 

> **Note:** The _effect function_ created a resource (the interval) that is not managed by React. It is therefore essential that the component also explicitly closes that resource when the component is no longer placed in that position of the render-tree.

When a component is placed in a specific position of the render-tree, we say that the component was _mounted_.
When a component is no longer present in that position, we say that the component was _unmounted_.

The only surprising aspect may be that the second timer starts at zero once it is re-enabled.
This is because React discards the state when a component is _unmounted_ and starts with the initial state when a component is _mounted_.

## Making the period configurable

Let's consider the evolution of the `Timer` component where the period is configurable.
The first change is in the props type:
```typescript
type TimerProps = {
    label: string;
    period: number; // new property
};
```

Then we use the `period` property when setting the interval period:
```typescript
(...)
    const intervalId = setInterval(() => {
        console.log("setInterval callback:", { ...props, observedCount });
        (...)
    }, props.period); // instead of 1000
(...)
```

Finally we change the `App` component to have a way to provide different periods.
```typescript
import React, { useState } from "react";
import { Timer } from "./Timer.tsx";
export function App() {
    const [observedShowSecondTimer, setShowSecondTimer] =
    useState<boolean>(false);
    const [observedPeriod, setPeriod] = useState<number>(2000);
    const buttonText = observedShowSecondTimer
    ? "disable second timer"
    : "enable second timer";
    return (
    <div>
        <div>
        <select
            value={observedPeriod}
            onChange={(ev) => setPeriod(Number(ev.target.value))}
        >
            <option value="100">100 ms</option>
            <option value="1000">1000 ms</option>
            <option value="2000">2000 ms</option>
            <option value="4000">4000 ms</option>
            <option value="8000">8000 ms</option>
        </select>
        <button onClick={() => setShowSecondTimer(!observedShowSecondTimer)}>
            {buttonText}
        </button>
        </div>
        <Timer label="First timer." period={observedPeriod}/>
        {observedShowSecondTimer && <Timer label="Second timer." period={observedPeriod}/>}
    </div>
    );
}
```
Note the new `observedPeriod` constant and `setPeriod` setter.
Note also the `select` used in the UI to change the desired period.

However, manually testing the changed component and the driving application seems to suggest that the component always uses the initial period.
An observation of the console logs shows that the `period` property provided to the render call is indeed changing; however, the update period does not.

This behavior is explained by the component capturing and using the initial period when creating the interval via the `setInterval`, and failing to react to a change in the period.
Remember that the _effect function_ is running only once, when the component is mounted.

It is now time to use the `useEffect` function's second parameter, which defines the _effect dependency array_.
Every time the `useEffect` function is called for the same render-tree position, React will compare this array with the array provided in the previous call, element by element and using `Object.is`.
For this comparison to be well defined, the array must have the same length on every call.
If any of the elements is different, React will schedule the cancellation of the previous effect, by running its cleanup function, and also schedule the execution of the new effect.

> **Note:** This comparison is *shallow*: a dependency holding an object, an array, or a function that is recreated on every render is always considered different from the one in the previous call, resulting in the effect being cancelled and executed again on every render.

If the dependency array is always empty, then it never changes between `useEffect` calls and there is only one execution of the effect for the whole lifetime of the component placement.

Given this, replace the empty array with an array containing the `props.period` dependency.

```typescript
    useEffect(() => {
    console.log("Effect:", { ...props, observedCount });
    (...)
    }, [props.period]); // instead of []
```

Observe the UI behavior after this change and notice how the period selection is reflected immediately in the timer increment period.

Observe also the console logs, namely:

* How there is a render immediately after a new period is defined.
* How that render is followed by the _effect cleanup_ and a new execution of the _effect function_, with the new `props.period` value.

## Summary

* Component functions must be free of side effects: React decides when and how many times they are called, so anything that creates or mutates state outside React (e.g. an interval, a subscription, a network request) cannot be done during the render.
* The `useEffect` *hook* function is the way to associate such side effects with a component. It receives an _effect function_ as the first argument and an _effect dependency array_ as the second one.
* `useEffect` and the _effect function_ are distinct things: `useEffect` is called on *every* render, however the _effect function_ it receives is not. React decides when to call it, based on the dependency array.
* With an empty dependency array, the _effect function_ is called exactly once for each placement of the component in the render-tree.
* The _effect function_ can return a _cleanup function_, called before each subsequent execution of the _effect function_ and when the component placement is removed from the render-tree. Resources created by the effect and not managed by React must be released there, otherwise they leak.
* On every `useEffect` call for the same render-tree position, React compares the dependency array with the one from the previous call, element by element and using `Object.is`. If any element is different, React runs the previous _cleanup function_ and then the new _effect function_. The array must have the same length on every call.
* Every *props* or *state* observation used by the _effect function_ should be present in the dependency array. Otherwise the effect keeps using the values observed when it last ran, which is why the `Timer` ignored the `period` changes until `props.period` was added as a dependency.
* Each *state* or *props* observation is a _constant* that is *local_ to one execution of the component function. A lambda expression defined during a render captures the observations of *that* render and keeps using them whenever it is called in the future, even after new renders have occurred.
* This _stale observation_ problem is not specific to timers: it applies to any lambda expression that outlives the render where it was created, such as interval or event callbacks. Component authors are responsible for ensuring that stale observations are not used.
* When the new state depends on the current state, use the *state setter* form that receives an _update function_ (e.g. `setCount(currState => currState + 1)`), instead of computing the new value from a captured observation.
* Placing a component in a position of the render-tree is called _mounting_ it, and removing it from that position is called _unmounting_ it. React discards the associated state on unmount, which is why a re-enabled `Timer` starts at zero again. Re-running an effect because its dependencies changed is *not* an unmount, so the state is preserved.
* A component can be _conditionally placed_ in the render-tree with the `&&` operator, since JSX expressions can contain JavaScript _expressions_ but not _statements_.

## Additional exercises

* Add a pause/resume single button to the `Timer` component. The button should present different text, depending on the pause/resume state.

## Additional resources

* From the official [React documentation](https://react.dev):
    * [Synchronizing with Effects](https://react.dev/learn/synchronizing-with-effects).
    * [You Might Not Need an Effect](https://react.dev/learn/you-might-not-need-an-effect).
    * [Lifecycle of Reactive Effects](https://react.dev/learn/lifecycle-of-reactive-effects).
    * [Separating Events from Effects](https://react.dev/learn/separating-events-from-effects).
    * [Removing Effect Dependencies](https://react.dev/learn/removing-effect-dependencies).
