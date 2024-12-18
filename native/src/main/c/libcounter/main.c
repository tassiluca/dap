#include "libcounter.h"
#include <assert.h>
#include <stdio.h>

int main(int argc, char** argv) {
  // This function needs to be called before invoking any methods defined in Scala Native.
  // Might be called automatically unless SCALANATIVE_NO_DYLIB_CTOR env variable is set.
  assert(ScalaNativeInit() == 0);
  addLongs(0L, 4L);
  libcounter_addInts(4, 0);
  printf("Current count %d\n", libcounter_current_count());
  libcounter_set_counter(42);
  printf("Current count %d\n", libcounter_current_count());
}
