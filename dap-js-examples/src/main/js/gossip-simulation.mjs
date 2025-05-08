"use strict";

import { Token } from "./gossip-model.js";
import { DAP } from "../../../../dap/js/target/scala-3.7.0/dap-fastopt/main.mjs";

const args = process.argv.slice(2); // Skip first 2 args: node and script name
if (args.length < 2) {
    console.error("Usage: node main.js <port> <net1> [<net2> ...]");
    process.exit(1);
}

const port = parseInt(args[0], 10);
const net = args.slice(1);
const neighborhood = net.map(n => DAP.Neighbor(n.split(":")[0], parseInt(n.split(":")[1])))

console.log("Port:", port);
console.log("My neighbors:", neighborhood.toString());

// Two simple tokens
const a = new Token("a", port);
const b = new Token("b", port);

// 1) a|a --1_000--> a
const rule1 = DAP.Rule(DAP.MSet([a, a]), 1_000.0, DAP.MSet([a]));
// 2) a --1--> a|^a
const rule2 = DAP.Rule(DAP.MSet([a]), 1.0, DAP.MSet([a]), a);
// 3) a|b --2--> a|b|^b
const rule3 = DAP.Rule(DAP.MSet([a, b]), 2.0, DAP.MSet([a, b]), b);
// 4) b|b --1_000--> b
const rule4 = DAP.Rule(DAP.MSet([b, b]), 1_000.0, DAP.MSet([b]));
const allRules = [rule1, rule2, rule3, rule4];
// Initial state
let initialState;
if (port === 2550) {
    initialState = DAP.State(DAP.MSet([a]));
} else if (port == 2553) {
    initialState = DAP.State(DAP.MSet([b]));
} else {
    initialState = DAP.State(DAP.MSet([]));
}

const simulation = DAP.simulation(
    allRules,
    initialState,
    neighborhood,
    token => token.serializeAsString(),
    stringified => Token.deserializeFromString(stringified),
    (t1, t2) => t1 != null && t2 != null && t1.equals(t2),
);
DAP.launch(simulation, port, state => {
    console.log("[JS]", new Date().toLocaleString());
    console.log("[JS] Local:", state.tokens.elems);
    console.log("[JS] Message:", state.msg);
    console.log("-".repeat(50));
    console.log();
});
setTimeout(() => DAP.stop(simulation), 30_000);
