%module(directors="1") dap

/* Include stdint C facilities to convert int32 and int64. */
%include <stdint.i>
%include <carrays.i>
%include <cpointer.i>
%pointer_class(size_t, SizeTPtr);
%pointer_class(uint8_t, UInt8Ptr);

%{
#include "dap.h"
#include "gossip.pb-c.h"
%}

/*
 * Allow using callbacks to the target language
 * (see https://rawgit.com/swig/swig/master/Doc/Manual/SWIGPlus.html#SWIGPlus_target_language_callbacks)
 */
%feature("director") Codec;
%feature("director") Equatable;
%feature("director") StateChangeListener;

%inline %{
    struct Codec {
        virtual uint8_t *serialize(Token data, SizeTPtr *out_size) = 0;
        virtual Token deserialize(uint8_t *bytes, int size) = 0;
        virtual ~Codec() {}
    };
    struct Equatable {
        virtual int equals(void *a, void *b) = 0;
        virtual ~Equatable() {}
    };
    struct StateChangeListener {
        virtual void on_state_change(struct DAPState *state) = 0;
        virtual ~StateChangeListener() {}
    };
%}

%{
    struct DirectorManager {
        static Codec* current_codec;
        static Equatable* current_equatable;
        static StateChangeListener* current_state_listener;

        static void reset() {
            current_codec = nullptr;
            current_equatable = nullptr;
            current_state_listener = nullptr;
        }
    };

    Codec* DirectorManager::current_codec = nullptr;
    Equatable* DirectorManager::current_equatable = nullptr;
    StateChangeListener* DirectorManager::current_state_listener = nullptr;

    static uint8_t* serialize_helper(void *data, size_t *out_size) {
        if (DirectorManager::current_codec == nullptr) {
            perror("Codec is NULL\n");
            return nullptr;
        }
        return DirectorManager::current_codec->serialize((Token)data, out_size);
    }

    static void* deserialize_helper(uint8_t *bytes, int size) {
        if (DirectorManager::current_codec == nullptr) {
            perror("Codec is NULL\n");
            return nullptr;
        }
        return DirectorManager::current_codec->deserialize(bytes, size);
    }

    static int equals_helper(void *a, void *b) {
        if (DirectorManager::current_equatable == nullptr) {
            perror("Equatable is NULL\n");
            return 0;
        }
        return DirectorManager::current_equatable->equals(a, b);
    }

    static void state_change_helper(struct DAPState *state) {
        if (DirectorManager::current_state_listener == nullptr) {
            perror("StateChangeListener is NULL\n");
            return;
        }
        DirectorManager::current_state_listener->on_state_change(state);
    }
%}

%inline %{
    int register_serde_wrapper(const char *name, Codec *codec) {
        DirectorManager::current_codec = codec;
        return register_serde(name, serialize_helper, deserialize_helper);
    }

    int register_eq_wrapper(const char *name, Equatable *e) {
        DirectorManager::current_equatable = e;
        return register_equatable(name, equals_helper);
    }

    void launch_simulation_wrapper(
        MSet_Rule *rules,
        struct DAPState *s0,
        int port,
        MSet_Neighbour *neighborhood,
        StateChangeListener *listener
    ) {
        DirectorManager::current_state_listener = listener;
        launch_simulation(rules, s0, port, neighborhood, state_change_helper);
    }

    int token_impl_equals_wrapper(void* a, void *b) {
        if (a == nullptr || b == nullptr) {
            return 0;
        }
        Token token_a = (Token)a;
        Token token_b = (Token)b;
        return strcmp(token_a->name, token_b->name) == 0;
    }
%}

/*
 * A type map that will free the memory of a char* when it is replaced by a new
 * only if it is not the empty string.
 */
%typemap(memberin) char * {
    if ($1 != NULL && $1 != protobuf_c_empty_string) {
        free($1);
    }
    $1 = strdup($input);
}

// Creates an array of uint8_t that Python can manipulate
%array_class(uint8_t, UInt8Array);
%extend UInt8Array {
    uint8_t *data() {
        return $self;
    }
}

%include "dap.h"
%include "gossip.pb-c.h"
