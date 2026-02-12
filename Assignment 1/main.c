#include <stdio.h>
#include <stdlib.h>
#include <string.h>

struct Node {
    struct Node* prev;
    struct Node* next;
    char* string_data;
};

struct List {
    struct Node* head;
    struct Node* tail;
};

// List functions and helper functions
struct List create_list();
void insert(char* string_data, struct List* list);
void delete(const char* string_to_delete, struct List* list);
struct Node* find(const char* string_to_find, const struct List* list);
void print_list(const struct List* list);


int main(void) {
    printf("Hello, World!\n");

    char* example_string_1 = "Hello, it is me!";
    char* example_string_2 = "Nice to meet you!";
    char* example_string_3 = "I am an example string!";

    struct List example_list = create_list();
    insert(example_string_1, &example_list);
    insert(example_string_2, &example_list);
    insert(example_string_3, &example_list);
    print_list(&example_list);
    // Hello, it is me!
    // Nice to meet you!
    // I am an example string!

    printf("\n");
    const struct Node* found_node = find(example_string_1, &example_list);
    if (found_node != NULL) {
        printf("%s\n", found_node->string_data);
    }
    // Hello, it is me!

    delete(example_string_2, &example_list);
    printf("\n");
    print_list(&example_list);
    // Hello, it is me!
    // I am an example string!

    return 0;
}

// Helper function that makes an empty list. Makes pointers NULL.
struct List create_list() {
    struct List list;
    list.head = NULL;
    list.tail = NULL;
    return list;
}

// Helper function that creates a node and returns the pointer to the node. Makes pointers NULL.
struct Node* create_node(char* string_data) {
    struct Node* new_node = (struct Node*)malloc(sizeof(struct Node));
    new_node->string_data = string_data;
    new_node->next = NULL;
    new_node->prev = NULL;
    return new_node;
}

// Inserts the string at the end of the list.
// If the list is empty, the lists head and tail will be set to the string node.
void insert(char* string_data, struct List* list) {
    struct Node* new_node = create_node(string_data);

    if (list->head == NULL) {
        list->head = new_node;
        list->tail = new_node;
        return;
    }

    new_node->prev = list->tail;
    list->tail->next = new_node;
    list->tail = new_node;
}

// Deletes the node that contains the given string w/o breaking the list.
void delete(const char* string_to_delete, struct List* list) {
    struct Node* temp = list->head;
    while (temp != NULL) {
        if (strcmp(temp->string_data, string_to_delete) == 0) {
            if (temp->prev == NULL) {
                list->head = temp->next;
            } else {
                temp->prev->next = temp->next;
            }
            if (temp->next == NULL) {
                list->tail = temp->prev;
            } else {
                temp->next->prev = temp->prev;
            }
            free(temp);
            temp = NULL;
            return;
        }
    temp = temp->next;
    }
}

// Returns the node that contains the given string.
struct Node* find(const char* string_to_find, const struct List* list) {
    struct Node* temp = list->head;
    while (temp != NULL) {
        if (strcmp(temp->string_data, string_to_find) == 0) {
            return temp;
        }
        temp = temp->next;
    }
    return temp;
}

// Helper function that prints the entire list.
void print_list(const struct List* list) {
    struct Node* temp = list->head;
    while (temp != NULL) {
        printf("%s\n", temp->string_data);
        temp = temp->next;
    }
}
