.PHONY: test test-clj

test: test-clj

test-clj:
	clojure -A:test

deploy: test
	clj -Spom
	mvn deploy
