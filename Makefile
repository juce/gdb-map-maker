all: clean build jar

clean:
	rm -rf dist
	mkdir -p dist/mapmaker
	cp -pr src/mapmaker/data dist/mapmaker/

build:
	cd src && javac -d ../dist mapmaker/MapMaker.java

jar:
	rm -f GDB_MapMaker.jar
	cd dist && jar cvfm ../GDB_MapMaker.jar ../src/MANIFEST mapmaker

