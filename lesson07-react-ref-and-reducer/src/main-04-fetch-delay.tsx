import * as React from "react";
import { useEffect, useReducer, useState } from "react";
import * as ReactDOM from "react-dom/client";

type RequestState =
  | { status: "pending"; countdown: number }
  | { status: "loading" }
  | { status: "success"; body: string }
  | { status: "error"; error: Error };

type Action =
  | { type: "reset" }
  | { type: "tick" }
  | { type: "start" }
  | { type: "succeed"; body: string }
  | { type: "failed"; error: Error };

const TIMEOUT = 5

function reduce(prev: RequestState, action: Action): RequestState {
  switch (action.type) {
    case "reset":
      return { status: "pending", countdown: TIMEOUT };
    case "tick":
      if(prev.status === "pending")
        return { status: "pending", countdown: prev.countdown - 1 };
      break;
    case "start":
      if(prev.status === "pending")
        return { status: "loading"};
      break;
    case "succeed":
      if(prev.status === "loading")
        return { status: "success", body: action.body };
      break;
    case "failed":
      if(prev.status === "loading")
        return { status: "error", error: action.error };
      break;
  }
  throw new Error(`Illegal action ${action.type} for state ${prev.status}`)
}

function FetchAndShow({ uri }: { uri: string }) {
  const [state, dispatch] = useReducer(reduce, {
    status: "pending",
    countdown: 0,
  });
  useEffect(() => {
    let ignore = false;
    // Reset state for the new URI
    dispatch({ type: "reset" });
    const tid = setInterval(() => dispatch({ type: "tick" }), 1000);
    (async function () {
      await delay(TIMEOUT * 1000); // Simulate delay of TIMEOUT secs
      if (ignore) return;
      try {
        const resp = await fetch(uri);
        dispatch({ type: "start" });
        if (!resp.ok) {
          dispatch({
            type: "failed",
            error: new Error(`${resp.status}: ${resp.statusText}`),
          });
          return;
        }
        const body = await resp.text();
        if (!ignore) {
          dispatch({ type: "succeed", body });
        }
      } catch (e) {
        dispatch({ type: "failed", error: new Error(String(e)) });
      } finally {
        clearInterval(tid);
      }
    })();
    return () => {
      ignore = true;
      clearInterval(tid);
    };
  }, [uri]);

  return (
    <div>
      {state.status.toUpperCase()} {uri}
      <hr></hr>
      Countdown: {state.status === "pending" ? state.countdown : ""}
      {state.status === "error" ? "ERROR: " + state.error : ""}
      <hr></hr>
      <textarea
        rows={20}
        cols={40}
        value={state.status === "success" ? state.body : ""}
        readOnly
      ></textarea>
    </div>
  );
}

function ReadAndFetch() {
  const [inputUri, setInputUri] = useState(
    "https://api.chucknorris.io/jokes/random",
  );
  const [fetchUri, setFetchUri] = useState(
    "https://v2.jokeapi.dev/joke/Programming",
  );
  return (
    <>
      <div>
        URI:
        <input
          type="text"
          value={inputUri}
          onChange={(e) => setInputUri(e.target.value)}
        ></input>
        <button onClick={() => setFetchUri(inputUri)}>Submit</button>
      </div>
      <hr></hr>
      <FetchAndShow uri={fetchUri}></FetchAndShow>
    </>
  );
}

/**
 * Returns a Promise that id fulfilled after a delay of ms.
 * @param ms timeout
 */
function delay(ms: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

const root = ReactDOM.createRoot(document.getElementById("container")!);
root.render(<ReadAndFetch></ReadAndFetch>);
