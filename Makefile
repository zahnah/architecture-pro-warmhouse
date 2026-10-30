.PHONY: build up precommit

APPS_COMPOSE = docker compose -f apps/docker-compose.yml

build:
	docker compose --profile tools build
	$(APPS_COMPOSE) build

up:
	docker compose up -d
	$(APPS_COMPOSE) up -d

# Viewer images copy the generated specs, so images are built after generation
precommit:
	docker compose run --rm --build --user "$$(id -u):$$(id -g)" plantuml
	docker compose run --rm --build --user "$$(id -u):$$(id -g)" protoc
	docker compose run --rm --build --user "$$(id -u):$$(id -g)" codegen
	$(APPS_COMPOSE) run --rm --build e2e
	$(MAKE) build
