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

mapfile=map.txt
test:
	javac -d dist src/mapmaker/GDBMap.java
	java -cp dist mapmaker.GDBMap $(mapfile)

version=0.1
packname=GDB_MapMaker-$(version)
zip: clean build jar
	rm -rf $(packname) $(packname).zip
	mkdir -p $(packname)
	cp -p GDB_MapMaker.jar team-names.csv $(packname)/
	zip -r -9 $(packname).zip $(packname)
