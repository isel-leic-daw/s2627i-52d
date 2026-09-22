import * as React from "react";
import * as ReactDOM from "react-dom/client";

// !!!!! DON'T DO THIS - only for demo
// DO NOT USE shared global mutable state
let count = 11;

function Counter() {
  function decHandler(): void {
    count--;
    console.log(count);
    mainRender(); // !!!!! DON'T DO THIS - only for demo
  }
  function incHandler(): void {
    count++;
    console.log(count);
    mainRender(); //!!!!! DON'T DO THIS - only for demo
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
mainRender();

/**
 * !!!!! DON'T DO THIS - only for demo
 * Do not use auxiliary function to explicitly invoke render
 */
function mainRender() {
  root.render(
    <>
      Counter 1: <Counter></Counter>
      <br></br>
      Counter 2: <Counter></Counter>
      <br></br>
      Counter 3: <Counter></Counter>
    </>,
  );
}
