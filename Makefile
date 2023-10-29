EXEC = course-description-automation

all: install

# build and move to root
install: clean build
	mv src/main/rust/target/release/$(EXEC) .

build:
	cd src/main/rust && cargo build --release && cd ../../..

clean:
	- make __clean__ &> /dev/null

clean_pdf:
	- rm files/res/pdf/*

__clean__:
	- rm $(EXEC)
