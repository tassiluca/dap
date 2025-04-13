/*
 * Swig interface file for the DAP library.
 * Notes: 
 * - to be called with -threads option! See hhttps://github.com/swig/swig/issues/927
 * - directors are used to allow the target language to provide implementation to be called by the C code
 */
%module(directors="1") dap

/* Include stdint C facilities to convert int32 and int64. */
%include <stdint.i>
%include <carrays.i>
%include <cpointer.i>
%include <pybuffer.i>
%pointer_class(size_t, SizeTPtr);
%pointer_class(uint8_t, UInt8Ptr);
// For input parameters (Python bytes → C uint8_t*)
%pybuffer_binary(uint8_t *data, size_t size);
// For output parameters (C uint8_t* → Python bytes)
%extend RawData {
    PyObject* to_bytes() {
        SWIG_PYTHON_THREAD_BEGIN_BLOCK; // important!!!
        PyObject* result = PyBytes_FromStringAndSize((const char*)$self->data, $self->size);
        SWIG_PYTHON_THREAD_END_BLOCK;
        return result;
    }
}

%{
#include "dap.h"
%}

/*
 * Allow using callbacks to the target language
 * (see https://rawgit.com/swig/swig/master/Doc/Manual/SWIGPlus.html#SWIGPlus_target_language_callbacks)
 */
%feature("director") Equatable;
%feature("director") StateChangeListener;

%inline %{
    struct Equatable {
        virtual int equals(RawData *a, RawData *b) = 0;
        virtual ~Equatable() {}
    };
    struct StateChangeListener {
        virtual void on_state_change(struct DAPState *state) = 0;
        virtual ~StateChangeListener() {}
    };
%}

%{
    struct DirectorManager {
        static Equatable* current_equatable;
        static StateChangeListener* current_state_listener;

        static void reset() {
            current_equatable = nullptr;
            current_state_listener = nullptr;
        }
    };

    Equatable* DirectorManager::current_equatable = nullptr;
    StateChangeListener* DirectorManager::current_state_listener = nullptr;

    static int equals_helper(RawData *a, RawData *b) {
        if (DirectorManager::current_equatable == nullptr) {
            fprintf(stderr, "Equatable is NULL\n");
            return 0;
        }
        return DirectorManager::current_equatable->equals(a, b);
    }

    static void state_change_helper(struct DAPState *state) {
        if (DirectorManager::current_state_listener == nullptr) {
            fprintf(stderr, "StateChangeListener is NULL\n");
            return;
        }
        DirectorManager::current_state_listener->on_state_change(state);
    }
%}

%inline %{
    int register_eq_wrapper(Equatable *e) {
        DirectorManager::current_equatable = e;
        return register_equatable(equals_helper);
    }

    void launch_simulation_wrapper(
        MSet_Rule* rules,
        struct DAPState *s0,
        int port,
        MSet_Neighbour *neighborhood,
        StateChangeListener *listener
    ) {
        DirectorManager::current_state_listener = listener;
        launch_simulation(rules, s0, port, neighborhood, state_change_helper);
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
