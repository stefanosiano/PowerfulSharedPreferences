.PHONY: all clean build dryRelease update stop checkFormat format api assembleBenchmarkTestRelease assembleUiTestRelease

all: stop clean build
assembleLibrary: stop clean assembleLibraryModules
publish: stop publishLibrary

# deep clean
clean:
	./gradlew clean

# build and run tests
build:
	./gradlew build

# We stop gradle to make sure there are no locked files
stop:
	./gradlew --stop

# Assemble release of the library modules to publish
assembleLibraryModules:
	./gradlew :powerfulsharedpreferences:assembleRelease
	./gradlew :powerfulsharedpreferences_livedata:assembleRelease

# Publish library to Sonatype
publishLibrary:
	./gradlew publish
