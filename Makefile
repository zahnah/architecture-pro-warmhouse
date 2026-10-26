.PHONY: up down precommit

up:
	docker compose up -d

down:
	docker compose down

precommit:
	bash schemas/scripts/export-diagrams.sh