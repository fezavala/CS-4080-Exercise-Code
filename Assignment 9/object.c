#include <stdio.h>
#include <string.h>

#include "memory.h"
#include "object.h"
#include "value.h"
#include "vm.h"

#define ALLOCATE_OBJ(type, objectType) \
    (type*)allocateObject(sizeof(type), objectType)

static Obj* allocateObject(size_t size, ObjType type) {
    Obj* object = (Obj*)reallocate(NULL, 0, size);
    object->type = type;

    object->next = vm.objects;
    vm.objects = object;
    return object;
}

// Not using this anymore, everything is in copyString now for convenience
static ObjString* allocateString(char* chars, int length) {
    ObjString* string = ALLOCATE_OBJ(ObjString, OBJ_STRING);
    string->length = length;
    // string->chars = chars;
    return string;
}

ObjString* takeString(char* chars, int length) {
    return allocateString(chars, length);
}


ObjString* copyString(const char* chars, int length) {
    // Allocate new ObjString with struct size and string length
    ObjString* string = (ObjString*)reallocate(NULL, 0, sizeof(ObjString) + length + 1);
    // Convert string to obj for obj setup
    Obj* new_str_obj = (Obj*)string;
    new_str_obj->type = OBJ_STRING;
    new_str_obj->next = vm.objects;
    memcpy(string->chars, chars, length);  // Copy chars directly to ObjString chars array
    string->chars[length] = '\0';
    string->length = length;
    string-> ownsChars = true;  // Indicate that the ObjString owns the string it is storing.
    return string;
}

void printObject(Value value) {
    switch (OBJ_TYPE(value)) {
        case OBJ_STRING:
            printf("%s", AS_CSTRING(value));
            break;
    }
}
