# React - Introduction

Guided set of exercises to introduce the [React](https://react.dev/) library.

## Build environment and dependencies

- Create a `lab04-react` folder and move into it.
- Run `npm init` and select `module` for the package type.
- Install the production (i.e. runtime) dependencies, which, for the time being,
  are only the React libraries.

```sh
npm install --save-prod react react-dom
```

The reason why the React library is divided into at least two packages will be
clarified further on.

- Install the development (i.e. build) dependencies:
  - `vite` - the development and build tool.
  - `@types/react` and `@types/react-dom` - type information for the `react` and
    `react-dom` libraries.
    - **Q.1.** Why are the packages with type information considered development
      dependencies?

```sh
npm install --save-dev vite @types/react @types/react-dom
```

- Edit the `package.json` file to add the `dev` script. After this, running `npm
  run dev` will start the Vite development server.

```json
{
    ...
    "scripts": {
        ...
        "dev": "vite"
    },
    ...
}
```

## Starting to use the React library

- Create an `index.html` file in the root folder.

```html
<!doctype html>
<html lang="en">
  <head>
    <title>React Introduction</title>
    <script type="module" src="/src/main.ts"></script>
  </head>
  <body>
    <div id="container"></div>
  </body>
</html>
```

- Create a `main.ts` in the new `src` sub-folder, with the following content. By
  default, all TypeScript source files will be located in the `src` folder or
  sub-folders.

```ts
import { createRoot } from "react-dom/client";
import React from "react";

console.log("main starting");

const root = createRoot(document.getElementById("container")!);

root.render(
  React.createElement(
    "div",
    null,
    React.createElement("h3", null, "Hello React"),
    React.createElement(
      "p",
      null,
      "My first ",
      React.createElement("a", { href: "https://react.dev" }, "React"),
      " application"
    )
  )
);
```

- This example:
  - Creates a react root referencing a DOM element.
  - Asks the root to render a tree of React elements created via the
    `React.createElement` function.
    - A top-level `div`.
    - An `h3` nested inside this `div` with some text.
    - A `p`, also nested inside this `div`, with some text and an `a` element.

```text
div
├── h3
│   └── "Hello React"
└── p
    ├── "My first"
    └── a (href="https://react.dev")
        ├── "React"
        └── " application"
```

- Observe the resulting user interface in the browser.
- When using the React library, the user interface is defined by creating trees
  of React elements.

## The JSX syntax

- Rename the file to `main.tsx`, i.e., change the extension from `.ts` to
  `.tsx`.
  - On `index.html`, load `main.tsx` instead of `main.ts`.

- Replace the `root.render` call with the following code. Note how the argument
  to `root.render` is now an expression written in an XML/HTML-like syntax.

```ts
root.render(
  <div>
    <h3>Hello React</h3>
    <p>My first <a href="https://react.dev">React</a> application</p>
  </div>
);
```

- [JSX](https://react.dev/learn/writing-markup-with-jsx) is an extension to the JavaScript
  language, and also to the TypeScript language, allowing the definition of
  expressions using an XML-like syntax.
  - These JSX expressions are converted into JavaScript expressions by build
    tools before the JavaScript is evaluated in the browser or on other
    execution environments, such as Node. JSX is not supported directly by the
    execution environments.
  - In a current React + Vite setup, JSX is usually compiled using the modern
    JSX runtime. That means the generated JavaScript typically imports helper
    functions from `react/jsx-runtime` instead of calling
    `React.createElement` directly.
  - **Q.2.** Try to identify the meaning of the first, second, and remaining
    parameters to the `React.createElement` function and how they map into the
    JSX syntax.

- Observe the resulting user interface in the browser.

- Observe the payload of the response produced by the Vite server to the
  `/src/main.tsx` GET request, namely the argument to the `root.render` call.
  Note how that script is not using the JSX syntax anymore.
  - In current React projects, Vite usually transforms JSX into calls to helper
    functions imported from `react/jsx-runtime`.
  - `React.createElement` is still the conceptual model for this section and is
    still used directly when you call it yourself, but it is no longer the
    default output you should expect from the JSX transform.

- Use the [TS playground](https://www.typescriptlang.org/play/?#code/Q) to 
  observe the resulting JavaScript expression from bellow JSX expressions:
  - `<a href="https://react.dev/learn/writing-markup-with-jsx">JSX</a>`
  - `<a href="https://react.dev/learn/writing-markup-with-jsx"><em>JSX</em></a>`
  - `<p>2 + 3</p>`
  - `<p>{2 + 3}</p>`
  - `<ul>{["hello", "react"].map(it => <li>{it}</li>)}</ul>`
  - `<ul>["hello", "react"].map(it => <li>it</li>)</ul>`

- **Q.3.** What is the effect of adding an expression delimited by `{` and `}`
  inside a JSX expression?

- Note how a JSX expression can contain JavaScript/TypeScript expressions
  inside, which can also then contain other JSX expressions.
- Note how JSX expressions are just an alternative way of defining UI
  expressions that are compiled into regular JavaScript before execution.
- In current React projects, that generated JavaScript often uses helpers from
  `react/jsx-runtime`, even though JSX still represents the same tree of React
  elements.
- When using the React library it is very common to use the JSX syntax to define
  the trees of React elements.

## Virtual DOM

- **Q.4.** What is the result of running the following two statements by adding
  them to `main.tsx`?
  - `console.log(document.createElement("p") instanceof HTMLElement)`
  - `console.log(React.createElement("p") instanceof HTMLElement)`

- **Q.5.** What is the result of running the following statement by adding it to
  `main.tsx`?
  - `document.getElementById("container")!.appendChild(React.createElement("p"))`

- Note:
  - Elements created by `React.createElement` are not of the same type as the
    elements created by `document.createElement`.
  - Elements created by `React.createElement` cannot be added to the DOM.

- The function `React.createElement` does not create DOM elements. Instead, it
  creates Virtual DOM elements.
  - DOM - tree of elements and other nodes, created via the `document` object,
    namely `document.createElement`.
  - Virtual DOM - tree of elements, created via the `React.createElement`
    function.

- The relation between the DOM and the Virtual DOM will be explored below.
  Before that, another example.

- Create a new file called `mutationObserver.ts` with the following content.

```ts
export function createMutationObserver() {
  return new MutationObserver((mutationsList, observer) => {
    for (const mutation of mutationsList) {
      if (mutation.type === "childList") {
        mutation.addedNodes.forEach((node) => {
          if (node.nodeType === Node.ELEMENT_NODE) {
            console.log("Added node", node);
          }
        });
        mutation.removedNodes.forEach((node) => {
          if (node.nodeType === Node.ELEMENT_NODE) {
            console.log("Removed node", node);
          }
        });
      }
    }
  });
}
```

This function will allow us to observe mutations to the DOM.

- Create a new `main2.tsx` with the following code.
  - Do not forget to import `createMutationObserver` and initialize the `root`
    variable.

```ts
// The model
type Model = {
    // Just a list of strings
    readonly items: Array<string>;

    // And an incrementing ID
    readonly nextId: number;
};

// Function to generate the next model instance
function nextModel(model: Model): Model {
    // if the size is greater than five, then remove the oldest item
    const prev = model.items.length >= 5
        ? model.items.slice(1)
        : model.items
    // return a new model object
    return {
        nextId: model.nextId + 1,
        items: prev.concat(`item-${model.nextId + 1}`)
    }
}

// Function to produce a view given a model,
// where the view is a React virtual node tree represented by the root element
function computeView(model: Model) {
    return (
        <ul>
            {model.items.map((it) => (
                <div key={it}>
                    <p>{it}</p>
                    <input type="text" />
                </div>
            ))}
        </ul>
    );
}

(() => {
    // The model instance
    let model: Model = {
        nextId: 0,
        items: [],
    };

    // Update the model and re-render every two seconds
    setInterval(() => {
        model = nextModel(model);
        const view = computeView(model);
        root.render(view);
    }, 2000);

    // Just a way to observe mutations to the (real) DOM
    const observer = createMutationObserver();
    observer.observe(document.getElementById("container")!, {
        childList: true,
        subtree: true,
    });
})();
```

- Note how `root.render` is called every two seconds with a new React element
  tree, i.e. Virtual DOM, with the view for the current model.
- Note how there aren't any mutations to the Virtual DOM. A new virtual DOM is
  created on every two seconds interval.
- Observe the resulting user interface in the browser, as well as the messages
  in the browser's console.
  - **Q.6.** Write something in an input box and check if the content is cleared
    after the interval. Does that suggest that the `input` DOM element was
    deleted or not deleted after the interval?
  - **Q.7.** On each two second interval, how many `div` elements are being
    added and removed from the DOM?
  - **Q.8.** Remove the `key={it}` property from the `div` element in the
    `computeView` function. Repeat the same experiment of Q.6. and observe the
    behavior. What changed? Why?

- Read [Describing the UI](https://react.dev/learn/describing-the-ui) and
  [Preserving and Resetting State](https://react.dev/learn/preserving-and-resetting-state).

- When using the React library we produce views, defined as React elements, to
  express the final state of the user interface, without considering the current
  state of the user interface.
  - No mutations are done directly to the DOM.
  - When producing the virtual DOM to reflect the current model state, the
    previous state doesn't need to be considered.
  - It is the React library that, using both the current DOM state and the new
    Virtual DOM, performs the set of required mutations to the DOM so that it
    starts reflecting the new Virtual DOM: reconciliation.

## React Components

- Create the following function.

```ts
function Item({ label }: { label: string }): ReactElement {
  return (
    <div>
      <p>{label}</p>
      <input type="text" />
    </div>
  );
}
```

- Replace the `computeView` function with:

```ts
function computeView(model: Model): ReactElement {
  return (
    <ul>
      {model.items.map((it) => (
        <Item key={it} label={it} />
      ))}
    </ul>
  );
}
```

- The function `Item` is a React component.
  - This function receives an object called the properties, or props for short,
    which in this case contains a `label` property.
  - React components are functions from properties, props, to React elements.
  - React components typically represent reusable user interface blocks.

- React components can be used instead of primitive HTML elements when defining
  React element trees.
  - Note how the `computeView` function now uses `<Item label={it} />`.
  - This JSX expression is equivalent to `React.createElement(Item, { label: it
    })`.
  - **Note that `computeView` does not call the `Item` function; it creates a
    React element that refers to the `Item` function instead.**

- Component functions should be pure.
  - The goal of a React component is to map properties, and other things, into a
    React element tree.
  - Application code never calls React components. Instead, application code
    creates React elements that refer to React components.
  - It is the React library that calls the React component functions.
  - This calling is not deterministic, i.e. it is not possible to know exactly
    when and how many times React calls a component function. This is why
    component functions should be pure, i.e. free from side-effects.
