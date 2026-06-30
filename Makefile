.PHONY: db-up db-down test run docker-run clean

db-up:
	docker compose up -d postgres

db-down:
	docker compose down

test:
	./gradlew test

run:
	./gradlew bootRun

docker-run:
	docker compose --profile app up --build

clean:
	./gradlew clean
