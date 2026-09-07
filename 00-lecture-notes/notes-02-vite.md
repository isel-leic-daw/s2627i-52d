## Introduction

Sequence of exercises to illustrate the use of the Vite tool during the
development of browser-based applications. The goal is to observe what Vite
provides beyond the plain static file serving used in the
[1-browser-application-loading](./1-browser-application-loading.md) lecture
note, namely the transformation of the content before it is served to the
browser and the automatic reloading of the document when a file changes.

## Requirements

- Ensure [Node.js](https://nodejs.org/en/download) is installed on your machine.
    - Check installation by running `node --version` and checking the returned
      version.

## Using the Vite tool

- Create an empty folder and locate or open a shell on it.

- Copy the files created in the
  [1-browser-application-loading](./1-browser-application-loading.md) lecture
  note to this folder.

- Run `npm init`
    - Provide `module` as the `type`, instead of `commonjs`.
    - 1. Is there a `node_modules` folder present?

- Run `npm install vite --save-dev`
    - Note how a `devDependencies` property was added to `package.json`,
      containing an object with a `vite` property.
        - 2. What does this mean?
        - 3. What is the difference between `dependencies` and `devDependencies`?
    - 4. Is the `node_modules` folder present now? What is its contents?
    - More information about `vite` can be found at
      [vite.dev](https://vite.dev/).

- Add `"dev": "vite"` to the `scripts` object inside `package.json` (don't
  forget the required commas to ensure the file content is valid JSON).

- Run `npm run dev` and open a browser tab on the URL provided by Vite (e.g.
  `http://localhost:5173`).
    - Observe the console output. Compare it with the output produced in the
      context of
      [1-browser-application-loading](./1-browser-application-loading.md)
      lecture note.
    - Open a different browser tab and use the following URL on it
      `http://localhost:5173/s1.js`.
        - 5. What is the result?
    - Running `npm run dev` seems to have started an HTTP server, that is
      serving the folder contents, similarly to what `serve` does.
    - Open the developer tools on the initial tab (the one with
      `http://localhost:5173`) and observe the network traffic.
        - Note how there are more requests, in addition to the `/`, `/s1.js`,
          `/s2.js`, and `/add.js` ones.
        - Compare the response to `/` with the contents of `index.html`.
          - 6. Are they the same?
          - 7. If no, what is the difference?
        - Compare the response to `/s1.js` with the contents of `s1.js`.
          - 8. Are they the same?
          - 9. If no, what is the difference?
    - Open the console tab in the developers tools, ensure it is visible, change
      the context of `something` in `s1.js` and save the file.
        - 10. Did something happened automatically on the browser's console?
        - 11. Where there any requests performed by the browser when the `s1.js`
          file was saved?
        - 12. How can the browser react to a file being saved?
        - On the network tab, locate a previous request to an URL starting with
          `ws:` and observe its `Messages`.

- Install a new NPM library, by running `npm install url-template --save`
    - Note how a `dependencies` property was added to `package.json`, containing
      an object with a `url-template` property.
    - Add a `s3.js` file containing the following script

```javascript
import { parseTemplate } from 'url-template';

const emailUrlTemplate = parseTemplate('/{email}/{folder}/{id}');
const emailUrl = emailUrlTemplate.expand({
  email: 'user@domain',
  folder: 'test',
  id: 42
});

console.log(emailUrl);
```

- Add `<script src="s3.js" type="module"></script>` to `index.html` (inside the
  `head` element).
- Observe the browser's console output.
  - 13. Did the result of `console.log(emailUrl)` appear in the console?
- Observe the HTTP requests in the Network tab. Note how there is a request to
  `node_modules/.vite/deps/url-template.js`.
  - 14. Why did the browser do this request?
- Observe the contents of the response to `/s3.js`.
  - 15. Is it exactly the same as the `s3.js` file?
  - 16. If no, what is changed?
- Rename `s3.js` to `s3.ts` and add some type information, such as `const
  emailUrl: string = ...` as shown in listing bellow.
  - Do not forget to also do the renaming in the `index.html` file.

```typescript
import { parseTemplate } from 'url-template';

const emailUrlTemplate = parseTemplate('/{email}/{folder}/{id}');
const emailUrl: string = emailUrlTemplate.expand({
  email: 'user@domain',
  folder: 'test',
  id: 42
});

console.log(emailUrl);
```

- Observe the contents of the response to `/s3.ts`.
  - 17. Is the type annotation still there?

## What is Vite

Vite is a tool that assists during the development and build of browser-based
applications. The exercises above explored three of its capabilities:

- It provides an HTTP server that serves the contents of the project's folder,
  similarly to what `serve` does (e.g. serving `index.html` on `/` and the
  script files by their names).

- It transforms the content before serving it to the browser, namely by
    - Rewriting bare module imports (e.g. `import { parseTemplate } from
      'url-template'`) into URLs the browser can actually fetch, by pre-bundling
      the dependency from `node_modules` and serving it from
      `node_modules/.vite/deps/`. This is what allows a module installed in
      `node_modules` to be used directly from browser-side `import` statements,
      something the browser cannot resolve on its own.
    - Applying TypeScript type erasure: when a `.ts` file, such as `s3.ts`, is
      requested, Vite strips the type annotations (e.g. `: string`) on the fly
      and serves plain JavaScript, without requiring a separate compilation
      step.

- It watches the project's files for changes and, via a WebSocket connection
  established between the browser and the dev server, notifies the browser when
  a file changes so it can reload. In the exercises, saving `s1.js` triggered
  messages on that `ws:` connection and caused the browser to automatically
  reload the document, without requiring a manual refresh.

## Summary

- Right after `npm init`, there is no `node_modules` folder - it only appears
  once a first dependency is installed. Running `npm install vite --save-dev`
  creates it and populates it with `vite` and every package `vite` itself
  depends on.

- `npm install vite --save-dev` adds a `devDependencies` property to
  `package.json`, whereas `npm install url-template --save` adds a
  `dependencies` property.
    - `dependencies` are packages the application needs at *runtime* (e.g.
      `url-template`, which is imported and used by the application's own code).
    - `devDependencies` are packages only needed while *developing or building*
      the application (e.g. `vite`, the dev server/build tool itself) - they are
      not required for the application to run once built and deployed.

- Running `npm run dev` starts an HTTP server, just like `serve` did in
  [1-browser-application-loading](./1-browser-application-loading.md): it serves
  `index.html` on `/` and the script files by their names, producing the same
  console output and the same behaviors around classic vs. module scripts,
  `document`, and ESM caching already explained in that lecture note's `Theory`
  section.

- Neither the response to `/` nor the responses to the individual script files
  are byte-for-byte identical to the files on disk - Vite transforms content as
  it serves it:
    - The response to `/` contains `index.html`'s content plus an extra `<script
      type="module" src="/@vite/client">` element injected into the page. This
      is the client-side piece that opens the WebSocket connection back to the
      dev server.
    - The responses to the script files (e.g. `/s1.js`) go through Vite's
      transform pipeline as well, even when the source is plain JavaScript with
      no imports to rewrite - e.g. an inline sourcemap comment (`//#
      sourceMappingURL=...`) is appended, which is also how the dev server keeps
      the ability to map served code back to the original file for debugging.

- Saving a change to a watched file (e.g. changing `something` in a script) is
  followed automatically, without any manual reload, by console output
  reflecting the new content. This happens because:
    - The dev server watches the project's files on disk.
    - When a file changes, it sends a message over the WebSocket connection
      already opened by the injected `/@vite/client` script (visible in the
      DevTools Network tab, on the `ws:` request's `Messages` view, as JSON
      payloads such as `{"type":"connected"}` on connect and an update or
      `full-reload` message when a file changes).
    - The client script reacts to that message by reloading the page (or, for
      supported file types, applying a hot update without a full reload), which
      is also why new HTTP requests for the page and its scripts are observed
      right after the save.

- The request to `node_modules/.vite/deps/url-template.js` happens because
  browsers cannot resolve *bare* module specifiers like `'url-template'` (only
  relative or absolute paths or full URLs are valid in an `import`). Vite's dev
  server pre-bundles such dependencies from `node_modules` and rewrites the
  import to point at the pre-bundled file it serves from
  `node_modules/.vite/deps/`.

- Consistently with this, the response to `/s3.js` is *not* identical to the
  `s3.js` file on disk: the `import { parseTemplate } from 'url-template'` line
  is rewritten by Vite to import from the resolved
  `/node_modules/.vite/deps/url-template.js` URL instead.

- The response to `/s3.ts` no longer contains the `: string` type annotation.
  Vite applies TypeScript *type erasure* on the fly when serving `.ts` files: it
  strips the type-only syntax and serves valid JavaScript, without a separate
  compile step the developer has to run.