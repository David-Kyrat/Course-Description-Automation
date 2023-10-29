EXEC = course-description-automation

all: install

# build and move to root
install: build
	mv src/main/rust/target/release/$(EXEC) .

build:
	cd src/main/rust && cargo build --release && cd ../../..

clean:
	- rm ./Course-Description-Automation
