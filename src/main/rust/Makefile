CARGO = rustup run stable-x86_64-pc-windows-msvc cargo

all: run

build:
	$(CARGO) build

run:
	$(CARGO) run

release:
	$(CARGO) build --release

test:
	$(CARGO) test

clean: 
	$(CARGO) clean
	rm target -rf

