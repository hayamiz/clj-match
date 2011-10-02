
VERSION = $(shell grep clj-match project.clj|sed -e 's/.*\([0-9][0-9]*\.[0-9][0-9]*\.[0-9][0-9]*\(-SNAPSHOT\)\).*/\1/g')
TARGET = clj-match-$(VERSION).jar

.PHONY: all clean check upload-clojars

all: $(TARGET)

$(TARGET): $(shell find . -type f -name "*.clj") project.clj
	lein jar

pom.xml: project.clj $(TARGET)
	lein pom

upload-clojars: $(TARGET) pom.xml
	scp pom.xml $(TARGET) clojars@clojars.org:

clean:
	lein clean

check:
	lein test
