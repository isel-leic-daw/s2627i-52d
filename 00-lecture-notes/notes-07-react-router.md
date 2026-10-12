# React and Client-Side Routing

## Introduction

This lecture note introduces the client-side routing concept in browser-based applications, the browser's History API, and the React Router library.

## Browser Session Navigation

We start this lecture note with an empirical analysis of the browser's History API.

Create an empty folder and add an `index.html` file to it:

```html
<!doctype html>
<html lang="en">
  <head>
    <title>History Introduction</title>
  </head>
  <body>
    <h1>History Introduction</h1>
  </body>
</html>
```

Start `serve .` and open a new browser tab at the provided URL.
Ensure the browser tab shows the expected content.

Open the developer tools, go to the console tab and evaluate `history.length`. 
- **Q.1.** What is the presented value?

Evaluate `history.back()` in the console tab. 
- **Q.2.** Where is the browser tab located now?

Evaluate `history.forward()`. 
- **Q.3.** Where is the browser located now?

Clear the network tab in the developer tools.

Back on the console tab, evaluate `history.pushState({a: "hello"}, "", "/some/path")`.
- **Q.4.** What is the path present in the browser tab address bar?
- **Q.5.** Was an HTTP request performed?

Back on the console tab, evaluate `history.pushState({a: "world"}, "", "/another/path")`.
- **Q.6.** What is the path present in the browser tab address bar?
- **Q.7.** Was an HTTP request performed?

Press the browser back-button.
- **Q.8.** What is the path present in the browser tab address bar?
- **Q.9.** Was an HTTP request performed?

Press the browser back-button.
- **Q.10.** What is the path present in the browser tab address bar?
- **Q.11.** Was an HTTP request performed?

Press the browser forward-button.
- **Q.12.** What is the path present in the browser tab address bar?
- **Q.13.** Was an HTTP request performed?

Press the browser forward-button.
- **Q.14.** What is the path present in the browser tab address bar?
- **Q.15.** Was an HTTP request performed?

On the console tab, evaluate 
```javascript
addEventListener("popstate", (event) => {console.log({path: location.pathname, state: history.state})})
```
Press the browser back-button.
- **Q.16.** Was an HTTP request performed?
- **Q.17.** What is the path present in the browser tab address bar?
- **Q.18.** What was printed in the console?

Press the browser forward-button.
- **Q.19.** Was an HTTP request performed?
- **Q.20.** What is the path present in the browser tab address bar?
- **Q.21.** What was printed in the console?

Check that the path shown in the browser address bar is _not_ the root path (`/`).
Refresh the browser tab.
- **Q.22.** Was an HTTP request performed?
- **Q.23.** If so, what is the response HTTP status code?
- **Q.24.** Why did `serve` return that status code?

Restart `serve` by running `serve -s .`
Set the browser address bar path to `/some/path` and do a tab refresh.
- **Q.25.** What is the response HTTP status code now?
- **Q.26.** What is the response payload?
Confirm that the response to `/some/path` is not a redirect response (3xx status code), but a 2xx status code response.
Run `serve --help` and see what is the description for the `-s` option.

Go to a Vite project, start the Vite development server by running `npm run dev`.
Open a browser tab in the `/some/path` path.
- **Q.27.** What is the Vite development server response, namely the status code and the response message payload?
Change the browser's tab path to `/another/path` and repeat the process.
Finally, change the browser's tab path to `/src/main.tsx` and repeat the process.
- **Q.28.** What are the differences in the observed behavior?

## The Browser's History API

The [History API](https://developer.mozilla.org/en-US/docs/Web/API/History_API) provides programmatic access to the browser's session history, via the global `history` object.

The `back`, `forward`, and `go` instance methods provide a programmatic way of doing back and forward navigation in the history.

The `pushState` method adds a new entry to the session history.
It receives three arguments: `history.pushState(state, unused, url)`.
The `url` argument (e.g. `/some/path`) is the URL associated with the new entry, and must belong to the same [Origin](https://developer.mozilla.org/en-US/docs/Glossary/Origin) as the current document.
After a `pushState` call, the browser's address bar will reflect the `url` provided to it.
The `state` argument is an object associated with the new history entry, and that will be available via `history.state`.
The `unused` argument should always be the empty string.

The `replaceState` is similar to the `pushState` method, however replaces the current history entry instead of adding a new one.

The [`popstate`](https://developer.mozilla.org/en-US/docs/Web/API/Window/popstate_event) event is fired when the active history entry changes due to a session history navigation, triggered either by the user (e.g. the back and forward buttons) or by the `back`, `forward`, and `go` methods.
Note that it is _not_ fired by `pushState` or `replaceState`.

The `pushState` method and the `popstate` event enable the _client-side routing_ technique, where a browser-based application is organized into different _client routes_, typically corresponding to different visual application pages.
The navigation between these pages:

* Does _not_ require any HTTP exchanges.
* Is integrated into the browser's history mechanism, that is, into the browser's back and forward navigation.
* Updates the browser's address path, enabling _deep-linking_, that is, the ability to have URIs to specific parts of the application.

### Deep Linking

A _single-page application_ is a browser-based application that is bootstrapped from a single static HTML document.
The application interactivity is achieved by client-side DOM (Document Object Model) mutations, without the need to subsequently load full HTML documents. 

Deep linking is the ability to have URIs that point to specific parts of the application, even when the application is a _single page application_.

As an example, consider a _single page application_ that helps manage student repositories.

* The application is loaded by a request to `https://example.net/`, meaning that the shown path is `/`.
* The user selects to see group 1's repository, triggering a client-side navigation to the path `/g01/repo`.
* The user can now create a bookmark based on that path, to use in the future or to share with someone else.
* A request to `https://example.net/g01/repo` should load the full application, similarly to a request to `https://example.net/`,
and then do the client-side navigation to group 1's repository.

The ability for this client-side application navigation, integrated with the browser's session history, is provided by the browser's History API, as we've seen before.

However, an additional change in the server behavior is required: a request to a path inside the application that doesn't map into any other resource (image, JS script) must _not_ result in a 404 status code.
Instead, it should result in a 200 status code and `index.html` content as the response payload.

That is, a request to `https://example.net/g01/repo` should result in:

* The application being loaded in the browser, that is, the server returning the `index.html` content.
* The application _dynamically_ showing the content associated with the path `/g01/repo`.

When using the `serve` server, this behavior is activated by the `-s` flag.
When using the `vite` development server, this behavior is the default behavior.

## React Router

[React Router](https://reactrouter.com/home) is a third-party library to help adding client-side routing to applications using the React library.
The React Router library is not developed by the authors of the React library.

React Router supports multiple usage _modes_: declarative, data, and framework.
In this lecture note, we will use the _data_ mode.

React Router provides an application developer with components, hook functions, and general functions to help with client-side routing activities.

On an existing project supporting React, run the following command to install the React Router library

```shell
npm install react-router
```

Create the `App.tsx` file inside `src/example-react-router`.
To keep things simple, we will add all the code to this file.
In a proper code base, this would most probably be separated into multiple files.

All the React Router components and hook functions used in the following sections come from the `react-router` module, while the remaining hook functions come from `react`.
So, start by adding the following imports to `App.tsx`.

```typescript
import { useEffect, useEffectEvent, useState } from "react";
import {
  createBrowserRouter,
  Link,
  Outlet,
  RouterProvider,
  useLocation,
  useNavigate,
  useParams,
} from "react-router";
```

### Routes and the Router

We start by creating a `routes` array, holding all the routes.
```typescript
// An array with all the routes
const routes = [
    ...
]
```
A route is an object with some specific fields.
Let's start with a route that maps a path to an _element_.
```typescript
const routes = [
  {
    path: "/some/path",
    element: <SomeComponent />,
  },  
]
```

The `SomeComponent` component is defined as
```typescript
function SomeComponent() {
  return (
    <>
      <h1>Some Component</h1>
    </>
  );
}
```

If the current path is `/some/path` then React Router will place `<SomeComponent />` in the render tree.

A route can map a path to a _component_, instead of an element.
Notice the uppercase "C" in the property name.
```typescript
const routes = [
  ...
  {
    path: "/another/path",
    Component: AnotherComponent,
  },  
]
```

`AnotherComponent` is defined as
```typescript
function AnotherComponent() {
  return (
    <>
      <h1>Another Component</h1>
    </>
  );
}
```

Finally, let's define a route for the `/` root path

```typescript
const routes = [
  ...
  {
    path: "/",
    Component: RootComponent,
  },  
]
```

and define `RootComponent` as:
```typescript
function RootComponent() {
  return (
    <>
      <ul>
        <li>
          A plain <code>a</code> link to{" "}
          <a href="/some/path">
            <code>/some/path</code>
          </a>
        </li>
        <li>
          A plain <code>a</code> link to{" "}
          <a href="/another/path">
            <code>/another/path</code>
          </a>
        </li>
      </ul>
    </>
  );
}
```

Now let's create the _router_ and use it in the app

```typescript
// create the router from the routes array
const router = createBrowserRouter(routes);
```

And use the `RouterProvider` component from the React Router library in our `App` component
```typescript
export function App() {
  return <RouterProvider router={router} />;
}
```

The `RouterProvider` component will use the `router`, and therefore the routes it was built from, to find which element or component to render.
Note how the render of the App component only contains an element using `RouterProvider` and the previously created router.
The selection of which concrete component to use will be done by the `RouterProvider` component.

The router created by `createBrowserRouter` uses the browser History API for application navigation.

Start the development server with `npm run dev` and open a browser tab in the root folder.

- **Q.29.** What is the component being shown?

- **Q.30.** What happens when one of the links is clicked? Namely:

* **Q.31.** What is the next component being shown?
* **Q.32.** Did the navigation reload the full application (check the network tab)?

> **Important:** by default, browsers react to an `a` link being clicked by issuing a GET request to the URI in the `href` attribute and then reloading the current window with the content in the response payload. On a SPA, this behavior is not the desired one, since it reloads the full application, with the associated cost (time and network bandwidth). All the in-memory state is also lost during such reloads.

React Router provides a `Link` component that can be used to create links to other parts of the application, without requiring a full application reload.
React Router uses the History API and the `pushState` method to achieve this functionality.

Add the following two items to the `ul` in `RootComponent`.

```typescript
      <ul>
        ...
        <li>
          A <code>Link</code> to{" "}
          <Link to="/some/path">
            <code>/some/path</code>
          </Link>
        </li>
        <li>
          A <code>Link</code> to{" "}
          <Link to="/another/path">
            <code>/another/path</code>
          </Link>
        </li>
      </ul>
```

Use the links produced by the `Link` component and compare the behavior with the links produced by the `a` element.
- **Q.33.** What are the changes in behavior?
- **Q.34.** How do both behave regarding the browser's back and forward navigation?

### Template Paths and Dynamic Routes

It is possible to associate a route to a path _template_ instead of just a path.

Add the following route object to the `routes` array.

```typescript
  {
    path: "/repos/:id",
    Component: RepoComponent,
  },
```

Note that the value assigned to `path` has `:id` in the second segment, where `id` is the template variable name.
This means that the `:id` segment can take any value, and so paths such as `/repos/1`, `/repos/2`, and `/repos/abc` will all route to `RepoComponent`.

The value used in the `id` segment variable can be retrieved inside the routed component by using the `useParams` hook function provided by React Router

```typescript
function RepoComponent() {
  const { id } = useParams();
  return (
    <>
      <h1>Repository {id}</h1>
    </>
  );
}
```

The `useParams` hook function returns an object holding all the template variables as properties.

### Dynamic Navigation

In addition to the `Link` component that can be used to render a link for _intra-application_ navigation, the React Router library also provides hook functions that can be used for dynamic navigation.

To illustrate this, let's create a component that is shown when the path doesn't match any previous route.
This component should then show a countdown counter and automatically navigate to the root path when the countdown is over.

First, a way to create such a route is to use a _splat route_, added at the end of the `routes` array.

```typescript
  {
    path: "/*",
    element: <Fallback />,
  },
```
The `/*` assigned to `path` means that any path that doesn't match a more specific route will be mapped to `<Fallback />`.

The `Fallback` component is defined as

```typescript
const COUNT_DOWN_IN_SECS = 4;
function Fallback() {
  const [countDown, setCountDown] = useState(COUNT_DOWN_IN_SECS);
  const navigate = useNavigate();
  const location = useLocation();
  const timerCallback = useEffectEvent(() => {
    if (countDown == 0) {
      // nothing to do
      return;
    }
    if (countDown == 1) {
      navigate("/", { replace: true });
    }
    setCountDown(countDown - 1);
  });
  useEffect(() => {
    const iid = setInterval(() => timerCallback(), 1000);
    return () => {
      clearInterval(iid);
    };
  }, []);
  return (
    <>
      <p>Path {location.pathname} is unknown</p>
      <p>Will be redirected to the app start in {countDown} seconds...</p>
    </>
  );
}
```

The `useNavigate` hook function returns a _non-hook_ function that can be used to start a navigation, namely from a timer callback.
Note how `navigate("/", { replace: true })` is called when the countdown reaches zero.
By setting `replace` to `true`, the started navigation replaces the current entry in the browser history instead of adding a new one.
This is achieved by using the `history.replaceState` instead of the function `history.pushState`.

The `useLocation` hook function returns an object that contains information about the current router location, namely the current path.

> **Note:** This is unrelated to React Router, however try to understand what `useEffectEvent` does and why it is being used here.

### Nested Routes

Sometimes we want client-side routing to define only a part of the UI, while the other UI parts remain common to all routes, or to a subset of routes.
The _nested routes_ mechanism provided by React Router is a way of achieving this goal.

Add the following route object, which defines a nested route, to the `routes` array.

```typescript
  {
    path: "/main",
    Component: MainComponent,
    children: [
      { path: "child1", Component: Child1Component },
      { path: "child2", Component: Child2Component },
      { index: true, Component: HomeComponent },
    ]
  },
```

Note that the paths in the `children` array do not start with `/`: they are relative to the parent route's path.
So, the `child1` child route defines the `/main/child1` path.

The `MainComponent` then uses the child routes via the `Outlet` component, which acts as a placeholder marking where the matched child route is rendered.

```typescript
function MainComponent() {
  return (
    <>
    <h1>Main Component</h1>
    <ul>
      <li><Link to="child1">Child 1</Link></li>
      <li><Link to="child2">Child 2</Link></li>
    </ul>
    <Outlet />
    </>
  );
}
```

The value passed to the `Link` component's `to` property is also resolved relatively to the current route, so `to="child1"` inside `MainComponent` produces a link to `/main/child1`.

The content rendered by the `Outlet` component will be the result of applying the sub-routes defined in the `children` property.
For instance, the path `/main/child1` will render `MainComponent`, with its heading and link list, followed by `Child1Component` in the place of the `Outlet`.

An _index route_, that is, a route with `index` set to `true` and no `path` property, is used when there is no path segment after the segment matched by the parent route.
That is, the `/main` path will render `MainComponent` with `HomeComponent` inside `Outlet`.

### Additional Resources

* [React Router - Routing](https://reactrouter.com/start/data/routing).
* [React Router - `createBrowserRouter` function](https://reactrouter.com/api/data-routers/createBrowserRouter).
* [React Router - `RouterProvider` component](https://reactrouter.com/api/data-routers/RouterProvider).
* [React Router - `Link` component](https://reactrouter.com/api/components/Link).
* [React Router - `Outlet` component](https://reactrouter.com/api/components/Outlet).
* [React Router - `Navigate` component](https://reactrouter.com/api/components/Navigate).
* [React Router - `useNavigate` hook function](https://reactrouter.com/api/hooks/useNavigate).
* [React Router - `useLocation` hook function](https://reactrouter.com/api/hooks/useLocation).
* [React Router - `useParams` hook function](https://reactrouter.com/api/hooks/useParams).
