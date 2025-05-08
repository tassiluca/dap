from dap import *
from datetime import datetime
import json
import logging

class TokenImpl():
    def __init__(self, name: str, device_id: int):
        self.name = name
        self.device_id = device_id

    def serialize(self) -> str:
        return json.dumps({'name': self.name,'device_id': self.device_id}, separators=(',', ':'))

    @classmethod
    def deserialize(cls, data: str):
        obj = json.loads(data)
        return cls(name = obj['name'], device_id = obj['device_id'])

    def __str__(self):
        return "Token[Name: {}, Device ID: {}]".format(self.name, self.device_id)
    
_global_eq_instance = None
_global_codec_instance = None
_global_state_change_listener = None

class PyEq(Equalizer):
    def __init__(self):
        global _global_eq_instance
        super().__init__()
        _global_eq_instance = self

    def equals(self, t1, t2):
        try:
            return isinstance(t1, TokenImpl) and isinstance(t2, TokenImpl) and t1.name == t2.name
        except Exception as e:
            logging.warning("PyEq.equals: %s", e)
            return False

class PyCodec(Codec):
    def __init__(self):
        global _global_codec_instance
        self._token_cache = []
        super().__init__()
        _global_codec_instance = self

    def serialize(self, token) -> str:
        if isinstance(token, TokenImpl):
            return token.serialize()
        else:
            logging.warning("PyCodec.serialize: token is not recognized!")
            return ""

    def deserialize(self, data):
        try:
            token = TokenImpl.deserialize(data)
            self._token_cache.append(token)
            return token
        except Exception as e:
            logging.warning("PyCodec.deserialize: %s", e)
            return None

class PyStateChangeListener(StateChangeListener):
    def __init__(self):
        global _global_state_change_listener
        super().__init__()
        _global_state_change_listener = self

    def on_state_change(self, state):
        tokens = state.tokens
        msg = state.msg
        print("-" * 50)
        print(f"[🐍] {datetime.now().strftime('%H:%M:%S.%f')[:-3]}")
        print("[🐍] Local: [")
        for i in range(tokens.size):
            token = MSet_Token_get(tokens, i)
            print(f"[🐍]   {token}")
        print("[🐍] ]")
        if msg is not None:
            print(f"[🐍] Message: {msg}")
        else:
            print("[🐍] Message: None")
        print("-" * 50, end = "\n\n")
