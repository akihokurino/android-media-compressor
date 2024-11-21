MAKEFLAGS=--no-builtin-rules --no-builtin-variables --always-make
ROOT := $(realpath $(dir $(lastword $(MAKEFILE_LIST))))

build-debug:
	./gradlew assembleDebug

build-release:
	./gradlew assembleRelease

debug-fingerprint:
	@keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey