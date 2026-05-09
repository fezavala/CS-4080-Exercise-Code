#include <stdlib.h>

#include "compiler.h"
#include "memory.h"
#include "vm.h"

#ifdef DEBUG_LOG_GC
#include <stdio.h>
#include "debug.h"
#endif

#define GC_HEAP_GROW_FACTOR 2

void* reallocate(void* pointer, size_t oldSize, size_t newSize) {
    vm.bytesAllocated += newSize - oldSize;
    if (newSize > oldSize) {
#ifdef DEBUG_STRESS_GC
        collectGarbage();
#endif

        if (vm.bytesAllocated > vm.nextGC) {
            collectGarbage();
        }
    }

    if (newSize == 0) {
        free(pointer);
        return NULL;
    }

    void* result = realloc(pointer, newSize);
    if (result == NULL) exit(1);
    return result;
}


static void freeObject(Obj* object) {
#ifdef DEBUG_LOG_GC
    printf("%p free type %d\n", (void*)object, object->type);
#endif

    switch (object->type) {
        case OBJ_CLOSURE: {
            ObjClosure* closure = (ObjClosure*)object;
            FREE_ARRAY(ObjUpvalue*, closure->upvalues,
                       closure->upvalueCount);
            FREE(ObjClosure, object);
            break;
        }
        case OBJ_FUNCTION: {
            ObjFunction* function = (ObjFunction*)object;
            freeChunk(&function->chunk);
            FREE(ObjFunction, object);
            break;
        }
        case OBJ_NATIVE:
            FREE(ObjNative, object);
            break;
        case OBJ_STRING: {
            ObjString* string = (ObjString*)object;
            FREE_ARRAY(char, string->chars, string->length + 1);
            FREE(ObjString, object);
            break;
        }
        case OBJ_UPVALUE:
            FREE(ObjUpvalue, object);
            break;
    }
}

static void sweep() {
    Obj* previous = NULL;
    Obj* object = vm.objects;
    while (object != NULL) {
        // Changing sweep to free refCounts of 0
        if (object->refCount > 0) {
            previous = object;
            object = object->next;
        } else {
            Obj* unreached = object;
            object = object->next;
            if (previous != NULL) {
                previous->next = object;
            } else {
                vm.objects = object;
            }

            freeObject(unreached);
        }
    }
}

void collectGarbage() {
#ifdef DEBUG_LOG_GC
    printf("-- gc begin\n");
    size_t before = vm.bytesAllocated;
#endif

    // collectGarbage() now only sweeps objects with a refCount of 0
    sweep();

    vm.nextGC = vm.bytesAllocated * GC_HEAP_GROW_FACTOR;

#ifdef DEBUG_LOG_GC
    printf("-- gc end\n");
    printf("   collected %zu bytes (from %zu to %zu) next at %zu\n",
       before - vm.bytesAllocated, before, vm.bytesAllocated,
       vm.nextGC);
#endif
}

// Helper functions for decrementing object reference counts
static void decrementValue(Value value) {
    if (IS_OBJ(value)) decRef(AS_OBJ(value));
}

static void decrementArray(ValueArray* value_array) {
    for (int i = 0; i < value_array->count; i++) {
        decrementValue(value_array->values[i]);
    }
}


// Increment reference and decrement reference objects
void incRef(Obj* value) {
    value->refCount++;
}


void decRef(Obj* value) {
    if (value->refCount > 1) {
        value->refCount--;
    } else {
        // Decrement refVal of objects referenced by the current object
        switch (value->type) {
            case OBJ_FUNCTION: {
                ObjFunction* function = (ObjFunction*)value;
                if (function->name != NULL) decRef((Obj*)function->name);
                decrementArray(&function->chunk.constants);
                break;
            }
            case OBJ_UPVALUE: {
                ObjUpvalue* upvalue = (ObjUpvalue*)value;
                decrementValue(upvalue->closed);
                break;
            }
            case OBJ_CLOSURE: {
                ObjClosure* closure = (ObjClosure*)value;
                decRef((Obj*)closure->function);
                for (int i = 0; i < closure->upvalueCount; i++) {
                    decRef((Obj*)closure->upvalues[i]);
                }
                break;
            }
            case OBJ_NATIVE:
            case OBJ_STRING:
                break;
        }
    }
}

void freeObjects() {
    Obj* object = vm.objects;
    while (object != NULL) {
        Obj* next = object->next;
        freeObject(object);
        object = next;
    }

    free(vm.grayStack);
}
