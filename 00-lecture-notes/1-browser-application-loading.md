## Introduction

Sequence of exercises to illustrate the loading and execution of scripts in the
browser, as well as the `document` API.

## Requirements

- Ensure [Node.js](https://nodejs.org/en/download) is installed on your machine.
    - Check installation by running `node --version` and checking the returned
      version.
- Install [serve](https://www.npmjs.com/package/serve) as a way to easily serve
  files from a local folder via the HTTP protocol.
    - Install `serve` via NPM - `npm install --global serve`.
    - Check installation by running `serve --version`.

## Loading of document and scripts in browsers

- Create an empty folder and locate or open a shell on it.

- Create the `index.html` file with the following contents.

```html
<!DOCTYPE html>
<html>
    <head>
    </head>
    <body>
        <h1 id="main-heading">Hello World</h1>
    </body>
</html>
```

- Start `serve` on the created folder
    - E.g. `serve .`

- Open the browser in `http://localhost:3000` (confirm port on the information
  produced by `serve`).
    - Observe the result in the browser.
    - Open the browser's development tools and observe the network requests.
    - Notice how a `GET` to `/` returns the content of `index.html` in the
      response message body.
    - Rename the HTML file to `index2.html` and see the result in the browser.
        - 1. What is the "special meaning" that `index.html` has to the *server*?
        - 2. Does it have a special meaning to the browser?
    - Rename the HTML file back to `index.html`.

- Change the address to `http://localhost:3000/index.html` and observe the
  result.

- Create a file `s1.js` with

```javascript
console.log("I'm s1")
const mainHeading = document.getElementById("main-heading")
console.log(mainHeading)
const something = "hello"
console.log(something)
```

- Add `<script src="s1.js"></script>` to the HTML file `head` element.
  - Observe the result in the console.
  - 3. What is `document`?
  - 4. What does `document.getElementById("main-heading")` do?
  - 5. Why is `console.log(mainHeading)` presenting `null`?

- Create a file `s2.js` with

```javascript
console.log("I'm s2")
const something = "world"
console.log(something)
```

- Add `<script src="s2.js"></script>` to the HTML file `head` element.
  - Observe the result of reloading the HTML document.
  - 6. Why is there an error in the console?

- Add the attribute `type="module"` to the `script` element for `s1.js`.
  - 7. What is the value presented for `console.log(mainHeading)`?
  - 8. Does the previous error still exist? Why?
  - 9. What appears first: `I'm s1` or `I'm s2`?

## JavaScript modules

- Create a file `add.js` with

```javascript
console.log("Look, I'm running in a browser")
export function add(x,y){
    return x + y;
}
```

- Add `<script src="add.js"></script>` to the HTML file `head` element.
  - Observe the error in the console.
  - Remove `add.js` from `index.html`.

- Add the following to the beginning of both `s1.js` and `s2.js`

```javascript
import { add } from './add.js'
console.log(add(1,2))
```

- Make sure that the loading of `s1.js` has the `"module"` type and the loading of `s2.js` does not.
  - 10. What is the error occurring during the document loading and why does it happen?
- Add the `"module"` type to the loading of `s2.js`.
- Observe the output in the console.
  - 11. Is the error still occurring?
  - 12. Is the result of the call to `add` being shown correctly?
  - 13. How many times is the message `Look, I'm running in a browser` presented?
- Observe all HTTP requests made by the browser?
  - 14. How many requests to `add.js` are there?
  - 15. How many HTTP requests would there be if the application used `N` modules?

## Theory

The exercises above surface behavior from three different layers: how a plain
HTTP static file server resolves requests, how the browser parses and executes
`<script>` elements, and how the ECMAScript module system (ESM) behaves once
scripts opt into `type="module"`. This section explains the *why* behind what
was observed.

### Server behavior: static file serving and the "index" convention

- When `serve` receives a `GET /` request, it does not know anything about "the
  application" - it just maps the request path to something inside the served
  folder.
- Mapping a *directory* path (like `/`) to a single file to return is ambiguous,
  so static file servers adopt the convention of looking for a file literally
  named `index.html` in that directory and returning its contents when it
  exists. This is a *server-side, tool-specific convention* (also followed by
  servers like Apache and nginx), not a rule defined by HTTP itself.
- This is why renaming `index.html` to `index2.html` changes what `GET /`
  returns: the server no longer finds a default document for that directory
  (`serve` falls back to showing a directory listing instead of page content).
- The browser has no notion of `index.html` being special. It only understands
  "I requested path `/`, I received a response with a `text/html` content type,
  I will parse it as HTML". Requesting `http://localhost:3000/index.html`
  explicitly names the resource and therefore works independently of the
  server's default-document logic - it succeeds as long as a file with that
  exact name exists.

### Browser behavior: parsing and script execution

- HTML parsing builds the DOM tree incrementally, from the top of the document
  downwards.
- A *classic* script (`<script src="...">` without `type="module"`, `async` or
  `defer`) blocks the parser: when the parser reaches the element, it pauses DOM
  construction, fetches (if needed) and runs the script synchronously, then
  resumes parsing.
- `document` is the object, provided by the browser to any script running in the
  page, that represents the DOM of the page currently loaded in that browsing
  context (the root of the parsed tree). `document.getElementById(id)` looks up,
  in that tree *as it currently exists*, the element with the given `id`,
  returning `null` if no such element is present yet (it does not wait for one
  to appear later).
- Because `s1.js` and `s2.js` are declared in `<head>`, at the moment a classic
  version of `s1.js` executes, the `<body>` (and therefore the `<h1
  id="main-heading">` element) has not been parsed yet - hence
  `document.getElementById("main-heading")` returns `null`.
- All classic scripts loaded via separate `<script>` elements execute in **the
  same shared global scope** (`window`), as if their code had been concatenated.
  A top-level `const`/`let` declared by one script becomes a binding in that
  shared scope; a second classic script declaring `const something` again is an
  illegal re-declaration in the same scope, hence `Uncaught SyntaxError:
  Identifier 'something' has already been declared` when `s2.js` is loaded after
  `s1.js`.
- A *module* script (`type="module"`) differs from a classic script on two axes:
    - *Scoping* - each module has its own top-level scope. Top-level
      declarations belong to that module only and are never added to the shared
      global scope, so two modules can each declare `const something` without
      conflict.
    - *Timing* - module scripts never block parsing: they are fetched
      asynchronously and their execution is deferred until after the document
      has been fully parsed (the same timing classic scripts get with the
      `defer` attribute), executing in relative document order among themselves.
- This explains why, once `s1.js` becomes a module,
  `document.getElementById("main-heading")` no longer returns `null`: the whole
  document, including `<body>`, is already parsed by the time it runs.
- With `s1.js` as a module and `s2.js` still classic: `s2.js` blocks the parser
  and runs immediately when encountered, while `s1.js` only runs after parsing
  finishes - so `"I'm s2"` is logged *before* `"I'm s1"`, even though `s1.js` is
  declared first in the markup. Because `s1.js` now has its own module scope,
  its `const something` no longer clashes with the one declared by `s2.js` in
  the global scope, so that error disappears.
- Module scripts also run in strict mode implicitly, without needing a `"use
  strict"` directive.

### ESM: `import`/`export`, module scope and module identity

- `import` and `export` are only valid syntax *inside a module*. Loading
  `add.js` (which contains `export function add`) as a classic script is a parse
  error, because a classic script's parser does not recognize `export` - hence
  the console error before `add.js` is removed from `index.html`.
- The same restriction applies to `import` declarations placed at the top of
  `s1.js` and `s2.js`: they are only valid once that script itself is loaded as
  a module. Using `import` in a classic script fails with a syntax error
  ("Cannot use import statement outside a module"), which is why `s2.js` must
  also be given `type="module"` before its `import` line works.
- To run a module, the browser resolves its dependencies into a *module graph*:
  before executing a module's own top-level code, it first fetches and executes
  every module that module statically imports, in dependency order.
- Each module is identified by its **resolved URL**, and the browser keeps a
  per-page module registry (module map). The first time a given module URL is
  imported, it is fetched, parsed, and its top-level code executed exactly once;
  the resulting module (and its exported bindings) is cached against that URL.
  Any further `import` of the same URL - from any number of importing modules -
  reuses the cached module instead of re-fetching or re-executing it.
- This explains the final observations:
    - `"Look, I'm running in a browser"` (the top-level code of `add.js`) is
      logged only *once*, even though both `s1.js` and `s2.js` import it - the
      second import is served from the module cache rather than re-executed.
    - The call to `add(1, 2)` still works correctly from both `s1.js` and
      `s2.js`, because both importers receive a live binding to the same
      exported `add` function, from the single cached module instance.
    - The network tab shows a **single** HTTP request for `add.js`, no matter
      how many modules import it. Generalizing, an application built from `N`
      modules that all (directly or transitively) import the same shared module
      would still trigger only **one** request for it, since caching keys on the
      resolved module URL, not on the number of `import` statements referencing
      it.