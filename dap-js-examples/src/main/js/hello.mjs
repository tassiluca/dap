import { DAPApi } from "../../../../dap/js/target/scala-3.6.4/dap-fastopt/main.mjs";

const args = process.argv.slice(2); // Skip first 2 args: node and script name
if (args.length < 2) {
    console.error("Usage: node main.js <port> <net1> [<net2> ...]");
    process.exit(1);
}

const port = parseInt(args[0], 10);
const net = args.slice(1);

console.log("Port:", port);
console.log("Net:", net);

// Two simply stringified token values
const a = "a";
const b = "b";

// 1) a|a --1_000-> a
const rule1 = DAPApi.ruleOf(DAPApi.MSet(a, a), 1_000.0, DAPApi.MSet(a));
// 2) a --1--> a|^a
const rule2 = DAPApi.ruleOf(DAPApi.MSet(a), 1.0, DAPApi.MSet(a), a);
const allRules = [rule1, rule2];
// Initial state
const s0 = port === 2550 ? DAPApi.stateOf(DAPApi.MSet(a)) : DAPApi.stateOf(DAPApi.MSet());
DAPApi.simulate(allRules, s0, port, net, state => {
    console.log(new Date().toLocaleString());
    console.log(state.toString());
    console.log("-".repeat(30));
});
