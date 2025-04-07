import { ProductAPI } from "../../../../dap/js/target/scala-3.6.4/dap-fastopt/main.mjs";

// Two simply stringified token values
const a = "a";
const b = "b";

// 1) a|a --1_000-> a
const rule1 = ProductAPI.JSInterface.Rule(
    [a, a],
    1_000.0,
    [a],
    null,
)
// 2) a --1--> a|^a
const rule2 = ProductAPI.JSInterface.Rule(
    [a],
    1.0,
    [a],
    a,
)
const allRules = [rule1, rule2];
// Initial state
const s0 = ProductAPI.JSInterface.State([a], null);
// Neighborhoods
const net = ["localhost:2551", "localhost:2552"]
ProductAPI.JSInterface.launchSimulation(allRules, s0, 2550, net, state => {
    console.log("Hello from the simulation!");
    console.log(state.tokens);
    console.log(state.msg)
});
