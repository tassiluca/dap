// test.mjs o qualsiasi file con "type": "module" in package.json
import { SocketAPI } from '../../../../dap/js/target/scala-3.6.4/dap-fastopt/main.mjs'

SocketAPI.startServer(9000, (msg) => {
    console.log("Server got:", msg)
})

const client = SocketAPI.connectToServer("localhost", 9000, (msg) => {
    console.log("Client got:", msg)
})

setTimeout(() => {
    client.send("Ping from client!")
}, 5000)
