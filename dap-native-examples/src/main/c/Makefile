OS := $(shell uname -s)
ifeq ($(OS), Darwin)
    OS := macOS $(shell sw_vers -productVersion | cut -d. -f1)
endif
CC := clang
CURRENT_DIR := $(shell pwd)
BINARIES_DIR := ${CURRENT_DIR}
ROOT_SCALA_PROJECT := ${CURRENT_DIR}/../../../../
LIBRARY := dap
# C compiler flags: https://clang.llvm.org/docs/DiagnosticsReference.html
CFLAGS += -Wpedantic        	# Enforce strict ISO compliance
CFLAGS += -Wall             	# Enable all common warnings
CFLAGS += -Wextra           	# Enable extra warnings
CFLAGS += -Werror           	# Treat warnings as errors
CFLAGS += -fsanitize=address,undefined,null  # Enable address/undefined/null sanitizers
ifneq ($(OS), macOS 15)
	CFLAGS += -fsanitize=leak  	# Enable leak sanitizer (not supported on macOS >= 15.x)
endif
CFLAGS += -g                	# Include debug symbols

# Protobuf paths - detect OS and set appropriate paths
ifeq ($(shell echo $(OS) | grep -c "macOS"), 1)
    # macOS with Homebrew
    PROTOBUF_PATH := $(shell brew --prefix protobuf 2>/dev/null || echo /opt/homebrew/opt/protobuf)
    PROTOBUFC_PATH := $(shell brew --prefix protobuf-c 2>/dev/null || echo /opt/homebrew/opt/protobuf-c)
else
    # Linux (assuming Ubuntu/Debian)
    PROTOBUF_PATH := /usr
    PROTOBUFC_PATH := /usr
endif
# Protobuf compiler and flags
PROTOC := $(PROTOBUF_PATH)/bin/protoc
PROTO_SRC_DIR := .
PROTO_OUT_DIR := .
PROTOC_FLAGS := --c_out=$(PROTO_OUT_DIR) --proto_path=$(PROTOBUF_PATH)/include --proto_path=$(PROTO_SRC_DIR)
# Additional include paths for compilation
INCLUDES := -I. -I$(PROTOBUFC_PATH)/include

all:	mains
	@echo "Detected OS: ${OS}"

mains: 	libdap bindings
	${CC} ${CFLAGS} $(INCLUDES) -o ${BINARIES_DIR}/gossip.exe gossip.c gossip.pb-c.c google/protobuf/timestamp.pb-c.c -L${BINARIES_DIR} -L$(PROTOBUFC_PATH)/lib -Wl,-rpath,${BINARIES_DIR} -l${LIBRARY} -lprotobuf-c

bindings: $(PROTO_SRC_DIR)/gossip.proto
	@echo "Generating protobuf-c bindings..."
	@echo "OS: $(OS)"
	@echo "PROTOBUF_PATH: ${PROTOBUF_PATH}"
	@echo "PROTOBUFC_PATH: ${PROTOBUFC_PATH}"
	@mkdir -p $(PROTO_OUT_DIR)/google/protobuf
	@if [ ! -f $(PROTO_OUT_DIR)/google/protobuf/timestamp.pb-c.h ]; then \
		echo "Generating timestamp bindings..."; \
		$(PROTOC) $(PROTOC_FLAGS) google/protobuf/timestamp.proto; \
	fi
	$(PROTOC) $(PROTOC_FLAGS) $(PROTO_SRC_DIR)/gossip.proto
	@echo "Protobuf bindings generated successfully"

.PHONY: clean libdap

libdap:
	@if [ ! -e ${BINARIES_DIR}/libdap* ]; then \
  		mkdir -p ${BINARIES_DIR}; \
  		echo "Building DAP library..."; \
  		cd ${ROOT_SCALA_PROJECT} && \
  			sbt ${LIBRARY}Native/nativeLink && \
  			cp ${LIBRARY}/native/target/scala-3.*/libdap.* ${BINARIES_DIR}; \
	fi

clean:
	-rm -rf ${BINARIES_DIR}/*.o ${BINARIES_DIR}*.dylib ${BINARIES_DIR}/*.dylib \
		${BINARIES_DIR}/*.so ${BINARIES_DIR}/*.exe *.o *.dylib *.so *.exe google/ *.pb-c.h *.pb-c.c