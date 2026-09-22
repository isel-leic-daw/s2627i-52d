import * as React from "react";
import { useState } from "react";
import * as ReactDOM from "react-dom/client";

function Counter() {
  const [count, setCount] = useState(0);
  function decHandler(): void {
    setCount(count - 1);
    console.log(count - 1);
  }
  function incHandler(): void {
    setCount(count + 1);
    console.log(count + 1);
  }

  return (
    <>
      <button onClick={decHandler}>-</button>
      {count}
      <button onClick={incHandler}>+</button>
    </>
  );
}

const root = ReactDOM.createRoot(document.getElementById("container")!);
root.render(
  <>
    Counter 1: <Counter></Counter>
    <br></br>
    Counter 2: <Counter></Counter>
    <br></br>
    Counter 3: <Counter></Counter>
  </>,
);
