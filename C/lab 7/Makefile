# Compiler and flags
CC = gcc
CFLAGS = -Wall -std=c11

#Input file
INPUT = input.txt
OUTPUT = output.txt
REF = ref.txt
# Source file
SRCS = lab7.c
# Output
TARGET = lab7.out

# Default target
all: $(TARGET)

# Rule to build the executable
$(TARGET): $(SRCS)
	$(CC) $(CFLAGS) $(SRCS) -o $(TARGET)

convert_input: $(INPUT)
	cat $(INPUT) | dos2unix | tee $(INPUT)
	@tail -c1 $(INPUT) | grep -q . && echo >> $(INPUT)
	tr -d '\r' < $(INPUT) > temp_file && mv temp_file $(INPUT)

convert_output: $(OUTPUT)
	cat $(OUTPUT) | dos2unix | tee $(OUTPUT)
	$(tail -c1 ($INPUT)) != ""  && echo >> $(INPUT)
	tr -d '\r' < $(OUTPUT) > temp_file && mv temp_file $(OUTPUT)

check: $(OUTPUT) $(REF)
	$(tail -c1 ($REF)) != ""  && echo >> $(REF)
	@diff -q $(OUTPUT) $(REF) > /dev/null; \
	if [ $$? -eq 0 ]; then \
		echo "Pass"; \
	else \
		echo "Fail"; \
	fi

# Rule to run the program
run: $(TARGET)
	./$(TARGET) $(INPUT) $(OUTPUT)

# Rule to clean generated files
clean:
	rm -f $(TARGET)
