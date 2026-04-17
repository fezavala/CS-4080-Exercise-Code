#include <stdio.h>
#include <string.h>

#include "object.h"
#include "memory.h"
#include "value.h"

void initValueArray(ValueArray* array) {
    array->values = NULL;
    array->capacity = 0;
    array->count = 0;
}

void writeValueArray(ValueArray* array, Value value) {
    if (array->capacity < array->count + 1) {
        int oldCapacity = array->capacity;
        array->capacity = GROW_CAPACITY(oldCapacity);
        array->values = GROW_ARRAY(Value, array->values,
                                   oldCapacity, array->capacity);
    }

    array->values[array->count] = value;
    array->count++;
}

void freeValueArray(ValueArray* array) {
    FREE_ARRAY(Value, array->values, array->capacity);
    initValueArray(array);
}

void printValue(Value value) {
    switch (value.type) {
        case VAL_BOOL:
            printf(AS_BOOL(value) ? "true" : "false");
            break;
        case VAL_NIL: printf("nil"); break;
        case VAL_NUMBER: printf("%g", AS_NUMBER(value)); break;
        case VAL_OBJ: printObject(value); break;
    }
}

bool valuesEqual(Value a, Value b) {
    if (a.type != b.type) return false;
    switch (a.type) {
        case VAL_BOOL:   return AS_BOOL(a) == AS_BOOL(b);
        case VAL_NIL:    return true;
        case VAL_NUMBER: return AS_NUMBER(a) == AS_NUMBER(b);
        case VAL_OBJ:    return AS_OBJ(a) == AS_OBJ(b);
        default:         return false; // Unreachable.
    }
}

// hashValue checks the value's type and hashes it accordingly, returning the hash
uint32_t hashValue(Value value) {
    uint32_t hash = 2166136261u;

    if (IS_OBJ(value) && IS_STRING(value)) return AS_STRING(value)->hash;

    if (IS_NIL(value)) return hash;

    if (IS_BOOL(value)) {
        hash ^= AS_BOOL(value) ? 1 : 0;
        hash *= 16777619;
        return hash;
    }

    if (IS_NUMBER(value)) {
        const uint64_t number = (uint32_t)AS_NUMBER(value);
        for (int i = 0; i < 8; i++) {
            const uint8_t numByte = (uint8_t)(number >> i * 8);
            hash ^= numByte;
            hash *= 16777619;
        }
    }

    return hash;
}
