/*
 * Swig interface file for the Native DAP library.
 * Notes: 
 * - to be called with `-threads` option! See [https://github.com/swig/swig/issues/927](this issue)
 * - directors are used to allow the target language to provide implementation to be called by the C code
 */
%module(directors="1") dap

%{
#include "dap.h"
%}

// Typemaps to hold a Python object on the C side and re-expose it to Python.
%typemap(in) void* {
    Py_INCREF($input);
    $1 = (void*)$input; // Store PyObject* as void*
}
%typemap(freearg) void* { // called when the C code is done with the object
    Py_DECREF((PyObject*)$1);
}
%typemap(out) void* {
    PyObject *obj = (PyObject*)$1;
    if (!obj) {
        Py_RETURN_NONE;
    }
    Py_INCREF(obj);
    $result = obj; // Set the result to the PyObject*
}

/*
 * Allow using callbacks to the target language
 * (see https://rawgit.com/swig/swig/master/Doc/Manual/SWIGPlus.html#SWIGPlus_target_language_callbacks)
 */
%feature("director") Equalizer;
%feature("director") Codec;
%feature("director") StateChangeListener;

%inline %{
    struct Equalizer {
        virtual bool equals(PyObject* t1, PyObject* t2) = 0;
        virtual ~Equalizer() {}
    };
    struct Codec {
        virtual const char* serialize(PyObject* t) = 0;
        virtual PyObject* deserialize(const char* str) = 0;
        virtual ~Codec() {}
    };
    struct StateChangeListener {
        virtual void on_state_change(const struct DAPState *state) = 0;
        virtual ~StateChangeListener() {}
    };
%}

%{
    struct DirectorManager {
        static Equalizer* current_equalizer;
        static Codec* current_codec;
        static StateChangeListener* current_state_listener;

        static void reset() {
            current_equalizer = nullptr;
            current_codec = nullptr;
            current_state_listener = nullptr;
        }
    };

    Equalizer* DirectorManager::current_equalizer = nullptr;
    Codec* DirectorManager::current_codec = nullptr;
    StateChangeListener* DirectorManager::current_state_listener = nullptr;

    static bool equals_helper(Token t1, Token t2) {
        if (DirectorManager::current_equalizer == nullptr) {
            fprintf(stderr, "Equalizer is NULL\n");
            return 0;
        }
        PyObject *token1 = (PyObject*)t1;
        Py_INCREF(token1);
        PyObject *token2 = (PyObject*)t2;
        Py_INCREF(token2);
        return DirectorManager::current_equalizer->equals(token1, token2);
    }

    static const char* serialize_helper(Token token) {
        if (DirectorManager::current_codec == nullptr) {
            fprintf(stderr, "Codec is NULL\n");
            return nullptr;
        }
        PyObject *py_token = (PyObject*)token;
        Py_INCREF(py_token);
        return DirectorManager::current_codec->serialize(py_token);
    }

    static Token deserialize_helper(const char* str) {
        if (DirectorManager::current_codec == nullptr) {
            fprintf(stderr, "Codec is NULL\n");
            return nullptr;
        }
        PyObject* py_token = DirectorManager::current_codec->deserialize(str);
        Py_INCREF(py_token);
        return (void*)py_token;
    }

    static void state_change_helper(const struct DAPState *state) {
        if (DirectorManager::current_state_listener == nullptr) {
            fprintf(stderr, "StateChangeListener is NULL\n");
            return;
        }
        DirectorManager::current_state_listener->on_state_change(state);
    }
%}

%define %array_typemap(TYPE, CAST_EXPR)
%typemap(in) (TYPE *elements, size_t size) {
    if (!PyList_Check($input)) {
        PyErr_SetString(PyExc_TypeError, "Expected a list of " #TYPE " objects");
        return NULL;
    }
    $2 = PyList_Size($input);
    $1 = (TYPE*) malloc($2 * sizeof(TYPE));
    if (!$1) {
        PyErr_SetString(PyExc_MemoryError, "Failed to allocate memory for " #TYPE " array");
        return NULL;
    }
    for (size_t i = 0; i < $2; i++) {
        PyObject *item = PyList_GetItem($input, i);
        void *argp;
        swig_type_info *descriptor = SWIG_TypeQuery(#TYPE " *");
        int res = SWIG_ConvertPtr(item, &argp, descriptor, 0);
        if (!SWIG_IsOK(res)) {
            argp = (void*)item;
        }
        $1[i] = CAST_EXPR(argp);
    }
}
%enddef

%define %array_typemap_value(TYPE)
%array_typemap(TYPE, *(TYPE*))
%enddef

%define %array_typemap_ptr(TYPE)
%array_typemap(TYPE, (TYPE))
%enddef

%array_typemap_ptr(Token)
%array_typemap_value(Rule)
%array_typemap_value(Neighbor)

%include "dap.h"

%inline %{
    DASPSimulation simulation(
        Array_Rule *rules, 
        const struct DAPState *initial_state,
        Array_Neighbor *neighborhood,
        Codec *codec,
        Equalizer* equalizer
    ) {
        DirectorManager::current_equalizer = equalizer;
        DirectorManager::current_codec = codec;
        return simulation(rules, initial_state, neighborhood, serialize_helper, deserialize_helper, equals_helper);
    }

    void launch(DASPSimulation simulation, int port, StateChangeListener* state_change_listener) {
        DirectorManager::current_state_listener = state_change_listener;
        launch(simulation, port, state_change_helper);
    }
%}
