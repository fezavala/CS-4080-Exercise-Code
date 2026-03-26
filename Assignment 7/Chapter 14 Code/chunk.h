#ifndef clox_chunk_h
#define clox_chunk_h

#include "common.h"
#include "value.h"

typedef enum {
    OP_CONSTANT,
    OP_RETURN,
    OP_LINE,
    OP_CONSTANT_LONG
} OpCode;

typedef struct {
    int count;
    int capacity;
    uint8_t* code;
    int current_line; // lines array replaced with current_line tracking variable
    ValueArray constants;
} Chunk;

void initChunk(Chunk* chunk);
void freeChunk(Chunk* chunk);
void writeChunk(Chunk* chunk, uint8_t byte, int line);
int addConstant(Chunk* chunk, Value value);

// New function definitions
uint8_t getLine(Chunk* chunk, int offset);
void writeConstant(Chunk* chunk, Value value, int line);

#endif //clox_chunk_h