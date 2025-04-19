/*
 * Swig interface file for the Native DAP library.
 * Notes: 
 * - to be called with `-threads` option! See [https://github.com/swig/swig/issues/927](this issue)
 * - directors are used to allow the target language to provide implementation to be called by the C code
 */
%module(directors="1") dap

%include <stdint.i>
%include <carrays.i>
%include <cpointer.i>
%include <std_vector.i>

%pointer_class(size_t, SizeTPtr);
%pointer_class(uint8_t, UInt8Ptr);
/* Python specific includes. */
%include <pybuffer.i>
%pybuffer_binary(uint8_t *data, size_t size); // For input parameters (Python bytes → C uint8_t*)
%extend RawData { // For output parameters (C uint8_t* → Python bytes)
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
        virtual int equals(const RawData *a, const RawData *b) = 0;
        virtual ~Equatable() {}
    };
    struct StateChangeListener {
        virtual void on_state_change(const struct DAPState *state) = 0;
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

    static int equals_helper(const RawData *a, const RawData *b) {
        if (DirectorManager::current_equatable == nullptr) {
            fprintf(stderr, "Equatable is NULL\n");
            return 0;
        }
        return DirectorManager::current_equatable->equals(a, b);
    }

    static void state_change_helper(const struct DAPState *state) {
        if (DirectorManager::current_state_listener == nullptr) {
            fprintf(stderr, "StateChangeListener is NULL\n");
            return;
        }
        DirectorManager::current_state_listener->on_state_change(state);
    }
%}

// Creates an array of uint8_t that Python can manipulate
%array_class(uint8_t, UInt8Array);
%extend UInt8Array {
    uint8_t *data() {
        return $self;
    }
}

/* Definisci una macro che gestisce anche i typedef di puntatori */
%define %array_typemap(TYPE, BASE_TYPE, CAST_EXPR)
%typemap(in) (TYPE* elements, size_t size) {
    if (!PyList_Check($input)) {
        PyErr_SetString(PyExc_TypeError, "Expected a list of " #TYPE " objects");
        return NULL;
    }
    $2 = PyList_Size($input);
    $1 = (TYPE*) malloc($2 * sizeof(TYPE));
    for (size_t i = 0; i < $2; i++) {
        PyObject *item = PyList_GetItem($input, i);
        void *argp;
        int res = SWIG_ConvertPtr(item, &argp, SWIGTYPE_p_ ## BASE_TYPE, 0);
        if (!SWIG_IsOK(res)) {
            free($1);
            PyErr_SetString(PyExc_TypeError, "List item is not a " #TYPE " object");
            return NULL;
        }
        $1[i] = CAST_EXPR(argp);
    }
}

%typemap(freearg) (TYPE* elements, size_t size) {
    free($1);
}
%enddef

%define %array_typemap_value(TYPE, BASE_TYPE)
%array_typemap(TYPE, BASE_TYPE, *(TYPE*))
%enddef

%define %array_typemap_ptr(TYPE, BASE_TYPE)
%array_typemap(TYPE, BASE_TYPE, (TYPE))
%enddef

%array_typemap_ptr(Token, RawData)
%array_typemap_value(Rule, Rule)
%apply (Rule* elements, size_t size) { (const Rule* rules, size_t rules_size) };
%array_typemap_value(Neighbour, Neighbour)
%apply (Neighbour* elements, size_t size) { (const Neighbour* neighbors, size_t neighbors_size) };

%include "dap.h"

%inline %{
    void launch_simulation_wrapper(
        const Rule* rules, size_t rules_size,
        const struct DAPState *s0,
        int port,
        const Neighbour *neighbors, size_t neighbors_size,
        StateChangeListener *listener,
        Equatable *equatable
    ) {
        DirectorManager::current_equatable = equatable;
        DirectorManager::current_state_listener = listener;
        launch_simulation(rules, rules_size, s0, port, neighbors, neighbors_size, state_change_helper, equals_helper);
    }
%}
