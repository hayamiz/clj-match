
TARGET = clj-match.jar

all: $(TARGET)

$(TARGET): $(shell find . -type f -name "*.clj")
	lein jar

pom.xml: project.clj $(TARGET)
	lein pom

upload-clojars: $(TARGET) pom.xml
	scp pom.xml $(TARGET) clojars@clojars.org:
