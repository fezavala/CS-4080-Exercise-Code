#include <stdlib.h>

#include "chunk.h"

#include <stdio.h>

#include "memory.h"

void initChunk(Chunk* chunk) {
    chunk->count = 0;
    chunk->capacity = 0;
    chunk->code = NULL;
    chunk->current_line = 1;
    initValueArray(&chunk->constants);
}

void freeChunk(Chunk* chunk) {
    FREE_ARRAY(uint8_t, chunk->code, chunk->capacity);
    freeValueArray(&chunk->constants);
    initChunk(chunk);
}

void writeChunk(Chunk* chunk, uint8_t byte, int line) {
    if (chunk->capacity < chunk->count + 1) {
        int oldCapacity = chunk->capacity;
        chunk->capacity = GROW_CAPACITY(oldCapacity);
        chunk->code = GROW_ARRAY(uint8_t, chunk->code,
            oldCapacity, chunk->capacity);
    }

    // New line placement code, uses + 2 offset to prevent a line number of 2 showing up as an Opcode
    if (line > chunk->current_line) {
        int oldLine = chunk->current_line;
        chunk->current_line = line;
        writeChunk(chunk, OP_LINE, oldLine);
        writeChunk(chunk, (uint8_t)(oldLine + 2), oldLine);
    }
    chunk->code[chunk->count] = byte;
    chunk->count++;
}

int addConstant(Chunk* chunk, Value value) {
    writeValueArray(&chunk->constants, value);
    return chunk->constants.count - 1;
}

// Get line finds the line number given an offset, uses -2 to undo the +2 from earlier.
uint8_t getLine(Chunk* chunk, int offset) {
    for (int i = offset; i < chunk->capacity; i++) {
        if (chunk->code[i] == OP_LINE) {
            return chunk->code[i + 1] - 2;
        }
        if (i != 0 && chunk->code[i - 1] == OP_LINE) {
            return chunk->code[i] - 2;
        }
    }

    return (uint8_t)chunk->current_line;
}

void writeConstant(Chunk* chunk, Value value, int line) {
    int index = addConstant(chunk, value);
    if (index < 256) {
        writeChunk(chunk, OP_CONSTANT, line);
        writeChunk(chunk, (uint8_t)index, line);
    } else {
        writeChunk(chunk, OP_CONSTANT_LONG, line);
        writeChunk(chunk, (uint8_t)(index & 0xff), line);
        writeChunk(chunk, (uint8_t)((index >> 8) & 0xff), line);
        writeChunk(chunk, (uint8_t)((index >> 16) & 0xff), line);
    }
}