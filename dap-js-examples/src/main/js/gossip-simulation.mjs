"use strict";

import { Token } from "./gossip-model.js";
import { DAPApi } from "../../../../dap/js/target/scala-3.6.4/dap-fastopt/main.mjs";

const args = process.argv.slice(2); // Skip first 2 args: node and script name
if (args.length < 2) {
    console.error("Usage: node main.js <port> <net1> [<net2> ...]");
    process.exit(1);
}

const port = parseInt(args[0], 10);
const net = args.slice(1);
const neighborhood = net.map(n => DAPApi.Neighbor(n.split(":")[0], parseInt(n.split(":")[1])))

console.log("Port:", port);
console.log("My neighbors:", neighborhood.toString());

// Two simple tokens
const a = new Token("a", port);
const b = new Token("b", port);

// 1) a|a --1_000--> a
const rule1 = DAPApi.Rule(DAPApi.MSet([a, a]), 1_000.0, DAPApi.MSet([a]));
// 2) a --1--> a|^a
const rule2 = DAPApi.Rule(DAPApi.MSet([a]), 1.0, DAPApi.MSet([a]), a);
const allRules = [rule1, rule2];
// Initial state
const initialState = port === 2550 ? DAPApi.State(DAPApi.MSet([a])) : DAPApi.State(DAPApi.MSet([]));

const simulation = DAPApi.simulation(
    allRules,
    initialState,
    neighborhood,
    token => token.serializeAsString(),
    stringified => Token.deserializeFromString(stringified),
    (t1, t2) => t1.equals(t2),
);
DAPApi.launch(simulation, port, state => {
    console.log("[JS]", new Date().toLocaleString());
    console.log("[JS] State:");
    console.log("[JS]  Local:", state.tokens.elems);
    console.log("[JS]  Message:", state.msg);
    console.log("-".repeat(30));
    console.log();
});
setTimeout(() => DAPApi.stop(simulation), 30_000);
