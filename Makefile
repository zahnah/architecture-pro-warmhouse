.PHONY: build up precommit

build:
	docker compose --profile tools build

up:
	docker compose up -d

# Viewer images copy the generated specs, so images are built after generation
precommit:
	docker compose run --rm --build --user "$$(id -u):$$(id -g)" plantuml
	docker compose run --rm --build --user "$$(id -u):$$(id -g)" protoc
	$(MAKE) build
