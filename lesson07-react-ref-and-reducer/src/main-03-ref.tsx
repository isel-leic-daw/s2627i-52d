import * as React from "react";
import { RefObject, useEffect, useRef, useState } from "react";
import * as ReactDOM from "react-dom/client";

/**
 * Whenever we click + -> change count -> Render -> eval new msg -> GUI
 */
// function SilentCounter() {
//   const [count, setCount] = useState(0);
//   const msg = " counter = " + count
//   return (
//     <>
//       <button onClick={() => setCount(count + 1)}>+</button>
//       <button >Update</button>
//       {msg}
//     </>
//   );
// }

/**
 * Click on + only change count, WITHOUT update GUI.
 * GUI is only updated by click on Update button.
 */
function SilentCounter() {
  const count: RefObject<number> = useRef(0);
  const [msg, setMsg] = useState(" counter = 0");
  return (
    <>
      <button
        onClick={() => {
          count.current = count.current + 1;
        }}
      >
        +
      </button>
      <button onClick={() => setMsg(` counter = ${count.current}`)}>
        Update
      </button>
      {msg}
    </>
  );
}

const root = ReactDOM.createRoot(document.getElementById("container")!);
root.render(<SilentCounter />);
