"use strict";

export class Token {
    constructor(name, deviceId) {
        this.name = name;
        this.deviceId = deviceId;
    }

    serialize() {
        const json = JSON.stringify({
            name: this.name,
            device_id: this.deviceId,
        });
        const encoder = new TextEncoder();
        return encoder.encode(json);
    }

    serializeAsString() {
        return JSON.stringify({
            name: this.name,
            device_id: this.deviceId,
        });
    }

    static deserialize(byteArray) {
        const decoder = new TextDecoder();
        const json = decoder.decode(byteArray);
        const obj = JSON.parse(json);
        return new Token(obj.name, obj.device_id);
    }

    static deserializeFromString(str) {
        const obj = JSON.parse(str);
        return new Token(obj.name, obj.device_id);
    }

    equals(other) {
        return this.name === other.name;
    }

    toString() {
        return `Token { name = ${this.name}, deviceId = ${this.deviceId} }`;
    }
}
