#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int main(int argc, char *argv[]) {
  int err1 = ENOENT, err2 = ENOMEM;
  printf("Hello, World!\n");
  printf("Err %d: \"%s\".\n", err1, strerror(err1)); 
  printf("Err %d: \"%s\".\n", err2, strerror(err2));
}
