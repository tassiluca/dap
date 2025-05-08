"use strict";

export class Token {
    constructor(name, deviceId) {
        this.name = name;
        this.deviceId = deviceId;
    }

    serializeAsString() {
        return JSON.stringify({
            name: this.name,
            device_id: this.deviceId,
        });
    }

    static deserializeFromString(str) {
        try {
            const obj = JSON.parse(str);
            return new Token(obj.name, obj.device_id);
        } catch (e) {
            return null;
        }
    }

    equals(other) {
        return other !== null && this.name === other.name;
    }

    toString() {
        return `Token { name = ${this.name}, deviceId = ${this.deviceId} }`;
    }
}
